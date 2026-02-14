package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent
import kotlin.concurrent.thread


class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl(
    )
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing


    init {
        load()
    }

    fun load(fromRefresh: Boolean = false) {
        if (fromRefresh) _refreshing.value = true // Только для свайпа

        thread {
            _data.postValue(FeedModel(loading = true))
            val result = try {
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (_: Exception) {
                FeedModel()
            }
            _data.postValue(result)
        }

        if (fromRefresh) _refreshing.value = false // Сброс только для свайпа
    }


    fun likeById(post: Post) {
        thread {
            val currentPosts = _data.value?.posts ?: emptyList()
            //Попробуем отправить изменения на сервер
            try {
                _data.postValue(FeedModel(loading = true))
                val updatedPost = if (post.likedByMe) {
                    repository.disLikeById(post)
                } else {
                    repository.likeById(post)
                }
                // Синхронизация с фактическим результатом из репозитория
                val updatedPosts = currentPosts.map { post ->
                    if (post.id == updatedPost.id) updatedPost else post
                }
                _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
            } catch (_: Exception) {
                //В случае ошибки вернем как старый список постов и покажем ошибку
                _data.postValue(FeedModel(posts = currentPosts, error = true))
            }
        }
    }

    //Не работает с текущим сервером
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        thread {
            val currentPosts = _data.value?.posts ?: emptyList()
            try {
                repository.removeById(id)
                load()
            }catch (_: Exception) {
                //В случае ошибки вернем как старый список постов и покажем ошибку
                _data.postValue(FeedModel(posts = currentPosts, error = true))
            }
        }
    }

    fun save(post: Post) {
        //TODO сейчас можно успеть несколько раз нажать на кнопку доварить, получив несколько одинаковых постов
        thread {
            repository.save(post)
            _postCreated.postValue(Unit)
        }
    }
}


