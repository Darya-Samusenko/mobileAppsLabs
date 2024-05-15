package com.example.firstapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.activities.items.SearchItemActivity
import com.example.firstapp.activities.profile.PreferredUserMoviesActivity
import com.example.firstapp.activities.profile.UserProfileActivity
import com.example.firstapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updatedUser = firebaseAuth.currentUser
                    if (updatedUser == null) {
                        startActivity(Intent(this, SignupActivity::class.java))
                    }
                } else {
                    startActivity(Intent(this, SignupActivity::class.java))
                }
            }
        } else {
         startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.profileButton.setOnClickListener{
            startActivity(Intent(this, UserProfileActivity::class.java))
        }

        binding.searchButton.setOnClickListener{
            startActivity(Intent(this, SearchItemActivity::class.java))
        }

        binding.favouritesButton.setOnClickListener{
            startActivity(Intent(this, PreferredUserMoviesActivity::class.java))
        }

        setContentView(binding.root)
    }
}