package ru.netology.nmedia.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

class PostRepositoryImpl(
) : PostRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val postsType = object : TypeToken<List<Post>>() {}.type

    private companion object {
        const val BASE_URL = "http://10.0.2.2:9999"
        val jsonType = "application/json".toMediaType()
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
        Log.d("PostRepository", "Запрос списка постов инициирован асинхронным методом")
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PostRepository", "Ошибка загрузки")
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body.string()
                    try {
                        val posts: List<Post> =
                            gson.fromJson(body, postsType) //Вынес в переменную ради логов
                        callback.onSuccess(gson.fromJson(body, postsType))
                        Log.d("PostRepository", "Полученные посты: $posts")
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            }
            )
    }

    override fun likeById(post: Post, callback: PostRepository.LikedByIdCallback) {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/${post.id}/likes") //Общий url для лайка и дизлайка
            .apply {
                if (!post.likedByMe) {
                    //Ставим лайк, если его нет
                    post(gson.toJson(post).toRequestBody(jsonType))
                } else {
                    //Удаляем лайк, если он был
                    delete()
                }
            }
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PostRepository", "Ошибка likeByID")
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(
                            gson.fromJson(
                                response.body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })
    }
//Не работает с текущим сервером
    override fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Long>) {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id")
            .delete()
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PostRepository", "Ошибка removeById")
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(id)
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }
            })

    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("PostRepository", "Ошибка save")
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        callback.onSuccess(
                            gson.fromJson(
                                response.body.string(),
                                Post::class.java
                            )
                        )
                    } catch (e: Exception) {
                        callback.onError(e)
                    }
                }

            })


    }
}