package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.SyncStatus

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {

    override val data: LiveData<List<Post>> = dao.getAll().map { posts ->
        posts.map { it.toDto() }
    }

    override suspend fun getAllAsync() {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map(PostEntity::fromDto))
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