package ru.mngerasimenko.todolist.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatusResponse(
    val status: Boolean,
    val version: String? = null,
    @SerialName("min_android_version")
    val minAndroidVersion: Int? = null
)
