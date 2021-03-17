package com.example.LetsRun.LogFragment


import android.annotation.SuppressLint
import android.os.Build
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.LetsRun.R
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class RunViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(run:Run) {

        val distanceTextView = itemView.findViewById<TextView>(R.id.distanceTextViewChangeable)
        val timeTextView = itemView.findViewById<TextView>(R.id.timeTextViewChangeable)
        val howManyDaysAgoTextView = itemView.findViewById<TextView>(R.id.daysAgoTextView)

        //format for ensuring there's always 2dp (looks nicer in the log)
        val df = DecimalFormat("0.00")
        distanceTextView.text = df.format(run.distance) + " km"
        timeTextView.text = formatMilsToProperTime(run.elapsedTime)

        //formats date value to give how many days/hours ago
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        try {
            val time = sdf.parse(run.dateCreated).time
            val now = System.currentTimeMillis()
            val ago =
                DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            howManyDaysAgoTextView.text = ago

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    //formats time to 6 digits, takes up white space and looks good
    private fun formatMilsToProperTime(timeToFormat: Long): String {
        return String.format(
            "%02d:%02d:%02d", timeToFormat / 3600,
            timeToFormat % 3600 / 60, timeToFormat % 60
        )
    }

}