package com.example.pantrypal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etReenterPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnBackToLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize UI components
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etReenterPassword = findViewById(R.id.etReenterPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)

        // Sign up button click handler
        btnSignUp.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val reenteredPassword = etReenterPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || reenteredPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (password != reenteredPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Proceed with sign-up logic (e.g., storing user info, API call, etc.)
                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()

                // After sign up, navigate to Login Activity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Finish SignUpActivity so it doesn't stay in the back stack
            }
        }

        // Back to Login button click handler
        btnBackToLogin.setOnClickListener {
            // Navigate back to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Finish SignUpActivity
        }
    }
}
