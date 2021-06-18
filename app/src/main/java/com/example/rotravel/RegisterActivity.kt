package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.bumptech.glide.annotation.GlideModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var fireAuth : FirebaseAuth
    private lateinit var dbRef : DatabaseReference
    private lateinit var registerBt : Button
    private lateinit var userName : EditText
    private lateinit var userEmail : EditText
    private lateinit var userPass : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        fireAuth = Firebase.auth

        registerBt = findViewById(R.id.btReg)
        userName = findViewById(R.id.etUser)
        userEmail = findViewById(R.id.etEmail)
        userPass = findViewById(R.id.etPass)

        registerBt.setOnClickListener {
            validate()
            createUser()
        }

    }

    private fun validate() {
        var inputUser = userName.text.toString().trim()
        var inputEmail = userEmail.text.toString().trim()
        var inputPassword = userPass.text.toString().trim()

        if(inputUser.isEmpty() || inputEmail.isEmpty() || inputPassword.isEmpty()) {
            Toast.makeText(this, "Fill in all the slots", Toast.LENGTH_SHORT).show()
        }

        //verificat unicitate somehow

    }

    private fun createUser() {
        fireAuth.createUserWithEmailAndPassword(userEmail.text.toString().trim(), userPass.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addInfoToDB()
                    Toast.makeText(baseContext, "Successful registration.",
                        Toast.LENGTH_SHORT).show()
                    backToLogin()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("createUser", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun backToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {}
        startActivity(intent)
    }

    private fun addInfoToDB() {
        dbRef = Firebase.database("https://rotravel-14ed2-default-rtdb.europe-west1.firebasedatabase.app/").reference
        val info = UserInfo(userName.text.toString().trim(), userEmail.text.toString().trim())
        val ref = dbRef.child("Users").child(fireAuth.currentUser.uid).push()
        dbRef.child("Users").child(fireAuth.currentUser.uid).setValue(info)
        //ref.setValue(info)
        //dbRef.push()

    }



}