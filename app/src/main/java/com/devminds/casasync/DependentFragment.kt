package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment

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




