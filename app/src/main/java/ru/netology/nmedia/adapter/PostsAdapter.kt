package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.functions.counterFormatter

typealias OnClickListener = (Post) -> Unit

class PostsAdapter(
    private val onLikeListener: OnClickListener,
    private val onShareListener: OnClickListener,
    private val onRemoveListener: OnClickListener

) :
    ListAdapter<Post, PostViewHolder>(PostDiffUtils) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            view, onLikeListener, onShareListener, onRemoveListener
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnClickListener,
    private val onShareListener: OnClickListener,
    private val onRemoveListener: OnClickListener
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            avatar.setImageResource(R.drawable.ic_netology_48dp)
            author.text = post.author
            content.text = post.content
            published.text = post.published
            likesCount.text = counterFormatter(post.likes)
            shareCount.text = counterFormatter(post.shareCount)
            if (post.likedByMe) {
                icLikes.setImageResource(R.drawable.ic_liked_24)
            } else (icLikes.setImageResource((R.drawable.ic_like_24)))
            icLikes.setOnClickListener {
                onLikeListener(post)
            }
            icShare.setOnClickListener {
                onShareListener(post)
            }
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                onRemoveListener(post)
                                true
                            }

                            else -> false
                        }
                    }
                    show()
                }
            }
        }
    }
}

object PostDiffUtils : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }
}
