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

        val btnNewHouse = view.findViewById<TextView>(R.id.btnAddTaskName)
        btnNewHouse.setOnClickListener {

            // lógica para add nome da tarefa
        }
    }
}




