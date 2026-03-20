package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data:  LiveData<List<Post>>
    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post): Post
    suspend fun getAllAsync()

    //Текущая версия сервера не поддерживает этот функционал
    suspend fun shareById(id: Long): Post
    suspend fun restorePost(post: Post)
}