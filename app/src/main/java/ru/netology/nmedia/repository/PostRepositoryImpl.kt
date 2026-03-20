package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

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

    override suspend fun save(post: Post): Post = PostApi.service.savePost(post)

    override suspend fun removeById(id: Long) {
        PostApi.service.deletePost(id)
        dao.removeById(id)

        //TODO откат при ошибке и обновление отображения
    }

    override suspend fun likeById(id: Long, likedByMe: Boolean) {
        //Вв dao уже реализован выбор между лайк и дизлайк в аннотации
        dao.likeById(id)
        //В API есть два вызова, между которыми нужно выбрать, в зависимости от того,
        // был ли лайк уже поставлен автором поста
        if (!likedByMe) {
            PostApi.service.like(id)
        }else{
            PostApi.service.dislike(id)
        }
    }

    override suspend fun restorePost(post: Post) {
        // Полная перезапись поста старыми данными
        dao.insert(PostEntity.fromDto(post))
    }

    //Не работает с текущим сервером
    override suspend fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }
}