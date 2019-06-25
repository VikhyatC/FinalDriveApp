package com.smarterhomes.wateronmetermap.RoomArch

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromList(value: List<String>): String {
        if (value == null) {
            return ""
        }
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {

        }.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toByteArray(value: String):List<String>{

        val gson = Gson()
        val type = object : TypeToken<List<String>>() {

        }.type
        return gson.fromJson<List<String>>(value, type)
    }
}