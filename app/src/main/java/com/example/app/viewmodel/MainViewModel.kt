package com.example.app.viewmodel

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.domain.repository.Repository
import com.example.app.network.models.ProductDto
import com.example.app.ui.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.http.Query
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _productState = MutableStateFlow<UiState<List<ProductDto>>>(UiState.Loading)
    val productState : StateFlow<UiState<List<ProductDto>>> = _productState.asStateFlow()

    private val _productDetailState = MutableStateFlow<UiState<ProductDto>>(UiState.Empty)
    val prductDetailState : StateFlow<UiState<ProductDto>> = _productDetailState.asStateFlow()

    private  val _searchQuery = MutableStateFlow("")
    val searchQuery : StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory : StateFlow<String> = _selectedCategory.asStateFlow()


    private val _sortOption = MutableStateFlow(SortOption.NONE)
    val sortOption : StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _allProducts = MutableStateFlow<List<ProductDto>>(emptyList())

    private val _currentLimit = MutableStateFlow(10)
    val currentLimit : StateFlow<Int>  = _currentLimit.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore : StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    init {

        loadProducts(true)
    }
    fun loadProducts(isInitialized : Boolean = false){

        viewModelScope.launch {

            if(isInitialized){
                _productState.value = UiState.Loading
            }else{
                _isLoadingMore.value = true
            }

            try {

                val products = repository.getProducts(_currentLimit.value).distinctBy { it.id }

                _allProducts.value = products
                //emitFilteredProduct
            }catch (e : Exception){

                //map Error later with mapError function mapError(e)
               val message = "Api Loading Failed"

                if(_allProducts.value.isEmpty()){
                    _productState.value = UiState.Error(message)
                }
            }finally {

                _isLoadingMore.value = false
            }
        }

    }

    fun loadNextPage(){

        if(_isLoadingMore.value) return
        val currentCount = _allProducts.value.size
        if(currentCount >= 20) return
        _currentLimit.value = (_currentLimit.value + 5).coerceAtMost(20)
        loadProducts(false)

    }

    fun loadProdctdetails(id : Int){

        viewModelScope.launch {

            _productDetailState.value = UiState.Loading

            try {
             val product = repository.getProduct(id)
                _productDetailState.value = UiState.Success(product)
            }catch (e: Exception){
                _productDetailState.value = UiState.Error("Api Loading Failed")
                //map Error later with mapError function mapError(e)
            }
        }
    }

    fun updateSearchQuery(query: String){
        _searchQuery.value = query
        //emitFilteredProduct
    }

    fun updateCategory(category: String){
        _selectedCategory.value = category
        //emitFilteredProduct

    }

    fun updateSortOption(option: SortOption){
        _sortOption.value = option
        //emitFilteredProduct
    }

    fun retryProducts(){
        loadProducts(isInitialized = true)
    }

    fun avialableCategories() : List<String> {

        val categories = _allProducts.value.map { it.category }.distinct().sorted()

        return listOf("All") + categories
    }

    private fun emitFilteredProducts(){

        val query = _searchQuery.value.trim()

        val category = _selectedCategory.value
        val sort = _sortOption.value


        var filtered = _allProducts.value.filter { product ->

            val matchesQuery = query.isBlank() ||
                    product.title.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)

            val matchesCategory = category == "All" || product.category == category
            matchesQuery && matchesCategory
        }
            filtered = when(sort){
                SortOption.NONE -> filtered
                SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
                SortOption.PRICE_HIGH_TO_LOW ->filtered.sortedByDescending { it.price }
                SortOption.TITLE_A_TO_Z -> filtered.sortedBy { it.title.lowercase() }
                SortOption.TITLE_Z_TO_A -> filtered.sortedByDescending { it.title.lowercase() }
            }

        _productState.value = if(filtered.isEmpty()){

            UiState.Empty
        }else {
            UiState.Success(filtered)
        }


        }

}