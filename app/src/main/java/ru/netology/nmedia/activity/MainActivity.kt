package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.functions.counterFormatter
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
        viewModel.data.observe(this) { posts ->
            posts.map { post ->
                CardPostBinding.inflate(layoutInflater, binding.root, true).apply {
                    avatar.setImageResource(R.drawable.ic_netology_48dp)
                    author.text = post.author
                    content.text = post.content
                    published.text = post.published
                    likesCount.text = counterFormatter(post.likes)
                    shareCount.text = counterFormatter(post.shareCount)
                    if (post.likedByMe) {
                        icLikes.setImageResource(R.drawable.ic_liked_24)
                    } else (icLikes.setImageResource((R.drawable.ic_like_24)))
                }


                icLikes.setOnClickListener {
                    viewModel.likeById(post.id)
                }
                icShare.setOnClickListener {
                    viewModel.shareById(post.id)

            }


            }
        }
    }
}