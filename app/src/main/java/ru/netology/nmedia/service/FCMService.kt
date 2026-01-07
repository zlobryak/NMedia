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

/**
 * Firebase Cloud Messaging (FCM) сервис для обработки входящих push-уведомлений.
 * Поддерживает два типа событий: "NewPost" (новый пост в ленте) и "LIKE" (лайк от пользователя).
 * При получении соответствующего сообщения формирует и отображает системное уведомление,
 * если приложению предоставлено разрешение POST_NOTIFICATIONS.
 *
 * Также отвечает за создание уведомления канала (начиная с Android Oreo/API 26)
 * и логирование нового FCM-токена устройства.
 */
class FCMService : FirebaseMessagingService() {

    /**
     * Ключ для получения типа действия из данных FCM-сообщения.
     */
    private val action = "action"

    /**
     * Ключ для получения содержимого (payload) из данных FCM-сообщения.
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
     * Извлекает тип действия из поля "action" и содержимое из поля "content".
     * На основе значения действия вызывает соответствующий обработчик:
     * - [handleNewPost] для действия [Action.NewPost]
     * - [handleLike] для действия [Action.LIKE]
     *
     * Если действие не распознано или отсутствует, записывает предупреждение в лог.
     *
     * @param message Объект [RemoteMessage], содержащий данные входящего FCM-сообщения.
     */
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

    /**
     * Обрабатывает событие создания нового поста.
     * Формирует уведомление с заголовком "Новый пост от [Post.author]" и текстом содержимого поста.
     * Использует [NotificationCompat.BigTextStyle] для корректного отображения длинного текста.
     *
     * Отображает уведомление только если приложению разрешено [Manifest.permission.POST_NOTIFICATIONS].
     *
     * @param post Объект [Post], десериализованный из JSON-данных FCM-сообщения.
     */
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

    /**
     * Обрабатывает событие получения лайка.
     * Формирует уведомление с текстом "[Like.userName] поставил(а) лайк вашему посту от [Like.postAuthor]".
     *
     * Отображает уведомление только если приложению разрешено [Manifest.permission.POST_NOTIFICATIONS].
     *
     * @param like Объект [Like], десериализованный из JSON-данных FCM-сообщения,
     *             содержит информацию о пользователе, поставившем лайк, и авторе поста.
     */
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

    /**
     * Вызывается при обновлении FCM-токена устройства.
     * Текущая реализация лишь выводит токен в консоль (через `println`).
     * В реальном приложении этот токен следует отправлять на сервер для последующей отправки push-уведомлений.
     *
     * @param token Новый FCM-токен устройства в виде строки.
     */
    override fun onNewToken(token: String) {
        println(token)
    }
}

/**
 * Перечисление поддерживаемых типов действий, которые могут приходить в FCM-сообщениях.
 * Используется для маршрутизации входящих сообщений к соответствующим обработчикам.
 */
enum class Action {
    /**
     * Действие: пользователь поставил лайк на пост.
     */
    LIKE,

    /**
     * Действие: опубликован новый пост.
     */
    NewPost,
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