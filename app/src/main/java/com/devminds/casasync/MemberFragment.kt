package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.devminds.casasync.R

class MemberFragment : Fragment(R.layout.fragment_tela_inicial) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewHouse = view.findViewById<TextView>(R.id.btnNewHouse)
        btnNewHouse.setOnClickListener {

            // lógica para criar casa
        }

        val btnCasa = view.findViewById<TextView>(R.id.btnCasa)
        btnCasa.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container, TaskFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}




