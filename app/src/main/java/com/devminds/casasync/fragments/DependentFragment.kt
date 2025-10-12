package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.Task
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.HouseViewModel
import com.google.android.material.appbar.MaterialToolbar

class DependentFragment : Fragment(R.layout.fragment_dependent) {

    private lateinit var adapter: GenericAdapter<Task>
    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val houseViewModel: HouseViewModel by activityViewModels()
    private var currentDependent: Dependent? = null
    private val taskList: MutableList<Task>
        get() = currentDependent?.tasks ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dependentTitle = view.findViewById<MaterialToolbar>(R.id.topBar)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            val text = "Tarefas para " + (dependent?.name ?: "Bilbo")
            dependentTitle.title = text
        }

        val recyclerTasks = view.findViewById<RecyclerView>(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())

        val dependentId = arguments?.getString("dependentId")

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentDependent = user?.houses
                ?.flatMap { it.dependents }
                ?.find { it.id == dependentId }

            currentDependent?.let { dependentViewModel.setDependent(it) }

            val recycler = recyclerTasks

            adapter = Utils.createTaskAdapter(
                recycler = recyclerTasks,
                list = taskList,
                fragmentFactory = { taskId ->
                    TaskFragment().apply {
                        arguments = Bundle().apply {
                            putString("taskId", taskId)
                        }
                    }
                },
                fragmentManager = parentFragmentManager,
                itemOptions = getString(R.string.task_options),
                successRenameToast = getString(R.string.rename_success_task_toast),
                userViewModel = userViewModel,
                context = requireContext()
            )

            recycler.adapter = adapter
            val position = taskList.indexOfFirst { it.id == dependentId }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
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
                        val user = userViewModel.user.value
                        user?.let {
                            JsonStorageManager.saveUser(requireContext(), it)
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
