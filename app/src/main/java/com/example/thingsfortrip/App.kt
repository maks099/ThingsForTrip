package com.example.thingsfortrip

import android.app.Application
import com.example.thingsfortrip.data.repositories.FirebaseRepo
import com.example.thingsfortrip.data.repositories.local.AppDatabase
import com.example.thingsfortrip.data.repositories.local.AppRepository
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class App : Application() {


    override fun onCreate() {
        Places.initialize(applicationContext, getString(R.string.google_map_api_key))
        FirebaseApp.initializeApp(applicationContext)
        FirebaseRepo.instance

         super.onCreate()
    }
}