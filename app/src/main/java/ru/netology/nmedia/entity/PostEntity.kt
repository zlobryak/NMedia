package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import kotlin.String

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val author: String,
    var content: String?,
    val published: String?,
    val likes: Int = 0,
    val shareCount: Int = 0,
    val likedByMe: Boolean = false,
    val views: Int = 0,
    val videoUrl: String? = null,
    val previewImageUrl: String? = null,
    val videoPreviewText: String? = null,
    val videoViewsCount: Int? = null,
    val authorAvatar: String,
    val attachment: Attachment? = null,
    val isSynced: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {

    //Для работы с локальной базой данных
    fun toDto() = Post(
        id,
        author,
        content,
        published,
        likes,
        shareCount,
        likedByMe,
        views,
        videoUrl,
        previewImageUrl,
        videoPreviewText,
        videoViewsCount,
        authorAvatar,
        attachment,
        isSynced,
        syncStatus
    )

    companion object {
        fun fromDto(post: Post) = PostEntity(
            post.id,
            post.author,
            post.content,
            post.published,
            post.likes,
            post.shareCount,
            post.likedByMe,
            post.views,
            post.videoUrl,
            post.previewImageUrl,
            post.videoPreviewText,
            post.videoViewsCount,
            post.authorAvatar,
            post.attachment,
            //Все пришедшие с сервера посты помечаются флагами и статусом
            isSynced = true,
            syncStatus = SyncStatus.SYNCED
        )

    }

    enum class SyncStatus {
            PENDING,
        SYNCED,
        FAILED
    }
}