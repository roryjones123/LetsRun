package com.example.LetsRun.SaveRunFragment

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.Utils.HelperClass.Companion.logErrorMessage

class SaveRunViewModel(application: Application) : AndroidViewModel(application) {

    private val savedRunRepository:SaveRunRepository? = SaveRunRepository()
    var wasSaved: MutableLiveData<Boolean>? = MutableLiveData()

    fun saveRun(run: Run) {
        val saveTask = savedRunRepository?.saveRun(run)
        saveTask?.addOnFailureListener {
            wasSaved?.postValue(false)
            logErrorMessage("Failed to save run!")
        }
        saveTask?.addOnCompleteListener {
            wasSaved?.postValue(true)
        }
    }
}