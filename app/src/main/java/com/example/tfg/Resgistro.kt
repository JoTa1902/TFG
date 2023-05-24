package com.example.tfg

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.example.tfg.databinding.ActivityInicioBinding
import com.example.tfg.databinding.ActivityResgistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Resgistro : AppCompatActivity() {
    private lateinit var binding: ActivityResgistroBinding
    //guardar datos de registro


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


    //creacion de perfil de usuario mediante firebase
    private fun setup(){
        title = "Crear Cuenta"

        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().getReference("users")

        binding.singup.setOnClickListener(){
            val email = binding.email.text.toString()
            val password = binding.pass.text.toString()
            val Id = binding.user.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(){
                    if(it.isSuccessful){ // Obtén la instancia del usuario actualmente autenticado
                        val currentUser = FirebaseAuth.getInstance().currentUser

                        // Obtén la referencia a la ubicación deseada en la base de datos
                        val userRef = FirebaseDatabase.getInstance("https://jey-emulators-default-rtdb.europe-west1.firebasedatabase.app/").reference.child("users").child(currentUser?.uid.toString())

                        // Guarda los datos del usuario en la base de datos
                        userRef.child("Id").setValue(Id)
                        userRef.child("email").setValue(email)
                        userRef.child("password").setValue(password)

                        showHome(it.result?.user?.email ?: "")
                    }
                    else{
                        showAlert()
                    }
                }
            }
        }
    }

    //acceder a la pantalla principal
    private fun showHome(email: String) {
        val homeIntent: Intent = Intent(this, Principal::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)

    }

    //generar alerta si el usuario no se ha podido crear
    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()

    }
}