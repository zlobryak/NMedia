package ru.netology.nmedia.model

import ru.netology.nmedia.dto.Post
import kotlin.collections.List

class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    )

class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)