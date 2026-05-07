package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentPostImageBinding
import ru.netology.nmedia.functions.counterFormatter
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.extensions.loadAttachment
import ru.netology.nmedia.fragments.NewPostFragment.Companion.postArg

/**
 * Фрагмент для полноэкранного просмотра изображения поста
 */
class PostImageFragment : Fragment() {

    private var _binding: FragmentPostImageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PostViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем пост bp тз аргументов
        val post = arguments?.postArg

        // Настраиваем Toolbar
        setupToolbar()

        // Загружаем изображение на весь экран
        binding.imageView.loadAttachment(post?.attachment?.url)


        // Отображаем количество лайков

        if (post != null) {
            binding.icLikes.text = counterFormatter(post.likes)


            // Опционально: показываем, лайкнул ли текущий пользователь
            binding.icLikes.isChecked = post.likedByMe

            // Обработка клика на лайк
            binding.icLikes.setOnClickListener {
                viewModel.likeById(post)
            }
        }


        // Закрытие по клику на изображение
        binding.imageView.setOnClickListener{
            findNavController().navigateUp()
        }

    }


    private fun setupToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false) // Скрываем заголовок для чистого вида
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}