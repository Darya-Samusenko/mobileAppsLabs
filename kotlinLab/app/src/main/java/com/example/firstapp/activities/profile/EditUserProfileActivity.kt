package com.example.firstapp.activities.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.R
import com.example.firstapp.activities.SignupActivity
import com.example.firstapp.databinding.ActivityEditUserProfileBinding
import com.example.firstapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditUserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditUserProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditUserProfileBinding.inflate(layoutInflater)
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

        binding.backButton.setOnClickListener {
            startActivity(Intent(this@EditUserProfileActivity, UserProfileActivity::class.java))
        }

        val dbRef = FirebaseDatabase.getInstance().getReference("users/${currentUser!!.uid}")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userMap = snapshot.value as? Map<*, *>
                    if (userMap != null) {
                        val name = userMap["name"]
                        val email = userMap["email"]
                        val dateOfBirth = userMap["dateOfBirth"]
                        val gender = userMap["gender"]
                        val occupation = userMap["occupation"]
                        val city = userMap["city"]
                        val country = userMap["country"]
                        val phoneNumber = userMap["phoneNumber"]
                        val age = userMap["age"]
                        val status = userMap["status"]

                        binding.editName.setText(name.toString())
                        binding.textEmail.setText(email.toString())
                        binding.textDateOfBirth.setText(dateOfBirth.toString())
                        binding.textGender.setText(gender.toString())
                        binding.textOccupation.setText(occupation.toString())
                        binding.textCity.setText(city.toString())
                        binding.textCountry.setText(country.toString())
                        binding.textPhoneNumber.setText(phoneNumber.toString())
                        binding.textAge.setText(age.toString())
                        binding.textStatus.setText(status.toString())

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        setContentView(binding.root)

        binding.saveButton.setOnClickListener {
            val name = binding.editName.text.toString()
            val email = binding.textEmail.text.toString()
            val dateOfBirth = binding.textDateOfBirth.text.toString()
            val gender = binding.textGender.text.toString()
            val occupation = binding.textOccupation.text.toString()
            val city = binding.textCity.text.toString()
            val country = binding.textCountry.text.toString()
            val phoneNumber = binding.textPhoneNumber.text.toString()
            val age = binding.textAge.text.toString()
            val ageI = binding.textAge.text.toString().toInt()

            val status = binding.textStatus.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && dateOfBirth.isNotEmpty() && gender.isNotEmpty() &&
                occupation.isNotEmpty() && city.isNotEmpty() && country.isNotEmpty() && phoneNumber.isNotEmpty() &&
                age.isNotEmpty() && status.isNotEmpty()
            ) {
                dbRef.setValue(User(
                    name, email, dateOfBirth, gender, occupation, city, country, phoneNumber, ageI, status
                ))

                startActivity(Intent(this@EditUserProfileActivity, UserProfileActivity::class.java))
            } else {
                Toast.makeText(this, "Please fill all of the empty fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}