package com.example.thingsfortrip.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thingsfortrip.model.UiEvent
import com.example.thingsfortrip.model.UiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<Event: UiEvent, State: UiState> : ViewModel() {


    private val initialState: State by lazy (::createInitialState)
    abstract fun createInitialState(): State

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    val currentState: State
        get() = uiState.value

    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()



    init {
        subscribeEvents()
    }

    private fun subscribeEvents() {
        viewModelScope.launch {
            event.collect{
                handleEvents(it)
            }
        }
    }

    abstract fun handleEvents(it: Event)

    fun setEvent(event: Event){
        viewModelScope.launch {
            _event.emit(event)
        }
    }

    fun setState(state: State){
        viewModelScope.launch{
            _uiState.value = state
        }
    }

}