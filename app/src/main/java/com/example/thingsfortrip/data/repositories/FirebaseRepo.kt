package com.example.thingsfortrip.data.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class FirebaseRepo private constructor() {
    private var user: FirebaseUser? = null
    private val firebaseFirestore: FirebaseFirestore

    init {
        authenticateUser()
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun authenticateUser() {
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user = auth.currentUser
                }
            }
    }

    val getCategories: Task<QuerySnapshot>
        get() = firebaseFirestore.collection("categories").get()

    fun getThings(categoryCode: Int): Task<QuerySnapshot> {
        return firebaseFirestore
            .collection("things")
            .whereArrayContains("categoriesCodes", categoryCode)
            .get()
    }

    fun getClothes(weatherCode: Int): Task<QuerySnapshot> {
        return firebaseFirestore
            .collection("clothes")
            .whereArrayContains("weatherCodes", weatherCode)
            .get()
    }

    companion object {
        var instance: FirebaseRepo = FirebaseRepo()
    }
}