package br.com.optimizer7.logworktime

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
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import kotlinx.android.synthetic.main.log_time_activity.*
import kotlinx.android.synthetic.main.log_time_activity.view.*
import android.widget.Toast




/**
 * Created by Tiago on 07/01/2018.
 */

val REQUEST_ACCOUNT_PICKER = 1000
val REQUEST_AUTHORIZATION = 1001
val REQUEST_GOOGLE_PLAY_SERVICES = 1002
val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
val SCOPES = arrayOf(SheetsScopes.SPREADSHEETS)
val PREF_ACCOUNT_NAME = "accountName"

class LogTimeActivity : AppCompatActivity() , EasyPermissions.PermissionCallbacks  {

    lateinit var mCredential: GoogleAccountCredential
    lateinit var mProgress: ProgressDialog

    var beginWorktime: EditText? = null
    var beginLunch: EditText? = null
    var stopLunch: EditText? = null
    var stopWorktime: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_time_activity)

        beginWorktime = findViewById<EditText>(R.id.edtBeginWorktime)
        beginLunch = findViewById<EditText>(R.id.edtBeginLunch)
        stopLunch = findViewById<EditText>(R.id.edtStopLunch)
        stopWorktime = findViewById<EditText>(R.id.edtStopWorktime)

        handleSelectWorktime()

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
    }

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
            if( event.getAction() == MotionEvent.ACTION_UP){
                TimePickerDialog(this, OnTimeSetListener { view, hourOfDay, minute ->
                    stopWorktime?.setText("" + hourOfDay + ":" + minute)
                }, 10, 10, true).show()
            }
            true
        })
    }



    private fun getResultsFromApi(cred: GoogleAccountCredential) {
        MakeRequestTask(cred).execute()

    }

    /**
     * An asynchronous task that handles the Google Sheets API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    class MakeRequestTask(credential: GoogleAccountCredential) : AsyncTask<Void, Void, List<String>>() {

        lateinit var mService: Sheets

        internal fun MakeRequestTask(credential: GoogleAccountCredential) {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = com.google.api.services.sheets.v4.Sheets.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Sheets API Android Quickstart")
                    .build()
        }

        override fun doInBackground(vararg params: Void): List<String>? {
            try {
                return getDataFromApi()
            } catch (e: Exception) {
                cancel(true)
                return null
            }

        }


        @Throws(IOException::class)
//        private fun getDataFromApi(): List<String> {
        private fun getDataFromApi(): List<String> {
            val spreadsheetId = "11mXQ5av_U0KG9AgWCEE7pSGomvCzyB2nLoRcySYtNno"//"1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            val range = "Janeiro!B2:E"
            val results = ArrayList<String>()
            val valueRange = ValueRange()

            valueRange.setRange("teste")

            this.mService.spreadsheets().values().update(spreadsheetId, range, valueRange)
                    .setValueInputOption("USER_ENTERED")
                    .execute()

            return results
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onCancelled() {
            super.onCancelled()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}