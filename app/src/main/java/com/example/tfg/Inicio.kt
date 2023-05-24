package com.example.tfg


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.tfg.databinding.ActivityInicioBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Inicio : AppCompatActivity() {

    private lateinit var binding: ActivityInicioBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.singup.setOnClickListener(){
            val intent = Intent(this, Resgistro::class.java)
            startActivity(intent)
        }

        setup()
        sesseion()

    }
    private fun sesseion(){
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if(email != null && provider != null){
            showHome(email)
        }
    }
    private fun setup(){
        title = "Acceder"
        binding.login.setOnClickListener(){
            val email = binding.user.text.toString()
            val password = binding.pass.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(){
                    if(it.isSuccessful){
                        showHome(it.result?.user?.email ?: "")

                    }
                    else{
                        showAlert()
                    }
                }
            }
        }
    }

    // Función para mostrar un mensaje de error
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Función para mostrar la pantalla principal
    private fun showHome(email: String) {
        val homeIntent = Intent(this, Principal::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }
}





