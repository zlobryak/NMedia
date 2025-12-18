package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import java.lang.reflect.Type
import kotlin.collections.plus

class PostRepositorySQLiteImpl(
    private val dao: PostDao
) : PostRepository {
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)

    init {
        posts = dao.getAll()
        data.value = posts
    }


    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        dao.likeById(id)
        posts = posts.map {
            if (it.id != id) it else it.copy(
                likedByMe = !it.likedByMe,
                likes = if (it.likedByMe) it.likes - 1 else it.likes + 1
            )
        }
        data.value = posts
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
        posts = posts.map { if (it.id != id) it else it.copy(shareCount = it.shareCount + 1) }
        data.value = posts
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        val id = post.id
        val saved = dao.save(post)
        posts = if (id == 0L) {
            listOf(saved) + posts
        } else {
            posts.map {
                if (it.id != id) it else saved
            }
        }
        data.value = posts
    }

//    private fun getPosts(): List<Post> = context.filesDir.resolve(FILE_NAME)
//        .takeIf { it.exists() }
//        ?.inputStream()
//        ?.bufferedReader()
//        ?.use { gson.fromJson(it, postType) }
//        ?: emptyList()

//    private fun getId() = (posts.maxByOrNull { it.id }?.id ?: 0L) + 1L

//    private fun sync() {
//        context.filesDir.resolve(FILE_NAME).outputStream().bufferedWriter()
//            .use {
//                it.write(gson.toJson(posts))
//            }
//    }


//    private companion object {
//        const val FILE_NAME = "posts.json"
//        val gson = Gson()
//        val postType: Type = object : TypeToken<List<Post>>() {}.type
//    }
}