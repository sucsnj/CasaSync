package com.devminds.casasync

import Tarefa
import TarefaAdapter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_tela_inicial) {

    private lateinit var editTask: EditText
    private lateinit var btnAdd: Button
    private lateinit var listTasks: ListView

    private val tasks = mutableListOf<Tarefa>()
    private lateinit var adapter: TarefaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editTask = view.findViewById(R.id.editTask)
        btnAdd = view.findViewById(R.id.btnAdd)
        listTasks = view.findViewById(R.id.listTasks)

        adapter = TarefaAdapter(requireContext(), tasks)
        listTasks.adapter = adapter

        // Adicionar tarefa
        btnAdd.setOnClickListener {
            val titulo = editTask.text.toString()
            if (titulo.isNotEmpty()) {
                tasks.add(Tarefa(titulo))
                adapter.notifyDataSetChanged()
                editTask.text.clear()
            }
        }

        // Remover tarefa ao clicar
        listTasks.setOnItemClickListener { _, _, position, _ ->
            tasks.removeAt(position)
            adapter.notifyDataSetChanged()
        }

    }
}



