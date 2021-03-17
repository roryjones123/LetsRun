package com.example.LetsRun.ManualCreateRunActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.Utils.HelperClass

class ManualCreateRunViewModel(application: Application) : AndroidViewModel(application) {
    private val manualCreateRunRepository: ManualCreateRunRepository? = ManualCreateRunRepository()
    val wasSaved = MutableLiveData<Boolean>()

    fun addNewRun(runToAdd: Run) {
        val saveTask = manualCreateRunRepository?.addNewRun(runToAdd)
        saveTask?.addOnFailureListener {
            wasSaved.postValue(false)
            HelperClass.logErrorMessage("Failed to save run!")
        }
        saveTask?.addOnCompleteListener {
            wasSaved.postValue(true)
        }
    }

}