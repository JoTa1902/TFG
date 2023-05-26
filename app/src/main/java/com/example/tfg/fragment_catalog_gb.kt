@file:Suppress("DEPRECATION")

package com.example.tfg

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class fragment_catalog_gb : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var gameListAdapter: GameListAdapter
    private lateinit var gameList: MutableList<Juegos>

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

        // Escuchar los cambios en la base de datos
        val juegosRef = database.child("Juegos")
        juegosRef.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                gameList.clear()
                for (snapshot in dataSnapshot.children) {
                    val juego = snapshot.getValue(Juegos::class.java)
                    juego?.let {
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

    fun download(context: Context, downloadUrl: String, appName: String) {
        // Crear un cuadro de diálogo de confirmación para descargar la aplicación
        AlertDialog.Builder(context)
            .setTitle("Descargar juego")
            .setMessage("¿Estás seguro que deseas descargar $appName?")
            .setPositiveButton("Descargar") { dialog, which ->
                // Preparar la descarga
                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                    setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    setTitle(appName)
                    setDescription("Descargando $appName")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$appName.apk")
                }

                // Descargar juego
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)

                // Mostrar un cuadro de diálogo con el progreso de la descarga
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Descargando $appName")
                progressDialog.setMessage("Espere por favor...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Registrar un BroadcastReceiver para saber cuando termina la descarga
                val onComplete = object : BroadcastReceiver() {
                    @SuppressLint("Range")
                    override fun onReceive(context: Context?, intent: Intent?) {
                        if (downloadId == intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                            // La descarga ha finalizado
                            progressDialog.dismiss()

                            // Verificar si la descarga ha sido exitosa
                            val downloadQuery = DownloadManager.Query().setFilterById(downloadId)
                            val cursor = downloadManager.query(downloadQuery)
                            if (cursor.moveToFirst()) {
                                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    Toast.makeText(context, "La descarga se ha completado", Toast.LENGTH_SHORT).show()
                                } else {
                                    // La descarga ha fallado, mostrar un mensaje de error
                                    Toast.makeText(context, "La descarga ha fallado. Por favor, inténtelo de nuevo más tarde.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}




