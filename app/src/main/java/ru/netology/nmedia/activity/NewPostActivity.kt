package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewPostBinding
import ru.netology.nmedia.dto.Post

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Получаем пост для редактирования (может быть null — тогда новый)
        val editingPost = intent.getParcelableExtra<Post>("post", Post::class.java)

        if (editingPost != null) {
            binding.content.setText(editingPost.content)
            supportActionBar?.title = getString(R.string.edit_mode)
        } else {
            supportActionBar?.title = getString(R.string.new_post)
        }

        binding.saveButton.setOnClickListener {
            val text = binding.content.text?.toString()?.trim()
            if (text.isNullOrBlank()) {
                setResult(RESULT_CANCELED)
            } else {
                val resultPost = editingPost?.copy(content = text) ?: Post(
                    id = 0L, // временный ID для новых
                    author = "Me", // или брать из профиля
                    content = text,
                    published = "Just now"
                )
                setResult(RESULT_OK, Intent().putExtra("result_post", resultPost))
                finish()
            }
        }
    }
}