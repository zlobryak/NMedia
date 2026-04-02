package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

//DAO (Data Access Object)
//Расшифровка: Объект доступа к данным (Data Access Object).
//Суть: Это интерфейс или класс, который инкапсулирует всю логику взаимодействия с базой данных.
//Он знает, как делать CRUD-операции (Create, Read, Update, Delete).
//Зачем нужен:
//Разделяет логику работы с БД и остальную часть приложения.
//Если ты решишь сменить SQLite на Room или даже на удаленную базу, тебе нужно будет переписать только DAO,
//а не весь проект.
//В Android (с библиотекой Room) DAO — это обычно интерфейс, где методы аннотируются запросами
//(@Query, @Insert, @Update).

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY published DESC, id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Insert(onConflict = REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insert(posts: List<PostEntity> )

    @Query("UPDATE PostEntity SET content = :text WHERE id = :id")
    fun updateContentById(id: Long, text: String?)

    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content)

    @Query(
        """
           UPDATE PostEntity SET
               likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
               likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
           WHERE id = :id;
    """
    )
    suspend fun likeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity WHERE syncStatus = :status")
    suspend fun removePending(status: PostEntity.SyncStatus)

    @Query("SELECT * FROM PostEntity WHERE isvisible = 1 ORDER BY published DESC, id DESC")
    fun getAllVisible(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM PostEntity WHERE isVisible = 0")
    suspend fun countHiddenPosts(): Int

    @Query("UPDATE PostEntity SET isVisible = 1 WHERE isVisible = 0")
    suspend fun showAllHiddenPosts()
    @Query("SELECT MAX(id) FROM PostEntity")
    suspend fun getMaxId(): Long

    @Query("UPDATE PostEntity SET shareCount = shareCount + 1 WHERE id = :id")
    suspend fun shareById(id: Long)
}