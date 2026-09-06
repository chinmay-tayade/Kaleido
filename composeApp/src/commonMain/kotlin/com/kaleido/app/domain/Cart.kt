package com.kaleido.app.domain

/** One line in the cart: a product plus how many of it. */
data class CartLine(val product: Product, val quantity: Int) {
    val lineTotal: Double get() = product.price * quantity
}

/** Derived totals for the cart screen. Pure so it can be tested directly. */
data class CartSummary(
    val lines: List<CartLine>,
    val itemCount: Int,
    val subtotal: Double,
    val savingsFromDeals: Double,
) {
    companion object {
        fun from(lines: List<CartLine>): CartSummary {
            val subtotal = lines.sumOf { it.lineTotal }
            // Playful "smart basket": every product with a deal score >= 70 is
            // treated as 10% below list, and we show what that saved.
            val savings = lines.filter { it.product.dealScore() >= 70 }
                .sumOf { it.lineTotal * 0.10 }
            return CartSummary(
                lines = lines,
                itemCount = lines.sumOf { it.quantity },
                subtotal = subtotal,
                savingsFromDeals = savings,
            )
        }
    }
}
