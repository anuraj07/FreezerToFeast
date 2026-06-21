package com.deep.freezertofeast

import kotlinx.serialization.Serializable

@Serializable
data class MealData(
    val recipeName: String,
    val prepTime: Int,
    val ingredientsUsed: List<String>,
    val steps: List<String>,
    val protein: Int = 0,
    val fiber: Int = 0,
    val carbs: Int = 0,
    val calories: Int = 0,
    val imageUrl: String = ""
)
