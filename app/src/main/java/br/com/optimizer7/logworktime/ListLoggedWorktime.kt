package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CalendarView
import android.widget.ImageView
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

    var dateSelectedText: String? = null
    var monthSelectedText: String? = null
    var yearSelectedText: String? = null

    var dateSelected: Date? = null
    var daySelected = 0
    var monthSelected = 0
    var yearSelected = 2018

    val mLogWorktimeRef = mRootRef.child("logworktimes")

    lateinit var currentUser: FirebaseAuth

    var cal = Calendar.getInstance()
    val month_date = SimpleDateFormat("MMMM")

    var txtSelectedMonth: TextView? = null
    var calendarPick: CalendarView? = null
    var previousMonth: ImageView? = null
    var nextMonth: ImageView? = null

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

        bindViews()

        setCalendarValues(Date())

        handleClicks()

        loadLoggedWorktime()
    }

    fun bindViews(){

        calendarPick = findViewById(R.id.listCalendarView)
        calendarPick!!.visibility=View.GONE
        previousMonth = findViewById(R.id.previousMonth)
        nextMonth = findViewById(R.id.nextMonth)
        txtSelectedMonth = findViewById(R.id.txtSelectedMonth)

    }

    fun setCalendarValues(date: Date){
        cal!!.time = date

        dateSelectedText = SimpleDateFormat("yyyy-MM-dd").format(date).toString()
        monthSelectedText = month_date.format(cal.time)
        yearSelectedText = SimpleDateFormat("yyyy").format(date).toString()
        dateSelected = date

        setMonthYearFullName()

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
            val date = Date(year,month,dayOfMonth)

            cal!!.time = date
            dateSelected = date
            dateSelectedText = ""+year+"-"+(month+1)+"-"+dayOfMonth
            monthSelectedText = month_date.format(cal.time)
            yearSelectedText = year.toString()
            setMonthYearFullName()


            if(calendarPick!!.visibility==View.GONE){
                calendarPick!!.visibility=View.VISIBLE
            }else{
                calendarPick!!.visibility=View.GONE
            }

            loadLoggedWorktime()
        }

        previousMonth!!.setOnClickListener(View.OnClickListener {
            dateSelected!!.month -= 1
            setCalendarValues(dateSelected!!)
        })

        nextMonth!!.setOnClickListener(View.OnClickListener {
            dateSelected!!.month += 1
            setCalendarValues(dateSelected!!)
        })
    }

    /**
     * Set text view with month and year values
     */
    fun setMonthYearFullName(){
        txtSelectedMonth!!.setText(monthSelectedText + " " + yearSelectedText)
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

                listOfWorktime.sortWith(compareBy(Worktime::id))

                //updateUI()
            }
        })
    }

    /**
     * Retrieve object with input data from user
     */
    fun callbackLoggedTime() : DateWorktime {
        return DateWorktime(yearSelectedText, monthSelectedText, dateSelectedText)
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