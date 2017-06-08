package com.gocation.gocation_android.data

/**
 * Created by dylanlange on 11/05/17.
 */
data class User(
        val id: String,
        val name: String,
        val email: String,
        val gender: String,
        val ageRange: String,
        val imageUrl: String,
        val lastSeenAt: String
)