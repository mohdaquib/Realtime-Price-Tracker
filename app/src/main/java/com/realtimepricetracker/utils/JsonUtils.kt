package com.realtimepricetracker.utils

import com.google.gson.Gson
import com.realtimepricetracker.data.PriceUpdate

val gson = Gson()

fun PriceUpdate.toJson(): String = gson.toJson(this)

fun String.toPriceUpdate(): PriceUpdate = gson.fromJson(this, PriceUpdate::class.java)