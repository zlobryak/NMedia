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

/**
 * Фрагмент для создания нового поста или редактирования существующего.
 * Определяет режим работы по переданным аргументам:
 * - если передан [Post] через [postArg] — работает в режиме редактирования,
 * - если передан только [textArg] или аргументы отсутствуют — создаёт новый пост.
 */
class NewPostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)

        // Используем ViewModel, общую для всех дочерних фрагментов (через родительский фрагмент)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Предзаполняем поле текста, если передан аргумент textArg (например, при "Поделиться")
        arguments?.textArg?.let(binding.content::setText)

        // Или, если передан целый пост (режим редактирования) — подставляем его содержимое
        arguments?.postArg?.let { binding.content.setText(it.content) }

        // Сохраняем ссылку на редактируемый пост (если есть), чтобы отличать режим редактирования от создания
        val editPost: Post? = arguments?.postArg

        // Обработка нажатия на кнопку "Сохранить"
        binding.saveButton.setOnClickListener {
            val text = binding.content.text?.toString()?.trim()

            if (editPost != null) {
                // Режим редактирования: обновляем существующий пост, сохраняя его ID и метаданные
                viewModel.save(
                    Post(
                        id = editPost.id,
                        content = text,
                        author = editPost.author,
                        published = editPost.published
                    )
                )
                // Возвращаемся назад в ленту
                findNavController().navigateUp()
            } else {
                // Режим создания нового поста:
                // — временный ID (0L) будет заменён в репозитории или ViewModel,
                // — автор и время публикации задаются заглушками (в реальном приложении — из профиля и текущего времени).
                // TODO: Добавить валидацию — запретить создание поста с пустым или пробельным текстом
                viewModel.save(
                    Post(
                        id = 0L,
                        author = "Me",
                        content = text,
                        published = "Just now",
                    )
                )
                findNavController().navigateUp()
            }
        }

        return binding.root
    }

    /**
     * Расширения для безопасной и удобной работы с аргументами Bundle.
     * Позволяют обращаться к аргументам как к свойствам через делегаты (StringArg, PostArg).
     */
    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postArg: Post? by PostArg
    }
}