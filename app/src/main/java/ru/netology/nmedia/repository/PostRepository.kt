package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post

interface PostRepository {
    val data: Flow<List<Post>>

    suspend fun likeById(id: Long, likedByMe: Boolean)
    suspend fun removeById(id: Long)
    suspend fun save(post: Post)
    suspend fun restorePost(post: Post)
    suspend fun removePending(post: Post)
    suspend fun setFailed(post: Post)

    //Текущая версия сервера не поддерживает этот функционал
    suspend fun shareById(id: Long): Post

    suspend fun getAllVisible(): Long
    suspend fun getHiddenPostsCount(): Int
    suspend fun showAllHiddenPosts()
    suspend fun fetchNewPosts(lastKnownId: Long): Int

    suspend fun getLastPostId(): Long

    fun getNewerCount(id: Long): Flow<Int>
}