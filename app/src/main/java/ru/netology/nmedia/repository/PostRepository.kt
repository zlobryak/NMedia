package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(post: Post, callback: Callback<Post>)
    fun shareById(id: Long): Post
    fun removeById(id: Long, callback: Callback<Long>)
    fun save(post: Post, callback: Callback<Post>)
    fun getAllAsync(callback: Callback<List<Post>>)



    //Интерфейс для любых типов принимаемых параметров
    interface Callback<T> {
        fun onSuccess(data: T)
        fun onError(e: Throwable, statusCode: Int? = null)
    }
}