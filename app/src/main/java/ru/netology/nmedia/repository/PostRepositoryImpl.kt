package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dto.Post
import kotlin.collections.orEmpty

class PostRepositoryImpl(

) : PostRepository {

    //Синхронная загрузка постов
    override fun getAll(): List<Post> =
        PostApi.service.getAll()
            .execute()
            .body()
            .orEmpty()


    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostApi.service.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(
                call: Call<List<Post>?>,
                response: Response<List<Post>?>
            ) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body().orEmpty())
                } else {
                    callback.onError(RuntimeException(response.errorBody()?.string().orEmpty()), response.code())
                }
            }

            override fun onFailure(
                call: Call<List<Post>?>,
                t: Throwable
            ) {
                callback.onError(t)
            }
        })
    }

    override fun likeById(post: Post, callback: PostRepository.Callback<Post>) {
        val call: Call<Post> = if (!post.likedByMe) {
            PostApi.service.like(post.id)
        } else {
            PostApi.service.dislike(post.id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(
                call: Call<Post>,
                response: Response<Post?>
            ) {
                if (response.isSuccessful) {
                    val body = response.body() ?: run {
                        callback.onError(RuntimeException("Empty response body"))
                        return
                    }
                    callback.onSuccess(body)
                } else {
                    callback.onError(RuntimeException(response.errorBody()?.string().orEmpty()), response.code())
                }
            }

            override fun onFailure(
                call: Call<Post?>,
                t: Throwable
            ) {
                callback.onError(t)
            }
        })
    }


    //Не работает с текущим сервером
    override fun shareById(id: Long): Post {
        TODO("Not yet implemented")
    }

    override fun removeById(id: Long, callback: PostRepository.Callback<Long>) {
        PostApi.service.deletePost(id).enqueue(object : Callback<Unit> {
            override fun onResponse(
                call: Call<
                        Unit>,
                response: Response<Unit>
            ) {
                if (response.isSuccessful) {
                    callback.onSuccess(id)

                } else {
                    callback.onError(RuntimeException(response.errorBody()?.string().orEmpty()), response.code())
                }
            }

            override fun onFailure(
                call: Call<Unit>,
                t: Throwable
            ) {
                callback.onError(t)
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.Callback<Post>) {
        PostApi.service.savePost(post).enqueue(object : Callback<Post> {
            override fun onResponse(
                call: Call<Post?>,
                response: Response<Post?>
            ) {
                if (response.isSuccessful) {
                    val post = response.body() ?: run {
                        callback.onError(RuntimeException("Empty body"))
                        return
                    }
                    callback.onSuccess(post)
                } else {
                    callback.onError(RuntimeException(response.errorBody()?.string().orEmpty()), response.code())
                }
            }

            override fun onFailure(
                call: Call<Post>,
                t: Throwable
            ) {
                callback.onError(t)
            }
        })
    }
}