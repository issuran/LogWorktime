package br.com.optimizer7.logworktime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task

class ListLoggedWorktime : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.list_logged_worktime_activity)

        val myToolbar = findViewById<View>(R.id.toolbar_list_logged_worktime) as Toolbar
        setSupportActionBar(myToolbar)

        myToolbar.inflateMenu(R.menu.list_main_menu)
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