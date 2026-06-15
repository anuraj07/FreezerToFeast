package com.deep.freezertofeast

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val errorMessage: String, val throwable: Throwable? = null) : Resource<Nothing>()
}
