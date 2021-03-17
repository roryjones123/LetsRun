package com.example.LetsRun.OldRunActivity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.LetsRun.Utils.HelperClass

class OldRunViewModel(application: Application) : AndroidViewModel(application) {
    private val oldRunRepository:OldRunRepository? = OldRunRepository()
    val wasDeleted = MutableLiveData<Boolean>()

    fun deleteRun(runToDelete:String) {
        val deleteTask = oldRunRepository?.deleteRun(runToDelete)
        deleteTask?.addOnFailureListener {
            wasDeleted.postValue(false)
            HelperClass.logErrorMessage("Failed to delete run!")
        }
        deleteTask?.addOnCompleteListener {
            wasDeleted.postValue(true)
        }
    }
}
