package com.example.firstapp.activities.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.R
import com.example.firstapp.activities.LoginActivity
import com.example.firstapp.activities.MainActivity
import com.example.firstapp.activities.SignupActivity
import com.example.firstapp.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
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


        val dbRef = FirebaseDatabase.getInstance().getReference("users/${currentUser!!.uid}")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
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


                        binding.textName.text = getString(R.string.name) + name
                        binding.textEmail.text = getString(R.string.email) + email
                        binding.textDateOfBirth.text =
                            getString(R.string.birth_date) + dateOfBirth
                        binding.textGender.text =
                            getString(R.string.gender) + gender
                        binding.textOccupation.text =
                            getString(R.string.occupation) + occupation
                        binding.textCity.text = getString(R.string.city) + city
                        binding.textCountry.text =
                            getString(R.string.country) + country
                        binding.textPhoneNumber.text =
                            getString(R.string.phonenumber) + phoneNumber
                        binding.textAge.text = getString(R.string.age) + age
                        binding.textStatus.text =
                            "Status:$status"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        binding.backButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.deleteProfile.setOnClickListener {
            var dbToRemoveRef = FirebaseDatabase.getInstance().getReference("prefs/${currentUser.uid}")
            dbToRemoveRef.removeValue()

            dbToRemoveRef = FirebaseDatabase.getInstance().getReference("users/${currentUser.uid}")

            dbToRemoveRef.removeValue()

            val user = FirebaseAuth.getInstance().currentUser

            FirebaseAuth.getInstance().signOut()


            user?.delete()
                ?.addOnSuccessListener {
                    Toast.makeText(this, "All done, see you later", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                ?.addOnFailureListener { exception ->
                    Toast.makeText(this, "error of deleting", Toast.LENGTH_SHORT).show()
                }

            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.editButton.setOnClickListener {
            startActivity(Intent(this, EditUserProfileActivity::class.java))
        }

        setContentView(binding.root)
    }

}