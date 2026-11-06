package com.example.fithub

import android.app.Application
import com.example.fithub.core.ServiceLocator
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FitHubApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        // ============================================================
        // SESSION POLICY: logout whenever the app process starts fresh.
        //
        // `onCreate()` runs only on cold process start — never on
        // backgrounding, never on rotation. So this guarantees:
        //   • Cold start after close / recents swipe / process death → logged out
        //   • Backgrounding + returning (same process)              → stays logged in
        //   • Rotation / config change                              → stays logged in
        //
        // Must run BEFORE any code reads SessionManager.currentUserId.
        // ============================================================
        FirebaseAuth.getInstance().signOut()

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        ServiceLocator.init(this)

        appScope.launch {
            // 1. Seed verified plans + exercises + foods (needed by both tracks)
            ServiceLocator.seedManager.seedIfNeeded()
            ServiceLocator.seedManager.seedFoodsIfNeeded()

            // 2. Demo user seeding — DISABLED now that real auth is in place.
            //    Uncomment only if you want the Alex Hunter demo profile back.
            // DemoDataSeeder.seedIfNeeded(ServiceLocator.database)

            // 3. Verification logs
            android.util.Log.d(
                "FitHubSeed",
                "VERIFIED PLANS: " + ServiceLocator.database.workoutPlanDao().verifiedCount()
            )
        }
    }
}