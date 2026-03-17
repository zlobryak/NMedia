package ru.netology.nmedia.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//DTO (Data Transfer Object)
//Расшифровка: Объект передачи данных (Data Transfer Object).
//Суть: Это простой объект (часто просто набор полей с геттерами/сеттерами или data class в Kotlin),
//который используется исключительно для передачи данных между разными слоями приложения или разными системами.
//Зачем нужен:
//Чтобы скрыть внутреннюю структуру базы данных от внешнего мира (например, от UI или API).
//Чтобы передавать только нужные данные, а не всю сущность целиком.
//Ключевая особенность: Обычно не содержит бизнес-логики, только данные.

enum class AttachmentType {    IMAGE

}

@Parcelize
data class Attachment (
    val url: String? = null,
    val description: String? = null,
    val type: AttachmentType? = null
) : Parcelable

