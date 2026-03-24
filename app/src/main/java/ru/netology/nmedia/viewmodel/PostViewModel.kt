package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.utils.SingleLiveEvent


class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(application).postDao)
    private val _state = MutableLiveData(FeedModelState())
    val state: LiveData<FeedModelState>
        get() = _state

    val data: LiveData<FeedModel> = repository.data.map {
        FeedModel(it, it.isEmpty())
    }

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _refreshing = MutableLiveData(false)
    val refreshing: LiveData<Boolean> = _refreshing

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private val _successEvent = SingleLiveEvent<String>()
    val successEvent: LiveData<String> = _successEvent

    init {
        load()
    }

    fun load(fromRefresh: Boolean = false) {
        if (fromRefresh) _refreshing.value = true // Только для свайпа

        _state.postValue(FeedModelState(loading = true))
        viewModelScope.launch {
            try {
                repository.getAllAsync()
                _state.value = FeedModelState()
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
            }
        }

        if (fromRefresh) _refreshing.value = false // Сброс только для свайпа
    }


    fun likeById(post: Post) {
        val currentPost = post.copy() //Сохраняем копию поста, на случай ошибок

        viewModelScope.launch {
            try {
                if (post.isSynced) {
                    repository.likeById(post.id, post.likedByMe)
                } else {
                    _errorEvent.value = "Post is not synchronised, try later"
                    repository.restorePost(currentPost)
                }
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
                repository.restorePost(currentPost) //Возвращаем старый пост, при ошибках
            }

        }
    }

    fun removeById(post: Post) {
        val currentPosts = post.copy()
        viewModelScope.launch {
            try {
                repository.removeById(post.id)
                _successEvent.value = "Post deleted"
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
                repository.restorePost(currentPosts)
            }
        }
    }

    fun save(post: Post) {
        viewModelScope.launch {
            try {
                repository.save(post)
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
                repository.setFailed(post)
            }
        }
    }

    //Не работает с текущим сервером
    fun shareById(id: Long) {
        viewModelScope.launch {
            try {
                repository.shareById(id)
            } catch (_: Throwable) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
}


