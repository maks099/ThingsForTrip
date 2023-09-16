package com.example.thingsfortrip.model

import com.example.thingsfortrip.data.AutocompleteResult
import com.example.thingsfortrip.data.models.Category
import com.example.thingsfortrip.data.models.Thing

class MainContract {

    sealed class Event: UiEvent{
        class OnLocationSelect(val result: AutocompleteResult): Event()
        class OnDateChange(val date: Long): Event()
        class SetOnCategory(val category: Category): Event()
        class ChangeThingStatus(val thing: Thing): Event()
        class OnLocationInputChanged(val location: String): Event()
        object OnSaveList: Event()
        object CheckSavedLists: Event()
    }

    sealed class State : UiState {
        object Initial : State()
        object ShowSavedListButton: State()
        class Error(val message: String): State()
        class OnThingsListWasSaved(val listId: Int) : State()
    }
}