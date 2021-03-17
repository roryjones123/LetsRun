package com.example.LetsRun.SaveRunFragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.MainActivity
import com.example.LetsRun.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SaveRunActivity: AppCompatActivity() {
    private var saveRunViewModel: SaveRunViewModel? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        saveRunViewModel = ViewModelProvider(this).get(SaveRunViewModel::class.java)
        setContentView(R.layout.finished_run)

        setupButtons()

        //listener for whether or not run saves
        saveRunViewModel!!.wasSaved!!.observe(
            this,
            Observer { wasSaved ->
                if (wasSaved) {
                    Toast.makeText(applicationContext,"Run was saved!",Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(applicationContext,"Run was not saved!",Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                }
            }
        )
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupDiscardDialogue() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Are you sure you want to discard this run?")
            .setPositiveButton("Confirm") { dialog, _ ->
                dialog.cancel()
                goToMainActivity()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun setupButtons() {
        val saveButton = findViewById<ImageView>(R.id.saveButton)
        val discardButton = findViewById<ImageView>(R.id.discardButton)

        //save run object from intent
        saveButton.setOnClickListener() {
            //sends run to firebase
            saveRunViewModel!!.saveRun(intent.getParcelableExtra<Run>("FinishedRun") as Run)
        }

        discardButton.setOnClickListener() {
            setupDiscardDialogue()
        }
    }
}