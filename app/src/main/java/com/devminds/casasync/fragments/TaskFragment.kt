package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.google.android.material.appbar.MaterialToolbar

class TaskFragment : Fragment(R.layout.fragment_task) {

    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()

    private var currentTask: Task? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topBar)
        val taskDescription = view.findViewById<TextView>(R.id.taskDescription)
        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            toolbar.title = task?.name ?: "Tarefa"
            taskDescription.text =
                getString(R.string.to_do, task?.description ?: "Descrição")
        }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        val menu = toolbar.menu
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

        val taskId = arguments?.getString("taskId")

        val dependentName = view.findViewById<TextView>(R.id.dependentName)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependentName.text = dependent?.name ?: "Dependente"

            currentTask = dependent?.tasks?.find { it.id == taskId }
            currentTask?.let { taskViewModel.setTask(it) }
        }
    }
}
