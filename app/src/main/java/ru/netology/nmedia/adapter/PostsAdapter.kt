package ru.netology.nmedia.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.functions.counterFormatter

/**
 * Интерфейс обратного вызова для обработки пользовательских действий с постами.
 * Используется для делегирования логики (редактирование, удаление, лайк и т.д.)
 * из адаптера в фрагмент или ViewModel.
 */
interface PostListener {
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onOpenVideo(url: String)
}

/**
 * Адаптер списка постов для RecyclerView.
 * Использует ListAdapter с DiffUtil.ItemCallback для эффективного обновления
 * при изменении данных. Все действия пользователя передаются через PostListener.
 *
 * @param listener экземпляр, реализующий обработку действий с постами
 */
class PostsAdapter(
    private val listener: PostListener
) :
    ListAdapter<Post, PostViewHolder>(PostDiffUtils) {

    /**
     * Создаёт новый ViewHolder, инфлатя макет карточки поста через View Binding.
     *
     * @param parent родительское ViewGroup, к которому будет добавлен элемент
     * @param viewType тип представления (не используется, так как все элементы одинаковы)
     * @return новый экземпляр PostViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Инфляция макета карточки с использованием View Binding
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(view, listener)
    }

    /**
     * Привязывает данные поста к соответствующему ViewHolder'у.
     * Вызывается при отображении или обновлении элемента списка.
     *
     * @param holder ViewHolder, которому передаются данные
     * @param position позиция элемента в списке
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * ViewHolder для отображения отдельного поста.
 * Отвечает за привязку данных к UI и обработку кликов.
 *
 * @param binding View Binding для макета карточки поста
 * @param listener слушатель действий пользователя
 */
class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: PostListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Привязывает данные поста к UI-элементам карточки.
     * Устанавливает текст, аватар, счетчики, обработчики кликов и состояние видео-превью.
     *
     * @param post объект поста, данные которого отображаются в карточке
     */
    fun bind(post: Post) {
        binding.apply {
            // Устанавливаем фиксированный аватар автора
            avatar.setImageResource(R.drawable.ic_netology_48dp)
            // Имя автора поста
            author.text = post.author
            // Основной текст поста
            content.text = post.content
            // Время публикации
            published.text = post.published
            // Форматируем и отображаем количество репостов
            icShare.text = counterFormatter(post.shareCount)
            // Состояние кнопки "лайк": отмечена, если пользователь уже поставил лайк
            icLikes.isChecked = post.likedByMe
            // Форматируем и отображаем общее количество лайков
            icLikes.text = counterFormatter(post.likes)
            // Обработка нажатия на кнопку "лайк"
            icLikes.setOnClickListener { listener.onLike(post) }

            // Обработка нажатия на кнопку "поделиться"
            icShare.setOnClickListener { listener.onShare(post) }

            // Открытие контекстного меню по нажатию на кнопку "три точки"
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_menu)
                    setOnMenuItemClickListener { menuItem ->
                        when (menuItem.itemId) {
                            R.id.remove -> {
                                listener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                listener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                    show()
                }
            }

            // Обработка клика по основному контенту поста — переход на отдельный экран поста
            content.setOnClickListener {
                findNavController(itemView).navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        putParcelable("postArg", post)
                    }
                )
            }

            // Отображение количества просмотров (форматированного)
            icViews.text = counterFormatter(post.views)

            // Управление видимостью блока видео-превью
            if (!post.videoUrl.isNullOrBlank()) {
                // Показываем блок с видео-превью
                editGroup.visibility = View.VISIBLE
                // Устанавливаем заглушку изображения
                videoPreview.setImageResource(R.drawable.preview_image)
                // Заголовок превью видео
                videoPreviewText.text = post.videoPreviewText
                // Количество просмотров видео (сырое значение, без форматирования)
                post.videoViewsCount?.let { videoPreviewCount.text = counterFormatter(it) }
            } else {
                // Скрываем блок, если видео нет
                editGroup.visibility = View.GONE
            }

            // Обработка клика по кнопке "Play" — открытие видео через слушатель
            playArrow.setOnClickListener {
                post.videoUrl?.let(listener::onOpenVideo)
            }

            // Обработка клика по превью видео — также открывает видео
            videoPreview.setOnClickListener {
                post.videoUrl?.let(listener::onOpenVideo)
            }
        }
    }
}

/**
 * Реализация DiffUtil.ItemCallback для оптимизации обновления списка постов.
 * Сравнивает элементы по ID и содержимому для минимизации перерисовок.
 */
object PostDiffUtils : DiffUtil.ItemCallback<Post>() {

    /**
     * Проверяет, относятся ли два элемента к одному и тому же посту.
     * Сравнение по уникальному идентификатору.
     *
     * @param oldItem предыдущая версия поста
     * @param newItem новая версия поста
     * @return true, если ID совпадают
     */
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Проверяет, изменилось ли содержимое поста.
     * Предполагается, что в классе Post переопределён метод equals(),
     * сравнивающий все релевантные поля.
     *
     * @param oldItem предыдущая версия поста
     * @param newItem новая версия поста
     * @return true, если содержимое идентично
     */
    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}