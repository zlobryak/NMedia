package ru.netology.nmedia.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
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
 * — если передан только [textArg] или аргументы отсутствуют — создаёт новый пост.
 */
class NewPostFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Инициализируем SharedPreferences при подключении фрагмента
        sharedPreferences = context.getSharedPreferences("PostDraftPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)

        // Используем ViewModel, общую для всех дочерних фрагментов (через родительский фрагмент)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Подгружаем черновик, если аргументы не переданы
        val initialText = when {
            arguments?.postArg != null -> requireArguments().postArg!!.content
            arguments?.textArg != null -> requireArguments().textArg
            else -> sharedPreferences?.getString(DRAFT_KEY, "") ?: ""
        }
        binding.content.setText(initialText)

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
                // Удаляем черновик после успешного сохранения (только в режиме создания)
                sharedPreferences?.edit{ remove(DRAFT_KEY) }

                findNavController().navigateUp()
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val draftText = binding.content.text?.toString()?.trim()
                if (!draftText.isNullOrEmpty()) {
                    sharedPreferences?.edit { putString(DRAFT_KEY, draftText) }
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        return binding.root
    }

    /**
     * Расширения для удобного доступа к аргументам Bundle с использованием делегатов.
     * Позволяют читать и записывать значения типа [String] и [Post] через свойства [textArg] и [postArg].
     * Также содержит константу [DRAFT_KEY] для хранения черновика и ссылку на [sharedPreferences].
     */
    companion object {
        var Bundle.textArg: String? by StringArg
        var Bundle.postArg: Post? by PostArg
        private const val DRAFT_KEY = "new_post_draft"
        private var sharedPreferences: SharedPreferences? = null
    }
}