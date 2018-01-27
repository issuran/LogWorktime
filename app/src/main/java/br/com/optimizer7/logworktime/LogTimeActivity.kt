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
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth


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

    lateinit var mCredential: FirebaseAuth

    var beginWorktime: EditText? = null
    var beginLunch: EditText? = null
    var stopLunch: EditText? = null
    var stopWorktime: EditText? = null
    var logWorktime: Button? = null

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
        logWorktime = findViewById(R.id.btLogWorktime)

        handleSelectWorktime()

        handleLogWorktime()

        // Initialize credentials and service object.
        mCredential = FirebaseAuth.getInstance()

        getWorktimeLoggedTodayFromApi()

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

        var onTouchListener = stopWorktime?.setOnTouchListener({ v, event ->
            if (event.getAction() == MotionEvent.ACTION_UP) {
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    stopWorktime?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })
    }

    fun handleLogWorktime(){
        logWorktime?.setOnClickListener(View.OnClickListener {
            println("Log Worktime")
            //TODO:Log Worktime from the chosen day

        })
    }

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

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    fun getWorktimeLoggedTodayFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential.currentUser == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            Toast.makeText(this@LogTimeActivity, "No network connection available.",
                    Toast.LENGTH_LONG).show()
        } else {
            //TODO:Retrieve info
        }
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val dialog = apiAvailability.getErrorDialog(
                    this@LogTimeActivity,
                    connectionStatusCode,
                    REQUEST_GOOGLE_PLAY_SERVICES)
            dialog.show()
        }
    }

    /**
     * Select account
     */
    fun chooseAccount(){
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this!!.finish()
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private fun isDeviceOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}