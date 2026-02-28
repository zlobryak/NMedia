package ru.netology.nmedia.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R

private const val BASE_URL = "http://10.0.2.2:9999"

fun ImageView.loadAvatar(authorAvatar: String?) {
    Glide.with(context)
        .load("$BASE_URL/avatars/$authorAvatar")
        .circleCrop()
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}

fun ImageView.loadAttachment(url: String) {
    Glide.with(context)
        .load("$BASE_URL/images/$url")
        .placeholder(R.drawable.ic_loading_100dp)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .into(this)
}