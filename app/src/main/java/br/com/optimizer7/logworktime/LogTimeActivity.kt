package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import br.com.optimizer7.logworktime.Model.DateWorktime
import br.com.optimizer7.logworktime.Model.Worktime
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.log_time_activity.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Tiago on 07/01/2018.
 */

class LogTimeActivity : AppCompatActivity() {

    /**
     * Variables
     */
    val mRootRef = FirebaseDatabase.getInstance().getReference()

    var dateSelected: String? = null
    var monthSelected: String? = null
    var yearSelected: String? = null

    val mLogWorktimeRef = mRootRef.child("logworktimes")

    lateinit var currentUser: FirebaseAuth

    var beginWorktime: EditText? = null
    var beginLunch: EditText? = null
    var stopLunch: EditText? = null
    var stopWorktime: EditText? = null
    var logWorktime: Button? = null
    var calendarPick: CalendarView? = null

    val cal = Calendar.getInstance()
    val month_date = SimpleDateFormat("MMMM")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_time_activity)

        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.main_menu)

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance()
        if (currentUser != null) {
            updateUserNameLogged(currentUser.currentUser!!)
        }

        beginWorktime = findViewById(R.id.edtBeginWorktime)
        beginLunch = findViewById(R.id.edtBeginLunch)
        stopLunch = findViewById(R.id.edtStopLunch)
        stopWorktime = findViewById(R.id.edtStopWorktime)
        logWorktime = findViewById(R.id.btLogWorktime)
        calendarPick = findViewById(R.id.calendarView)

        dateSelected = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()

        monthSelected = month_date.format(cal.time)
        yearSelected = SimpleDateFormat("yyyy").format(Date()).toString()

        handleSelectWorktime()

        logWorktime()
    }

    /**
     * Set User name logged
     */
    fun updateUserNameLogged(user: FirebaseUser){
        txtUserName.text = "Bom dia : ${user.displayName}"
    }

    /**
     * Get the month's full name
     */
    fun getMonthFullName(date: Date){
        monthSelected = month_date.format(date)
    }

    /**
     * Handle select time and select calendar day
     */
    @SuppressLint("ClickableViewAccessibility")
    fun handleSelectWorktime(){

        calendarPick!!.setOnDateChangeListener { view, year, month, dayOfMonth ->

            dateSelected = ""+year+"-"+(month+1)+"-"+dayOfMonth
            getMonthFullName(Date(year, month, dayOfMonth))
            yearSelected = year.toString()

            beginWorktime?.setText("")
            beginLunch?.setText("")
            stopLunch?.setText("")
            stopWorktime?.setText("")
        }

        beginWorktime?.setOnTouchListener({ v, event ->
            if( event.getAction() == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    beginWorktime?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })

        beginLunch?.setOnTouchListener({ v, event ->
            if( event.getAction() == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    beginLunch?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })

        stopLunch?.setOnTouchListener({ v, event ->
            if( event.getAction() == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    stopLunch?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })

        stopWorktime?.setOnTouchListener({ v, event ->
            if (event.getAction() == MotionEvent.ACTION_UP) {
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    stopWorktime?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })
    }

    /**
     * Persist data on Firebase Database
     */
    fun logWorktime(){
        logWorktime?.setOnClickListener(View.OnClickListener {
            println("Log Worktime")

            var worktimeModel = retrieveLoggedTime()

            mLogWorktimeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    //dataSnapshot?.child(currentUser.uid)!!.child(currentUser.currentUser!!.displayName).child(worktime.dateWorktime)!!.getValue() != null
//                    if (dataSnapshot!!.child(currentUser.uid).child(currentUser.currentUser!!.displayName).child(worktimeModel.dateWorktime).exists()) {
                        mLogWorktimeRef.child(currentUser.uid)
                                .child(currentUser.currentUser!!.displayName)
                                .child(worktimeModel.yearWorktime)
                                .child(worktimeModel.monthWorktime)
                                .child(worktimeModel.dateWorktime)
                                .setValue(worktimeModel.worktime)
                }
            })
        })
    }

    /**
     * Retrieve object with input data from user
     */
    fun retrieveLoggedTime() : DateWorktime {
        val beginWorktime = beginWorktime?.getText().toString()
        val beginLunch = beginLunch?.getText().toString()
        val stopLunch = stopLunch?.getText().toString()
        val stopWorktime = stopWorktime?.getText().toString()

        return DateWorktime(yearSelected, monthSelected, dateSelected, Worktime(beginWorktime, beginLunch, stopLunch, stopWorktime, dateSelected))
    }

    /**
     * Create Menu
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
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

            R.id.action_list_logged_worktime ->{
                println("List of Logged Worktime")
                val intent = Intent(this, ListLoggedWorktime::class.java)
                startActivity(intent)
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
}