package com.example.thingsfortrip.data.repositories.local

import com.example.thingsfortrip.data.models.ListData
import com.example.thingsfortrip.data.models.Thing
import kotlinx.coroutines.flow.Flow

class AppRepository(private val dao: AppDAO) {

    fun  getListNames(): Flow<MutableList<ListData>> = dao.getListDataArray()
    fun getThingsList(listCode: Int): Flow<MutableList<Thing>> = dao.getThingsByList(listCode)

    suspend fun insertListData(listData: ListData){
        dao.insertListData(listData)
    }

    suspend fun insertThings(things: List<Thing>) = dao.insertThingsArray(things)

    suspend fun getListDataByName(listDataName: String): ListData{
        return dao.getListDataByName(listDataName)
    }

    suspend fun deleteList(listCode: Int){
        dao.deleteList(listCode)
        dao.deleteThingsByList(listCode)
    }

    suspend fun changeThingStatus(isChecked: Boolean, index: Int) = dao.changeThingStatus(isChecked, index)

    suspend fun insertThing(thing: Thing) = dao.insertThing(thing)
    suspend fun deleteThing(id: Int) = dao.deleteThing(id)


}