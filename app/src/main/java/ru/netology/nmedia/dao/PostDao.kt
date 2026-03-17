package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
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
    @Query("SELECT * FROM PostEntity ORDER BY id  DESC")
    fun getAll(): LiveData<List<PostEntity>>

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

    @Query("UPDATE PostEntity SET shareCount = shareCount + 1 WHERE id = :id")
    suspend fun shareById(id: Long)
}