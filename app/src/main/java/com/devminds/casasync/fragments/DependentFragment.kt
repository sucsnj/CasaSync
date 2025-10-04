package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.UserViewModel
import java.util.UUID

class DependentFragment : Fragment(R.layout.fragment_dependent) {

    private lateinit var adapter: GenericAdapter<Task>
    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private var currentDependent: Dependent? = null
    private val taskList: MutableList<Task>
        get() = currentDependent?.tasks ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dependentName = view.findViewById<TextView>(R.id.dependentName)
        val recyclerTasks = view.findViewById<RecyclerView>(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())

        val dependentId = arguments?.getString("dependentId")

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentDependent = user?.houses
                ?.flatMap { it.dependents }
                ?.find { it.id == dependentId }

            currentDependent?.let { dependentViewModel.setDependent(it) }

            adapter = GenericAdapter(
                items = taskList,
                layoutResId = R.layout.item_generic,
                bind = { itemView, task ->
                    itemView.findViewById<TextView>(R.id.itemName).text = task.name
                },
                onItemClick = { selectedTask ->
                    val fragment = TaskFragment().apply {
                        arguments = Bundle().apply {
                            putString("taskId", selectedTask.id)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            )

            recyclerTasks.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependentName.text = "Você é o, ${dependent?.name ?: "Smeagol"}"
        }

        val btnAddTask = view.findViewById<TextView>(R.id.btnAddTask)
        btnAddTask.setOnClickListener {
            val context = requireContext()

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(context).apply {
                hint = "Nome da tarefa"
            }

            val inputDescription = EditText(context).apply {
                hint = "Descrição da tarefa"
            }

            layout.addView(inputName)
            layout.addView(inputDescription)

            AlertDialog.Builder(context)
                .setTitle("Adicionar nova tarefa")
                .setView(layout)
                .setPositiveButton("Adicionar") { _, _ ->
                    val name = inputName.text.toString().trim()
                    val description = inputDescription.text.toString().trim()
                    if (name.isNotEmpty()) {
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description
                        )
                        currentDependent?.tasks?.add(newTask)
                        adapter.notifyItemInserted(taskList.size - 1)

                        // persiste o usuário em json
                        JsonStorageManager.saveUser(requireContext(), userViewModel.user.value!!)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
