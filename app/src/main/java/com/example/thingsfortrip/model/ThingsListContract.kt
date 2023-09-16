package com.example.thingsfortrip.model

import com.example.thingsfortrip.data.models.Thing

class ThingsListContract {

    sealed class State : UiState{
        object Init : State()
        class ShowToast(val stringResource: Int): State()
    }

    sealed class Event : UiEvent{
        object SaveNewThing: Event()
        class LoadThingsByListCode(val listCode: Int) : Event()
        class ChangeThingStatus(val thing: Thing): Event()
        class UpdateNewThingName(val name: String): Event()
        class DeleteThing(val id: Int): Event()
    }
}