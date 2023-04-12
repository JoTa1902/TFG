package com.example.tfg

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.File


@Suppress("DEPRECATION")
class fragment_store : Fragment() {

    private val PERMISSION_REQUEST_CODE = 1
    private val ipServer = "192.168.56.104"

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        val gameboy = view.findViewById<ImageButton>(R.id.gameboy)
        val nds = view.findViewById<ImageButton>(R.id.nds)
        val psp = view.findViewById<ImageButton>(R.id.psp)

        gameboy.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showWriteStoragePermissionDialog(this)
            } else {
                downloadAndInstall(requireContext(), "https://file.hapmod.com/uploads/My-Boy-Pro-1.8.0-premium-CoiMobile.Com.com.apk", "MyBoy")
            }
        }

        nds.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showWriteStoragePermissionDialog(this)
            } else {
                downloadAndInstall(requireContext(), "https://file.hapmod.com/uploads/DraStic-r2.5.2.2a-CoiMobile.Com.apk", "Drastic")
            }
        }

        psp.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showWriteStoragePermissionDialog(this)
            } else {
                downloadAndInstall(requireContext(), "https://file.hapmod.com/uploads/PPSSPP-Gold-v1.13.1-CoiMobile.com.apk", "PPSSPP")
            }
        }

        return view
    }



    fun downloadAndInstall(context: Context, downloadUrl: String, appName: String) {
        // Crear un cuadro de diálogo de confirmación para descargar la aplicación
        AlertDialog.Builder(context)
            .setTitle("Descargar aplicación")
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

                // Descargar la aplicación
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
                                    val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                                    if (localUri != null) {
                                        val file = File(Uri.parse(localUri).path.toString())
                                        if (file.exists() && file.length() == cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))) {
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

    private fun isAppInstalled(packageName: String): Boolean {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            packageInfo != null
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }









    //funciones para comprobar y aceptar permisos de escritura y fuentes desconocidas en la app
    fun requestInstallUnknownSourcesPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.packageManager.canRequestPackageInstalls()) {
            // Si los permisos de fuentes desconocidas no están activados, muestre una ventana emergente con los botones "SETTINGS" y "CANCEL"
            AlertDialog.Builder(activity)
                .setTitle("Permisos de fuentes desconocidas")
                .setMessage("Se requieren permisos de fuentes desconocidas para continuar.")
                .setPositiveButton("SETTINGS") { _, _ ->
                    // Si el usuario presiona "SETTINGS", abra la configuración de seguridad para que pueda activar los permisos de fuentes desconocidas
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivityForResult(intent, 2)
                }
                .setNegativeButton("CANCEL", null)
                .show()
        } else {
            // Si los permisos de fuentes desconocidas ya están activados, continuar con la lógica de tu aplicación
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario otorga los permisos de escritura, solicite permisos de instalación de fuentes desconocidas
                requestInstallUnknownSourcesPermission(requireContext() as Activity)
            } else {
                // Si el usuario no otorga los permisos de escritura, muestre un mensaje de error
                Toast.makeText(requireContext(), "Los permisos son necesarios para continuar", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario otorga los permisos de instalación de fuentes desconocidas, continue con la lógica de su aplicación
            } else {
                // Si el usuario no otorga los permisos de instalación de fuentes desconocidas, muestre un mensaje de error
                Toast.makeText(requireContext(), "Los permisos son necesarios para continuar", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @SuppressLint("NewApi")
    fun showWriteStoragePermissionDialog(fragment: Fragment) {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos no están activados, muestre una ventana emergente con los botones "ALLOW" y "CANCEL"
            AlertDialog.Builder(fragment.requireContext())
                .setTitle("Permisos de escritura")
                .setMessage("Se requieren permisos de escritura para continuar.")
                .setPositiveButton("ALLOW") { _, _ ->
                    // Si el usuario presiona "ALLOW", solicite los permisos
                    fragment.requestPermissions(arrayOf(permission), 1)
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }


}

