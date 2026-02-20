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

        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
        if (fromRefresh) _refreshing.value = false // Сброс только для свайпа
    }


    fun likeById(post: Post) {
        val currentPosts = _data.value?.posts ?: emptyList()
        _data.postValue(FeedModel(loading = true))
        repository.likeById(post, object : PostRepository.LikedByIdCallback {
            override fun onSuccess(post: Post) {
                //Перезаписываем в списке постов тот, отображение которого нужно обновить
                val updatedPosts = currentPosts.map { currentPost ->
                    if (currentPost.id == post.id) post else currentPost
                }
                _data.postValue(FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(posts = currentPosts, error = true))
            }
        })
    }


    //Не работает с текущим сервером
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        _data.postValue(FeedModel(loading = true))

        val currentPosts = _data.value?.posts ?: emptyList()

        repository.removeById(id, object : PostRepository.Callback<Long> {
            override fun onSuccess(data: Long) {
                load()
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(posts = currentPosts, error = true))
            }

        })
    }

    fun save(post: Post) {
        //TODO сейчас можно успеть несколько раз нажать на кнопку, получив несколько одинаковых постов
        _data.postValue(FeedModel(loading = true))

        repository.save(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                _postCreated.postValue(Unit)
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}


