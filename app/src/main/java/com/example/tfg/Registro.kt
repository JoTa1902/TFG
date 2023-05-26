package com.example.tfg

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.tfg.databinding.ActivityResgistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Registro : AppCompatActivity() {
    private lateinit var binding: ActivityResgistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResgistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        title = "Crear Cuenta"

        val auth = FirebaseAuth.getInstance()

        binding.signup.setOnClickListener(){
            val email = binding.email.text.toString()
            val password = binding.pass.text.toString()
            val provider = "basic"
            val Id = binding.user.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(){
                    if(it.isSuccessful){
                        val currentUser = auth.currentUser

                        val database = FirebaseDatabase.getInstance()

                        val userRef = database.reference.child("users").child(currentUser?.uid.toString())

                        userRef.child("email").setValue(email)
                        userRef.child("name").setValue(Id)
                        userRef.child("provider").setValue(provider)

                        showHome(it.result?.user?.email ?: "")
                    }
                    else{
                        showAlert()
                    }
                }
            }
        }
    }

    private fun showHome(email: String) {
        val homeIntent: Intent = Intent(this, Principal::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
