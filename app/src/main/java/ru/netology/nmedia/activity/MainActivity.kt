package ru.netology.nmedia.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.PostListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.utils.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val margin = resources.getDimensionPixelSize(R.dimen.common_spacing)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left + margin,
                systemBars.top + margin,
                systemBars.right + margin,
                systemBars.bottom + margin
            )
            insets
        }

        val viewModel: PostViewModel by viewModels()
        val adapter = PostsAdapter(
            object : PostListener {
                override fun onEdit(post: Post) {
                    viewModel.edit(post)
                }

                override fun onRemove(post: Post) {
                    viewModel.removeById(post.id)
                }

                override fun onLike(post: Post) {
                    viewModel.likeById(post.id)
                }

                override fun onShare(post: Post) {
                    viewModel.shareById(post.id)
                    val intent = Intent()
                        .putExtra(Intent.EXTRA_TEXT, post.content)
                        .setAction(Intent.ACTION_SEND)
                        .setType("text/plain")
                    try {

                        startActivity(Intent.createChooser(intent, null))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.app_is_not_found),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

        )

        adapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding.list.smoothScrollToPosition(0)
                    }
                }
            }
        )


        viewModel.edited.observe(this) { post ->
            if (post.id != 0L) {
                binding.content.setText(post.content)
                binding.editGroup.visibility = android.view.View.VISIBLE
                binding.editPreviewText.text = post.content
                AndroidUtils.showKeyboard(binding.content)
            } else {
                binding.editGroup.visibility = android.view.View.GONE
                binding.content.setText("")
            }

            binding.saveButton.setOnClickListener {
                with(binding.content) {

                    if (text.isNullOrBlank()) {
                        Toast.makeText(context, R.string.text_is_blank, Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    viewModel.save(text.toString())
                    setText("")
                    clearFocus()
                    binding.editGroup.visibility = android.view.View.GONE
                    AndroidUtils.hideKeyboard(this)
                }
            }

            binding.cancelButton.setOnClickListener {
                with(binding.content) {
                    setText("")
                    clearFocus()
                    binding.editGroup.visibility = android.view.View.GONE
                    AndroidUtils.hideKeyboard(this)
                    viewModel.resetEdited() //Возвращает стандартный шаблон для создания поста
                }
            }
            binding.list.adapter = adapter
            viewModel.data.observe(this) { posts ->
                adapter.submitList(posts)

            }

            val newPostLuncher: ActivityResultLauncher<Unit> =
                registerForActivityResult(NewPostActivityContract()) { result ->
                    if (result != null) {
                        viewModel.save(result)
                    }
                }

            binding.addButton.setOnClickListener { newPostLuncher.launch() }

        }
    }
}
