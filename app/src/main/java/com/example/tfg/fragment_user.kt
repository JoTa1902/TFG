package com.example.tfg

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.tfg.databinding.FragmentUserBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class fragment_user : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        val currentUser: FirebaseUser? = auth.currentUser
        val userId: String? = currentUser?.uid

        if (userId != null) {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val id = user?.name
                    val email = user?.email

                    // Actualiza los TextView con el ID y el email obtenidos
                    binding.id.text = id
                    binding.emailT.text = email
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error al obtener los datos del usuario", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.LogOutButton.setOnClickListener {
            logout()
        }

        binding.deleteAccount.setOnClickListener {
            deleteAccount()
            logout()
        }
    }

    private fun logout() {
        if (isAdded) {
            auth.signOut()
            clearPrefs()
            val intent = Intent(requireContext(), Inicio::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
    }



    private fun clearPrefs() {
        prefs.edit().clear().apply()
    }

    private fun deleteAccount() {
        val currentUser: FirebaseUser? = auth.currentUser

        currentUser?.delete()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Eliminación de la cuenta exitosa
                // Eliminar también los datos del usuario de la base de datos de Firebase Realtime Database
                val userId: String? = currentUser.uid
                if (userId != null) {
                    val usersRef = FirebaseDatabase.getInstance().getReference("users")
                    val userRef = usersRef.child(userId)

                    // Eliminar los datos del usuario de la base de datos
                    userRef.removeValue()
                }

                // Si es una cuenta de Google, también se revoca el acceso
                val providerId = currentUser.providerId
                if (providerId == GoogleAuthProvider.PROVIDER_ID) {
                    val googleSignInClient = GoogleSignIn.getClient(requireContext(),
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build())
                    googleSignInClient.revokeAccess()
                }

                logout()
            } else {
                Toast.makeText(requireContext(), "Error al eliminar la cuenta", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
