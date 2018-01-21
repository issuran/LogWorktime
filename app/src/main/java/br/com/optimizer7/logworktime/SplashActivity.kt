package br.com.optimizer7.logworktime

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity



/**
 * Created by Tiago on 28/12/2017.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, LogTimeActivity::class.java)
        startActivity(intent)
        Thread.sleep(1000)
        finish()
    }
}