package com.example.generator2.playlist

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@Serializable
data class User(val id: Int, val name: String, val email: String, val additionalData: Map<String, Any>)
