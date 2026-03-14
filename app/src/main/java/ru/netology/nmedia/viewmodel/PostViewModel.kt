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

    private val _errorEvent = SingleLiveEvent<String>()
    val errorEvent: LiveData<String> = _errorEvent

    private val _successEvent = SingleLiveEvent<String>()
    val successEvent: LiveData<String> = _successEvent

    init {
        load()
    }

    fun load(fromRefresh: Boolean = false) {
        if (fromRefresh) _refreshing.value = true // Только для свайпа

        _data.postValue(FeedModel(loading = true))

        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(data: List<Post>) {
                _data.value = (FeedModel(posts = data, empty = data.isEmpty()))
            }

            override fun onError(e: Throwable, statusCode: Int?) {
                _errorEvent.value = ("Error code: $statusCode")
                _data.postValue(FeedModel(loading = false, error = true))
            }
        })
        if (fromRefresh) _refreshing.value = false // Сброс только для свайпа
    }


    fun likeById(post: Post) {
        val currentPosts = _data.value?.posts ?: emptyList()
        repository.likeById(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                //Перезаписываем в списке постов тот, отображение которого нужно обновить
                val updatedPosts = currentPosts.map { currentPost ->
                    if (currentPost.id == post.id) data else currentPost
                }
                _data.value = (FeedModel(posts = updatedPosts, empty = updatedPosts.isEmpty()))
            }

            override fun onError(e: Throwable, statusCode: Int?) {
                _errorEvent.value = ("Error code: $statusCode, try later")
                _data.postValue(FeedModel(posts = currentPosts, loading = false))
            }
        })
    }


    //Не работает с текущим сервером
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) {
        val currentPosts = _data.value?.posts ?: emptyList()

        repository.removeById(id, object : PostRepository.Callback<Long> {
            override fun onSuccess(data: Long) {
                // Удаляем пост из локального списка
                val updatedPosts = currentPosts.filter { it.id != id }

                // Обновляем UI: список + сброс загрузки
                _data.value = FeedModel(
                    posts = updatedPosts,
                    empty = updatedPosts.isEmpty(),
                    loading = false
                )
//                Покажем сообщение об удачном удалении поста
                _successEvent.value = "Post deleted"
            }

            override fun onError(e: Throwable, statusCode: Int?) {
                _errorEvent.value = ("Error code: $statusCode, post was not deleted")
                _data.postValue(FeedModel(posts = currentPosts, loading = false))
            }

        })
    }

    fun save(post: Post) {
        //TODO проверить, работает ли сохранение вообще, возможно неверное обращение по API
        _data.postValue(FeedModel(loading = true))

        repository.save(post, object : PostRepository.Callback<Post> {
            override fun onSuccess(data: Post) {
                _postCreated.postValue(Unit)
            }

            override fun onError(e: Throwable, statusCode: Int?) {
                _errorEvent.value = ("Error code: $statusCode, post was not created")
                _data.postValue(FeedModel(error = true))
            }
        })
    }
}


