package ru.netology.nmedia.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dto.Attachment

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return if (attachment == null) null else gson.toJson(attachment)
    }

    @TypeConverter
    fun toAttachment(json: String?): Attachment? {
        return if (json == null) null else gson.fromJson(json, object : TypeToken<Attachment>() {}.type)
    }
}