package com.example.LetsRun.Utils

import android.content.ContentValues.TAG
import android.util.Log

class HelperClass {
    companion object {
        fun logErrorMessage(errorMessage: String) {
            Log.d(TAG, errorMessage)
        }
    }
}