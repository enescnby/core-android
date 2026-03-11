package com.shade.app.model

data class RegisterRequest(
    val public_key: String,
    val encrypted_private_key: String,
    val salt: String,
    val device_model: String,
    val fcm_token: String
)

data class RegisterResponse(
    val core_guard_id: String,
    val message: String
)