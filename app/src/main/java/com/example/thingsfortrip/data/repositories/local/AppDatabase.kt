package com.example.thingsfortrip.data.repositories.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thingsfortrip.data.models.ListData
import com.example.thingsfortrip.data.models.Thing
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Thing::class, ListData::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun createDAO(): AppDAO



    companion object {

        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getDatabase(context: Context):AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "things_db"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
