package com.jefferson.tracker.session

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.jefferson.tracker.persistance.Persistence

class SessionViewModel(application: Application) : AndroidViewModel(application) {
    val sessions = Persistence.getInstance(application).allSessions()
}