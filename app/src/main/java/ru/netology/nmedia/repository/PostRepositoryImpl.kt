package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.netology.nmedia.api.ApiCallback
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl(
    private val apiService: PostsApiService
) : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val postsType = object : TypeToken<List<Post>>() {}.type

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999"
    }

    //Старый вариант синхронной загрузки списка постов
    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()
        return gson.fromJson(
            client.newCall(request)
                .execute()
                .body.string(),
            postsType
        )
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        apiService.getPosts(object : ApiCallback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                callback.onSuccess(data)
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        })
    }

    override fun likeById(post: Post, callback: PostRepository.LikedByIdCallback) {
        apiService.toggleLike(
            postId = post.id,
            liked = !post.likedByMe, // Инвертируем состояние
            callback = object : ApiCallback<Post> {
                override fun onSuccess(data: Post) {
                    callback.onSuccess(data)
                }

                override fun onError(e: Exception) {
                    callback.onError(e)
                }
            }
        )
    }

    //Не работает с текущим сервером
    override fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Long>) {
        apiService.deletePost(id, object : ApiCallback<Unit> {
            override fun onSuccess(data: Unit) {
                callback.onSuccess(id)
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        apiService.createPost(post, object : ApiCallback<Post> {
            override fun onSuccess(data: Post) {
                callback.onSuccess(data)
            }

            override fun onError(e: Exception) {
                callback.onError(e)
            }
        })
    }
}