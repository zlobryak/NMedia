package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryInMemoryImpl

private val empty = Post(
    id = 0,
    author = "",
    content = "",
    published = "",
    likes = 0,
    shareCount = 0,
    likedByMe = false
)

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    val edited = MutableLiveData(empty)

    fun likeById(id: Long) = repository.likeById(id)
    fun shareById(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeBbyId(id)
    fun save(content: String) {
        val trim = content.trim()
        edited.value?.let { post ->
            if (trim != post.content) {
                repository.save(post.copy(content = trim))
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }
}
