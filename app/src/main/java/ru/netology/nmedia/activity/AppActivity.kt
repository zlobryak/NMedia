package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
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
        setContentView(binding.root)

        // Настройка отступов для корневого View с учётом системных баров (статус-бар, навигационная панель)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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
    }
}