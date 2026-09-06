package com.kaleido.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleido.app.domain.CatalogOverview
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.CategoryInsight
import com.kaleido.app.domain.InsightsEngine
import com.kaleido.app.domain.RatingBucket
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn


data class InsightsUiState(
    val overview: CatalogOverview = CatalogOverview(0, 0, 0.0, 0.0, null, null),
    val categories: List<CategoryInsight> = emptyList(),
    val ratingHistogram: List<RatingBucket> = emptyList(),
    val ready: Boolean = false,
)

class InsightsViewModel(
    repository: CatalogRepository,
) : ViewModel() {

    val state: StateFlow<InsightsUiState> =
        repository.products.map { products ->
            InsightsUiState(
                overview = InsightsEngine.overview(products),
                categories = InsightsEngine.byCategory(products),
                ratingHistogram = InsightsEngine.ratingHistogram(products),
                ready = products.isNotEmpty(),
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())
}
