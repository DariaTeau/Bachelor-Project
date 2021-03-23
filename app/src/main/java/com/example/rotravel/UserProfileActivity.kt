package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class UserProfileActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient : GoogleSignInClient
    lateinit var fireAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("318758695349-3ntogdpicl027jop4dqtb05jd2jvgd1v.apps.googleusercontent.com")
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fireAuth = Firebase.auth

    }

    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_profile_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.it_signOut -> {
                if(fireAuth.currentUser != null) {
                    fireAuth.signOut()
                    backToLogin()
                }
                mGoogleSignInClient.signOut().addOnCompleteListener {
                    backToLogin()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun backToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {}
        startActivity(intent)
    }

}