package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import ru.netology.nmedia.dto.Post

class NewPostActivityContract : ActivityResultContract<Post?, Post?>() {
    override fun createIntent(
        context: Context,
        input: Post?
    ): Intent = Intent(context, NewPostActivity::class.java).apply {
        putExtra("post", input) }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Post? = if (resultCode == Activity.RESULT_OK) {
        intent?.getParcelableExtra("result_post")
    } else {
        null
    }
}