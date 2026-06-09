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
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Post

/**
 * Firebase Cloud Messaging (FCM) сервис для обработки входящих push-уведомлений.
 * Поддерживает два типа событий: "NewPost" (новый пост в ленте) и "LIKE" (лайк от пользователя).
 * При получении соответствующего сообщения формирует и отображает системное уведомление,
 * если приложению предоставлено разрешение POST_NOTIFICATIONS.
 */
@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {

    @Inject
    lateinit var appAuth: AppAuth

    /**
     * Ключ для получения содержимого из данных FCM-сообщения.
     */
    private val content = "content"

    /**
     * Идентификатор канала уведомлений, используемый для группировки уведомлений в системе Android.
     */
    private val channelId = "remote"

    /**
     * Экземпляр Gson для десериализации JSON-данных из FCM-сообщений в объекты Kotlin.
     */
    private val gson = Gson()

    /**
     * Уникальный идентификатор для каждого нового уведомления. Инкрементируется при каждом вызове
     * [NotificationManagerCompat.notify] для обеспечения уникальности уведомлений.
     */
    private var notificationId = 1

    /**
     * Вызывается при создании сервиса. Регистрирует канал уведомлений (Notification Channel),
     * если версия Android >= Oreo (API 26), так как начиная с этой версии уведомления
     * обязательно должны быть привязаны к каналу.
     */
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

    /**
     * Вызывается при получении нового сообщения от Firebase Cloud Messaging.
     *
     * @param message Объект [RemoteMessage], содержащий данные входящего FCM-сообщения.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        Log.d("FCMService", "Message received: ${message.data}")

        // Парсим JSON. Если поле 'content' отсутствует или невалидно — выходим.
        val contentJson = message.data[content] ?: run {
            Log.w("FCMService", "No 'content' field in message")
            return
        }

        val pushMessage = try {
            gson.fromJson(contentJson, PushMessage::class.java)
        } catch (e: Exception) {
            Log.e("FCMService", "Failed to parse PushMessage: $contentJson", e)
            return
        }

        // Передаём в бизнес-логику
        handlePush(pushMessage)
    }

    /**
     * Обрабатывает входящий PushMessage согласно бизнес-логике:
     * - null или совпадение с текущим юзером - показать уведомление
     * - несовпадение (включая 0) - переотправить токен для синхронизации
     */
    private fun handlePush(pushMessage: PushMessage) {
        val auth = appAuth
        val currentUserId = auth.authStateFlow.value.id

        when {
            // Массовая рассылка или личное сообщение нам
            pushMessage.recipientId == null || pushMessage.recipientId == currentUserId -> {
                Log.d("FCMService", "Showing notification for recipient: ${pushMessage.recipientId}")
                showNotification(pushMessage.content)
            }
            // Конфликт авторизации: сервер считает, что мы другой пользователь (или аноним)
            else -> {
                Log.w("FCMService", "Recipient mismatch! Server: ${pushMessage.recipientId}, Local: $currentUserId. Resending token...")
                if (currentUserId > 0) {
                    auth.sendPushToken(auth.authStateFlow.value.token ?: return)
                }
            }
        }
    }

    /**
     * Вспомогательный метод для отображения уведомления.
     * Вынесен отдельно для чистоты кода.
     */
    private fun showNotification(text: String) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name)) // Или динамический заголовок
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(notificationId++, notification)
        }
    }


    /**
     * Обрабатывает событие создания нового поста.
     * Формирует уведомление с заголовком "Новый пост от [Post.author]" и текстом содержимого поста.
     * Использует [NotificationCompat.BigTextStyle] для корректного отображения длинного текста.
     *
     * Отображает уведомление только если приложению разрешено [Manifest.permission.POST_NOTIFICATIONS].
     *
     * @param post Объект [Post], десериализованный из JSON-данных FCM-сообщения.
     */
//    private fun handleNewPost(post: Post) {
//        Log.w("FCMService", "handleNewPost: $post")
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle(
//                getString(R.string.author_of_new_post_push_notification_title, post.author)
//            )
//            .setContentText(post.content)
//            .setStyle(
//                NotificationCompat.BigTextStyle()
//                    .bigText(post.content)
//            )
//            .build()
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(this).notify(notificationId++, notification)
//        }
//    }

    /**
     * Обрабатывает событие получения лайка.
     * Формирует уведомление с текстом "[Like.userName] поставил(а) лайк вашему посту от [Like.postAuthor]".
     *
     * Отображает уведомление только если приложению разрешено [Manifest.permission.POST_NOTIFICATIONS].
     *
     * @param like Объект [Like], десериализованный из JSON-данных FCM-сообщения,
     *             содержит информацию о пользователе, поставившем лайк, и авторе поста.
     */
//    private fun handleLike(like: Like) {
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentText(
//                getString(R.string.notification_user_liked, like.userName, like.postAuthor)
//            )
//            .build()
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            NotificationManagerCompat.from(this).notify(notificationId++, notification)
//        }
//    }

    /**
     * Вызывается при обновлении FCM-токена устройства.
     * Текущая реализация лишь выводит токен в консоль (через `println`).
     * В реальном приложении этот токен следует отправлять на сервер для последующей отправки push-уведомлений.
     *
     * @param token Новый FCM-токен устройства в виде строки.
     */
    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
    }
}

/**
 * DTO-класс для передачи данных о лайке через FCM.
 * Содержит идентификаторы и имена участвующих пользователей.
 *
 * @property userId Идентификатор пользователя, поставившего лайк.
 * @property userName Имя пользователя, поставившего лайк.
 * @property postId Идентификатор поста, на который поставлен лайк.
 * @property postAuthor Имя автора поста, получившего лайк.
 */
data class Like(
    val userId: Long,
    val userName: String,
    val postId: Long,
    val postAuthor: String,
)

data class PushMessage(
    val recipientId: Long?,
    val content: String,
)