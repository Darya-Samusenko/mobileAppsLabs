package com.example.firstapp.activities.items

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.firstapp.R
import com.example.firstapp.activities.SignupActivity
import com.example.firstapp.activities.profile.PreferredUserMoviesActivity
import com.example.firstapp.databinding.ActivityCardWatchBinding
import com.example.firstapp.models.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference

//class ImagesAdapter(
//    val context: Context,
//    var
//)

class CardWatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCardWatchBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private val imageUrls = mutableListOf<String>()

    @SuppressLint("DiscouragedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCardWatchBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()

        val imageUrls = mutableListOf<String>()

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

        val movieName = intent.getStringExtra("movieName")
        val buttonScActionName = intent.getStringExtra("action")
        val shouldDelete = buttonScActionName.equals("removing")

        val dbRef = FirebaseDatabase.getInstance().getReference("movies/${movieName}")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val movieMap = snapshot.value as? Map<*, *>
                    if (movieMap != null) {
                        val name = movieMap["name"] as? String
                        val year = movieMap["year"] as? String
                        val director = movieMap["director"] as? String
                        val tags = movieMap["tags"] as? List<*>

                        if (name != null && director != null && tags != null && year != null) {
                            val tagList = tags.mapNotNull { tagString ->
                                Tag.entries.find { it.name == tagString }
                            }

                            binding.movieName.text = "Name: $name"
                            binding.movieDirector.text = "Director: $director"
                            binding.movieYear.text = "-$year-"



                            var str = ""
                            for (tag in tagList) {
                                str += "$tag "
                            }

                            binding.movieTags.text = "Genres: $str"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Oops! See you later on implementation")
            }
        })

        val buttonBackNameActivity = intent.getStringExtra("backActivityName")
        val callbackActivity = if (buttonBackNameActivity.equals("Search page")) {
            SearchItemActivity::class.java
        } else {
            PreferredUserMoviesActivity::class.java
        }

        val imagesRef = FirebaseStorage.getInstance().getReference("images").child(movieName!!)

        val imageViews = Array<ImageView>(7) {
            binding.image1
        }
        imageViews[0] = binding.image1
        imageViews[1] = binding.image2
        imageViews[2] = binding.image3
        imageViews[3] = binding.image4
        imageViews[4] = binding.image5
        imageViews[5] = binding.image6
        imageViews[6] = binding.image7

        imagesRef.listAll()
            .addOnSuccessListener { listResult: ListResult ->

                var i = 0;
                for (item: StorageReference in listResult.items) {
                    val fileName = item.name

                    val fileExtension = fileName.substringAfterLast(".", "")

                    if (fileExtension.equals("jpg", ignoreCase = true) ||
                        fileExtension.equals("jpeg", ignoreCase = true) ||
                        fileExtension.equals("png", ignoreCase = true)
                    ) {
                        item.downloadUrl.addOnSuccessListener { uri ->
                            val imageUrl = uri.toString()
                            imageUrls.add(imageUrl)
                            Glide.with(this).load(imageUrl).placeholder(R.drawable.bg)
                                .into(imageViews[i])
                            i += 1
                        }.addOnFailureListener { exception ->

                        }
                    }

                }
            }
            .addOnFailureListener { exception ->

            }

        binding.addToFavourites.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("prefs/${currentUser!!.uid}")

            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val favourites = mutableListOf<String>()

                    if (snapshot.exists()) {
                        val dataSnapshotValue = snapshot.value
                        if (dataSnapshotValue is List<*>) {
                            for (item in dataSnapshotValue) {
                                if (item is String) {
                                    favourites.add(item)
                                }
                            }
                        }
                    }

                    if (!favourites.contains(movieName)) {
                        favourites.add(movieName)
                        dbRef.setValue(favourites)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@CardWatchActivity,
                                    "All done",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this@CardWatchActivity,
                                    exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }else if (favourites.contains(movieName) && shouldDelete){
                        favourites.remove(movieName)
                        dbRef.setValue(favourites)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@CardWatchActivity,
                                    "Removed! ",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(
                                    this@CardWatchActivity,
                                    exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        startActivity(Intent(this@CardWatchActivity, PreferredUserMoviesActivity::class.java))
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

        binding.searchPageButton.text = buttonBackNameActivity
        binding.addToFavourites.text = buttonScActionName

        binding.searchPageButton.setOnClickListener {
            startActivity(Intent(this@CardWatchActivity, callbackActivity))
        }
        setContentView(binding.root)

    }
}