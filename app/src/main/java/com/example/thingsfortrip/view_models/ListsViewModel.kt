package com.example.thingsfortrip.view_models

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.thingsfortrip.R
import com.example.thingsfortrip.data.models.ListData
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.example.thingsfortrip.model.ListsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ListsViewModel : BaseViewModel<ListsContract.Event, ListsContract.State>() {

    val newListName = mutableStateOf("")
    lateinit var appRepository: AppRepository
    var lists = mutableListOf<ListData>()

    override fun createInitialState(): ListsContract.State {
        return ListsContract.State.Initial
    }

    override fun handleEvents(it: ListsContract.Event) {
        when(it){
            is ListsContract.Event.LoadLists -> {
                viewModelScope.launch(Dispatchers.IO) {
                    appRepository.getListNames().collect{
                        lists = it
                    }
                }
            }

            is ListsContract.Event.OnDeleteList -> {
                viewModelScope.launch {
                    Log.d(this::javaClass.name, "on delete list ${it.listCode}")
                    appRepository.deleteList(it.listCode)
                }
            }

            is ListsContract.Event.OnNewListNameInput -> newListName.value = it.listName
            is ListsContract.Event.OnSaveNewList -> {
                val unique = lists.filter { lt -> lt.name.equals(newListName) }
                if(unique.isEmpty()) {
                    val listData = ListData(name = newListName.value, time = it.time)
                    viewModelScope.launch(Dispatchers.IO) {
                        newListName.value = ""
                        appRepository.insertListData(listData)
                    }
                } else{
                    setState(ListsContract.State.ShowToast(R.string.list_must_be_unique))
                }

            }
        }
    }
}