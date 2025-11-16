package com.devminds.casasync.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.devminds.casasync.utils.DialogUtils
import com.devminds.casasync.utils.TaskAlarmReceiver
import com.devminds.casasync.utils.DatePickers
import com.devminds.casasync.utils.DateUtils

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
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // cancela notificações anteriores
    fun cancelAllTaskNotifications(context: Context, task: Task) {
        TaskAlarmReceiver().cancelScheduleNotification(
            context, task.id, "hour", task.name, "Menos de uma hora para ser concluída")
        TaskAlarmReceiver().cancelScheduleNotification(
            context, task.id, "day", task.name, "Menos de um dia para ser concluída")
    }

    fun scheduleTaskNotification(context: Context, viewModel: TaskViewModel) {
        viewModel.task.value?.let { task ->

            if (task.finishDate == null && task.previsionHour != null) {
                val formatter = DateUtils.formatter(task.previsionDate, task.previsionHour)
                val prevMillis = DateUtils.prevDateMillis(formatter)

                if (prevMillis > System.currentTimeMillis()) {
                    // notifica 1 hora antes da conclusão prevista
                    TaskAlarmReceiver().scheduleNotification(
                        context,
                        task.id,
                        task.name,
                        "Menos de uma hora para ser concluída",
                        DateUtils.minusHour(
                            task.previsionDate,
                            task.previsionHour,
                            1
                        ),
                        "hour"
                    )

                    // notifica 1 dia antes da conclusão prevista
                    TaskAlarmReceiver().scheduleNotification(
                        context,
                        task.id,
                        task.name,
                        "Menos de um dia para ser concluída",
                        DateUtils.minusDay(
                            task.previsionDate,
                            task.previsionHour,
                            1
                        ),
                        "day"
                    )
                }
            }
        }
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
            userViewModel.persistAndSyncUser()

            // agenda notificações
            scheduleTaskNotification(context, taskViewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        taskDescription = view.findViewById(R.id.taskDescription)
        checker = view.findViewById(R.id.checker)

        // TODO
//        swipeRefresh = view.findViewById(R.id.swipeRefresh)
//        refreshPage(swipeRefresh, userViewModel)

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
                delayEditText(editText, context)
                layout.addView(editText)

                // diálogo
                AlertDialog.Builder(context)
                    .setTitle("Editar Descrição")
                    .setView(layout)
                    .setCancelable(false)
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
                    val date = DateUtils.date(0).fullDate // data atual

                    finishDate.text = date
                    checker.text = getString(R.string.finished) // muda o texto do checkbox
                    previsionDate.isEnabled = false // desabilita a data de previsão
                    previsionHour.isEnabled = false // desabilita a hora de previsão

                    // atualiza a tarefa e o usuário no json
                    saveTask(context, item = "finishDate", date)
                } else {
                    finishDate.text = getString(R.string.finished)
                    checker.text = getString(R.string.finished)
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
            val datePicker = DatePickers.datePicker("Previsão da data de conclusão")

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
            val hourPicker = DatePickers.hourPicker("Previsão da hora de conclusão")

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

            currentTask = dependent?.tasks?.find { it.id == taskId } // seta a tarefa atual
            currentTask?.let { taskViewModel.setTask(it) } // atualiza a tarefa no ViewModel
        }

        // botão de salvar
        btnSaveTask = view.findViewById(R.id.btnSaveTask)
        btnSaveTask.setOnClickListener {
            val task = taskViewModel.task.value
            if (task != null) {
                userViewModel.persistAndSyncUser()
                DialogUtils.showMessage(context, "Tarefa salva com sucesso!")
            }
        }
    }
}
