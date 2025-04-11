package com.example.pantrypal

import java.io.Serializable

data class Recipe(
    val title: String,
    val ingredients: List<String>,
    val instructions: String
) : Serializable
