package com.kaleido.app.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleido.app.domain.CatalogEngine
import com.kaleido.app.domain.CatalogQuery
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.DataStatus
import com.kaleido.app.domain.Product
import com.kaleido.app.domain.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val status: DataStatus = DataStatus.Idle,
    val query: CatalogQuery = CatalogQuery(),
    val categories: List<String> = emptyList(),
    val forYou: List<Product> = emptyList(),
    val results: List<Product> = emptyList(),
    val totalCount: Int = 0,
) {
    val isFirstLoad: Boolean get() = status == DataStatus.Loading && results.isEmpty()
    val isEmpty: Boolean get() = status != DataStatus.Loading && results.isEmpty()
    val isOffline: Boolean get() = status == DataStatus.Offline
}

class CatalogViewModel(
    private val repository: CatalogRepository,
) : ViewModel() {

    private val query = MutableStateFlow(CatalogQuery())
    private val pageLimit = MutableStateFlow(INITIAL_LIMIT)

    val state: StateFlow<CatalogUiState> =
        combine(repository.products, repository.status, query) { products, status, q ->
            CatalogUiState(
                status = status,
                query = q,
                categories = CatalogEngine.categories(products),
                forYou = CatalogEngine.forYou(products),
                results = CatalogEngine.apply(products, q),
                totalCount = products.size,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init {
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        repository.refresh(pageLimit.value)
    }

    fun loadMore() {
        if (pageLimit.value >= MAX_LIMIT) return
        pageLimit.update { (it + PAGE_STEP).coerceAtMost(MAX_LIMIT) }
        refresh()
    }

    fun onSearch(text: String) = query.update { it.copy(search = text) }

    fun onCategory(category: String?) = query.update {
        it.copy(category = if (it.category == category) null else category)
    }

    fun onSort(sort: SortOption) = query.update { it.copy(sort = sort) }

    private companion object {
        const val INITIAL_LIMIT = 10
        const val PAGE_STEP = 10
        const val MAX_LIMIT = 20 // Fake Store API only has 20 products
    }
}
