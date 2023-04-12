package com.example.tfg


import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tfg.databinding.ActivityInicioBinding
import com.google.firebase.auth.FirebaseAuth

class Inicio : AppCompatActivity() {

    private lateinit var binding: ActivityInicioBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val intent = Intent(this, Principal::class.java)
            startActivity(intent)
        }

    }
}





