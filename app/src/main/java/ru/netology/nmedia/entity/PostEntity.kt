package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
class PostEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    var content: String?,
    val published: String,
    val likes: Int = 0,
    val shareCount: Int = 0,
    val likedByMe: Boolean = false,
    val views: Int = 0,
    val videoUrl: String? = null,
    val previewImageUrl: String? = null,
    val videoPreviewText: String? = null,
    val videoViewsCount: Int? = null
){
    fun toDto() = Post(
        id, author, content, published, likes, shareCount, likedByMe, views, videoUrl, previewImageUrl, videoPreviewText, videoViewsCount
    )

    companion object{
        fun fromDto(post: Post) = PostEntity(post.id, post.author, post.content, post.published, post.likes, post.shareCount, post.likedByMe, post.views, post.videoUrl, post.previewImageUrl, post.videoPreviewText, post.videoViewsCount)

    }
}