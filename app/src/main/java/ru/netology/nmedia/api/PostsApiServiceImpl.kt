package ru.netology.nmedia.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostsApiServiceImpl(
    private val baseUrl: String = "http://10.0.2.2:9999"
) : PostsApiService {

    private val client = OkHttpClient.Builder()

        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonType = "application/json".toMediaType()
    private val postsType = object : TypeToken<List<Post>>() {}.type

    override fun getPosts(callback: ApiCallback<List<Post>>) {
        val request = Request.Builder()
            .url("$baseUrl/api/slow/posts")
            .build()

        enqueue(request, callback, postsType)
    }

    override fun toggleLike(postId: Long, liked: Boolean, callback: ApiCallback<Post>) {
        val builder = Request.Builder()
            .url("$baseUrl/api/slow/posts/$postId/likes")

        val request = if (liked) {
            builder.post("{}".toRequestBody(jsonType)) // Пустое тело для лайка
        } else {
            builder.delete()
        }.build()

        enqueue(request, callback, Post::class.java)
    }

    override fun deletePost(postId: Long, callback: ApiCallback<Unit>) {
        val request = Request.Builder()
            .url("$baseUrl/api/slow/posts/$postId")
            .delete()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PostsApiService", "Ошибка удаления: ${e.message}")
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, callback) {
                    callback.onSuccess(Unit)
                }
            }
        })
    }

    override fun createPost(post: Post, callback: ApiCallback<Post>) {
        val request = Request.Builder()
            .url("$baseUrl/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        enqueue(request, callback, Post::class.java)
    }

    // Универсальный метод для выполнения запросов
    private fun <T> enqueue(
        request: Request,
        callback: ApiCallback<T>,
        type: java.lang.reflect.Type
    ) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("PostsApiService", "Ошибка запроса: ${e.message}")
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, callback) { body ->
                    try {
                        val data = gson.fromJson<T>(body, type)
                        callback.onSuccess(data)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
        })
    }

    // Обработка HTTP-ответов
    private fun <T> handleResponse(
        response: Response,
        callback: ApiCallback<T>,
        onSuccess: (String) -> Unit
    ) {
        if (!response.isSuccessful) {
            val error = when (response.code) {
                404 -> RuntimeException("Resource not found")
                500 -> RuntimeException("Server error")
                else -> RuntimeException("HTTP ${response.code}")
            }
            callback.onError(error)
            return
        }

        val body = response.body?.string()
        if (body != null) {
            onSuccess(body)
        } else {
            callback.onError(IOException("Empty response body"))
        }
    }
}