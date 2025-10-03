package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.setCustomTransition

class DependentFragment : Fragment(R.layout.fragment_dependent) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddTask = view.findViewById<TextView>(R.id.btnAddTask)
        btnAddTask.setOnClickListener {

            // lógica para add tarefa
        }

        val recyclerTasks = view.findViewById<TextView>(R.id.recyclerTasks)
        recyclerTasks.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .setCustomTransition(TransitionType.SLIDE)
                .replace(R.id.fragment_container, TaskFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}




