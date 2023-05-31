package com.example.tfg

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.tfg.fragment_gb


class fragment_emulators : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_emulators, container, false)

        val gb = view.findViewById<ImageView>(R.id.gbImageView)
        val ds = view.findViewById<ImageView>(R.id.dsImageView)
        val psp = view.findViewById<ImageView>(R.id.pspImageView)

        gb.setOnClickListener{
            val isMyBoyProInstalled = Utils.isPackageInstalled(requireContext(), "com.fastemulator.gba")
            if (isMyBoyProInstalled){
                val fragmentGB = fragment_gb()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor_fragments, fragmentGB, "fragment_gb")
                    .addToBackStack(null)
                    .commit()
            }
            else{
               Utils.showInstallEmulatorDialog(requireContext(), "MyBoy")
            }

        }

        ds.setOnClickListener{
            val isDrasticInstalled = Utils.isPackageInstalled(requireContext(), "com.dsemu.drastic")
            if (isDrasticInstalled){
                val fragmentDS = fragment_ds()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor_fragments, fragmentDS, "fragment_ds")
                    .addToBackStack(null)
                    .commit()
            }
            else{
                Utils.showInstallEmulatorDialog(requireContext(), "Drastic DS")
            }
        }

        psp.setOnClickListener{
            val isPPSSPPInstalled = Utils.isPackageInstalled(requireContext(), "org.ppsspp.ppsspp")
            if (isPPSSPPInstalled){
                val fragmentPSP = fragment_psp()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.contenedor_fragments, fragmentPSP, "fragment_psp")
                    .addToBackStack(null)
                    .commit()
            }
            else{
                Utils.showInstallEmulatorDialog(requireContext(), "PPSSPP")
            }
        }




        // Inflate the layout for this fragment
        return view
    }

}