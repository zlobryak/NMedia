package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.functions.counterFormatter

typealias OnClickListener = (Post) -> Unit


class PostsAdapter(private val onLikeListener: OnClickListener, private val onShareListener: OnClickListener) :
    RecyclerView.Adapter<PostViewHolder>() {
    var list = emptyList<Post>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            view, onLikeListener, onShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnClickListener,
    private val onShareListener: OnClickListener
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
        }
    }
}
