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
import java.time.Month
import java.util.*


class MonthViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    fun bind(month: Month,pair:Pair<Int,Double>) {

        val activityTextView = itemView.findViewById<TextView>(R.id.activityTextView)
        val distanceTextView = itemView.findViewById<TextView>(R.id.distanceTextView)
        val monthTextView = itemView.findViewById<TextView>(R.id.monthTextView)

        val df = DecimalFormat("0.00")

        monthTextView.text = month.toString()
        distanceTextView.text = df.format(pair.second) + " km"
        activityTextView.text = pair.first.toString() + " activities"
    }
}