package com.kaleido.app.ui.nav

/**
 * Route table. String routes (rather than serializable type-safe routes) because
 * the kotlinx.serialization compiler plugin currently can't generate code for
 * the Kotlin/Wasm target.
 */
object Routes {
    const val CATALOG = "catalog"
    const val WISHLIST = "wishlist"
    const val CART = "cart"
    const val INSIGHTS = "insights"
    const val COMPARE = "compare"

    const val DETAIL_ARG = "id"
    const val DETAIL_PATTERN = "detail/{$DETAIL_ARG}"
    fun detail(id: Int) = "detail/$id"
}

enum class TopLevel(val route: String, val label: String) {
    Catalog(Routes.CATALOG, "Browse"),
    Wishlist(Routes.WISHLIST, "Saved"),
    Cart(Routes.CART, "Cart"),
    Insights(Routes.INSIGHTS, "Insights"),
}
