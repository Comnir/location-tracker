package com.jefferson.tracker.persistance

import android.os.AsyncTask

class AsyncRunner(val function: () -> Void?) : AsyncTask<String, String, Void?>() {
    override fun doInBackground(vararg params: String?): Void? {
        function()
        return null
    }

    companion object {
        fun run(function: () -> Void?) {
            AsyncRunner(function).execute()
        }
    }
}