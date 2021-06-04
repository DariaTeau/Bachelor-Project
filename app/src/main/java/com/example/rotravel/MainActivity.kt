package com.example.rotravel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var user : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var registerButton : Button
    private lateinit var googleLoginButton : SignInButton
    private lateinit var fireAuth: FirebaseAuth
    lateinit var mGoogleSignInClient : GoogleSignInClient
    lateinit var requestPermissionLauncher : ActivityResultLauncher<String>


    private var usersMap : HashMap<String, String> = HashMap()

    private val reqCodeSignIn = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        user = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btLogin)
        registerButton = findViewById(R.id.btRegister)

        usersMap["ana"] = "1234"

        loginButton.setOnClickListener{ login() }
        registerButton.setOnClickListener { register() }
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Perm Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Perm NOT Granted", Toast.LENGTH_SHORT).show()
            }
        }
        checkPerm()
        GlobalScope.launch(Dispatchers.IO) {
            Log.i("MainActivity", "launch -> ${Thread.currentThread().name}")
            NearbyCommunication.doInit(this@MainActivity) }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("318758695349-3ntogdpicl027jop4dqtb05jd2jvgd1v.apps.googleusercontent.com")
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        var account : GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            goToMapActivity()
        } else {
            googleLoginButton = findViewById(R.id.sign_in_button)
            googleLoginButton.setOnClickListener{
                googleSignIn()
            }
        }

        fireAuth = Firebase.auth
        if(fireAuth.currentUser != null) {
            goToMapActivity()
        }

    }

    private fun checkPerm() {
        var isGranted = ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if(isGranted == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION)
        }

        isGranted = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if(isGranted == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        isGranted = ContextCompat.checkSelfPermission(this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if(isGranted == PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            Log.i("checkperm", "pot sa scriu")

        }

    }

    private fun login() {
        var inputUser = user.text.toString()
        var inputPassword = password.text.toString()

        if(inputUser == "ana" && inputPassword == "1234") {
            Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show()
            goToMapActivity()
        }

        fireAuth.signInWithEmailAndPassword(inputUser, inputPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = fireAuth.currentUser
                    Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show()
                    goToMapActivity()
                } else {
                    Toast.makeText(baseContext, "Wrong Email or Password",
                        Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun register() {
        val intent : Intent = Intent(this, RegisterActivity::class.java).apply {}
        startActivity(intent)
    }

    private fun googleSignIn() {
        var  signInIntent : Intent? = mGoogleSignInClient.signInIntent;
        startActivityForResult(signInIntent, reqCodeSignIn);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqCodeSignIn && data != null) {
            var  task : Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private fun handleSignInResult(completedTask : Task<GoogleSignInAccount>) {
        var  account : GoogleSignInAccount? = completedTask.result

        if(account != null) {
            // Signed in successfully, show authenticated UI.
            goToMapActivity()
        } else {
            Toast.makeText(this, "Failed to Sign In with Google", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToMapActivity() {
        val intent : Intent = Intent(this, MapActivity::class.java).apply {}
        startActivity(intent)
    }

}