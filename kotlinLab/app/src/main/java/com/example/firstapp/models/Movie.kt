package com.example.firstapp.models

data class Movie(
    val name: String,
    val year: String,
    val director: String,
    val tags: List<Tag>
)