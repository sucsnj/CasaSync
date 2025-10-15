package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.google.android.material.appbar.MaterialToolbar

class TaskFragment : BaseFragment(R.layout.fragment_task) {

    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()
    private var currentTask: Task? = null
    private lateinit var toolbar: MaterialToolbar
    private lateinit var taskDescription: TextView
    private lateinit var menu: Menu
    private var taskId: String? = null
    private lateinit var dependentName: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.topBar)
        taskDescription = view.findViewById(R.id.taskDescription)
        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            toolbar.title = task?.name ?: "Tarefa"
            taskDescription.text =
                getString(R.string.to_do, task?.description ?: "Descrição")
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
                    replaceFragment( HomeFragment(), TransitionType.FADE)
                    true
                }
                else -> false
            }
        }

        taskId = arguments?.getString("taskId")

        dependentName = view.findViewById(R.id.dependentName)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependentName.text = dependent?.name ?: "Dependente"

            currentTask = dependent?.tasks?.find { it.id == taskId }
            currentTask?.let { taskViewModel.setTask(it) }
        }
    }
}
