package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView

class UserProfileActivity : AppCompatActivity() {
    private  lateinit var bt : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        bt = findViewById(R.id.button)
    }

    override fun onCreateOptionsMenu(menu: Menu) : Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.user_profile_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.it_signOut -> {
                MainActivity.mGoogleSignInClient.signOut().addOnCompleteListener {
                    backToLogin()
                }
//                val intent = Intent(this, UploadPhotoActivity::class.java).apply {}
//                startActivity(intent)
                true
            }
//            R.id.help -> {
//                showHelp()
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
        //return true
    }

    private fun backToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {}
        startActivity(intent)
    }

}