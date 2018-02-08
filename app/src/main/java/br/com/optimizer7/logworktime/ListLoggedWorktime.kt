package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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

class ListLoggedWorktime : AppCompatActivity() {
    /**
     * Variables
     */
    val mRootRef = FirebaseDatabase.getInstance().getReference()

    var dateSelected: String? = null
    var monthSelected: String? = null
    var yearSelected: String? = null

    var mOutputText: TextView? = null

    val mLogWorktimeRef = mRootRef.child("logworktimes")

    lateinit var currentUser: FirebaseAuth

    val cal = Calendar.getInstance()
    val month_date = SimpleDateFormat("MMMM")

    var txtSelectedMonth: TextView? = null
    var calendarPick: CalendarView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.list_logged_worktime_activity)

        val myToolbar = findViewById<View>(R.id.toolbar_list_logged_worktime) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.list_main_menu)

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance()

        calendarPick = findViewById(R.id.listCalendarView)
        calendarPick!!.visibility=View.GONE

        dateSelected = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()
        monthSelected = month_date.format(cal.time)
        yearSelected = SimpleDateFormat("yyyy").format(Date()).toString()

        txtSelectedMonth = findViewById(R.id.txtSelectedMonth)

        mOutputText = findViewById(R.id.temp)
        mOutputText!!.isVerticalScrollBarEnabled = true


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
    }

    val listOfWorktime: MutableList<Worktime> = mutableListOf()

    fun loadLoggedWorktime(){
        mLogWorktimeRef.addValueEventListener(object : ValueEventListener {

            var worktimeModel = callbackLoggedTime()

            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {

                val chu = dataSnapshot!!.children
                chu.forEach{
                    println("TESTEEEEEEEEEEEEEEEEEE")
                   println(it.toString())
                }

                dataSnapshot!!
                        ?.child(currentUser.uid)
                        ?.child(currentUser.currentUser!!.displayName)
                        ?.child(worktimeModel.yearWorktime)
                        ?.child(worktimeModel.monthWorktime)
                        ?.children
                        ?.mapNotNullTo(listOfWorktime){
                    it.getValue<Worktime>(Worktime::class.java)
                }

                updateUI()

                //dataSnapshot?.child(currentUser.uid)!!.child(currentUser.currentUser!!.displayName).child(worktime.dateWorktime)!!.getValue() != null
//                    if (dataSnapshot!!.child(currentUser.uid).child(currentUser.currentUser!!.displayName).child(worktimeModel.dateWorktime).exists()) {
//                mLogWorktimeRef.child(currentUser.uid).child(currentUser.currentUser!!.displayName).child(worktimeModel.dateWorktime).setValue(worktimeModel.worktime)
//                    } else {
//                        mLogWorktimeRef.child(currentUser.uid).child(currentUser.currentUser!!.displayName).child(worktimeModel.dateWorktime).setValue(worktimeModel.worktime)
//                    }


//                mLogWorktimeRef.child(currentUser.uid)
//                        .child(currentUser.currentUser!!.displayName)
//                        .child(worktimeModel.yearWorktime)
//                        .child(worktimeModel.monthWorktime)


//                val listWorktime = dataSnapshot
//                        ?.child(currentUser.uid)
//                        ?.child(currentUser.currentUser!!.displayName)
//                        ?.child(worktimeModel.yearWorktime)
//                        ?.child(worktimeModel.monthWorktime)
//                        .getValue(Post.class)
//                        //.child(worktimeModel.dateWorktime)
            }
        })
    }

    /**
     * Update List worked time
     */
    fun updateUI(){
        mOutputText?.setText("Test listing ")
        mOutputText!!.setText(TextUtils.join("\n", listOfWorktime))
    }

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

class Worktime2{
    val beginWorktime: String? = null
    val beginLunch: String? = null
    val doneLunch: String? = null
    val doneWorktime: String? = null
}
