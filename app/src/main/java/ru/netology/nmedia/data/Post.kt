package ru.netology.nmedia.data

data class Post(
    val id: Long,
    val author: String,
    var content: String,
    val published: String,
    var likes: Int = 0,
    var shareCount: Int = 0,
    var likedByMe: Boolean = false
) {

}
