package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import br.com.optimizer7.logworktime.Model.DateWorktime
import br.com.optimizer7.logworktime.Model.Worktime
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ListLoggedWorktime : AppCompatActivity() {
    /**
     * Variables
     */
    val mRootRef = FirebaseDatabase.getInstance().getReference()

    val listOfWorktime: MutableList<Worktime> = mutableListOf()

    var mRecyclerView: RecyclerView? = null
    var mAdapter: RecyclerView.Adapter<ListLoggedWorktimeAdapter.PlaceHolder>? = null
    var mLayoutManager: RecyclerView.LayoutManager? = null

    var dateSelected: String? = null
    var monthSelected: String? = null
    var yearSelected: String? = null

    val mLogWorktimeRef = mRootRef.child("logworktimes")

    lateinit var currentUser: FirebaseAuth

    val cal = Calendar.getInstance()
    val month_date = SimpleDateFormat("MMMM")

    var txtSelectedMonth: TextView? = null
    var calendarPick: CalendarView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.list_logged_worktime_activity)
        mRecyclerView = findViewById(R.id.recyclerView)

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView!!.setHasFixedSize(true)

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = mLayoutManager

        // specify an adapter (see also next example)
        mAdapter = ListLoggedWorktimeAdapter(listOfWorktime as ArrayList<Worktime>)
        mRecyclerView!!.setAdapter(mAdapter)


        val myToolbar = findViewById<View>(R.id.toolbar_list_logged_worktime) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.list_main_menu)

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance()

        calendarPick = findViewById(R.id.listCalendarView)
        //calendarPick!!.visibility=View.GONE

        dateSelected = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
        monthSelected = month_date.format(cal.time)
        yearSelected = SimpleDateFormat("yyyy").format(Date()).toString()

        txtSelectedMonth = findViewById(R.id.txtSelectedMonth)

        handleClicks()

        loadLoggedWorktime()
    }

    fun handleClicks() {
        txtSelectedMonth?.setOnClickListener(View.OnClickListener {
            if(calendarPick!!.visibility==View.GONE){
                calendarPick!!.visibility=View.VISIBLE
            }else{
                calendarPick!!.visibility=View.GONE
            }
        })

        calendarPick!!.setOnDateChangeListener { view, year, month, dayOfMonth ->

            dateSelected = ""+year+"-"+(month+1)+"-"+dayOfMonth
            getMonthFullName(Date(year, month, dayOfMonth))
            yearSelected = year.toString()

            loadLoggedWorktime()
        }
    }

    /**
     * Get the month's full name
     */
    fun getMonthFullName(date: Date){
        monthSelected = month_date.format(date)
        txtSelectedMonth!!.setText(monthSelected)
    }

    fun loadLoggedWorktime(){

        mLogWorktimeRef.addValueEventListener(object : ValueEventListener {

            var worktimeModel = callbackLoggedTime()

            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                listOfWorktime.clear()
                mRecyclerView!!.invalidate()
                mAdapter!!.notifyDataSetChanged()

                dataSnapshot!!
                        ?.child(currentUser.uid)
                        ?.child(currentUser.currentUser!!.displayName)
                        ?.child(worktimeModel.yearWorktime)
                        ?.child(worktimeModel.monthWorktime)
                        ?.children
                        ?.mapNotNullTo(listOfWorktime){
                    it.getValue<Worktime>(Worktime::class.java)
                }

                //updateUI()
            }
        })
    }

    @SuppressLint("SetTextI18n")
     /**
     * Update List worked time

    fun updateUI(){
        val iterator = listOfWorktime.iterator()

        var logTimeString = ""

        iterator.forEach {

            logTimeString +=

            "Date: " + it.date + "\n" + it.beginWorktime + "  |  " + it.beginLunch + "  |  " + it.doneLunch + "  |  " + it.doneWorktime + "\n\n"

        }
        mOutputText!!.setText( logTimeString )
    }
      */

    /**
     * Retrieve object with input data from user
     */
    fun callbackLoggedTime() : DateWorktime {
        return DateWorktime(yearSelected, monthSelected, dateSelected)
    }

    /**
     * Create Menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.list_main_menu, menu)
        return true
    }

    /**
     * MENU toolbar
     * - Logout
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.action_settings -> {
                println("Logout")
                logout()
            }

            R.id.action_log_worktime ->{
                println("Log Worktime")
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Logout function
     */
    fun logout(){
        AuthUI.getInstance()
                .signOut(this!!)
                .addOnCompleteListener(OnCompleteListener {
                    task: Task<Void> -> println("I am out")
                })
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this!!.finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}