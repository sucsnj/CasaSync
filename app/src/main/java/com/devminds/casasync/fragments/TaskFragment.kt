package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.parts.Task
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.google.android.material.appbar.MaterialToolbar

class TaskFragment : Fragment(R.layout.fragment_task) {

    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()

    private var currentTask: Task? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskName = view.findViewById<MaterialToolbar>(R.id.topBar)
        val taskDescription = view.findViewById<TextView>(R.id.taskDescription)
        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            taskName.title = task?.name ?: "Tarefa"
            taskDescription.text = "O que fazer? ${task?.description ?: "Um Anel para a todos governar"}"
        }

        val taskId = arguments?.getString("taskId")

        val dependentName = view.findViewById<TextView>(R.id.dependentName)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependentName.text = dependent?.name ?: "Smeagol"

            currentTask = dependent?.tasks?.find { it.id == taskId }
            currentTask?.let { taskViewModel.setTask(it) }
        }
    }
}
