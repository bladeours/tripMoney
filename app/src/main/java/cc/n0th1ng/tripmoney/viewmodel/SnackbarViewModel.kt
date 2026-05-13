package cc.n0th1ng.tripmoney.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SnackbarViewModel @Inject constructor(
    val snackbarManager: SnackbarManager
) : ViewModel() {
    fun showMessage(message: String) {
        viewModelScope.launch {
            snackbarManager.showMessage(message)
        }
    }
}