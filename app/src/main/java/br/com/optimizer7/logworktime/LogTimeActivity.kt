package br.com.optimizer7.logworktime

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.util.*
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import kotlinx.android.synthetic.main.log_time_activity.*
import kotlinx.android.synthetic.main.log_time_activity.view.*
import android.widget.Toast
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import br.com.optimizer7.logworktime.Model.DateWorktime
import br.com.optimizer7.logworktime.Model.Worktime
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.util.DateTime
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import kotlin.collections.HashMap


/**
 * Created by Tiago on 07/01/2018.
 */

class LogTimeActivity : AppCompatActivity() {

    val mRootRef = FirebaseDatabase.getInstance().getReference()
    val map = HashMap<String, Any>()

    val mLogWorktimeRef = mRootRef.child("logworktimes")

    lateinit var currentUser: FirebaseAuth

    var beginWorktime: EditText? = null
    var beginLunch: EditText? = null
    var stopLunch: EditText? = null
    var stopWorktime: EditText? = null
    var logWorktime: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_time_activity)
        map.put("logtime", ServerValue.TIMESTAMP)

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

        handleSelectWorktime()

        logWorktime()
    }

//    override fun onStart() {
//        super.onStart()
//

//    }

    fun updateUserNameLogged(user: FirebaseUser){
        txtUserName.text = "Bom dia : ${user.displayName}"
    }

    fun updateLoggedWorktimeForToday(user: FirebaseUser){
        //TODO : CALL FIREBASE DATABASE TO RETRIEVE TODAY's INFO IN CASE THE DAY SELECTED HAS ALREADY BEEN LOGGED
    }

    /**
     * Handle select time
     */
    @SuppressLint("ClickableViewAccessibility")
    fun handleSelectWorktime(){

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

    //TODO:Log Worktime from the chosen day
    fun logWorktime(){
        logWorktime?.setOnClickListener(View.OnClickListener {
            println("Log Worktime")

            var worktime = retrieveLoggedTime()

            mLogWorktimeRef.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(dataSnapshot: DataSnapshot?) {
                    //dataSnapshot?.child(currentUser.uid)!!.child(currentUser.currentUser!!.displayName).child(worktime.dateWorktime)!!.getValue() != null
                    if (dataSnapshot!!.child(currentUser.uid).child(currentUser.currentUser!!.displayName).child(worktime.dateWorktime).exists()) {
                        mLogWorktimeRef.child(currentUser.uid).child(currentUser.currentUser!!.displayName).setValue(worktime)
                    } else {
                        mLogWorktimeRef.child(currentUser.uid).child(currentUser.currentUser!!.displayName).push().setValue(worktime)
                    }
                }
            })
        })
    }

    fun retrieveLoggedTime() : DateWorktime {
        val beginWorktime = beginWorktime?.getText().toString()
        val beginLunch = beginLunch?.getText().toString()
        val stopLunch = stopLunch?.getText().toString()
        val stopWorktime = stopWorktime?.getText().toString()

        val dateNow = SimpleDateFormat("yyyy-MM-dd").format(Date()).toString()

        return DateWorktime(dateNow, Worktime(beginWorktime, beginLunch, stopLunch, stopWorktime))
    }

//        logWorktime?.setOnClickListener(View.OnClickListener {
//            println("Log Worktime")
//
//            if (!isGooglePlayServicesAvailable()) {
//                acquireGooglePlayServices()
//            } else if (mCredential.getSelectedAccountName() == null) {
//                chooseAccount()
//            } else if (!isDeviceOnline()) {
//                Toast.makeText(this@LogTimeActivity, "No network connection available.",
//                        Toast.LENGTH_LONG).show()
//            } else {
//                //MakeRequestTask(mCredential).execute()
//            }
//        })
//    }

    // Create Menu
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