package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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

    override fun getAll(): List<Post> {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val jsonResponse = response.body.string()
        return gson.fromJson(jsonResponse, postsType)
    }

    override fun likeById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long) {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts/$id")
            .delete()
            .build()
        val call = client.newCall(request)
        call.execute()
    }

    override fun save(post: Post): Post {
        val request = Request.Builder()
            .url("$BASE_URL/api/slow/posts")
            .post(gson.toJson(post).toRequestBody(jsonType))
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        val jsonResponse = response.body.string()
        return gson.fromJson(jsonResponse, Post::class.java)
    }
}