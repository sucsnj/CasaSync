package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class DependentFragment : BaseFragment(R.layout.fragment_dependent) {

    private lateinit var adapter: GenericAdapter<Task>
    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private var currentDependent: Dependent? = null
    private val taskList: MutableList<Task>
        get() = currentDependent?.tasks ?: mutableListOf()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var recyclerTasks: RecyclerView
    private var dependentId: String? = null
    private lateinit var btnAddTask: TextView

    // data atual para criação
    fun date(): String {
        val dateNow = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val formattedDate = dateNow.format(formatter)

        return formattedDate
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.topBar)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            val text = dependent?.name ?: "Dependente"
            toolbar.title = text
        }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu
        menu.findItem(R.id.more_options).isVisible = false

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_homepage -> {
                    parentFragmentManager.beginTransaction()
                        .setCustomTransition(TransitionType.FADE)
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }

        recyclerTasks = view.findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(requireContext())

        dependentId = arguments?.getString("dependentId")

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

        btnAddTask = view.findViewById(R.id.btnAddTask)
        btnAddTask.setOnClickListener {
            val context = requireContext()

            // layout do diálogo
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(context).apply {
                hint = context.getString(R.string.task_name)
            }

            val inputDescription = EditText(context).apply {
                hint = context.getString(R.string.task_description)
            }

            layout.addView(inputName)
            layout.addView(inputDescription)

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.add_task_dialog))
                .setView(layout)
                .setPositiveButton(getString(R.string.button_add)) { _, _ ->
                    val name = inputName.text.toString().trim()
                    val description = inputDescription.text.toString().trim()
                    if (name.isNotEmpty()) {
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            date = date()
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
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show()
        }
    }
}
