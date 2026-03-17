package ru.netology.nmedia.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.utils.Converters

//DB (Database)
//Расшифровка: База данных (Database).
//Суть: Это само хранилище данных. В мобильной разработке это чаще всего локальная база данных
//на устройстве (SQLite, Room, Realm) или удаленный сервер (PostgreSQL, Firebase Firestore).
//Роль: Физическое или логическое место, где данные хранятся постоянно.
//DAO является "дверью" в эту базу.

@Database(entities = [PostEntity::class], version = 1, exportSchema = false )
@TypeConverters(Converters::class)

abstract class AppDb : RoomDatabase() {
    abstract val postDao: PostDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .build()

    }
}