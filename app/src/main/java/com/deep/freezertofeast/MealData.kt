package com.deep.freezertofeast

import kotlinx.serialization.Serializable

@Serializable
data class MealData(
    val recipeName: String,
    val prepTime: Int,
    val ingredientsUsed: List<String>,
    val steps: List<String>
)
