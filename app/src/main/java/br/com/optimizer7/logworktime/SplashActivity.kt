package br.com.optimizer7.logworktime

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase


/**
 * Created by Tiago on 28/12/2017.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        Thread.sleep(1000)
        finish()
    }
}