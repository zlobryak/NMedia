package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityAppBinding
import kotlin.apply

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityAppBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val action = intent.action

        if (action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)


            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.text_is_blank_error,
                    Snackbar.LENGTH_SHORT
                ).setAction(android.R.string.ok) {
                    finish()
                }.show()
            } else {
                binding.root.post {
                    findNavController(R.id.nav_controller).navigate(
                        R.id.action_feedFragment_to_newPostFragment,
                        Bundle().apply { putString("textArg", text) }
                    )
                }
            }

        }
    }
}