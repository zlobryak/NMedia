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


        arguments?.textArg?.let(binding.content::setText)
        arguments?.postArg?.let { binding.content.setText(it.content) }
        val editPost: Post? = arguments?.postArg

        binding.saveButton.setOnClickListener {
            val text = binding.content.text?.toString()?.trim()
            if (editPost != null) {
                viewModel.save(
                    Post(
                        id = editPost.id,
                        content = text,
                        author = editPost.author,
                        published = editPost.published
                    )
                )
                findNavController().navigateUp()
            } else {
                //TODO Пост теперь может быть создан с пустым текстом.
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
        }
        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postArg: Post? by PostArg
    }

}