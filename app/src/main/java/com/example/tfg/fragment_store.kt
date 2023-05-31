package com.example.tfg

import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tfg.Utils.downloadAndInstall
import com.example.tfg.Utils.requestInstallUnknownSourcesPermission
import com.example.tfg.Utils.showWriteStoragePermissionDialog

@Suppress("DEPRECATION")
class fragment_store : Fragment() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_store, container, false)

        val gameboy = view.findViewById<ImageButton>(R.id.gameboy)
        val nds = view.findViewById<ImageButton>(R.id.nds)
        val psp = view.findViewById<ImageButton>(R.id.psp)

        gameboy.setOnClickListener {
            val isMyBoyProInstalled = Utils.isPackageInstalled(requireContext(), "com.fastemulator.gba")
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                showWriteStoragePermissionDialog(this)
            }
            if (isMyBoyProInstalled){
                val fragmentCatalog = fragment_catalog_gb()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor_fragments, fragmentCatalog, "fragment_catalog_gb")
                    .addToBackStack(null)
                    .commit()
            }else {
                downloadAndInstall(requireContext(), "https://dl.apkbe.com/down.do/com.fastemulator.gba_1.8.0_paid?code=6681b2bb6e37f8768ee31398a85ed36f", "MyBoy")
            }
        }

        nds.setOnClickListener {
            val isDrasticInstalled = Utils.isPackageInstalled(requireContext(), "com.dsemu.drastic")
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showWriteStoragePermissionDialog(this)
            }
            if (isDrasticInstalled){
                val fragmentCatalog = fragment_catalog_ds()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor_fragments, fragmentCatalog, "fragment_catalog_ds")
                    .addToBackStack(null)
                    .commit()
            }else {
                downloadAndInstall(requireContext(), "https://file.hapmod.com/uploads/DraStic-r2.5.2.2a-CoiMobile.Com.apk", "Drastic")
            }
        }

        psp.setOnClickListener {
            val isPPSSPPInstalled = Utils.isPackageInstalled(requireContext(), "org.ppsspp.ppssppgold")
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showWriteStoragePermissionDialog(this)
            }
            if (isPPSSPPInstalled){
            val fragmentCatalog = fragment_catalog_gb()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragments, fragmentCatalog, "fragment_catalog")
                .addToBackStack(null)
                .commit()
            }else {
                downloadAndInstall(requireContext(), "https://objects.githubusercontent.com/github-production-release-asset-2e65be/437561412/1cc3419f-b458-4bb9-88f0-597fd036a818?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=AKIAIWNJYAX4CSVEH53A%2F20230531%2Fus-east-1%2Fs3%2Faws4_request&X-Amz-Date=20230531T013746Z&X-Amz-Expires=300&X-Amz-Signature=cbb9fd6f293c741ca877e6d483a1dfab2348a049e8448f84870243a417210f76&X-Amz-SignedHeaders=host&actor_id=114432526&key_id=0&repo_id=437561412&response-content-disposition=attachment%3B%20filename%3DPPSSPP-Gold-v1.15.4_www.ppsspp.gold_.apk&response-content-type=application%2Fvnd.android.package-archive", "PPSSPP")
            }
        }

        return view
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario otorga los permisos de escritura, solicita permisos de instalación de fuentes desconocidas
                requestInstallUnknownSourcesPermission(requireContext() as Activity)
            } else {
                // Si el usuario no otorga los permisos de escritura, muestra un mensaje de error
                Toast.makeText(requireContext(), "Los permisos son necesarios para continuar", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == 2) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el usuario otorga los permisos de instalación de fuentes desconocidas
            } else {
                // Si el usuario no otorga los permisos de instalación de fuentes desconocidas, muestra un mensaje de error
                Toast.makeText(requireContext(), "Los permisos son necesarios para continuar", Toast.LENGTH_SHORT).show()
            }
        }
    }


}

