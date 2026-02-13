package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post

class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val empty: Boolean = false
)