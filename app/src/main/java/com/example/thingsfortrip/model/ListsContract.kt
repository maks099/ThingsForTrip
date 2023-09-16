package com.example.thingsfortrip.model

import com.example.thingsfortrip.data.AutocompleteResult
import com.example.thingsfortrip.data.models.Category
import com.example.thingsfortrip.data.models.Thing

class ListsContract {

    sealed class Event: UiEvent{
        object LoadLists : Event()
        class OnSaveNewList(val time: Long) : Event()
        class OnDeleteList(val listCode: Int) : Event()
        class OnNewListNameInput(val listName: String): Event()
    }

    sealed class State : UiState {
        object Initial : State()
        class ShowToast(val stringResource: Int): State()
    }
}