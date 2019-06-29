package br.com.optimizer7.logworktime

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.optimizer7.logworktime.Model.Worktime
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ListLoggedWorktime : AppCompatActivity() {
    /**
     * Variables
     */
    private val mRootRef: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val listOfWorktime: MutableList<Worktime> = mutableListOf()
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<ListLoggedWorktimeAdapter.PlaceHolder>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var dateSelectedText: String? = null
    private var monthSelectedText: String? = null
    private var monthSelectedDisplayText: String? = null
    private var yearSelectedText: String? = null
    private var daySelected = 0
    private var monthSelected = 0
    private var yearSelected = 0
    private val mLogWorktimeRef = mRootRef.child("logworktimes")
    private var currentUser: FirebaseUser? = null
    private var cal = Calendar.getInstance()
    private val monthSimpleDateFormat = SimpleDateFormat("MMMM", getCurrentLocale())
    private val monthDatabaseFormat = SimpleDateFormat("MMMM", Locale.US)
    private var txtSelectedMonth: TextView? = null
    private var previousMonth: ImageView? = null
    private var nextMonth: ImageView? = null
    private var sendEmail: Button? = null

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
        mRecyclerView!!.adapter = mAdapter

        val myToolbar = findViewById<View>(R.id.toolbar_list_logged_worktime) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.list_main_menu)

        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = FirebaseAuth.getInstance().currentUser

        bindViews()

        updateDateWorktimeToLog()

        handleClicks()

        loadLoggedWorktime()
    }

    private fun bindViews(){

        previousMonth = findViewById(R.id.previousMonth)
        nextMonth = findViewById(R.id.nextMonth)
        txtSelectedMonth = findViewById(R.id.txtSelectedMonth)
        sendEmail = findViewById(R.id.btSendEmail)

    }

    private fun updateDateWorktimeToLog(){
        updateDayValue()
        updateMonthValue()
        updateYearValue()
        updateDateSelectedTextValue()
        updateMonthYearLabel()
        loadLoggedWorktime()
    }

    /**
     * Set text view with month and year values
     */
    private fun updateMonthYearLabel(){
        txtSelectedMonth!!.text = getString(R.string.month_year_label, monthSelectedDisplayText, yearSelectedText)
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
        monthSelectedText = monthDatabaseFormat.format(cal.time)
        monthSelectedDisplayText = monthSimpleDateFormat.format(cal.time)
        monthSelected = cal.get(Calendar.MONTH)
    }

    /**
     * Get the year's value
     */
    private fun updateYearValue(){
        yearSelectedText = cal.get(Calendar.YEAR).toString()
        yearSelected = cal.get(Calendar.YEAR)
    }

    /**
     * Get date selected in format
     */
    private fun updateDateSelectedTextValue(){
        dateSelectedText =
                ""+cal.get(Calendar.DAY_OF_MONTH)+
                "-"+cal.get(Calendar.MONTH + 1)+
                "-"+cal.get(Calendar.YEAR)
    }

    private fun handleClicks() {

        txtSelectedMonth?.setOnClickListener({
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)

                updateDateWorktimeToLog()

                updateMonthYearLabel()

                loadLoggedWorktime()
            }, yearSelected, monthSelected, daySelected) .show()

        })

        previousMonth!!.setOnClickListener({
            cal.add(Calendar.MONTH, -1)
            updateDateWorktimeToLog()
        })

        nextMonth!!.setOnClickListener({
            cal.add(Calendar.MONTH, 1)
            updateDateWorktimeToLog()
        })

        sendEmail!!.setOnClickListener ({
            val intent = Intent(Intent.ACTION_SEND)

            intent.type = "text/html"

            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(currentUser!!.email!!))

            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_email_subject_label, getString(R.string.app_name), monthSelectedText))

            intent.putExtra(Intent.EXTRA_TEXT, buildBodyEmailLoggedWorkTimeSheet())

            startActivity(intent)
        })
    }

    private fun buildBodyEmailLoggedWorkTimeSheet() : String{

        val stringBuilder = StringBuilder()

        stringBuilder.appendln("-----------------------------------")
        stringBuilder.appendln(monthSelectedText)
        stringBuilder.appendln("-----------------------------------")

        for(loggedWorkTime in listOfWorktime){
            stringBuilder.appendln("")
            stringBuilder.appendln("\t| " + loggedWorkTime.date + "\t| ")
            stringBuilder.append("\t| " + isEmptyOrNull(loggedWorkTime.beginWorktime) + "\t| ")
            stringBuilder.append(isEmptyOrNull(loggedWorkTime.beginLunch) + "\t| ")
            stringBuilder.append(isEmptyOrNull(loggedWorkTime.doneLunch) + "\t| ")
            stringBuilder.append(isEmptyOrNull(loggedWorkTime.doneWorktime) + "\t|\n")
        }

        return stringBuilder.toString()
    }

    private fun isEmptyOrNull(data: String?) : String{
        return if(data.equals("") || data == null){
            "     N/A     "
        } else{
            data
        }
    }

    private fun loadLoggedWorktime(){

        mLogWorktimeRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                print("Nothing here")
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                listOfWorktime.clear()
                mRecyclerView!!.invalidate()
                mAdapter!!.notifyDataSetChanged()

                dataSnapshot
                        ?.child(currentUser!!.uid)
                        ?.child(currentUser!!.displayName.toString())
                        ?.child(yearSelectedText.toString())
                        ?.child(monthSelectedText.toString())
                        ?.children
                        ?.mapNotNullTo(listOfWorktime){
                            it.getValue<Worktime>(Worktime::class.java)
                        }

                listOfWorktime.sortWith(compareBy(Worktime::id))
            }

        })
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
                logout()
            }

            R.id.action_log_worktime ->{
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Logout function
     */
    private fun logout(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener({
                    println("I am out")
                })
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    private fun getCurrentLocale() : Locale{
        return Locale.getDefault()
    }
}