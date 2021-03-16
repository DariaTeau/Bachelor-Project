package com.example.rotravel

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var user : EditText
    private lateinit var password : EditText
    private lateinit var loginButton : Button
    private lateinit var registerButton : Button

    private var usersMap : HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        user = findViewById(R.id.etUsername)
        password = findViewById(R.id.etPassword)
        loginButton = findViewById(R.id.btLogin)
        registerButton = findViewById(R.id.btRegister)

        usersMap["ana"] = "1234"

        //set on click listener pe buton
        // in functia aia fac verificarile ca userul si parola sunt bune
        // nu am DB momentan asa ca o sa pun niste useri si parole intr-un hashmap
        // daca nu sunt bune display a toast, altfel navigam la activity-ul cu harta
        // adaug jos un text daca esti nou da register -> si bag userul in map <=> inserez in DB

        loginButton.setOnClickListener{ login() }
        registerButton.setOnClickListener { register() }

    }

    private fun login() {
        var inputUser = user.text.toString()
        var inputPassword = password.text.toString()

        if(!usersMap.containsKey(inputUser)) {
            Toast.makeText(this, "You have to register first", Toast.LENGTH_SHORT).show()
            return
        }

        if( usersMap.containsKey(inputUser) && usersMap[inputUser] != inputPassword) {
            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Successful Login", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MapActivity::class.java).apply {}
        startActivity(intent)
    }

    private fun register() {
        var inputUser = user.text.toString()
        var inputPassword = password.text.toString()

        if(usersMap.containsKey(inputUser)) {
            Toast.makeText(this, "This username already exists", Toast.LENGTH_SHORT).show()
            return
        }

        usersMap[inputUser] = inputPassword

        Toast.makeText(this, "Successful Registration", Toast.LENGTH_SHORT).show()
    }


}