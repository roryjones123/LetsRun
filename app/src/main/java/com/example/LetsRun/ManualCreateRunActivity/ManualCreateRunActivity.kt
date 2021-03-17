package com.example.LetsRun.ManualCreateRunActivity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.LetsRun.LogFragment.Run
import com.example.LetsRun.MainActivity
import com.example.LetsRun.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.manual_run_activity.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class ManualCreateRunActivity:AppCompatActivity() {
    private var manualCreateRunViewModel: ManualCreateRunViewModel? = null
    private var distanceToSave = 3.0
    @RequiresApi(Build.VERSION_CODES.O)
    private var caloriesToSave = 300L
    @RequiresApi(Build.VERSION_CODES.O)
    private var startDateToSave = LocalDateTime.now().toLocalDate()
    @RequiresApi(Build.VERSION_CODES.O)
    private var startTimeToSave = LocalTime.now()
    private var paceToSave = 600.0
    private var timeInMilsToSave = 1800L
    //hours and minutes (duration)
    private var hours = 1
    private var minutes = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_run_activity)
        manualCreateRunViewModel = ViewModelProvider(this).get(ManualCreateRunViewModel::class.java)

        createStartingValuesForDateAndTime()
        initButtons()

        manualCreateRunViewModel!!.wasSaved.observe(
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun createCalorieChangeAlert() {
        val dialogView = this.layoutInflater.inflate(R.layout.calorie_dialogue, null)
        val calorieText = dialogView.findViewById<EditText>(R.id.calorieText)

        this.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Are you sure you want to finish this run?")
                .setView(dialogView)
                .setPositiveButton("Confirm") { dialog, _ ->
                    if(calorieText.text.isNotEmpty()) {
                        caloriesToSave = (calorieText.text.toString()).toLong()
                        calorieChangeableTextView.text = "$caloriesToSave cals"
                    } else {
                        calorieChangeableTextView.text = "0 cals"
                    }
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createDateChangeAlert() {

        val datePicker = DatePicker(this)

        this.let {
            MaterialAlertDialogBuilder(it)
                .setView(datePicker)
                .setPositiveButton("Confirm") { dialog, _ ->
                    startDateToSave =
                        LocalDate.of(datePicker.year,datePicker.month+1,datePicker.dayOfMonth) //month picker giving wrong values so +1 offset it
                    dateChangeableTextView.text = startDateToSave.toString()
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createTimeChangeAlert() {

        val timePicker = TimePicker(this)

        this.let {
            MaterialAlertDialogBuilder(it)
                .setView(timePicker)
                .setPositiveButton("Confirm") { dialog, _ ->
                    val startTime = LocalTime.of(timePicker.hour,timePicker.minute,20)
                    startTimeToSave = startTime
                    startTimeChangeableTextView.text = startTimeToSave.toString()
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun createDurationChangeAlert() {

        val timePicker = this.layoutInflater.inflate(R.layout.time_picker_dialogue, null)

        val numberPicker1 = timePicker.findViewById<NumberPicker>(R.id.numberPicker1)
        val numberPicker2 = timePicker.findViewById<NumberPicker>(R.id.numberPicker2)

        numberPicker1.minValue = 0
        numberPicker1.maxValue = 999

        numberPicker2.minValue = 0
        numberPicker2.maxValue = 59

        this.let {
            MaterialAlertDialogBuilder(it)
                .setView(timePicker)
                .setPositiveButton("Confirm") { dialog, _ ->

                    hours = numberPicker1.value
                    minutes = numberPicker2.value

                    durationChangeableTextView.text = numberPicker1.value.toString() + "hrs " + numberPicker2.value.toString() + "mins"
                    avgPaceChangeableTextView.text = calculateAveragePace(numberPicker1.value,numberPicker2.value) + " km/h"
                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun createDistanceChangeAlert() {
        val numberPicker = this.layoutInflater.inflate(R.layout.distance_picker_dialogue, null)

        val numberPicker1 = numberPicker.findViewById<NumberPicker>(R.id.numberPicker1)
        val numberPicker2 = numberPicker.findViewById<NumberPicker>(R.id.numberPicker2)

        numberPicker1.minValue = 0
        numberPicker1.maxValue = 999

        numberPicker2.minValue = 0
        numberPicker2.maxValue = 9

        this.let {
            MaterialAlertDialogBuilder(it)
                .setView(numberPicker)
                .setPositiveButton("Confirm") { dialog, _ ->

                    distanceToSave = numberPicker1.value.toDouble() + (numberPicker2.value.toDouble()/10)
                    avgPaceChangeableTextView.text = calculateAveragePace(hours,minutes)+ " km/h"
                    distanceChangeableTextView.text = "$distanceToSave km"

                    dialog.cancel()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
                .show()
        }
    }

    private fun calculateAveragePace(elapsedHours:Int, elapsedMinutes:Int): String {

        val hoursToMils = elapsedHours*3600
        val minutesToMils = elapsedMinutes*60

        val pace = ((hoursToMils+minutesToMils) / ((this.distanceToSave))).toLong()

        paceToSave = pace.toDouble()
        timeInMilsToSave = (elapsedHours + elapsedMinutes).toLong()

        return formatMilsToProperTime(pace)
    }

    //slightly altered in this use case
    private fun formatMilsToProperTime(timeToFormat: Long): String {
        val formattedTime = String.format(
            "%02d:%02d:%02d", timeToFormat / 3600,
            timeToFormat % 3600 / 60, timeToFormat % 60
        )

        //for removing/adding hours if time < 1 hour
        return if (formattedTime[0] == '0' && formattedTime[1] == '0') {
            formattedTime.substring(3, formattedTime.length)
        } else if (formattedTime[0] == '0' && formattedTime[1] != '0') {
            formattedTime.substring(1, formattedTime.length)
        } else {
            formattedTime
        }
    }

    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initButtons() {
        val calorieChangeButton = findViewById<Button>(R.id.calorieButton)
        val distanceChangeButton = findViewById<Button>(R.id.distanceButton)
        val durationChangeButton = findViewById<Button>(R.id.durationButton)
        val dateChangeButton = findViewById<Button>(R.id.dateButton)
        val saveButton = findViewById<ImageView>(R.id.saveButton)
        val startTimeButton = findViewById<Button>(R.id.startTimeButton)
        val avgPaceButton = findViewById<Button>(R.id.avgPaceButton)

        discardButton.setOnClickListener {
            goToMainActivity()
        }

        calorieChangeButton.setOnClickListener {
            createCalorieChangeAlert()
        }

        distanceChangeButton.setOnClickListener {
            createDistanceChangeAlert()
        }

        durationChangeButton.setOnClickListener {
            createDurationChangeAlert()
        }

        dateChangeButton.setOnClickListener {
            createDateChangeAlert()
        }

        startTimeButton.setOnClickListener {
            createTimeChangeAlert()
        }

        avgPaceButton.setOnClickListener {
            Toast.makeText(this,"This is automatically generated! Change time or distance.",Toast.LENGTH_LONG).show()
        }

        saveButton.setOnClickListener {
            val dateTimeToSave = LocalDateTime.of(startDateToSave,startTimeToSave)
            val runToAdd = Run("default name",paceToSave,distanceToSave,timeInMilsToSave,caloriesToSave,dateTimeToSave.toString())
            manualCreateRunViewModel!!.addNewRun(runToAdd)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createStartingValuesForDateAndTime() {
        val formatter =
            DateTimeFormatter.ofPattern("HH:mm")
        val time = LocalTime.now()
        val formattedTime = formatter.format(time)

        dateChangeableTextView.text = startDateToSave.toString()
        startTimeChangeableTextView.text = formattedTime

    }

}