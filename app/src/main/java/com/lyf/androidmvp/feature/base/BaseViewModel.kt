package com.lyf.androidmvp.feature.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.lyf.androidmvp.core.common.UiMessage

interface UiState {
    val messages: List<UiMessage>
}

interface UiEvent

abstract class BaseViewModel<S : UiState, E : UiEvent>(
    initialState: S,
    private val withMessages: (S, List<UiMessage>) -> S
) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    abstract fun onEvent(event: E)

    protected fun updateState(reducer: (S) -> S) {
        _uiState.update(reducer)
    }

    protected fun showMessage(text: String) {
        val message = UiMessage(text)
        updateState { withMessages(it, it.messages + message) }
    }

    fun dismissMessage(id: String) {
        updateState { withMessages(it, it.messages.filterNot { message -> message.id == id }) }
    }

    protected fun launchAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showMessage(exception.localizedMessage ?: "操作失败，请重试")
            }
        }
    }
}
