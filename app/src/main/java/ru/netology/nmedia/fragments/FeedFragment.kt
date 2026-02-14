package ru.netology.nmedia.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

/**
 * Фрагмент ленты постов.
 * Отображает список постов, обрабатывает действия пользователя (лайк, шер, редактирование и т.д.)
 * и управляет навигацией к экрану создания/редактирования поста.
 */
class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)

        // Получаем общую ViewModel через parent fragment (для совместного использования с другими дочерними фрагментами)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Создаём адаптер списка постов и реализуем обработчики действий через PostListener
        val adapter = PostsAdapter(
            object : PostListener {
                /**
                 * Обработка запроса на редактирование поста.
                 * Переход к фрагменту создания/редактирования с передачей существующего поста.
                 */
                override fun onEdit(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply {
                            putParcelable("postArg", post)
                        }
                    )
                }

                /**
                 * Обработка удаления поста — делегирует операцию ViewModel по ID.
                 */
                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                /**
                 * Обработка лайка/дизлайка — передаёт ID поста в ViewModel.
                 */
                override fun onLike(post: Post) {
                    viewModel.likeById(post)
                }

                /**
                 * Обработка действия "Поделиться":
                 * — сначала фиксируется факт шеринга в ViewModel (для аналитики или увеличения счётчика),
                 * — затем запускается системный диалог выбора приложения для отправки текста.
                 */
                //TODO не работает с текущим сервером
                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)

                    val intent = Intent()
                        .putExtra(Intent.EXTRA_TEXT, post.content)
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")

                    try {
                        startActivity(Intent.createChooser(intent, null))
                    } catch (_: ActivityNotFoundException) {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.no_app_found),
                            Snackbar.LENGTH_SHORT
                        ).show()

                    }
                }

                /**
                 * Обработка открытия видео по ссылке.
                 * Используется стандартный Intent.ACTION_VIEW для запуска внешнего видеоплеера.
                 */
                override fun onOpenVideo(url: String) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            }
        )

        // Отслеживаем добавление новых элементов в начало списка:
        // если новые посты добавлены в позицию 0 — плавно прокручиваем список вверх
        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            }
        )

        // Привязываем адаптер к RecyclerView
        binding.list.adapter = adapter

        // Наблюдаем за изменением списка постов в ViewModel и обновляем UI через submitList
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.progress.isVisible = state.loading
            binding.errorGroup.isVisible = state.error
            binding.empty.isVisible = state.empty
        }

        // Обработаем нажатие на кнопку повторить
        binding.retry.setOnClickListener { viewModel.load() }

        // Обработка нажатия на FAB (кнопку "Новый пост") — переход к экрану создания поста без аргументов
        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        return binding.root
    }
}