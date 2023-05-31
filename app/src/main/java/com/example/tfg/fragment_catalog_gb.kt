@file:Suppress("DEPRECATION")

package com.example.tfg

import GameListAdapter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class fragment_catalog_gb : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var gameListAdapter: GameListAdapter
    private lateinit var gameList: MutableList<Games>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_catalog_gb, container, false)

        // Inicializar la base de datos
        database = FirebaseDatabase.getInstance().reference

        // Inicializar la lista de juegos
        gameList = mutableListOf()

        // Configurar el RecyclerView y el adaptador
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        gameListAdapter = GameListAdapter(requireContext(), gameList)
        recyclerView.adapter = gameListAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Escuchar los cambios en la base de datos para juegos de plataforma "GBA"
        val juegosRef = database.child("games")
        juegosRef.orderByChild("platform").equalTo("GBA").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gameList.clear()
                for (gameSnapshot in dataSnapshot.children) {
                    val gameId = gameSnapshot.key // Obtener el identificador del juego
                    val juego = gameSnapshot.getValue(Games::class.java)
                    juego?.let {
                        it.id = gameId // Asignar el identificador al objeto de juego
                        gameList.add(it)
                    }
                }
                gameListAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar el error, si es necesario
            }
        })

        return view
    }
}




