package com.example.firstapp.activities.profile

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.firstapp.activities.MainActivity
import com.example.firstapp.activities.SignupActivity
import com.example.firstapp.activities.items.CardWatchActivity
import com.example.firstapp.databinding.ActivityPreferedUserMoviesBinding
import com.example.firstapp.models.Movie
import com.example.firstapp.models.Tag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PreferredUserMoviesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferedUserMoviesBinding
    private lateinit var firebaseAuth: FirebaseAuth

    val movieList = mutableListOf<Movie>()
    val movieNamesList = mutableListOf<String>()
    val userMovieNamesList = mutableListOf<String>()
    val searchMovieNameList = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPreferedUserMoviesBinding.inflate(layoutInflater)
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

        binding.mainPageButton.setOnClickListener {
            startActivity(Intent(this@PreferredUserMoviesActivity, MainActivity::class.java))
        }

        val dbMoviesRef = FirebaseDatabase.getInstance().getReference("movies")

        val dbRef = FirebaseDatabase.getInstance().getReference("prefs/${currentUser!!.uid}")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    val dataSnapshotValue = snapshot.value
                    if (dataSnapshotValue is List<*>) {
                        for (item in dataSnapshotValue) {
                            if (item is String) {
                                userMovieNamesList.add(item)
                            }
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибки чтения из базы данных
            }
        })



        dbMoviesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (snapshot.exists()) {
                        val movieMap = snapshot.value as? Map<*, *>
                        if (movieMap != null) {
                            val name = movieMap["name"] as? String
                            val year = movieMap["year"] as? String
                            val author = movieMap["director"] as? String
                            val tags = movieMap["tags"] as? List<*>

                            if (name != null && author != null && tags != null && year!= null) {
                                val tagList = tags.mapNotNull { tagString ->
                                    Tag.entries.find { it.name == tagString }
                                }

                                val movie = Movie(name, year, author, tagList)
                                //movieList[movie.name] = movie
                                movieList.add(movie)
                                movieNamesList.add(movie.name)
                                searchMovieNameList.add(movie.name)
                            }
                        }
                    }
                }

                val movieAdapter = ArrayAdapter<String>(
                    this@PreferredUserMoviesActivity,
                    android.R.layout.simple_list_item_1,
                    userMovieNamesList.toList()
                )
                binding.listview.adapter = movieAdapter
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })


        binding.listview.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedItem = parent.getItemAtPosition(position) as String
                startActivity(
                    Intent(
                        this@PreferredUserMoviesActivity,
                        CardWatchActivity::class.java
                    ).apply {
                        putExtra("movieName", selectedItem)
                        putExtra("backActivityName", "Preferred list ")
                        putExtra("action", "removing")
                        putExtra("backActivity", PreferredUserMoviesActivity::class.java)
                    })
            }

        binding.searchingStroke.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Не используется
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchString = s.toString()

                searchMovieNameList.clear()

                for (item in userMovieNamesList) {
                    for (movieName in movieNamesList) {
                        if (movieName.contains(searchString, ignoreCase = true) && movieName == item) {
                            searchMovieNameList.add(movieName)
                        }
                    }
                }

                val movieAdapter = ArrayAdapter<String>(
                    this@PreferredUserMoviesActivity,
                    android.R.layout.simple_list_item_1,
                    searchMovieNameList.toList()
                )
                binding.listview.adapter = movieAdapter
            }

            override fun afterTextChanged(s: Editable?) {
                //unused
            }
        })

        setContentView(binding.root)
    }
}