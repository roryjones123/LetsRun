package com.example.LetsRun.LogFragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.LetsRun.ManualCreateRunActivity.ManualCreateRunActivity
import com.example.LetsRun.OldRunActivity.OldRunActivity
import com.example.LetsRun.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

class LogFragment : Fragment() {
    private var logFragmentViewModel: LogFragmentViewModel? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        logFragmentViewModel = ViewModelProvider(this).get(LogFragmentViewModel::class.java)
        val rootView = inflater.inflate(R.layout.log_fragment,container,false)
        val listView = rootView.findViewById<RecyclerView>(R.id.list_of_runs)

        initAddActivityButton(rootView)

        //creates adapter when get runs triggers view model livedata
        logFragmentViewModel!!.savedRuns.observe(
            viewLifecycleOwner,
            Observer { value ->
                createAdapter(listView,value)
            }
        )

        logFragmentViewModel!!.getRuns()

        return rootView
    }

    private fun goToViewOldRunActivity(run:Run) {
        val intent = Intent(context,OldRunActivity::class.java)
        intent.putExtra("runToView",run)
        startActivity(intent)
    }

    /*This is for creating an array of arrays, so that each run is split by the month it occurred in.
    This allows us to make a more interesting and dynamic recycler view*/
    @RequiresApi(Build.VERSION_CODES.O)
    fun splitArrayByMonth(runs:List<Run>): ArrayList<Any> {
        val splitList = ArrayList<Any>()
        var placeHolderMonth: Month? = null

        for(value in runs) {
            if(splitList.isEmpty()) {
                splitList.add(getMonth(value))
                placeHolderMonth = getMonth(value)
                splitList.add(value)
            } else if(getMonth(value)!==placeHolderMonth) {
                splitList.add(getMonth(value))
                splitList.add(value)
                placeHolderMonth = getMonth(value)
            } else {
                splitList.add(value)
            }
        }
        return splitList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonth(run:Run): Month {
        val sdf =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        return try {
            val time = sdf.parse(run.dateCreated)
            val localDate: LocalDate = time.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            localDate.month
        } catch (e: ParseException) {
            e.printStackTrace()
            Month.APRIL
        }
    }

    private fun goToCreateRunActivity() {
        val intent = Intent(context,ManualCreateRunActivity::class.java)
        startActivity(intent)
    }

    private fun initAddActivityButton(view:View) {
        val addActivityButton = view.findViewById<FloatingActionButton>(R.id.addActivityButton)
        addActivityButton.setOnClickListener {
            goToCreateRunActivity()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAdapter(listView:RecyclerView,value:List<Run>) {
        val logFragmentListAdapter =
            LogFragmentListAdapter(splitArrayByMonth(value)) { run -> goToViewOldRunActivity(run)  }
        listView.adapter = logFragmentListAdapter
        listView.layoutManager = LinearLayoutManager(context)
    }
}