package com.example.fithub.ui.screens.food.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fithub.core.Resource
import com.example.fithub.core.ServiceLocator
import com.example.fithub.domain.model.Food
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BarcodeScanUiState(
    val isLookingUp: Boolean = false,
    val resolvedFood: Food? = null,
    val errorMessage: String? = null
)

class BarcodeScannerViewModel : ViewModel() {

    private val foodRepo = ServiceLocator.foodRepository

    private val _uiState = MutableStateFlow(BarcodeScanUiState())
    val uiState: StateFlow<BarcodeScanUiState> = _uiState.asStateFlow()

    fun lookup(barcode: String) {
        if (_uiState.value.isLookingUp || _uiState.value.resolvedFood != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLookingUp = true, errorMessage = null) }
            when (val result = foodRepo.getByBarcode(barcode)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isLookingUp = false, resolvedFood = result.data)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isLookingUp = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeError() = _uiState.update { it.copy(errorMessage = null) }
}