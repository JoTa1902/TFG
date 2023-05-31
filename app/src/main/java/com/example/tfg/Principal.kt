package com.example.tfg

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

@Suppress("DEPRECATION")
class Principal : AppCompatActivity(){
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        val fragmentStart = fragment_start()
        supportFragmentManager.beginTransaction()
            .add(R.id.contenedor_fragments, fragmentStart, "fragment_start")
            .commit()

        val usuario = findViewById<ImageView>(R.id.UserImageView)

        usuario.setOnClickListener(){
            val fragmentUser = fragment_user()
            supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragments, fragmentUser, "fragment_user")
                .addToBackStack(null)
                .commit()
        }

        val arrow = findViewById<ImageView>(R.id.arrow)

        //si el contenedor de fragments es distinto a fragment_start, arrowBack es visible
        supportFragmentManager.addOnBackStackChangedListener {
            if(supportFragmentManager.findFragmentById(R.id.contenedor_fragments) != fragmentStart){
                arrow.visibility = ImageView.VISIBLE
            }
            else{
                arrow.visibility = ImageView.GONE
            }
        }

        arrow.setOnClickListener(){
            //si el fragmento es distinto de fragment_start() vuelve al fragment anterior
            if(supportFragmentManager.findFragmentById(R.id.contenedor_fragments) != fragmentStart){
                supportFragmentManager.popBackStack()
            }
        }

    }

}