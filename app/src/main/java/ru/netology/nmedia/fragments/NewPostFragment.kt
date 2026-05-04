package ru.netology.nmedia.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.edit
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
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

        // Обработка нажатия на кнопку "Сохранить" старая версия
        binding.saveButton.setOnClickListener {
            val text = binding.content.text?.toString()?.trim()

            if (editPost != null) {
                // Режим редактирования: обновляем существующий пост, сохраняя его ID и метаданные
                viewModel.save(
                    Post(
                        id = editPost.id,
                        author = editPost.author,
                        content = text,
                        published = editPost.published,
                        authorAvatar = "netology.jpg",
                        isSynced = editPost.isSynced,
                        syncStatus = editPost.syncStatus,
                        isVisible = isVisible,
                    )
                )
                findNavController().navigateUp()
            } else {
                // Режим создания нового поста:
                // — временный ID (0L) будет заменён на сервере,
                // — автор и время публикации задаются заглушками (в реальном приложении — из профиля и текущего времени).
                viewModel.save(
                    Post(
                        id = 0L,
                        author = "Student",
                        content = text,
                        published = System.currentTimeMillis().toString(),
                        authorAvatar = "netology.jpg",
                        isSynced = false,
                        syncStatus = PostEntity.SyncStatus.PENDING,
                        isVisible = true
                    )
                )
                // Удаляем черновик после сохранения (только в режиме создания)
                sharedPreferences?.edit { remove(DRAFT_KEY) }
                findNavController().navigateUp()
            }
        }

        //Наблюдаем за появлением нового поста и только после этого возвращаемся в ленту
        viewModel.postCreated.observe(viewLifecycleOwner) {
            viewModel.load() //В то же время запросим новые посты
            findNavController().navigateUp()
        }

        //Показываем ошибку пользователю
        viewModel.errorEvent.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
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

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when(item.itemId){
                    R.id.save  ->{
                        val text = binding.content.text?.toString()?.trim()

                        if (editPost != null) {
                            // Режим редактирования: обновляем существующий пост, сохраняя его ID и метаданные
                            viewModel.save(
                                Post(
                                    id = editPost.id,
                                    author = editPost.author,
                                    content = text,
                                    published = editPost.published,
                                    authorAvatar = "netology.jpg",
                                    isSynced = editPost.isSynced,
                                    syncStatus = editPost.syncStatus,
                                    isVisible = isVisible,
                                )
                            )
                            findNavController().navigateUp()
                        } else {
                            // Режим создания нового поста:
                            // — временный ID (0L) будет заменён на сервере,
                            // — автор и время публикации задаются заглушками (в реальном приложении — из профиля и текущего времени).
                            viewModel.save(
                                Post(
                                    id = 0L,
                                    author = "Student",
                                    content = text,
                                    published = System.currentTimeMillis().toString(),
                                    authorAvatar = "netology.jpg",
                                    isSynced = false,
                                    syncStatus = PostEntity.SyncStatus.PENDING,
                                    isVisible = true
                                )
                            )
                            // Удаляем черновик после сохранения (только в режиме создания)
                            sharedPreferences?.edit { remove(DRAFT_KEY) }
                            findNavController().navigateUp()
                        }
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner)

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