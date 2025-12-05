package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    var content: String,
    val published: String,
    val likes: Int = 0,
    val shareCount: Int = 0,
    val likedByMe: Boolean = false,
    val views: Int = 0
) {

}
