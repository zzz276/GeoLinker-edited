package com.internetofthings.displaycontroller

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize SessionManager
        sessionManager = SessionManager(this)
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is logged in, navigate directly to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()

            return
        }
        
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // Validate inputs
            when {
                email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(
                        this@MainActivity,
                        "Please enter both email and password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !isValidEmail(email) -> {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.invalid_email,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !isValidPassword(password) -> {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.invalid_password,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    // Save login state
                    sessionManager.saveLoginState(email)
                    
                    // Navigate to HomeActivity
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                }
            }
        }
    }
    
    /**
     * Validates email format: must contain "@" and at least one "."
     */

    private fun isValidEmail(email: String): Boolean { return email.contains("@") && email.contains(".") }

    /**
     * Validates password format: must contain at least eight characters
     */

    private fun isValidPassword(password: String): Boolean { return password.length >= 8 }
}

