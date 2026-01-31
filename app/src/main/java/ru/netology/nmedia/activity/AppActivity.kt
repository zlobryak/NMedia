package ru.netology.nmedia.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityAppBinding
import kotlin.apply

/**
 * Главная активность приложения, отображающая навигацию через NavHostFragment.
 * Обрабатывает входящие Intent'ы с действием ACTION_SEND (например, "Поделиться" из других приложений).
 */
class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включает режим edge-to-edge для полного использования экрана (включая системные бары)
        enableEdgeToEdge()

        // Инициализация binding'а для макета activity_app
        val binding = ActivityAppBinding.inflate(layoutInflater)

//Ниже старая версия, где быо=л XML вместо JetPack Compose
////        setContentView(binding.root)
//
//        // Настройка отступов для корневого View с учётом системных баров (статус-бар, навигационная панель)
//        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PostCard(
                        Post(
                            author = "Sergey",
                            published = "31.01.2025",
                            content = "\uD83D\uDD25 Тест прошёл. Система в норме.\n" +
                                    "Готов к работе.",
                            likedByMe = false
                        ),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }


        requestNotificationsPermission()

        // Получаем действие, с которым был запущен Intent
        val action = intent.action

        // Обработка интента с действием "поделиться текстом" (ACTION_SEND)
        if (action == Intent.ACTION_SEND) {
            // Извлекаем текст из EXTRA_TEXT
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)

            // Если текст отсутствует или состоит только из пробелов — показываем ошибку
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.text_is_blank_error,
                    Snackbar.LENGTH_SHORT
                ).setAction(android.R.string.ok) {
                    // Закрываем активность по нажатию на кнопку "OK"
                    finish()
                }.show()
            } else {
                // Откладываем навигацию на следующий цикл обработки UI,
                // чтобы убедиться, что NavController уже инициализирован
                binding.root.post {
                    // Выполняем переход на фрагмент создания нового поста,
                    // передавая текст через аргумент "textArg"
                    findNavController(R.id.nav_controller).navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { putString("textArg", text) }
                    )
                }
            }
        }
        checkGoogleApiAvailability()
    }

    private fun requestNotificationsPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }

        requestPermissions(arrayOf(permission), 1)
    }

    private fun checkGoogleApiAvailability() {
        with(GoogleApiAvailability.getInstance()) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(
                this@AppActivity,
                getString(R.string.google_play_unavailable), Toast.LENGTH_LONG
            ).show()
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener {
            println(it)
        }
    }
}

//Ниже закомментирована тестовая функция для Jetpack Compose
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name",
//        modifier = modifier
//    )
//
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    Greeting("Test")
//}

data class Post(
    val author: String,
    val published: String,
    val content: String,
    val likedByMe: Boolean
)

@Composable
fun PostCard(post: Post, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(text = post.author.take(1))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(post.author)
                Text(post.published)
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(post.content)
//TODO Уменьшить отступ иконки
        IconButton(onClick = {}, Modifier.padding(start = (-12).dp)) {
            Icon(
                imageVector = if (post.likedByMe) {
                    Icons.Filled.Favorite
                } else {
                    Icons.Outlined.FavoriteBorder
                },
                contentDescription = if (post.likedByMe) {
                    "Unlike"
                } else {
                    "Like"
                },
                tint = if (post.likedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }

    }

}

@Preview(showBackground = true)
@Composable
fun PostCardPreview() {
    MaterialTheme {
        PostCard(
            Post(
                author = "Sergey",
                published = "31.01.2025",
                content = "\uD83D\uDD25 Тест прошёл. Система в норме.\n" +
                        "Готов к работе.",
                likedByMe = true
            )
        )
    }
}
