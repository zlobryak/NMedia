package ru.netology.nmedia.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.StringArg
import ru.netology.nmedia.utils.PostArg
import ru.netology.nmedia.viewmodel.PostViewModel
import kotlin.getValue

class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Получаем пост для редактирования (может быть null — тогда новый)
//        val editingPost = intent.getParcelableExtra<Post>("post") //TODO Получать пост для редактирования через фрагменты

        arguments?.textArg?.let(binding.content::setText)
        arguments?.postArg

        binding.saveButton.setOnClickListener {
            val text = binding.content.text?.toString()?.trim()
            if (text.isNullOrBlank()) {
                findNavController().navigateUp()
                //TODO Показать ошибку и попросить ввод текста
            } else {
                //TODO Если был получен пост для редактирования, то нужно сохранить уже существующий пост
                viewModel.save(
                    Post(
                        id = 0L, // временный ID для новых
                        author = "Me", // или брать из профиля
                        content = text,
                        published = "Just now",
                    )
                )
                findNavController().navigateUp()
            }

//TODO Вставлять текст в content если получен не устой пост
//            if (editingPost != null) {
//                binding.content.setText(editingPost.content)
//            }
        }
        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postArg: Post? by PostArg
    }

}