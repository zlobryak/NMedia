package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.functions.counterFormatter

/**
 * Интерфейс для обработки пользовательских действий с постами.
 * Реализуется, например, во ViewModel или Activity, чтобы реагировать на клики.
 */
interface PostListener {
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onOpenVideo(url: String)
}

/**
 * Адаптер для отображения списка постов в RecyclerView.
 * Использует ListAdapter и DiffUtil для эффективного обновления данных.
 * Все взаимодействия с пользователем делегируются через единый интерфейс PostListener.
 *
 * @param listener обработчик действий пользователя (лайк, шер, удаление, редактирование, запуск видео)
 */
class PostsAdapter(
    private val listener: PostListener
) :
    ListAdapter<Post, PostViewHolder>(PostDiffUtils) {

    /**
     * Создаёт новый ViewHolder для элемента списка.
     * Использует View Binding для инфляции макета карточки поста.
     *
     * @param parent родительский ViewGroup, к которому будет присоединён элемент
     * @param viewType тип элемента (в данном случае не используется, так как все элементы одинаковые)
     * @return экземпляр PostViewHolder, готовый к привязке данных
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Создание привязки (binding) для макета карточки поста
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            view, listener
        )
    }

    /**
     * Связывает данные конкретного поста с его ViewHolder'ом.
     * Вызывается каждый раз, когда элемент списка появляется или обновляется.
     *
     * @param holder ViewHolder, который будет обновлён
     * @param position позиция элемента в текущем списке
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // Получение элемента по позиции и передача его в метод bind ViewHolder
        holder.bind(getItem(position))
    }
}

/**
 * ViewHolder для отдельного элемента списка (поста).
 * Отвечает за привязку данных к UI-элементам и установку обработчиков кликов.
 *
 * @param binding привязка к макету карточки поста (CardPostBinding)
 * @param listener интерфейс для передачи действий пользователя наверх
 */
class PostViewHolder(
    private val binding: CardPostBinding,
    private val listener: PostListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Привязывает данные поста к элементам интерфейса.
     * Устанавливает текст, изображения, форматирует счётчики и назначает обработчики кликов.
     *
     * @param post данные поста, которые нужно отобразить
     */
    fun bind(post: Post) {
        binding.apply {
            // Установка аватара автора (в данном случае — фиксированная иконка)
            avatar.setImageResource(R.drawable.ic_netology_48dp)
            // Имя автора поста
            author.text = post.author
            // Текст поста
            content.text = post.content
            // Время публикации
            published.text = post.published
            // Форматированное количество репостов
            icShare.text = counterFormatter(post.shareCount)
            // Установка иконки лайка в зависимости от состояния likedByMe
            icLikes.isChecked = post.likedByMe
            // Форматированное количество лайков (например, "1K", "2.5M")
            icLikes.text = counterFormatter(post.likes)
            // Обработка нажатия на иконку "лайк"
            icLikes.setOnClickListener {
                listener.onLike(post)
            }
            // Обработка нажатия на иконку "поделиться"
            icShare.setOnClickListener {
                listener.onShare(post)
            }
//             Обработка нажатия на кнопку меню (три точки) для открытия меню поста
            menuButton.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    // Загрузка меню из ресурсов (post_menu.xml)
                    inflate(R.menu.post_menu)
                    // Обработка выбора пунктов меню
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

            icViews.text = counterFormatter(post.views)
            // Обработка видео-превью
            if (!post.videoUrl.isNullOrBlank()) {
                // Показываем группу с превью, если есть ссылка на видео
                editGroup.visibility = View.VISIBLE
                //Ставим заглушку на картинку
                videoPreview.setImageResource(R.drawable.preview_image)
                //Подставляем заголовок
                videoPreviewText.text = post.videoPreviewText
                //Подставляем количество просмотров
                videoPreviewCount.text = post.videoViewsCount
            } else {
                editGroup.visibility = View.GONE
            }
            playArrow.setOnClickListener {
                post.videoUrl?.let { url ->
                    listener.onOpenVideo(url)
                }
//                val intent = Intent(Intent.ACTION_VIEW, post.videoUrl?.toUri())
//                itemView.context.startActivity(intent)
            }
            videoPreview.setOnClickListener {
//                val intent = Intent(Intent.ACTION_VIEW, post.videoUrl?.toUri())
//                itemView.context.startActivity(intent)
                post.videoUrl?.let { url ->
                    listener.onOpenVideo(url)
                }
            }
//            content.setOnClickListener {
//                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
//TODO Нажатие на текст должно открывать фрагмент с постом
//            }
        }
    }
}

/**
 * Объект, реализующий логику сравнения постов для DiffUtil.
 * Используется ListAdapter для определения, какие элементы изменились,
 * и минимизации количества анимаций и перерисовок.
 */
object PostDiffUtils : DiffUtil.ItemCallback<Post>() {
    /**
     * Определяет, представляют ли два объекта один и тот же элемент списка.
     * Сравнение происходит по уникальному идентификатору — id.
     *
     * @param oldItem предыдущая версия поста
     * @param newItem новая версия поста
     * @return true, если это один и тот же пост (по id)
     */
    override fun areItemsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Определяет, изменилось ли содержимое поста.
     * Использует полное сравнение объектов (через переопределённый equals в Post).
     *
     * @param oldItem предыдущая версия поста
     * @param newItem новая версия поста
     * @return true, если содержимое не изменилось
     */
    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }
}