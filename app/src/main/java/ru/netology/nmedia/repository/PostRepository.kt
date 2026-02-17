package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post): Post
    fun shareById(id: Long): Post
    fun removeById(id: Long)
    fun save (post: Post): Post
    fun disLikeById(post: Post): Post

    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }
}