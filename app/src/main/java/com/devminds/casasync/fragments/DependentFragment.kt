package com.devminds.casasync.fragments

import android.content.Context
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
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.time.format.DateTimeFormatter
import java.util.UUID
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import com.devminds.casasync.parts.date
import com.devminds.casasync.parts.datePicker
import com.devminds.casasync.parts.hourPicker
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

class DependentFragment : BaseFragment(R.layout.fragment_dependent) {

    private var dependentId: String? = null
    private val userViewModel: UserViewModel by activityViewModels()
    private val dependentViewModel: DependentViewModel by activityViewModels()
    private var currentDependent: Dependent? = null
    private val taskList: MutableList<Task>
        get() = currentDependent?.tasks ?: mutableListOf()

    private lateinit var adapter: GenericAdapter<Task> // adaptador para a lista de tarefas
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var btnAddTask: TextView
    private lateinit var title: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        toolbar = view.findViewById(R.id.topBar) // cabeçalho
        menu = toolbar.menu
        recyclerTasks = view.findViewById(R.id.recyclerTasks)
        btnAddTask = view.findViewById(R.id.btnAddTask)
        title = view.findViewById(R.id.title)
        
        // muda o título do cabeçalho
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            title.text = dependent?.name ?: "Dependente"
        }
        // botão de voltar
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // infla o menu suspenso em toolbar
        toolbar.inflateMenu(R.menu.topbar_menu)
        // esconde o item de mais opções em toolbar
        menu.findItem(R.id.more_options).isVisible = false

        // botão de homepage (ínicio)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_homepage -> {
                    replaceFragment(HomeFragment(), TransitionType.FADE)
                    true
                }
                else -> false
            }
        }

        // manuseia a lista de tarefas
        recyclerTasks.layoutManager = LinearLayoutManager(context)
        dependentId = arguments?.getString("dependentId")
        // observa o usuário e o dependente selecionado
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            // Busca o dependente atual com base no ID recebido, percorrendo todas as casas do usuário
            currentDependent = user?.houses // casas do usuário
                ?.flatMap { it.dependents } // todos os dependentes
                ?.find { it.id == dependentId } // dependente selecionado

            // Atualiza o ViewModel de dependente com o dependente selecionado
            currentDependent?.let { dependentViewModel.setDependent(it) }

            // coloca o recyclerTasks no recycler
            val recycler = recyclerTasks

            // cria o adaptador
            adapter = Utils.createTaskAdapter(
                recycler = recyclerTasks,
                list = taskList, // lista de tarefas
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
                context = context
            )
            
            // Aplica o adaptador à RecyclerView para exibir as tarefas
            recycler.adapter = adapter
            val position = taskList.indexOfFirst { it.id == dependentId }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
        }

        // adicionar tarefa
        btnAddTask.setOnClickListener {
            // layout do diálogo
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            // campos do diálogo
            val inputName = EditText(context).apply {
                hint = context.getString(R.string.task_name) // texto de ajuda
            }

            // teclado com delay
            inputName.postDelayed({
                inputName.requestFocus() // traz o foco

                // levanta o teclado
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(inputName, InputMethodManager.SHOW_IMPLICIT)
                inputName.setSelection(0) // texto selecionado
            },200)

            val inputDescription = EditText(context).apply {
                hint = context.getString(R.string.task_description)
            }
            val inputPrevisionDate = EditText(context).apply {
                inputType = InputType.TYPE_NULL // desabilita o teclado
                isFocusable = false // desabilita o foco do campo
                isClickable = true // permite que o campo seja clicado
                hint = context.getString(R.string.prevision_date)

                // quando inputPrevisionDate for clicado, exibe o diálogo de seleção de data
                setOnClickListener {
                    // MaterialDatePicker é um diálogo de seleção de data
                    val datePicker = datePicker("Selecione a data prevista de conclusão")

                    // quando o usuário clicar em OK, o valor é salvo no campo inputPrevisionDate
                    datePicker.addOnPositiveButtonClickListener { selection ->
                        // converter a data selecionada para uma data local
                        val instant = Instant.ofEpochMilli(selection).plusSeconds(12 * 60 * 60)
                        val zoneId = ZoneId.systemDefault() // o fuso horário padrão do dispositivo
                        val localDate = instant.atZone(zoneId).toLocalDate() // converter para data local

                        val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        setText(formattedDate)
                    }
                    datePicker.show(parentFragmentManager, "DATE_PICKER") // exibe o diálogo
                }
            }

            val inputPrevisionHour = EditText(context).apply {
                inputType = InputType.TYPE_NULL
                isFocusable = false
                isClickable = true
                hint = context.getString(R.string.prevision_hour)

                setOnClickListener {
                    // MaterialTimePicker é um diálogo de seleção de hora
                    val hourPicker = hourPicker("Selecione a hora prevista de conclusão")

                    hourPicker.addOnPositiveButtonClickListener {
                        val hour = hourPicker.hour
                        val minute = hourPicker.minute
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                        setText(formattedTime)
                    }
                    hourPicker.show(parentFragmentManager, "HOUR_PICKER")
                }
            }

            // adiciona os campos ao layout
            layout.addView(inputName)
            layout.addView(inputDescription)
            layout.addView(inputPrevisionDate)
            layout.addView(inputPrevisionHour)

            // dialogo principal
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.add_task_dialog))
                .setView(layout) // layout criado acima
                .setPositiveButton(getString(R.string.button_add)) { _, _ ->
                    val name = inputName.text.toString().trim()
                    val description = inputDescription.text.toString().trim()
                    val previsionDate = inputPrevisionDate.text.toString().trim()
                    var previsionHour = inputPrevisionHour.text.toString().trim()

                    // impede que hora seja escrita sem uma data
                    if (previsionDate.isEmpty()) {
                        previsionHour = ""
                    }

                    // se houver pelo menos um nome, cria a tarefa
                    if (name.isNotEmpty()) {
                        val newTask = Task(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            description = description,
                            startDate = date().fullDate, // data atual
                            previsionDate = previsionDate,
                            previsionHour = previsionHour,
                            finishDate = null // inicia 'em progresso'
                        )
                        // adiciona a tarefa à lista e notifica o adapter
                        currentDependent?.tasks?.add(newTask)
                        adapter.notifyItemInserted(taskList.size - 1)

                        // persiste o usuário
                        userViewModel.persistUser(context, userViewModel.user.value)
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show()
        }
    }
}
