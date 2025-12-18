package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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
        val postToView = arguments?.postArg

        if (postToView != null) {
            // Привязываем данные поста к UI-элементам карточки
            with(binding.postCard) {
                // Фиксированный аватар автора (в реальном приложении — из URL или профиля)
                avatar.setImageResource(R.drawable.ic_netology_48dp)
                // Имя автора
                author.text = postToView.author
                // Основной текст поста
                content.text = postToView.content
                // Время публикации (в формате строки, например "2 ч назад")
                published.text = postToView.published

                // Отображаем форматированное количество репостов (например, "1.2K")
                icShare.text = counterFormatter(postToView.shareCount)

                // Отображаем состояние лайка: кнопка отмечена, если пользователь уже лайкнул
                icLikes.isChecked = postToView.likedByMe
                // Форматированное количество лайков
                icLikes.text = counterFormatter(postToView.likes)

                // Обработка нажатия на кнопку "лайк" — переключает состояние через ViewModel
                icLikes.setOnClickListener {
                    viewModel.likeById(postToView.id)
                }

                // Обработка нажатия на кнопку "поделиться" — увеличивает счётчик репостов в ViewModel
                icShare.setOnClickListener {
                    viewModel.shareById(postToView.id)
                }

                // Открытие контекстного меню (редактировать / удалить) по нажатию на "три точки"
                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_menu)
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.remove -> {
                                    // Удаляем пост по ID
                                    viewModel.removeById(postToView.id)
                                    // После удаления фрагмент закроется автоматически при возврате
                                    true
                                }
                                R.id.edit -> {
                                    // Переход к экрану редактирования с передачей текущего поста
                                    findNavController().navigate(
                                        R.id.action_postFragment_to_newPostFragment,
                                        Bundle().apply { putParcelable("postArg", postToView) }
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

        // Обработка нажатия на кнопку "Назад" — возврат к предыдущему фрагменту (ленте)
        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }

        return binding.root
    }
}