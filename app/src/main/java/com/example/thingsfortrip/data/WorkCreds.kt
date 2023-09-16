package com.example.thingsfortrip.data

import com.example.thingsfortrip.data.models.Category
import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class WorkCreds(val latLng: LatLng = LatLng(0.0, 0.0), val date: Date = Date(), val category: Category? = null) {

    fun isWeatherOptionAvailable() :Boolean = latLng != LatLng(0.0, 0.0) && date != Date()
    fun isFilled() :Boolean = latLng != LatLng(0.0, 0.0) && date != Date() && category != null
}