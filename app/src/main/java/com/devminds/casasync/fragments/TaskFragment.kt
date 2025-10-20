package com.devminds.casasync.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Task
import com.devminds.casasync.utils.Utils
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

    fun timePicker(): MaterialTimePicker { // ainda sem uso TODO
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Selecione a hora de conclusão")
            .build()

        return timePicker
    }
    fun saveTask(context: Context, item: String, itemValue: String?) {
        taskViewModel.task.value?.let {
            when (item) {
                "description" -> itemValue?.let { value -> it.description = value }
                "previsionDate" -> itemValue?.let { value -> it.previsionDate = value }
                "previsionHour" -> itemValue?.let { value -> it.previsionHour = value }
                "finishDate" -> it.finishDate = itemValue
            }
            dependentViewModel.updateTask(it)
            userViewModel.persistUser(context, userViewModel.user.value)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                val context = requireContext()

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
                        saveTask(requireContext(), item = "description", newDescription)
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
                    val date = Utils.date() // data atual

                    finishDate.text = date
                    checker.text = "Concluído" // muda o texto do checkbox
                    previsionDate.isEnabled = false // desabilita a data de previsão
                    previsionHour.isEnabled = false // desabilita a hora de previsão

                    // atualiza a tarefa e o usuário no json
                    saveTask(requireContext(), item = "finishDate", date)
                } else {
                    finishDate.text = "Não concluído"
                    checker.text = "Não concluído"
                    previsionDate.isEnabled = true
                    previsionHour.isEnabled = true

                    // atualiza a tarefa e o usuário no json
                    saveTask(requireContext(), item = "finishDate", null)
                }
            }

            startDate = view.findViewById(R.id.startDate)
            startDate.text = task?.startDate ?: "Sem data"
            previsionDate.setText(task?.previsionDate ?: "Sem data")
            if (previsionDate.text.toString() == "") { // impede que hora seja escrito sem uma data
                previsionHour.isEnabled = false
            }
            previsionHour.setText(task?.previsionHour ?: "Sem hora")
            finishDate.text = task?.finishDate ?: "Sem data"
            checker.isChecked = task?.finishDate != null
        }

        // data de conclusão prevista
        previsionDate = view.findViewById(R.id.previsionDate)
        previsionDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Previsão da data de conclusão")
                .build()

            // transformar data em string
            datePicker.addOnPositiveButtonClickListener { selection ->
                val instant = Instant.ofEpochMilli(selection).plusSeconds(12 * 60 * 60)
                val zoneId = ZoneId.systemDefault()
                val localDate = instant.atZone(zoneId).toLocalDate()

                val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                previsionDate.setText(formattedDate)
                // atualiza a tarefa e o usuário no json
                saveTask(requireContext(), item = "previsionDate", formattedDate)
                previsionHour.isEnabled = true // permite editar hora após selecionar uma data
            }
            datePicker.show(parentFragmentManager, "DATE_PICKER") // mostra o datePicker (calendário)
        }

        // hora de conclusão prevista
        previsionHour = view.findViewById(R.id.previsionHour)
        previsionHour.setOnClickListener {
            val hourPicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H) // formato de 24 horas
                .setHour(12)
                .setMinute(0)
                .setTitleText("Previsão da hora de conclusão")
                .build()

            hourPicker.addOnPositiveButtonClickListener {
                val hour = hourPicker.hour
                val minute = hourPicker.minute
                val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                previsionHour.setText(formattedTime)
                // atualiza a tarefa e o usuário no json
                saveTask(requireContext(), item = "previsionHour", formattedTime)
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
    }
}
