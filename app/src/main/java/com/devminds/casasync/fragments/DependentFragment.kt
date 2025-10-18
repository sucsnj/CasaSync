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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import android.text.InputType
import com.google.android.material.timepicker.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

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
    private lateinit var title: TextView
    private lateinit var subtitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // title e subtitle
        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)

        toolbar = view.findViewById(R.id.topBar)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            // val text = dependent?.name ?: "Dependente"
            // toolbar.title = text
            title.text = dependent?.name ?: "Dependente"
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

        // adicionar tarefa
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

            val inputPrevisionDate = EditText(context).apply {
                inputType = InputType.TYPE_NULL
                isFocusable = false
                isClickable = true

                hint = context.getString(R.string.prevision_date)

                setOnClickListener {
                    val datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Insira a data prevista de conclusão")
                        .build()

                    datePicker.addOnPositiveButtonClickListener { selection ->
                        val instant = Instant.ofEpochMilli(selection).plusSeconds(12 * 60 * 60)
                        val zoneId = ZoneId.systemDefault()
                        val localDate = instant.atZone(zoneId).toLocalDate()

                        val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        setText(formattedDate)
                    }
                    datePicker.show(parentFragmentManager, "DATE_PICKER")
                }
            }

            val inputPrevisionHour = EditText(context).apply {
                inputType = InputType.TYPE_NULL
                isFocusable = false
                isClickable = true

                hint = context.getString(R.string.prevision_hour)

                setOnClickListener {
                    val hourPicker = MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(12)
                        .setMinute(0)
                        .setTitleText("Insira a hora prevista de conclusão")
                        .build()

                    hourPicker.addOnPositiveButtonClickListener {
                        val hour = hourPicker.hour
                        val minute = hourPicker.minute
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                        setText(formattedTime)
                    }
                    hourPicker.show(parentFragmentManager, "HOUR_PICKER")
                }
            }

            layout.addView(inputName)
            layout.addView(inputDescription)
            layout.addView(inputPrevisionDate)
            layout.addView(inputPrevisionHour)

            AlertDialog.Builder(context)
                .setTitle(getString(R.string.add_task_dialog))
                .setView(layout)
                .setPositiveButton(getString(R.string.button_add)) { _, _ ->
                    val name = inputName.text.toString().trim()
                    val description = inputDescription.text.toString().trim()
                    val previsionDate = inputPrevisionDate.text.toString().trim()
                    var previsionHour = inputPrevisionHour.text.toString().trim()

                    // impede que hora seja escrito sem uma data
                    if (previsionDate.isEmpty()) {
                        previsionHour = ""
                    }

                    if (name.isNotEmpty()) {
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            startDate = Utils.date(),
                            previsionDate = previsionDate,
                            previsionHour = previsionHour,
                            finishDate = null
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
