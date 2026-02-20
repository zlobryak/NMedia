package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post, callback: LikedByIdCallback)
    fun shareById(id: Long): Post
    fun removeById(id: Long, callback: Callback<Long>)
    fun save(post: Post, callback: Callback<Post>)
    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }

    //Интерфейс для лайка и дизлайка одновременно
    interface LikedByIdCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Exception) {}
    }

    //Интерфейс для любых типов принимаемых параметров
    interface Callback<T> {
        fun onSuccess(data: T)
        fun onError(e: Exception)
    }
}


