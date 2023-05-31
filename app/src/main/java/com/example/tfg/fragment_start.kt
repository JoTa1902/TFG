package com.example.tfg

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat

@Suppress("DEPRECATION")
class fragment_start : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)
        val storeButton = view.findViewById<ImageButton>(R.id.store)
        val emulatorsButton = view.findViewById<ImageButton>(R.id.emulators)
        storeButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.GET_PACKAGE_SIZE) != PackageManager.PERMISSION_GRANTED){
                showPackagePermissionDialog(this)
            }
            val fragmentStore = fragment_store()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragments, fragmentStore, "fragment_store")
                .addToBackStack(null)
                .commit()
        }

        emulatorsButton.setOnClickListener {
            val fragmentEmulators = fragment_emulators()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.contenedor_fragments, fragmentEmulators, "fragment_emulators")
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    fun showPackagePermissionDialog(fragment: Fragment) {
        val permission = android.Manifest.permission.GET_PACKAGE_SIZE
        if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
            // Si los permisos no están activados, muestre una ventana emergente
            AlertDialog.Builder(fragment.requireContext())
                .setTitle("Permiso package")
                .setMessage("Se requieren permisos de paquetes para continuar.")
                .setPositiveButton("ALLOW") { _, _ ->
                    // Si el usuario presiona "ALLOW", solicite los permisos
                    fragment.requestPermissions(arrayOf(permission), 1)
                }
                .setNegativeButton("CANCEL", null)
                .show()
        }
    }

}

