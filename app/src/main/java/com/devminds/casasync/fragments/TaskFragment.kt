package com.devminds.casasync.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devminds.casasync.FirestoreHelper
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
import com.devminds.casasync.utils.PermissionHelper
import com.google.firebase.firestore.ListenerRegistration

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
    private val listeners = mutableListOf<ListenerRegistration>()

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
                        context.getString(R.string.less_than_one_hour),
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
                        context.getString(R.string.less_than_one_day),
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

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_granted))
            } else {
                DialogUtils.showMessage(requireContext(), getString(R.string.notification_permission_denied))
            }
        }

    fun saveTask(context: Context, item: String, itemValue: String?) {
        taskViewModel.task.value?.let { task ->
            when (item) {
                "description" -> itemValue?.let { task.description = it }
                "previsionDate" -> itemValue?.let { task.previsionDate = it }
                "previsionHour" -> itemValue?.let { task.previsionHour = it }
                "finishDate" -> task.finishDate = itemValue
            }

            dependentViewModel.updateTask(task)

            val houseId = task.houseId
            val depId = task.dependentId
            userViewModel.updateTask(houseId, depId, task)

            userViewModel.persistAndSyncUser()
            dependentViewModel.persistAndSyncDependent()

            // agenda notificações
            scheduleTaskNotification(context, taskViewModel)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!PermissionHelper.hasNotificationPermission(requireContext())) {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            Log.d("TaskFragment", "Sincronizado com sucesso")
        }
    }

    fun syncFirestoreToApp() {
        val userId = userViewModel.user.value?.id
        userId?.let { id ->
            FirestoreHelper.syncFirestoreToUser(id) { result ->
                if (result != null) {

                    // atualiza o model do usuário (tb na tela)
                    userViewModel.setUser(result)

                    // atualiza o dependente
                    val currentDepId = dependentViewModel.dependent.value?.id
                    val updatedDep = result.houses
                        .flatMap { it.dependents }
                        .find { it.id == currentDepId }

                    if (updatedDep != null)
                        dependentViewModel.setDependent(updatedDep)

                    // atualiza a tarefa
                    val currentTaskId = taskViewModel.task.value?.id
                    val updatedTask = updatedDep?.tasks?.find { it.id == currentTaskId }

                    if (updatedTask != null)
                        taskViewModel.setTask(updatedTask)

                    Log.d("TaskFragment", "Sincronizado com sucesso")
                } else {
                    Log.d("TaskFragment", "Erro ao sincronizar o usuário")
                }
            }
        }
    }

    fun syncFirestoreToAppDep() {
        val depId = dependentViewModel.dependent.value?.id
        depId?.let { depId ->
            FirestoreHelper.syncFirestoreToDependent(depId) { result ->
                if (result != null) {
                    dependentViewModel.setDependent(result)
                    // também atualiza a task atual se precisar
                    val currentTaskId = taskViewModel.task.value?.id
                    val updatedTask = result.tasks.find { depId == currentTaskId }
                    if (updatedTask != null) {
                        taskViewModel.setTask(updatedTask)
                    }
                }
            }
        }
    }

    // atualiza a UI
    fun refreshPage(swipeRefresh: SwipeRefreshLayout, userViewModel: UserViewModel) {
        swipeRefresh.setOnRefreshListener {
            // pega o usuário atual do ViewModel
            val user = userViewModel.user.value
            // pega o dependente atual do ViewModel -> dependente vindo do login
            val dep = dependentViewModel.dependent.value

            if (user != null) {
                syncFirestoreToApp() // fluxo do admin
            } else if (dep != null) {
                syncFirestoreToAppDep() // fluxo do dependent
            }
            // encerra o efeito de refresh
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onStop() {
        super.onStop()
        listeners.forEach { it.remove() }
        listeners.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        title = view.findViewById(R.id.title)
        subtitle = view.findViewById(R.id.subtitle)
        taskDescription = view.findViewById(R.id.taskDescription)
        checker = view.findViewById(R.id.checker)

        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        refreshPage(swipeRefresh, userViewModel)

        // variáveis para condicionar o tipo de login
        val user = userViewModel.user.value
        val dep = dependentViewModel.dependent.value

        // id da task atual
        taskId = arguments?.getString("taskId") // inicializada pela primeira vez

        // listener para detectar mudanças na task em tempo real para a UI
        listeners.add(listenDocumentRealtime(
            documentPath = "dependents/${dep?.id}/tasks/$taskId", // caminho da collection para a task
            clazz = Task::class.java, // classe modelo
            onUpdate = { updatedTask ->
                updatedTask?.let {
                    // atualiza o ViewModel e a UI para ambos os logins
                    taskViewModel.setTask(it)
                    title.text = it.name
                    taskDescription.text = it.description
                    subtitle.text = it.previsionDate ?: ""
                }
            }
        ))

        // observa a tarefa selecionada
        taskViewModel.task.observe(viewLifecycleOwner) { task ->
            title.text = task?.name ?: "Tarefa" // nome no título
            taskDescription.text = task?.description ?: getString(R.string.no_description) // descrição da tarefa
            // se a descrição estiver vazia, mostra "Sem descrição" na dica
            if (task?.description.toString() == "") {
                taskDescription.hint = getString(R.string.no_description)
            }

            // alterar descrição
            taskDescription.setOnClickListener {
                if (user != null) {
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
                        .setTitle(getString(R.string.edit_description))
                        .setView(layout)
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.cancel_dialog)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .setPositiveButton(getString(R.string.save_dialog)) { dialog, _ ->
                            val newDescription = editText.text.toString()
                            taskDescription.text = newDescription

                            // atualizar descrição na task e no dependent e persiste no usuário
                            saveTask(context, item = "description", newDescription)
                            dialog.dismiss() // fecha o diálogo
                        }
                        .show() // mostra o diálogo
                } else if (dep != null) {
                    DialogUtils.showMessage(context, getString(R.string.permission_denied))
                }
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
            startDate.text = task?.startDate ?: getString(R.string.not_finished)
            previsionDate.setText(task?.previsionDate ?: getString(R.string.not_finished))
            if (previsionDate.text.toString() == "") { // impede que hora seja escrito sem uma data
                previsionHour.isEnabled = false
            }
            previsionHour.setText(task?.previsionHour ?: getString(R.string.no_hour))
            finishDate.text = task?.finishDate ?: getString(R.string.not_finished)
            checker.isChecked = task?.finishDate != null
        }

        // data de conclusão prevista
        previsionDate = view.findViewById(R.id.previsionDate)
        previsionDate.setOnClickListener {
            if (user != null) {
                val datePicker = DatePickers.datePicker(getString(R.string.expected_date))

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
            } else if (dep != null) {
                DialogUtils.showMessage(context, getString(R.string.permission_denied))
            }
        }

        // hora de conclusão prevista
        previsionHour = view.findViewById(R.id.previsionHour)
        previsionHour.setOnClickListener {
            if (user != null) {
                val hourPicker = DatePickers.hourPicker(getString(R.string.expected_hour))

                hourPicker.addOnPositiveButtonClickListener {
                    val hour = hourPicker.hour
                    val minute = hourPicker.minute
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                    previsionHour.setText(formattedTime)
                    // atualiza a tarefa e o usuário no json
                    saveTask(context, item = "previsionHour", formattedTime)
                }
                hourPicker.show(parentFragmentManager, "HOUR_PICKER") // mostra o hourPicker (hora)
            } else if (dep != null) {
                DialogUtils.showMessage(context, getString(R.string.permission_denied))
            }
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

        taskId = arguments?.getString("taskId") // deve ser inicializada novamente para o ciclo da fragment não quebrar

        // observa o dependente selecionado
        subtitle = view.findViewById(R.id.subtitle)
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            subtitle.text = dependent?.name ?: getString(R.string.dependent) // nome do dependente no subtítulo

            currentTask = dependent?.tasks?.find { it.id == taskId } // seta a tarefa atual
            currentTask?.let { taskViewModel.setTask(it) } // atualiza a tarefa no ViewModel
        }

        // botão de salvar
        btnSaveTask = view.findViewById(R.id.btnSaveTask)
        btnSaveTask.setOnClickListener {
            val task = taskViewModel.task.value
            if (task != null) {
                userViewModel.persistAndSyncUser()
                DialogUtils.showMessage(context, getString(R.string.task_saved))
            }
        }
    }
}
