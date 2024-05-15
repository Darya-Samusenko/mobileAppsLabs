package com.example.firstapp.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.databinding.ActivitySignupBinding
import com.example.firstapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.loginRedirectText.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.signupButton.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPass = binding.repSignupPassword.text.toString()
            val name = binding.signupName.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPass.isNotEmpty() && name.isNotEmpty()) {
                if (password == confirmPass) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                //adding new user
                                var db = FirebaseDatabase.getInstance().getReference("users")

                                val user = User(
                                    name,
                                    email,
                                    "",
                                    "",
                                    "",
                                    "Minsk",
                                    "Belarus",
                                    "",
                                    10,
                                    "no information",
                                )

                                db.child(firebaseAuth.currentUser!!.uid).setValue(user)

                                db = FirebaseDatabase.getInstance().getReference("prefs")
                                db.child(firebaseAuth.currentUser!!.uid).setValue(mutableListOf<String>())

                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill up all the empty fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}