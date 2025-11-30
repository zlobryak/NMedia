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

// Тип-псевдоним для функции-слушателя, которая принимает Post и ничего не возвращает
typealias OnClickListener = (Post) -> Unit

/**
 * Адаптер для отображения списка постов в RecyclerView
 * Использует ListAdapter для эффективного обновления списка с помощью DiffUtil
 * @param onLikeListener слушатель для обработки нажатий на "лайк"
 * @param onShareListener слушатель для обработки нажатий на "поделиться"
 * @param onRemoveListener слушатель для обработки нажатий на "удалить"
 */
class PostsAdapter(
    private val onLikeListener: OnClickListener,
    private val onShareListener: OnClickListener,
    private val onRemoveListener: OnClickListener

) :
    ListAdapter<Post, PostViewHolder>(PostDiffUtils) {

    /**
     * Создает новый ViewHolder для элемента списка
     * @param parent родительский ViewGroup
     * @param viewType тип представления (не используется в данном случае)
     * @return новый экземпляр PostViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        // Создание привязки (binding) для макета карточки поста
        val view = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(
            view, onLikeListener, onShareListener, onRemoveListener
        )
    }

    /**
     * Привязывает данные поста к ViewHolder
     * @param holder ViewHolder, который будет связан с данными
     * @param position позиция элемента в списке
     */
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // Получение элемента по позиции и передача его в метод bind ViewHolder
        holder.bind(getItem(position))
    }
}

/**
 * ViewHolder для отдельного элемента списка (поста)
 * Отвечает за привязку данных к представлению и обработку взаимодействий с пользователем
 * @param binding привязка к макету карточки поста
 * @param onLikeListener слушатель для обработки нажатий на "лайк"
 * @param onShareListener слушатель для обработки нажатий на "поделиться"
 * @param onRemoveListener слушатель для обработки нажатий на "удалить"
 */
class PostViewHolder(
    private val binding: CardPostBinding,
    private val onLikeListener: OnClickListener,
    private val onShareListener: OnClickListener,
    private val onRemoveListener: OnClickListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Привязывает данные поста к элементам интерфейса
     * Устанавливает текст, изображения и обработчики кликов
     * @param post пост, данные которого нужно отобразить
     */
    fun bind(post: Post) {
        binding.apply {
            // Установка изображения аватара (в данном случае, используется стандартное изображение)
            avatar.setImageResource(R.drawable.ic_netology_48dp)
            // Установка имени автора поста
            author.text = post.author
            // Установка содержимого поста
            content.text = post.content
            // Установка даты публикации
            published.text = post.published
            // Установка количества лайков с форматированием (например, "1K" вместо "1000")
            likesCount.text = counterFormatter(post.likes)
            // Установка количества шеров с форматированием
            shareCount.text = counterFormatter(post.shareCount)
            // Проверка, лайкнут ли пост текущим пользователем, и установка соответствующего изображения
            if (post.likedByMe) {
                // Если пост лайкнут, устанавливается изображение лайкнутого сердечка
                icLikes.setImageResource(R.drawable.ic_liked_24)
            } else (
                    // Если пост не лайкнут, устанавливается изображение обычного сердечка
                    icLikes.setImageResource((R.drawable.ic_like_24)))
            // Установка слушателя клика на иконку лайка
            icLikes.setOnClickListener {
                // Вызов переданного слушателя с текущим постом
                onLikeListener(post)
            }
            // Установка слушателя клика на иконку шеринга
            icShare.setOnClickListener {
                // Вызов переданного слушателя с текущим постом
                onShareListener(post)
            }
            // Установка слушателя клика на кнопку меню (три точки)
            menuButton.setOnClickListener {
                // Создание и настройка всплывающего меню
                PopupMenu(it.context, it).apply {
                    // Загрузка меню из ресурсов
                    inflate(R.menu.post_menu)
                    // Установка слушателя клика по элементам меню
                    setOnMenuItemClickListener { menuItem ->
                        // Обработка нажатия на элементы меню
                        when (menuItem.itemId) {
                            // Если нажата кнопка "удалить"
                            R.id.remove -> {
                                // Вызов слушателя удаления с текущим постом
                                onRemoveListener(post)
                                // Возврат true, чтобы указать, что клик обработан
                                true
                            }

                            // Для всех остальных элементов меню
                            else -> false
                        }
                    }
                    // Отображение всплывающего меню
                    show()
                }
            }
        }
    }
}

/**
 * Объект для сравнения элементов списка с помощью DiffUtil
 * Необходим для эффективного обновления списка в RecyclerView
 */
object PostDiffUtils : DiffUtil.ItemCallback<Post>() {
    /**
     * Проверяет, представляют ли два элемента один и тот же объект
     * Сравнивает по id, так как это уникальный идентификатор поста
     * @param oldItem старый элемент
     * @param newItem новый элемент
     * @return true, если элементы представляют один и тот же объект
     */
    override fun areItemsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Проверяет, содержат ли два элемента одинаковые данные
     * Сравнивает по всем полям объекта Post
     * @param oldItem старый элемент
     * @param newItem новый элемент
     * @return true, если содержимое элементов одинаково
     */
    override fun areContentsTheSame(
        oldItem: Post,
        newItem: Post
    ): Boolean {
        return oldItem == newItem
    }
}