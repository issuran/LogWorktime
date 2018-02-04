package br.com.optimizer7.logworktime;

import android.app.Activity
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import com.google.firebase.auth.FirebaseUser
import com.firebase.ui.auth.IdpResponse
import android.content.Intent

/**
 * Created by Tiago on 27/01/2018.
 */
class LoginActivity : AppCompatActivity(){

    private val RC_SIGN_IN = 2
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(applicationContext)
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // already signed in
            val user = FirebaseAuth.getInstance().currentUser
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
                    RC_SIGN_IN);
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                val intent = Intent(this, LogTimeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }
}