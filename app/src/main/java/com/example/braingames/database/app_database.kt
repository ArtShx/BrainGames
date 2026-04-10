package com.example.braingames.database

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.compose.ui.platform.LocalContext
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.braingames.core.GameType
import com.example.braingames.database.converter.Converters
import com.example.braingames.database.dao.GameMetadataDao
import com.example.braingames.database.dao.HighScoreDao
import com.example.braingames.database.entity.GameMetadata
import com.example.braingames.database.entity.HighScore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [HighScore::class, GameMetadata::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun highScoreDao(): HighScoreDao
    abstract fun gameMetadataDao(): GameMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_database"
                )
                    .addCallback(AppDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    class AppDatabaseCallback(
        private val context: Context
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateDatabase(getDatabase(context))
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            var gameDao = db.gameMetadataDao()

            gameDao.insert(
                GameMetadata(
                    gameId = GameType.Memory,
                    gameName = GameType.Memory.toString(),
                    description = null
                )
            )
        }
    }
}