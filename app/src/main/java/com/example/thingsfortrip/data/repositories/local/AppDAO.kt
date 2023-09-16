package com.example.thingsfortrip.data.repositories.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thingsfortrip.data.models.ListData
import com.example.thingsfortrip.data.models.Thing
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDAO {

    @Query("SELECT * FROM listdata ORDER by time ASC")
    fun getListDataArray(): Flow<MutableList<ListData>>

    @Query("SELECT * FROM thing WHERE listCode = :listCode")
    fun getThingsByList(listCode: Int): Flow<MutableList<Thing>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListData(listData: ListData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThingsArray(things: List<Thing>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThing(thing: Thing)

    @Query("SELECT * FROM listdata WHERE name=:listDataName")
    suspend fun getListDataByName(listDataName: String): ListData

    @Query("DELETE FROM listdata WHERE id=:id")
    suspend fun deleteList(id: Int)

    @Query("DELETE FROM thing WHERE listCode=:listCode")
    suspend fun deleteThingsByList(listCode: Int)

    @Query("UPDATE thing SET isChecked=:isChecked WHERE `index`=:index")
    suspend fun changeThingStatus(isChecked: Boolean, index: Int)

    @Query("DELETE FROM thing WHERE `index`=:index")
    suspend fun deleteThing(index: Int)

}