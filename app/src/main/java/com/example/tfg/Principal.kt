package com.example.tfg

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Principal : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        val fragmentStart = fragment_start()
        supportFragmentManager.beginTransaction()
            .add(R.id.contenedor_fragments, fragmentStart, "fragment_start")
            .commit()
    }
}