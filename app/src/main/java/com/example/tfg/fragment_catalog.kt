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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import java.io.File

class fragment_catalog : Fragment() {


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_catalog, container, false)

        val pacman = view.findViewById<LinearLayout>(R.id.pacman)

        pacman.setOnClickListener {
            download(requireContext(), "https://server.emulatorgames.net/roms/gameboy-advance/Pac-Man%20Collection%20(U)%20[!].zip", "Pac-Man")
        }



        return view
    }

    fun download(context: Context, downloadUrl: String, appName: String) {
        // Crear un cuadro de diálogo de confirmación para descargar la aplicación
        AlertDialog.Builder(context)
            .setTitle("Descargar juego")
            .setMessage("¿Estás seguro que deseas descargar e instalar $appName?")
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
                                    // Instalar la aplicación
                                    val localUri = cursor.getString(cursor.getColumnIndex(
                                        DownloadManager.COLUMN_LOCAL_URI))
                                    if (localUri != null) {
                                        val file = File(Uri.parse(localUri).path.toString())
                                        if (file.exists() && file.length() == cursor.getLong(cursor.getColumnIndex(
                                                DownloadManager.COLUMN_TOTAL_SIZE_BYTES))) {
                                            val apkUri = FileProvider.getUriForFile(context!!, "${context.packageName}.provider", file)
                                            val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                                                setDataAndType(apkUri, "application/vnd.android.package-archive")
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(installIntent)
                                        }
                                    }
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

