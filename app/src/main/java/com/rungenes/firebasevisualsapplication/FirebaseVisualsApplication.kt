package com.rungenes.firebasevisualsapplication

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class FirebaseVisualsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}