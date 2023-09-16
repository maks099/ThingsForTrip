package com.example.thingsfortrip.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewModelScope
import com.example.thingsfortrip.R
import com.example.thingsfortrip.data.models.Thing
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.example.thingsfortrip.model.ThingsListContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ThingsListViewModel : BaseViewModel<ThingsListContract.Event, ThingsListContract.State>() {

    val newThingName = mutableStateOf("")
    lateinit var appRepository: AppRepository
    var listCode = -1
    var things = mutableListOf<Thing>()

    override fun createInitialState(): ThingsListContract.State {
        return ThingsListContract.State.Init
    }

    override fun handleEvents(it: ThingsListContract.Event) {
        when(it){
            is ThingsListContract.Event.LoadThingsByListCode -> {
                listCode = it.listCode
                viewModelScope.launch(Dispatchers.IO){
                    appRepository.getThingsList(it.listCode).collect{

                        things = it
                        Log.d("ThingsListVM", things.toString())
                    }
                }
            }

            is ThingsListContract.Event.ChangeThingStatus -> {
                viewModelScope.launch {
                    appRepository.changeThingStatus(!it.thing.isChecked, it.thing.index)
                }
            }

            ThingsListContract.Event.SaveNewThing -> {
                val newThing = Thing(name = newThingName.value, isChecked = true, listCode = listCode)
                val uniqueCheck = things.filter { thing -> thing.name == newThing.name }
                if(uniqueCheck.isNotEmpty()){
                    setState(ThingsListContract.State.ShowToast(R.string.thing_name_must_be_unique))
                } else{
                    viewModelScope.launch(Dispatchers.IO) {
                        newThingName.value = ""
                        appRepository.insertThing(newThing)
                    }
                }

            }

            is ThingsListContract.Event.UpdateNewThingName -> newThingName.value = it.name
            is ThingsListContract.Event.DeleteThing -> {
                viewModelScope.launch(Dispatchers.IO) {
                    appRepository.deleteThing(it.id)
                }
            }
        }
    }
}