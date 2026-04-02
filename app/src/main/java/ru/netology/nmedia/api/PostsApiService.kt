package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dto.Post
import java.util.concurrent.TimeUnit

private const val BASE_URL: String = "${BuildConfig.BASE_URL}/api/slow/"


private val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    })
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface PostsApiService {
    @GET("posts")
    suspend  fun getAll(): List<Post>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Post

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)

    @POST("posts/{id}/likes")
    suspend  fun like(@Path("id") id: Long): Post

    @DELETE("posts/{id}/likes")
    suspend fun dislike(@Path("id") id: Long): Post


    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): List<Post>

}

object PostApi{
    val service by lazy {
        retrofit.create<PostsApiService>()
    }
}
