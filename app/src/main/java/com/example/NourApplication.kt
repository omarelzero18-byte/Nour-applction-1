package com.example

import android.app.Application
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck

class NourApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebaseAndAppCheck()
    }

    private fun initFirebaseAndAppCheck() {
        try {
            // Check if Firebase was initialized automatically by google-services
            val app = if (FirebaseApp.getApps(this).isNotEmpty()) {
                FirebaseApp.getInstance()
            } else {
                Log.i(TAG, "No default FirebaseApp found; initializing fallback")
                null
            }

            if (app != null) {
                val appCheck = FirebaseAppCheck.getInstance(app)
                if (BuildConfig.DEBUG) {
                    try {
                        // Debug provider factory loaded reflectively or from debug artifact
                        val debugFactoryClass = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                        val getInstanceMethod = debugFactoryClass.getMethod("getInstance")
                        val factory = getInstanceMethod.invoke(null) as com.google.firebase.appcheck.AppCheckProviderFactory
                        appCheck.installAppCheckProviderFactory(factory)
                        Log.i(TAG, "Firebase App Check initialized with DebugAppCheckProviderFactory")
                    } catch (e: Exception) {
                        Log.w(TAG, "DebugAppCheckProviderFactory could not be installed: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase / App Check initialization handled gracefully: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "NourApplication"
    }
}
