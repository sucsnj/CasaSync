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

class TaskFragment : Fragment(R.layout.fragment_task) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnNewHouse = view.findViewById<TextView>(R.id.btnNewMember)
        btnNewHouse.setOnClickListener {

            // lógica para add membro
        }

        val btnMember1 = view.findViewById<TextView>(R.id.btnMember1)
        btnMember1.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container, MemberFragment())
                .addToBackStack(null)
                .commit()
        }

        val btnMember2 = view.findViewById<TextView>(R.id.btnMember2)
        btnMember2.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container, MemberFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}




