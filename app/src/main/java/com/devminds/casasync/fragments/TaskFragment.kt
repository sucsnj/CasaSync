package com.devminds.casasync.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class TaskFragment : BaseFragment(R.layout.fragment_task) {

    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()
    private var currentTask: Task? = null
    private lateinit var toolbar: MaterialToolbar
    private lateinit var taskDescription: TextView
    private lateinit var menu: Menu
    private var taskId: String? = null
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var previsionDate: EditText
    private lateinit var previsionHour: EditText
    private lateinit var startDate: TextView
    private lateinit var finishDate: TextView

    fun timePicker(): MaterialTimePicker {
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Selecione a hora de conclusão")
            .build()

        return timePicker
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.topBar)
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        taskDescription = view.findViewById(R.id.taskDescription)

        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            title.text = task?.name ?: "Tarefa"
            taskDescription.text = task?.description ?: "Descrição"


            startDate = view.findViewById(R.id.startDate)
            startDate.text = task?.startDate ?: "Data"

            previsionDate.setText(task?.previsionDate ?: "")
            previsionHour.setText(task?.previsionHour ?: "")
            // finishDate.setText(task?.finishDate ?: "")
        }

        // data de conclusão prevista
        previsionDate = view.findViewById(R.id.previsionDate)
        previsionDate.setOnClickListener {

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Previsão da data de conclusão")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val instant = Instant.ofEpochMilli(selection).plusSeconds(12 * 60 * 60)
                val zoneId = ZoneId.systemDefault()
                val localDate = instant.atZone(zoneId).toLocalDate()

                val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                previsionDate.setText(formattedDate)

                currentTask?.let {
                    it.previsionDate = formattedDate
                    dependentViewModel.updateTask(it)
                    userViewModel.persistUser(requireContext(), userViewModel.user.value)
                }
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER")
        }

        // hora de conclusão prevista
        previsionHour = view.findViewById(R.id.previsionHour)
        previsionHour.setOnClickListener {
            val hourPicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(12)
                .setMinute(0)
                .setTitleText("Previsão da hora de conclusão")
                .build()

            hourPicker.addOnPositiveButtonClickListener {
                val hour = hourPicker.hour
                val minute = hourPicker.minute
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                previsionHour.setText(formattedTime)

                currentTask?.let {
                    it.previsionHour = formattedTime
                    dependentViewModel.updateTask(it)
                    userViewModel.persistUser(requireContext(), userViewModel.user.value)
                }
            }
            hourPicker.show(parentFragmentManager, "HOUR_PICKER")
        }

        // data de conclusão
        finishDate = view.findViewById(R.id.finishDate)

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

        subtitle = view.findViewById(R.id.subtitle)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            subtitle.text = dependent?.name ?: "Dependente"

            currentTask = dependent?.tasks?.find { it.id == taskId }
            currentTask?.let { taskViewModel.setTask(it) }
        }
    }
}
