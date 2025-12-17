package ru.netology.nmedia.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.PostViewModel

class FeedFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        // Получаем ViewModel через делегат viewModels (привязан к жизненному циклу Activity)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)


        // Создаём адаптер RecyclerView и передаём обработчики действий над постами
        val adapter = PostsAdapter(
            object : PostListener {
                override fun onEdit(post: Post) {
                    findNavController().navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { putString("textArg", post.content) }
                        )

//                    newPostLuncher.launch(post) //Запустим новую активити и передадим в нее пост TODO Переделать на фрагмент (было через контракт на создание активити)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id) // Удаляем пост по ID
                }

                override fun onLike(post: Post) {
                    viewModel.likeById(post.id) // Ставим/убираем лайк по ID
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id) // Фиксируем факт шеринга (в ViewModel)

                    // Создаём Intent для шеринга текста поста через системный диалог
                    val intent = Intent()
                        .putExtra(Intent.EXTRA_TEXT, post.content)
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")

                    try {
                        // Запускаем выбор приложения для шеринга
                        startActivity(Intent.createChooser(intent, null))
                    } catch (_: ActivityNotFoundException) {
                        // Если нет приложений для шеринга — показываем сообщение
//TODO() Показать сообщение
                    }
                }

                override fun onOpenVideo(url: String) {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    startActivity(intent)
                }
            }
        )

        // Слушатель изменений данных в адаптере:
        // При добавлении новых элементов в начало списка — плавно прокручиваем к верху
        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            }
        )

        // Назначаем адаптер списку постов
        binding.list.adapter = adapter

        // Наблюдаем за списком постов и обновляем RecyclerView через адаптер
        viewModel.data.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
        }

        // Обработчик нажатия на кнопку "Добавить пост" — запускает NewPostActivity
        binding.addButton.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }


        return binding.root
    }
}
