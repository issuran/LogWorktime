package br.com.optimizer7.logworktime

import android.annotation.SuppressLint
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
import android.content.Intent
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
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


/**
 * Created by Tiago on 07/01/2018.
 */

val REQUEST_ACCOUNT_PICKER = 1000
val REQUEST_AUTHORIZATION = 1001
val REQUEST_GOOGLE_PLAY_SERVICES = 1002
val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)
val PREF_ACCOUNT_NAME = "accountName"

class LogTimeActivity : AppCompatActivity() {

    lateinit var mCredential: GoogleAccountCredential

    var beginWorktime: EditText? = null
    var beginLunch: EditText? = null
    var stopLunch: EditText? = null
    var stopWorktime: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_time_activity)

        val myToolbar = findViewById<View>(R.id.my_toolbar) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.main_menu)

        beginWorktime = findViewById(R.id.edtBeginWorktime)
        beginLunch = findViewById(R.id.edtBeginLunch)
        stopLunch = findViewById(R.id.edtStopLunch)
        stopWorktime = findViewById(R.id.edtStopWorktime)

        handleSelectWorktime()

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
    }

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

        var onTouchListener = stopWorktime?.setOnTouchListener({ v, event ->
            if (event.getAction() == MotionEvent.ACTION_UP) {
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    stopWorktime?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.action_settings -> {
                println("Logout")
                logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

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