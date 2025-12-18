package ru.netology.nmedia.fragments

import android.os.Bundle
import android.util.Log
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


class PostFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        Log.d("PostFragment", "Arguments: ${arguments}")
        Log.d("PostFragment", "postArg = ${arguments?.postArg}")

        val postToView = arguments?.postArg
        if (postToView != null) {

            with(binding.postCard) {
                // Установка аватара автора (в данном случае — фиксированная иконка)
                avatar.setImageResource(R.drawable.ic_netology_48dp)
                // Имя автора поста
                author.text = postToView.author
                // Текст поста
                content.text = postToView.content
                // Время публикации
                published.text = postToView.published
                // Форматированное количество репостов
                icShare.text = counterFormatter(postToView.shareCount)
                // Установка иконки лайка в зависимости от состояния likedByMe
                icLikes.isChecked = postToView.likedByMe
                // Форматированное количество лайков (например, "1K", "2.5M")
                icLikes.text = counterFormatter(postToView.likes)
                // Обработка нажатия на иконку "лайк"
                icLikes.setOnClickListener {
                    viewModel.likeById(postToView.id)
                }
                // Обработка нажатия на иконку "поделиться"
                icShare.setOnClickListener {
                    viewModel.shareById(postToView.id)
                }
//             Обработка нажатия на кнопку меню (три точки) для открытия меню поста
                menuButton.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        // Загрузка меню из ресурсов (post_menu.xml)
                        inflate(R.menu.post_menu)
                        // Обработка выбора пунктов меню
                        setOnMenuItemClickListener { menuItem ->
                            when (menuItem.itemId) {
                                R.id.remove -> {
                                    viewModel.removeById(postToView.id)
                                    true
                                }

                                R.id.edit -> {
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

        binding.cancelButton.setOnClickListener { findNavController().navigateUp() }

        return binding.root
    }
}
