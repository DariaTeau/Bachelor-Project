package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var user : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var registerButton : Button
    private lateinit var googleLoginButton : SignInButton
    private lateinit var fireAuth: FirebaseAuth
    lateinit var mGoogleSignInClient : GoogleSignInClient


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

    private fun login() {
        var inputUser = user.text.toString()
        var inputPassword = password.text.toString()

//        if(!usersMap.containsKey(inputUser)) {
//            Toast.makeText(this, "You have to register first", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        if( usersMap.containsKey(inputUser) && usersMap[inputUser] != inputPassword) {
//            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
//            return
//        }

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
//        var inputUser = user.text.toString()
//        var inputPassword = password.text.toString()
//
//        if(usersMap.containsKey(inputUser)) {
//            Toast.makeText(this, "This username already exists", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        usersMap[inputUser] = inputPassword
//
//        Toast.makeText(this, "Successful Registration", Toast.LENGTH_SHORT).show()
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