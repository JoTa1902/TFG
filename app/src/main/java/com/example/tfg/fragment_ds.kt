package com.example.tfg

import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class fragment_ds : Fragment() {

    private lateinit var gameRecyclerView: RecyclerView
    private lateinit var adapter: GameListAdapter_2
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ds, container, false)
        gameRecyclerView = view.findViewById(R.id.recyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar la base de datos
        database = FirebaseDatabase.getInstance().reference.child("games")

        // Obtener la lista de archivos de juegos con extensión .gba en la carpeta de descargas
        val downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val gameFileList = downloadsDirectory.listFiles { _, name -> name.endsWith(".nds") }

        // Configurar el RecyclerView y el adaptador
        adapter = GameListAdapter_2(requireContext(), emptyList())
        gameRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        gameRecyclerView.adapter = adapter

        // Verificar si se encontraron archivos de juegos en la carpeta de descargas
        if (gameFileList.isNotEmpty()) {
            val gameNames = gameFileList.map { file -> file.nameWithoutExtension }

            // Escuchar los cambios en los juegos de la base de datos
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val games = mutableListOf<Games>()
                    for (gameSnapshot in dataSnapshot.children) {
                        val game = gameSnapshot.getValue(Games::class.java)
                        game?.let {
                            if (gameNames.contains(game.name)) {
                                games.add(it)
                            }
                        }
                    }

                    // Actualizar la lista de juegos en el adaptador
                    adapter.updateGameList(games)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error, si es necesario
                }
            })
        }
    }



}