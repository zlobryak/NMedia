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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.R.drawable.ic_download_done_24
import ru.netology.nmedia.R.drawable.ic_sync_24
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.fragments.NewPostFragment.Companion.postArg
import ru.netology.nmedia.functions.counterFormatter
import ru.netology.nmedia.viewmodel.PostViewModel
import timber.log.Timber

@AndroidEntryPoint
class PostFragment : Fragment() {
    companion object {
        private const val TAG = "PostFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentPostBinding.inflate(layoutInflater, container, false)

        val viewModel: PostViewModel by viewModels()
//        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        // Получаем ID из аргументов. Если нет - выходим.
        val currentPostId = arguments?.postArg?.id ?: return binding.root

        // Наблюдаем за данными поста
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getPostById(currentPostId).collectLatest { currentPost ->
                    if (currentPost != null) {
                        Timber.tag(TAG).d("=== Render Post ID: ${currentPost.id} ===")


                        with(binding.postCard) {
                            avatar.setImageResource(R.drawable.ic_netology_48dp)
                            author.text = currentPost.author
                            content.text = currentPost.content
                            published.text = currentPost.published.toString()
                            icShare.text = counterFormatter(currentPost.shareCount)
                            icLikes.isChecked = currentPost.likedByMe
                            icLikes.text = counterFormatter(currentPost.likes)
                            icViews.text = counterFormatter(currentPost.views)

                            when (currentPost.syncStatus) {
                                PostEntity.SyncStatus.PENDING -> icSync.setIconResource(ic_sync_24)
                                PostEntity.SyncStatus.SYNCED -> icSync.setIconResource(ic_download_done_24)
                                PostEntity.SyncStatus.FAILED -> icSync.setIconResource(R.drawable.ic_refresh_24)
                                else -> {icSync.visibility = View.GONE}
                            }
                        }

                        with(binding.postCard) {
                            // Синхронизация
                            icSync.setOnClickListener {
                                if (currentPost.syncStatus == PostEntity.SyncStatus.FAILED) {
                                    viewModel.save(currentPost)
                                }
                            }

                            // Лайк
                            icLikes.setOnClickListener {
                                viewModel.likeById(currentPost)
                            }

                            // Шеринг
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

                            // Меню (редактировать / удалить)
                            menuButton.setOnClickListener {
                                PopupMenu(it.context, it).apply {
                                    inflate(R.menu.post_menu)
                                    setOnMenuItemClickListener { menuItem ->
                                        when (menuItem.itemId) {
                                            R.id.remove -> {
                                                viewModel.removeById(currentPost)
                                                true
                                            }
                                            R.id.edit -> {
                                                findNavController().navigate(
                                                    R.id.action_postFragment_to_newPostFragment,
                                                    Bundle().apply {
                                                        putParcelable("postArg", currentPost)
                                                    }
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
                    } else {
                        // Пост не найден (удален) — возвращаемся назад
                        findNavController().navigateUp()
                    }
                }
            }
        }

        // Обработчик кнопки "Назад" (не зависит от данных поста)
        binding.cancelButton.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }
}