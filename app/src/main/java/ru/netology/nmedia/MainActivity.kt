package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.functions.counterFormatter

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val post = Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
            published = "21 мая в 18:36",
            shareCount = 1099,
            likes = 1_123_345,
            likedByMe = false
        )

        with(binding) {
            author.text = post.author
            content.text = post.content
            published.text = post.published
            likesCount.text = counterFormatter(post.likes)
            shareCount.text = counterFormatter(post.shareCount)
            if (post.likedByMe) {
                icLikes.setImageResource(R.drawable.ic_liked_24)
            }
            icLikes.setOnClickListener {
                post.likedByMe = !post.likedByMe
                icLikes.setImageResource(
                    if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_like_24
                )
                if (post.likedByMe) post.likes++ else post.likes--
                likesCount.text = counterFormatter(post.likes)
            }
            icShare.setOnClickListener {
                post.shareCount++
                shareCount.text = counterFormatter(post.shareCount)
            }
        }
    }
}