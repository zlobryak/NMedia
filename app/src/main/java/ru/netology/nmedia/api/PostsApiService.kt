package ru.netology.nmedia.api

import ru.netology.nmedia.dto.Post

interface PostsApiService {
    fun getPosts(callback: ApiCallback<List<Post>>)
    fun toggleLike(postId: Long, liked: Boolean, callback: ApiCallback<Post>)
    fun deletePost(postId: Long, callback: ApiCallback<Unit>)
    fun createPost(post: Post, callback: ApiCallback<Post>)
}


interface ApiCallback<T> {
    fun onSuccess(data: T)
    fun onError(e: Exception)
}