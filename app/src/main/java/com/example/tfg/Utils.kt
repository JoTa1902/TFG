@file:Suppress("DEPRECATION")

package com.example.tfg

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream


object Utils {

    //Función para comprobar si una app está instalada
    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val installedPackages = packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)

        for (packageInfo in installedPackages) {
            if (packageInfo.packageName == packageName) {
                return true
            }
        }

        return false
    }

    //Función para comprobar y aceptar permisos de fuentes desconocidas en la ap
    fun requestInstallUnknownSourcesPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !activity.packageManager.canRequestPackageInstalls()) {
            // Si los permisos de fuentes desconocidas no están activados, muestre una ventana emergente
            AlertDialog.Builder(activity)
                .setTitle("Permisos de fuentes desconocidas")
                .setMessage("Se requieren permisos de fuentes desconocidas para continuar.")
                .setPositiveButton("SETTINGS") { _, _ ->
                    // Si el usuario presiona "SETTINGS", abre la configuración para activar los permisos de fuentes desconocidas
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                    intent.data = Uri.parse("package:${activity.packageName}")
                    activity.startActivityForResult(intent, 2)
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

    //Función para comprobar y aceptar permisos de escritura
    @SuppressLint("NewApi")
    fun showWriteStoragePermissionDialog(fragment: Fragment) {
        val permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos no están activados, muestre una ventana emergente
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

    //Función para comprobar si un emulador está instalado
    fun showInstallEmulatorDialog(context: Context, emulatorName: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Emulador No Instalado")
        builder.setMessage("El emulador $emulatorName no está instalado. ¿Quieres ir a la tienda?")
        builder.setPositiveButton("Yes") { _, _ ->
            // Acción para abrir la tienda y descargar el emulador
            val fragmentStore = fragment_store()
            (context as FragmentActivity).supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragments, fragmentStore, "fragment_store")
                .addToBackStack(null)
                .commit()
        }
        builder.setNegativeButton("No", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    //Función para descargar e instalar una app
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

    //Función para descargar juegos
    @SuppressLint("UseCompatLoadingForDrawables")
    fun downloadGame(context: Context, downloadUrl: String?, gameName: String?) {
        // Crear un cuadro de diálogo de confirmación para descargar el juego
        AlertDialog.Builder(context)
            .setTitle("Descargar juego")
            .setMessage("¿Estás seguro que deseas descargar $gameName?")
            .setPositiveButton("Descargar") { dialog, which ->
                // Preparar la descarga
                val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                    setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                    setTitle(gameName)
                    setDescription("Descargando $gameName")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$gameName.zip")
                }

                // Descargar el juego
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)

                // Mostrar un cuadro de diálogo con el progreso de la descarga
                val progressDialog = ProgressDialog(context)
                progressDialog.setTitle("Descargando $gameName")
                progressDialog.setMessage("Espere por favor...")
                progressDialog.setCancelable(false)
                progressDialog.show()

                // Registrar un BroadcastReceiver para saber cuando termina la descarga
                val onComplete = object : BroadcastReceiver() {
                    @SuppressLint("Range")
                    override fun onReceive(context: Context, intent: Intent?) {
                        if (downloadId == intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)) {
                            // La descarga ha finalizado
                            progressDialog.dismiss()

                            // Verificar si la descarga ha sido exitosa
                            val downloadQuery = DownloadManager.Query().setFilterById(downloadId)
                            val cursor = downloadManager.query(downloadQuery)
                            if (cursor.moveToFirst()) {
                                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    // Obtener la ruta del archivo ZIP descargado
                                    val downloadUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                                    val zipFile = File(Uri.parse(downloadUri).path)

                                    // Descomprimir el archivo ZIP en la carpeta de descargas
                                    val destinationDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                    unzipFile(zipFile, destinationDir)

                                    // Eliminar el archivo ZIP descargado
                                    zipFile.delete()

                                    // Mostrar un mensaje de éxito
                                    Toast.makeText(context, "El archivo ZIP se ha descomprimido correctamente.", Toast.LENGTH_SHORT).show()
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

    // Función para descomprimir el archivo ZIP en la carpeta de destino
    private fun unzipFile(zipFile: File, destinationDir: File) {
        val buffer = ByteArray(1024)
        val renameMap = mutableMapOf<String, String>()

        try {
            val zipInputStream = ZipInputStream(FileInputStream(zipFile))
            var zipEntry: ZipEntry? = zipInputStream.nextEntry

            while (zipEntry != null) {
                val newFile = File(destinationDir, zipEntry.name)

                if (zipEntry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    val outputStream = FileOutputStream(newFile)

                    var len: Int
                    while (zipInputStream.read(buffer).also { len = it } > 0) {
                        outputStream.write(buffer, 0, len)
                    }

                    outputStream.close()

                    // Obtener la extensión del archivo descomprimido
                    val extension = getFileExtension(newFile.name)
                    if (extension == "gba" || extension == "nds" || extension == "iso") {
                        // Agregar al mapa de renombrado
                        val newFileName = "${zipFile.nameWithoutExtension}.$extension"
                        renameMap[newFile.name] = newFileName
                    } else {
                        // Eliminar el archivo que no es .gba, .nds o .iso
                        newFile.delete()
                    }
                }

                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
            zipInputStream.close()

            // Renombrar los archivos
            for ((oldName, newName) in renameMap) {
                val oldFile = File(destinationDir, oldName)
                val newFile = File(destinationDir, newName)
                oldFile.renameTo(newFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // Función para obtener la extensión de un archivo
    private fun getFileExtension(fileName: String): String {
        val dotIndex = fileName.lastIndexOf('.')
        return if (dotIndex != -1) fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT) else ""
    }

    // Función para abrir el emulador DraStic DS
    fun launchNDSGame(context: Context, gameFile: File) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage("com.dsemu.drastic")
        if (intent != null) {
            intent.action = Intent.ACTION_VIEW
            val fileUri = FileProvider.getUriForFile(context, context.packageName + ".provider", gameFile)
            intent.setDataAndType(fileUri, "application/x-nds-rom")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No se puede abrir el juego. Asegúrate de tener instalado el emulador DraStic DS.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No se puede abrir el juego. Asegúrate de tener instalado el emulador DraStic DS.", Toast.LENGTH_SHORT).show()
        }
    }

    // Función para abrir el emulador MyBoy
    fun launchGBGame(context: Context, gameFile: File) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage("com.fastemulator.gba")
        if (intent != null) {
            intent.action = Intent.ACTION_VIEW
            val fileUri = FileProvider.getUriForFile(context, context.packageName + ".provider", gameFile)
            intent.setDataAndType(fileUri, "application/x-gba-rom")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "No se puede abrir el juego. Asegúrate de tener instalado el emulador MyBoy.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "No se puede abrir el juego. Asegúrate de tener instalado el emulador MyBoy.", Toast.LENGTH_SHORT).show()
        }
    }




}