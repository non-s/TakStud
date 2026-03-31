package com.example.takstud.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Conversor Room para lista de strings
 * Usado para armazenar listas como JSON no banco de dados
 */
class StringListConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) {
            return "[]"
        }
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty() || value == "[]") {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            gson.fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
