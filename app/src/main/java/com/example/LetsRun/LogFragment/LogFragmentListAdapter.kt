package com.example.LetsRun.LogFragment

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.LetsRun.R
import java.time.Month

//TODO change list type and bind each thing then work out how to make total value
//this adapter either binds a header (the month) or a run
class LogFragmentListAdapter(private val runList:List<Any>,private val listener: (Run) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val MONTH = 1
        private const val RUN = 2
    }

    //either inflates Month or Run
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType==RUN) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.run_in_list, parent, false)
            RunViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.month_in_list, parent, false)
            MonthViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return runList.size
    }

    //the month heading does not need an on click listener, Run items do for taking user to run log page
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val run = runList[position]

        if(holder.itemViewType==RUN) {
            holder.itemView.setOnClickListener { (listener(run as Run)) }
            (holder as RunViewHolder).bind(runList[position] as Run)
        } else {
            (holder as MonthViewHolder).bind(runList[position] as Month,calculateTimeAndActivities(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(runList[position] is Run ) {
            RUN
        } else {
            MONTH
        }
    }

    //calculates the number of runs in a month, and the total length and returns them as a pair
    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateTimeAndActivities(position: Int): Pair<Int, Double> {
        var activityCount = 0
        var distanceCount = 0.0

        for(i in position+1 until runList.size) {
            if(runList[i] is Month) {
                break
            } else {
                val run = runList[i] as Run
                activityCount++
                distanceCount += run.distance
            }
        }
        return Pair(activityCount,distanceCount)
    }

}