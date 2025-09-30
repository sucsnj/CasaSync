package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.fragment.app.Fragment

// Tela principal do app (implementação do fragment)
class HomeFragment : Fragment(R.layout.fragment_tela_inicial) {

    private lateinit var editTask: EditText
    private lateinit var btnAdd: Button
    private lateinit var listTasks: ListView

    private val tasks = ArrayList<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Ligando os elementos do layout XML
        editTask = view.findViewById(R.id.editTask)
        btnAdd = view.findViewById(R.id.btnAdd)
        listTasks = view.findViewById(R.id.listTasks)

        // Adaptador para mostrar a lista de tarefas
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tasks)
        listTasks.adapter = adapter

        // Ação do botão de adicionar
        btnAdd.setOnClickListener {
            val task = editTask.text.toString()
            if (task.isNotEmpty()) {
                tasks.add(task)                  // adiciona na lista
                adapter.notifyDataSetChanged()   // atualiza a lista
                editTask.text.clear()            // limpa o campo
            }
        }
        // Ação para remover tarefa ao clicar nela
        listTasks.setOnItemClickListener { _, _, position, _ ->
            tasks.removeAt(position)             // remove a tarefa clicada
            adapter.notifyDataSetChanged()       // atualiza a lista
        }
        listTasks.setOnItemClickListener { _, _, position, _ ->
            tasks.removeAt(position)             // remove a tarefa clicada
            adapter.notifyDataSetChanged()       // atualiza a lista
        }
    }
}




