package ru.netology.nmedia.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.fragments.NewPostFragment.Companion.postArg
import ru.netology.nmedia.functions.counterFormatter
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

/**
 * Фрагмент для просмотра отдельного поста в полноэкранном или детальном режиме.
 * Принимает пост через аргумент [postArg] и отображает его содержимое с возможностью взаимодействия:
 * лайк, репост, редактирование, удаление.
 */
class PostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)

        // Используем общую ViewModel, привязанную к родительскому фрагменту (для синхронизации с FeedFragment)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Получаем пост, переданный через аргументы навигации
        val currentPostId = arguments?.postArg?.id

        //Подпишемся на обновления и заполним элементы отображения данными (сейчас только список постов, без лайф дата)
        viewModel.data.observe(viewLifecycleOwner) { state ->

            val currentPost = state.posts.find { it.id == currentPostId }

            if (currentPost != null) {
                // Привязываем данные поста к UI-элементам карточки
                with(binding.postCard) {
                    // Фиксированный аватар автора (в реальном приложении — из URL или профиля)
                    avatar.setImageResource(R.drawable.ic_netology_48dp)
                    // Имя автора
                    author.text = currentPost.author
                    // Основной текст поста
                    content.text = currentPost.content
                    // Время публикации (в формате строки, например "2 ч назад")
                    published.text = currentPost.published
                    // Отображаем форматированное количество репостов (например, "1.2K")
                    icShare.text = counterFormatter(currentPost.shareCount)
                    // Отображаем состояние лайка: кнопка отмечена, если пользователь уже лайкнул
                    icLikes.isChecked = currentPost.likedByMe
                    // Форматированное количество лайков
                    icLikes.text = counterFormatter(currentPost.likes)
                    // Форматирование количества
                    icViews.text = counterFormatter(currentPost.views)
                }
            } else {
                //Если пост удален вернемся назад в ленту
                findNavController().navigateUp()
            }
            //Обработаем нажатия на кнопки
            if (currentPost != null) {
                with(binding.postCard) {
                    // Обработка нажатия на кнопку "лайк" — переключает состояние через ViewModel
                    icLikes.setOnClickListener {
                        viewModel.likeById(currentPost.id)
                    }

                    // Обработка нажатия на кнопку "поделиться" — увеличивает счётчик репостов в ViewModel
                    icShare.setOnClickListener {
                        viewModel.shareById(currentPost.id)
                        val intent = Intent()
                            .putExtra(Intent.EXTRA_TEXT, currentPost.content)
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

                    // Открытие контекстного меню (редактировать / удалить) по нажатию на "три точки"
                    menuButton.setOnClickListener {
                        PopupMenu(it.context, it).apply {
                            inflate(R.menu.post_menu)
                            setOnMenuItemClickListener { menuItem ->
                                when (menuItem.itemId) {
                                    R.id.remove -> {
                                        // Удаляем пост по ID
                                        viewModel.removeById(currentPost.id)
                                        // После удаления фрагмент закроется автоматически при возврате
                                        true
                                    }

                                    R.id.edit -> {
                                        // Переход к экрану редактирования с передачей текущего поста
                                        findNavController().navigate(
                                            R.id.action_postFragment_to_newPostFragment,
                                            Bundle().apply { putParcelable("postArg", currentPost) }
                                        )
                                        true
                                    }

                                    else -> false
                                }
                            }
                            show()
                        }
                    }
                }
            }
        }


        // Обработка нажатия на кнопку "Назад" — возврат к предыдущему фрагменту (ленте)
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }

        return binding.root
    }
}