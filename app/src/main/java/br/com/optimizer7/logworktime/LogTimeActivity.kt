package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
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
    private val mRootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private var dateSelectedText: String? = null
    private var monthSelectedText: String? = null
    private var yearSelectedText: String? = null
    private var daySelected: Int = 0
    private val mLogWorktimeRef = mRootRef.child("logworktimes")
    private var currentUser: FirebaseAuth? = null
    private var beginWorktime: EditText? = null
    private var beginLunch: EditText? = null
    private var stopLunch: EditText? = null
    private var stopWorktime: EditText? = null
    private var logWorktime: Button? = null
    private var calendarPick: CalendarView? = null
    private val cal = Calendar.getInstance()
    private val monthSimpleDateFormat = SimpleDateFormat("MMMM", Locale.US)
    private var hour = 0
    private var minute = 0
    private var worktimeModel: DateWorktime? = null
    var rootView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_time_activity)

        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.main_menu)

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance()
        if (currentUser != null) {
            updateUserNameLogged(currentUser!!.currentUser!!)
        }

        beginWorktime = findViewById(R.id.edtBeginWorktime)
        beginLunch = findViewById(R.id.edtBeginLunch)
        stopLunch = findViewById(R.id.edtStopLunch)
        stopWorktime = findViewById(R.id.edtStopWorktime)
        logWorktime = findViewById(R.id.btLogWorktime)
        calendarPick = findViewById(R.id.calendarView)

        handleSelectWorktime()

        logWorktime()
    }

    /**
     * Set User name logged
     */
    private fun updateUserNameLogged(user: FirebaseUser){
        txtUserName.text = getString(R.string.message_welcome_user_name, user.displayName)
    }

    private fun updateDateWorktimeToLog(){
        updateDayValue()
        updateMonthValue()
        updateYearValue()
        updateDateSelectedTextValue()
    }

    /**
     * Get the day's value
     */
    private fun updateDayValue(){
        daySelected = cal.get(Calendar.DAY_OF_MONTH)
    }

    /**
     * Get the month's full name
     */
    private fun updateMonthValue(){
        monthSelectedText = monthSimpleDateFormat.format(cal.time)
    }

    /**
     * Get the year's value
     */
    private fun updateYearValue(){
        yearSelectedText = cal.get(Calendar.YEAR).toString()
    }

    /**
     * Get date selected in format
     */
    private fun updateDateSelectedTextValue(){
        dateSelectedText =
                "" + cal.get(Calendar.DAY_OF_MONTH) +
                "-"+ (cal.get(Calendar.MONTH) + 1) +
                "-"+ cal.get(Calendar.YEAR)
    }

    /**
     * Handle select time and select calendar day
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun handleSelectWorktime(){

        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)

        calendarPick!!.setOnDateChangeListener { _, year, month, dayOfMonth ->

            cal.set(year, month, dayOfMonth)
            updateDateWorktimeToLog()

            clearFields()
        }

        beginWorktime?.setOnTouchListener({ _, event ->
            if( event.action == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { _, hourOfDay, minute ->
                    beginWorktime?.setText(getString(R.string.start_worktime_displayed_label, fixTimeLayout(hourOfDay), fixTimeLayout(minute)))
                }, hour, minute, true).show()
            }
            true
        })

        beginLunch?.setOnTouchListener({ _, event ->
            if( event.action == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { _, hourOfDay, minute ->
                    beginLunch?.setText(getString(R.string.start_lunch_displayed_label, fixTimeLayout(hourOfDay), fixTimeLayout(minute)))
                }, hour, minute, true).show()
            }
            true
        })

        stopLunch?.setOnTouchListener({ _, event ->
            if( event.action == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { _, hourOfDay, minute ->
                    stopLunch?.setText(getString(R.string.done_lunch_displayed_label, fixTimeLayout(hourOfDay), fixTimeLayout(minute)))
                }, hour, minute, true).show()
            }
            true
        })

        stopWorktime?.setOnTouchListener({ _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                TimePickerDialog(this, OnTimeSetListener { _, hourOfDay, minute ->
                    stopWorktime?.setText(getString(R.string.done_worktime_displayed_label, fixTimeLayout(hourOfDay), fixTimeLayout(minute)))
                }, hour, minute, true).show()
            }
            true
        })

    }

    private fun fixTimeLayout(minute: Int) : String{
        return if(minute in 0..9){
            "0"+minute
        }else{
            minute.toString()
        }
    }

    /**
     * Persist data on Firebase Database
     */
    private fun logWorktime(){
        logWorktime?.setOnClickListener({

            retrieveLoggedTime()

            mLogWorktimeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {

                    rootView = it

                    LogWorkTimeFirebaseAsync().execute()

                    clearFields()

                    Snackbar.make(rootView!!,getString(R.string.success_log_worktime_message), Snackbar.LENGTH_SHORT).show()
                }
            })
        })
    }

    /**
     * Retrieve object with input data from user
     */
    private fun retrieveLoggedTime() {
        val beginWorktime = beginWorktime?.text.toString()
        val beginLunch = beginLunch?.text.toString()
        val stopLunch = stopLunch?.text.toString()
        val stopWorktime = stopWorktime?.text.toString()

        updateDateWorktimeToLog()

        worktimeModel = DateWorktime(
                yearSelectedText,
                monthSelectedText,
                dateSelectedText,
                Worktime(beginWorktime, beginLunch, stopLunch, stopWorktime, dateSelectedText, daySelected))
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
                logout()
            }

            R.id.action_list_logged_worktime ->{
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
                .signOut(this)
                .addOnCompleteListener({
                    println("I am out")
                })
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    fun clearFields(){
        beginWorktime?.setText("")
        beginLunch?.setText("")
        stopLunch?.setText("")
        stopWorktime?.setText("")
    }

    @SuppressLint("StaticFieldLeak")
    inner class LogWorkTimeFirebaseAsync : AsyncTask<Void, Void, String>() {

        override fun doInBackground(vararg params: Void?): String? {
            mLogWorktimeRef.child(currentUser!!.uid)
                    .child(currentUser!!.currentUser!!.displayName)
                    .child(worktimeModel?.yearWorktime)
                    .child(worktimeModel?.monthWorktime)
                    .child(worktimeModel?.dateWorktime)
                    .setValue(worktimeModel?.worktime)
            return null
        }
    }
}