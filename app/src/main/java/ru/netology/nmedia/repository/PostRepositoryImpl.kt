package ru.netology.nmedia.repository


import kotlinx.coroutines.flow.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostEntity.SyncStatus

class PostRepositoryImpl(
    private val dao: PostDao
) : PostRepository {
    override suspend fun getLastPostId(): Long {
        return dao.getMaxId()
    }


    override val data = dao.getAllVisible().map { entities -> entities.map { it.toDto() } }

    override suspend fun fetchNewPosts(lastKnownId: Long): Int {
        val newerPosts = PostApi.service.getNewer(lastKnownId)
        // Новые посты добавляются с isVisible=false (по умолчанию в fromDto)
        dao.insert(newerPosts.map(PostEntity::fromDto))
        return newerPosts.size
    }

    //Метод для первоначальной загрузки списка постов. Все полученные посты сразу отображаются в ленте.
    override suspend fun getAllInit(): Long {
        val posts = PostApi.service.getAll()
        dao.insert(posts.map { post ->
            PostEntity.fromDto(post).copy(isVisible = true)
        })
        return posts.maxOfOrNull { it.id } ?: 0L
    }

    override suspend fun getHiddenPostsCount(): Int {
        return dao.countHiddenPosts()
    }

    override suspend fun syncNewer() {
        //Перед запросом списка постов, проверим последний известный ID и запросим
        //только посты с id выше через getNewer. Если база данных пустая - запросятся все
        val posts = PostApi.service.getNewer(dao.getMaxId() ?: 0L)
        dao.insert(posts.map { post ->
            PostEntity.fromDto(post)
        })
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