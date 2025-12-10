package ru.netology.nmedia.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включает Edge-to-Edge режим (полноэкранный UI с учетом системных инсетов)
        enableEdgeToEdge()

        // Инициализация View Binding для основного макета
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем отступ из ресурсов для соблюдения общего отступа интерфейса
        val margin = resources.getDimensionPixelSize(R.dimen.common_spacing)

        // Настройка обработки системных инсетов (статус-бар, навигация и т.д.)
        // Устанавливаем отступы с учётом системных элементов и общего отступа margin
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + margin,
                systemBars.top + margin,
                systemBars.right + margin,
                systemBars.bottom + margin
            )
            insets
        }

        // Получаем ViewModel через делегат viewModels (привязан к жизненному циклу Activity)
        val viewModel: PostViewModel by viewModels()

        // Регистрация контракта для запуска активности создания поста
        // Результат возвращается через колбэк, где сохраняется новый пост
        val newPostLuncher: ActivityResultLauncher<Post?> =
            registerForActivityResult(NewPostActivityContract()) { result ->

                result?.let { viewModel.save(it) }

            }

        // Создаём адаптер RecyclerView и передаём обработчики действий над постами
        val adapter = PostsAdapter(
            object : PostListener {
                override fun onEdit(post: Post) {
                    newPostLuncher.launch(post) //Запустим новую активити и передадим в нее пост
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id) // Удаляем пост по ID
                }

                override fun onLike(post: Post) {
                    viewModel.likeById(post.id) // Ставим/убираем лайк по ID
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id) // Фиксируем факт шеринга (в ViewModel)

                    // Создаём Intent для шеринга текста поста через системный диалог
                    val intent = Intent()
                        .putExtra(Intent.EXTRA_TEXT, post.content)
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")

                    try {
                        // Запускаем выбор приложения для шеринга
                        startActivity(Intent.createChooser(intent, null))
                    } catch (_: ActivityNotFoundException) {
                        // Если нет приложений для шеринга — показываем сообщение
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.app_is_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onOpenVideo(url: String) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            }
        )

        // Слушатель изменений данных в адаптере:
        // При добавлении новых элементов в начало списка — плавно прокручиваем к верху
        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            }
        )

        // Наблюдение за состоянием редактируемого поста в ViewModel.
        // Используется как для создания нового поста, так и для редактирования существующего
//        viewModel.edited.observe(this) { post ->
//            if (post.id != 0L) {
//                // Если редактируется существующий пост (id != 0):
//                binding.content.setText(post.content) // Загружаем текст в поле ввода
//                binding.editGroup.visibility =
//                    android.view.View.VISIBLE // Показываем панель редактирования
//                binding.editPreviewText.text = post.content // Показываем превью текста
//                AndroidUtils.showKeyboard(binding.content) // Показываем клавиатуру
//            } else {
//                // Если создаётся новый пост (id == 0):
//                binding.editGroup.visibility =
//                    android.view.View.GONE // Скрываем панель редактирования
//                binding.content.setText("") // Очищаем поле ввода
//            }

        // Устанавливаем обработчик нажатия на кнопку сохранения
//            binding.saveButton.setOnClickListener {
//                with(binding.content) {
//                    // Проверка, что текст не пустой
//                    if (text.isNullOrBlank()) {
//                        Toast.makeText(context, R.string.text_is_blank, Toast.LENGTH_SHORT).show()
//                        return@setOnClickListener
//                    }
//                    // Сохраняем текст поста через ViewModel
//                    viewModel.save(text.toString())
//                    // Очищаем поле, убираем фокус и скрываем панель редактирования
//                    setText("")
//                    clearFocus()
//                    binding.editGroup.visibility = android.view.View.GONE
//                    AndroidUtils.hideKeyboard(this)
//                }
//            }

        // Устанавливаем обработчик нажатия на кнопку отмены
//            binding.cancelButton.setOnClickListener {
//                with(binding.content) {
//                    setText("") // Очищаем поле
//                    clearFocus()
//                    binding.editGroup.visibility = android.view.View.GONE // Скрываем панель
//                    AndroidUtils.hideKeyboard(this)
//                    viewModel.resetEdited() // Сбрасываем редактируемый пост до "нового"
//                }
//            }

        // Назначаем адаптер списку постов
        binding.list.adapter = adapter

        // Наблюдаем за списком постов в ViewModel и обновляем RecyclerView через адаптер
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }

        // Обработчик нажатия на кнопку "Добавить пост" — запускает NewPostActivity
        binding.addButton.setOnClickListener { newPostLuncher.launch(null) }
    }
}
