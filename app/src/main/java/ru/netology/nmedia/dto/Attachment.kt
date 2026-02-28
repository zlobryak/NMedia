package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class AttachmentType {
    IMAGE

}

@Parcelize
data class Attachment (
    val url: String? = null,
    val description: String? = null,
    val type: AttachmentType? = null
) : Parcelable

