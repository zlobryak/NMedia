package ru.netology.nmedia.repository


import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.SyncStatus
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException
import kotlin.collections.map

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {

    override suspend fun getLastPostId(): Long {
        return dao.getMaxId()
    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(10_000L)
            val response = PostApi.service.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
            emit(body.size)
        }
    }.catch { e -> throw AppError.from(e) }

    override val data = dao.getAllVisible().map { entities -> entities.map { it.toDto() } }

    override suspend fun fetchNewPosts(lastKnownId: Long): Int {
        val response = PostApi.service.getNewer(lastKnownId)
        val body = response.body() ?: throw ApiError(response.code(), response.message())

        // Новые посты добавляются с isVisible=false (по умолчанию в fromDto)
        dao.insert(body.toEntity())
        return body.size
    }

    //Метод для первоначальной загрузки списка постов. Все полученные посты сразу отображаются в ленте.
    override suspend fun getAllVisible(): Long {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map { post ->
            PostEntity.fromDto(post).copy(isVisible = true)
        })
        return posts.maxOfOrNull { it.id } ?: 0L
    }

    override suspend fun getHiddenPostsCount(): Int {
        return dao.countHiddenPosts()
    }

    override suspend fun save(post: Post) {
        //Отправляем в бд временный пост с ID = 0L и флагом isSynced = false и статусом PENDING
        dao.insert(
            PostEntity.fromDto(post).copy(
                id = 0L,
                isSynced = false,
                syncStatus = SyncStatus.PENDING
            )
        )
        //Если получаем с сервера ответ - заменяем временный пост, для замены id и смены флагов(по умолчанию все прошедшие fromDto)
        dao.insert(PostEntity.fromDto(PostApi.service.savePost(post)))
        //Удаляем временный пост (надо реализовать точечное удаление поста, а не всех с этим статусом)
        dao.removePending(SyncStatus.PENDING)

    }

    //Удалят несохраненный пост из базы данных при исключениях
    override suspend fun removePending(post: Post) {
        dao.removeById(post.id)
    }

    override suspend fun removeById(id: Long) {
        dao.removeById(id)
        PostApi.service.deletePost(id)
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        //Вв dao уже реализован выбор между лайк и дизлайк в аннотации
        dao.likeById(id)
        //В API есть два вызова, между которыми нужно выбрать, в зависимости от того,
        // был ли лайк уже поставлен автором поста
        if (!likedByMe) {
            PostApi.service.like(id)
        } else {
            PostApi.service.dislike(id)
        }
    }

    override suspend fun restorePost(post: Post) {
        // Полная перезапись поста старыми данными
        if (post.isSynced) {
            dao.insert(PostEntity.fromDto(post))
        } else {
            //Для постов, которые не синхронизированы, вернем флаг и исходное состояние.
            dao.insert(
                PostEntity.fromDto(post).copy(
                    isSynced = false,
                    syncStatus = post.syncStatus
                )
            )
        }

    }

    //В локальной базе данных объявим все невидимые посты видимыми
    override suspend fun showAllHiddenPosts() {
        dao.showAllHiddenPosts()
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            // TODO: add support for other types
            val postWithAttachment = post.copy(attachment = Attachment(url = media.id, type = AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostApi.service.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (_: IOException) {
            throw NetworkError
        } catch (_: Exception) {
            throw UnknownError
        }
    }

    //Не работает с текущим сервером
    override suspend fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override suspend fun setFailed(post: Post) {
        dao.insert(
            PostEntity.fromDto(post).copy(
                isSynced = false,
                syncStatus = SyncStatus.FAILED
            )
        )
    }
}