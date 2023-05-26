package com.example.tfg

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.tfg.databinding.ActivityInicioBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class Inicio : AppCompatActivity() {

    private val GOOGLE_SIGN_IN = 100

    private lateinit var binding: ActivityInicioBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            val intent = Intent(this, Registro::class.java)
            startActivity(intent)
        }

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configurar opciones de inicio de sesión con Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        // Verificar si el usuario ya ha iniciado sesión
        prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null && auth.currentUser != null) {
            val name = auth.currentUser?.displayName ?: ""
            showHome(email, name)
        }

        setup()
    }

    private fun setup() {
        title = "Iniciar Sesión"

        val auth = FirebaseAuth.getInstance()

        binding.login.setOnClickListener() {
            val email = binding.user.text.toString()
            val password = binding.pass.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener() {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", "")
                    } else {
                        showAlert()
                    }
                }
            }
        }

        binding.googleButton.setOnClickListener() {
            val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this, googleConf)
            googleClient.signOut()
            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    // Función para guardar los datos de inicio de sesión en SharedPreferences y en la base de datos
    private fun savePrefs(email: String, provider: String, name: String = "") {
        prefs.edit().apply {
            putString("email", email)
            putString("provider", provider)
            apply()
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        if (provider == "google") {
            val uid = auth.currentUser?.uid
            val userId = uid ?: ""
            val userRef = usersRef.child(userId)
            userRef.child("email").setValue(email)
            userRef.child("provider").setValue(provider)
            userRef.child("name").setValue(name)
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error al autenticar el usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, name: String) {
        val homeIntent = Intent(this, Principal::class.java).apply {
            putExtra("email", email)
            putExtra("name", name)
        }
        startActivity(homeIntent)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Resultado de inicio de sesión con Google
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            try {
                if (account != null) {
                    val name = account.displayName ?: ""
                    val email = account.email ?: ""

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener() {
                        if (it.isSuccessful) {
                            savePrefs(email, "google", name)
                            showHome(email, name)

                            // Aquí puedes agregar las declaraciones de registro
                            Log.d("GoogleSignIn", "ID: ${account.id}")
                            Log.d("GoogleSignIn", "Email: $email")
                            Log.d("GoogleSignIn", "Name: $name")
                        } else {
                            showAlert()
                        }
                    }
                }


            }
            catch (e: ApiException) {
                showAlert()
            }
        }
    }
}

