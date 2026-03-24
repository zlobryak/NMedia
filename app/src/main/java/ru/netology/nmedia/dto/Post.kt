package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.netology.nmedia.entity.PostEntity


@Parcelize
data class Post(
    val id: Long,
    val author: String,
    var content: String?,
    val published: String? = null, //Сервер возвращает время создания
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
    val isSynced: Boolean,
    val syncStatus: PostEntity.SyncStatus
) : Parcelable
