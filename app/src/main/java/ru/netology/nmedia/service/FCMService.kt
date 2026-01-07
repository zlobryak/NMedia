package ru.netology.nmedia.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import kotlin.enumValues

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val channelId = "remote"
    private val gson = Gson()

    private var notificationId = 1

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val actionStr = message.data[action] ?: run {
            Log.w("FCMService", "No 'action' field in message")
            return
        }

        val actionEnum = enumValues<Action>().find { it.name == actionStr }

        when (actionEnum) {
            Action.LIKE -> handleLike(gson.fromJson(message.data[content], Like::class.java))
            //При получении сообщения с пометкой "NewPost" передадим в метод handleNewPost полученный пост
            Action.NewPost -> handleNewPost(gson.fromJson(message.data[content], Post::class.java))
            null ->
                // Логируем неизвестное действие
                Log.w("FCMService", "Unknown action: $actionStr")
        }

    }

    private fun handleNewPost(post: Post) {
        Log.w("FCMService", "handleNewPost: $post")
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(R.string.author_of_new_post_push_notification_title, post.author)
            )
            .setContentText(post.content)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(post.content))
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId++, notification)
        }
    }

    private fun handleLike(like: Like) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentText(
                getString(R.string.notification_user_liked, like.userName, like.postAuthor)
            )
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId++, notification)
        }
    }

    override fun onNewToken(token: String) {
        println(token)
    }
}

enum class Action {
    LIKE,
    NewPost,
}

data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)