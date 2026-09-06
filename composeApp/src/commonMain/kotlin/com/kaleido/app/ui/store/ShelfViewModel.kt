package com.kaleido.app.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaleido.app.data.local.ShelfStore
import com.kaleido.app.domain.CartLine
import com.kaleido.app.domain.CartSummary
import com.kaleido.app.domain.CatalogRepository
import com.kaleido.app.domain.Product
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * App-scoped hub for the user's personal state. A single instance is shared by
 * every screen (provided as a Koin `single`) so a heart tapped on the detail
 * screen lights up instantly on the list.
 */
class ShelfViewModel(
    private val shelf: ShelfStore,
    private val repository: CatalogRepository,
) : ViewModel() {

    val wishlistIds: StateFlow<Set<Int>> = shelf.wishlist
    val compareIds: StateFlow<List<Int>> = shelf.compare

    val wishlist: StateFlow<List<Product>> =
        combine(repository.products, shelf.wishlist) { products, ids ->
            products.filter { it.id in ids }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val recentlyViewed: StateFlow<List<Product>> =
        combine(repository.products, shelf.recentlyViewed) { products, ids ->
            ids.mapNotNull { id -> products.firstOrNull { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val cart: StateFlow<CartSummary> =
        combine(repository.products, shelf.cart) { products, quantities ->
            val lines = quantities.mapNotNull { (id, qty) ->
                products.firstOrNull { it.id == id }?.let { CartLine(it, qty) }
            }.sortedBy { it.product.title }
            CartSummary.from(lines)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, CartSummary.from(emptyList()))

    fun isWishlisted(id: Int) = id in shelf.wishlist.value
    fun toggleWishlist(id: Int) = shelf.toggleWishlist(id)

    fun quantityOf(id: Int) = cart.value.lines.firstOrNull { it.product.id == id }?.quantity ?: 0
    fun addToCart(id: Int) = shelf.addToCart(id, 1)
    fun setQuantity(id: Int, qty: Int) = shelf.setCartQuantity(id, qty)
    fun clearCart() = shelf.clearCart()

    fun recordView(id: Int) = shelf.recordView(id)

    fun inCompare(id: Int) = id in shelf.compare.value
    fun toggleCompare(id: Int) = shelf.toggleCompare(id)
    fun clearCompare() = shelf.clearCompare()
}
