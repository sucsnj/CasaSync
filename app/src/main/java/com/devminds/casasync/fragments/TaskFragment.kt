package com.devminds.casasync.fragments

import android.Manifest
import android.app.AlarmManager
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.parts.date
import com.devminds.casasync.parts.datePicker
import com.devminds.casasync.parts.hourPicker
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
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.TaskAlarmReceiver
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date

class TaskFragment : BaseFragment(R.layout.fragment_task) {

    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()
    private var currentTask: Task? = null
    private var taskId: String? = null

    private lateinit var toolbar: MaterialToolbar
    private lateinit var taskDescription: TextView
    private lateinit var menu: Menu
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var previsionDate: EditText
    private lateinit var previsionHour: EditText
    private lateinit var startDate: TextView
    private lateinit var finishDate: TextView
    private lateinit var checker: CheckBox
    private lateinit var btnSaveTask: TextView

    fun formatter(date: String?, hour: String?): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val previsionDateTime = LocalDateTime.parse("$date $hour", formatter)
        return previsionDateTime
    }

    private fun saveTask(context: Context, item: String, itemValue: String?) {
        taskViewModel.task.value?.let { task ->
            when (item) {
                "description" -> itemValue?.let { task.description = it }
                "previsionDate" -> itemValue?.let { task.previsionDate = it }
                "previsionHour" -> itemValue?.let { task.previsionHour = it }
                "finishDate" -> task.finishDate = itemValue
            }

            dependentViewModel.updateTask(task)
            userViewModel.persistUser(context, userViewModel.user.value)

            // agenda notificações

            fun minusHour(hours: Long): Long {
                val previsionDateTime = formatter(task.previsionDate, task.previsionHour)

                val notifyTime = previsionDateTime.minusHours(hours)
                val notifyMillis = notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                return notifyMillis
            }

            fun minusDay(days: Long): Long {
                val previsionDateTime = formatter(task.previsionDate, task.previsionHour)

                val notifyTime = previsionDateTime.minusDays(days)
                val notifyMillis = notifyTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                return notifyMillis
            }

            // quando a tarefa esta proxíma da data de conclusão (um dia antes)
            if (task.finishDate == null && task.previsionHour != null) {
                // notifica 1 hora antes da conclusão prevista
                TaskAlarmReceiver().scheduleNotification(
                    context,
                    task.name,
                    "Menos de uma hora para ser concluída",
                    minusHour(1)
                )
                // notifica 1 dia antes da conclusão prevista
                TaskAlarmReceiver().scheduleNotification(
                    context,
                    task.name,
                    "Menos de um dia para ser concluída",
                    minusDay(1)
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        taskDescription = view.findViewById(R.id.taskDescription)
        checker = view.findViewById(R.id.checker)

        // observa a tarefa selecionada
        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            title.text = task?.name ?: "Tarefa" // nome no título
            taskDescription.text = task?.description ?: "Sem descrição" // descrição da tarefa
            // se a descrição estiver vazia, mostra "Sem descrição" na dica
            if (task?.description.toString() == "") {
                taskDescription.hint = "Sem descrição"
            }

            // alterar descrição
            taskDescription.setOnClickListener {
                // layout do diálogo
                val layout = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(50, 40, 50, 10)
                }

                val editText = EditText(context).apply {
                    setText(taskDescription.text)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // teclado com delay
                editText.postDelayed({
                    editText.requestFocus() // traz o foco

                    // levanta o teclado
                    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
                    editText.setSelection(0, editText.length()) // texto selecionado
                },500)

                layout.addView(editText)

                // diálogo
                AlertDialog.Builder(context)
                    .setTitle("Editar Descrição")
                    .setView(layout)
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton("Salvar") { dialog, _ ->
                        val newDescription = editText.text.toString()
                        taskDescription.text = newDescription

                        // atualizar descrição na task e no dependent e persiste no usuário
                        saveTask(context, item = "description", newDescription)
                        // currentTask?.let {
                        //     it.description = newDescription
                        //     dependentViewModel.updateTask(it) // atualiza a task
                        //     userViewModel.persistUser(context, userViewModel.user.value) // persiste o usuário
                        // }
                        dialog.dismiss() // fecha o diálogo
                    }
                    .show() // mostra o diálogo
            }

            // checkbox de conclusão (se comunica com a data de conclusão)
            checker.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    val date = date(0).fullDate // data atual

                    finishDate.text = date
                    checker.text = "Concluído" // muda o texto do checkbox
                    previsionDate.isEnabled = false // desabilita a data de previsão
                    previsionHour.isEnabled = false // desabilita a hora de previsão

                    // atualiza a tarefa e o usuário no json
                    saveTask(context, item = "finishDate", date)
                } else {
                    finishDate.text = "Concluído"
                    checker.text = "Concluído"
                    previsionDate.isEnabled = true
                    previsionHour.isEnabled = true

                    // atualiza a tarefa e o usuário no json
                    saveTask(context, item = "finishDate", null)
                }
            }

            startDate = view.findViewById(R.id.startDate)
            startDate.text = task?.startDate ?: "Não concluído"
            previsionDate.setText(task?.previsionDate ?: "Não concluído")
            if (previsionDate.text.toString() == "") { // impede que hora seja escrito sem uma data
                previsionHour.isEnabled = false
            }
            previsionHour.setText(task?.previsionHour ?: "Sem hora")
            finishDate.text = task?.finishDate ?: "Não concluído"
            checker.isChecked = task?.finishDate != null
        }

        // data de conclusão prevista
        previsionDate = view.findViewById(R.id.previsionDate)
        previsionDate.setOnClickListener {
            val datePicker = datePicker("Previsão da data de conclusão")

            // transformar data em string
            datePicker.addOnPositiveButtonClickListener { selection ->
                val instant = Instant.ofEpochMilli(selection).plusSeconds(12 * 60 * 60)
                val zoneId = ZoneId.systemDefault()
                val localDate = instant.atZone(zoneId).toLocalDate()

                val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                previsionDate.setText(formattedDate)
                // atualiza a tarefa e o usuário no json
                saveTask(context, item = "previsionDate", formattedDate)
                previsionHour.isEnabled = true // permite editar hora após selecionar uma data
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER") // mostra o datePicker (calendário)
        }

        // hora de conclusão prevista
        previsionHour = view.findViewById(R.id.previsionHour)
        previsionHour.setOnClickListener {
            val hourPicker = hourPicker("Previsão da hora de conclusão")

            hourPicker.addOnPositiveButtonClickListener {
                val hour = hourPicker.hour
                val minute = hourPicker.minute
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                previsionHour.setText(formattedTime)
                // atualiza a tarefa e o usuário no json
                saveTask(context, item = "previsionHour", formattedTime)
            }
            hourPicker.show(parentFragmentManager, "HOUR_PICKER") // mostra o hourPicker (hora)
        }

        // data de conclusão
        finishDate = view.findViewById(R.id.finishDate)

        // barra no topo
        toolbar = view.findViewById(R.id.topBar)
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // menu suspenso (3 pontos)
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
        // observa o dependente selecionado
        subtitle = view.findViewById(R.id.subtitle)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            subtitle.text = dependent?.name ?: "Dependente" // nome do dependente no subtítulo

            currentTask = dependent?.tasks?.find { it.id == taskId } // tarefa selecionada (para que? TODO)
            currentTask?.let { taskViewModel.setTask(it) } // atualiza a tarefa no ViewModel
        }

        btnSaveTask = view.findViewById(R.id.btnSaveTask)
        btnSaveTask.setOnClickListener {
            val task = taskViewModel.task.value
            if (task != null) {
                userViewModel.persistUser(context, userViewModel.user.value)
                DialogUtils.showMessage(context, "Tarefa salva com sucesso!")
            }
        }
    }
}
