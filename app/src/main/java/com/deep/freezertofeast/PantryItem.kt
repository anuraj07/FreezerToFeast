package com.deep.freezertofeast

import kotlinx.serialization.Serializable

@Serializable
data class PantryItem(
    val id: String = "",
    val name: String,
    val category: String, // "perishables", "proteins", "foundations"
    val quantity: String = "",
    val expiryInfo: String = "",
    val tag: String = "",
    val outOfStock: Boolean = false,
    val lastRestocked: String = "",
    val imageUrl: String = ""
)
