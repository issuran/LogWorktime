package br.com.optimizer7.logworktime;

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.*

/**
 * Created by Tiago on 27/01/2018.
 */
class LoginActivity : AppCompatActivity(){

    private val RC_SIGN_IN = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // already signed in
            val intent = Intent(this, LogTimeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                            Arrays.asList(
                                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                    AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                    .setIsSmartLockEnabled(false)
                    .setLogo(R.mipmap.ic_launcher)
                    .setTheme(R.style.AppTheme)
                    .build(),
                    RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val intent = Intent(this, LogTimeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Sign in failed, check response for error code
            }
        }
    }
}