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
                bind = { itemView, task, position, viewHolder ->
                    val editText = itemView.findViewById<EditText>(R.id.itemName)
                    editText.setText(task.name)
                    editText.isEnabled = false
                    editText.isFocusable = false

                    itemView.setOnLongClickListener {
                        val options = arrayOf("Renomear", "Apagar")

                        AlertDialog.Builder(itemView.context)
                            .setTitle("Opções para a tarefa \"${task.name}\"")
                            .setItems(options) { _, which ->
                                when (which) {
                                    0 -> {
                                        editText.isEnabled = true
                                        editText.isFocusableInTouchMode = true
                                        editText.requestFocus()
                                        editText.setSelection(editText.text.length)

                                        val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

                                        editText.setOnEditorActionListener { _, actionId, _ ->
                                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                val newName = editText.text.toString().trim()
                                                if (newName.isNotEmpty()) {
                                                    task.name = newName
                                                    JsonStorageManager.saveUser(itemView.context, userViewModel.user.value!!)
                                                    adapter.notifyItemChanged(position)
                                                    Toast.makeText(itemView.context, "Tarefa renomeada", Toast.LENGTH_SHORT).show()
                                                }
                                                editText.isEnabled = false
                                                editText.isFocusable = false
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                    }
                                    1 -> {
                                        // Apagar
                                        AlertDialog.Builder(itemView.context)
                                            .setTitle("Apagar Tarefa")
                                            .setMessage("Tem certeza que deseja apagar a tarefa \"${task.name}\"?")
                                            .setPositiveButton("Apagar") { _, _ ->
                                                val index = taskList.indexOfFirst { it.id == task.id }
                                                if (index != -1) {
                                                    taskList.removeAt(index)
                                                    adapter.notifyItemRemoved(index)

                                                    userViewModel.user.value?.let {
                                                        JsonStorageManager.saveUser(itemView.context, it)
                                                    }

                                                    Toast.makeText(
                                                        itemView.context,
                                                        "Tarefa apagada com sucesso",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .setNegativeButton("Cancelar", null)
                                            .show()
                                    }
                                }
                            }
                            .show()
                        true
                    }
                },
                onItemClick = { task ->
                    val fragment = TaskFragment().apply {
                        arguments = Bundle().apply {
                            putString("taskId", task.id)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .setCustomTransition(TransitionType.SLIDE)
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
