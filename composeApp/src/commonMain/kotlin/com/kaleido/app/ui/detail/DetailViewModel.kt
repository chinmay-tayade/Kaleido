package com.kaleido.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val loading: Boolean = true,
    val product: Product? = null,
    val error: String? = null,
    val related: List<Product> = emptyList(),
)

class DetailViewModel(
    private val productId: Int,
    private val repository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = DetailUiState(loading = true)
        viewModelScope.launch {
            val product = repository.product(productId)
            if (product == null) {
                _state.value = DetailUiState(loading = false, error = "Couldn't load this product.")
                return@launch
            }
            val related = repository.products.value
                .filter { it.category == product.category && it.id != product.id }
                .sortedByDescending { it.dealScore() }
                .take(6)
            _state.value = DetailUiState(loading = false, product = product, related = related)
        }
    }
}
