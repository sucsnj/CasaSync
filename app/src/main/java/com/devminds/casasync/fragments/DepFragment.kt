package com.devminds.casasync.fragments

import android.util.Log
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.parts.Task
import com.devminds.casasync.utils.Animations
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.devminds.casasync.utils.PopupMenu
import com.devminds.casasync.views.DependentViewModel
import com.devminds.casasync.views.TaskViewModel
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ListenerRegistration
import com.devminds.casasync.utils.DialogUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange

class DepFragment : BaseFragment(R.layout.fragment_dependent) {

    private val dependentViewModel: DependentViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val taskViewModel: TaskViewModel by activityViewModels()
    private val taskList = mutableListOf<Task>()
    private lateinit var adapter: GenericAdapter<Task>
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var menuItemView: View
    private lateinit var btnNewTask: TextView
    private lateinit var recyclerTasks: RecyclerView
    private lateinit var recycler: RecyclerView
    private lateinit var photo: ImageView
    private lateinit var title: TextView
    private lateinit var loadingOverlay: View
    private lateinit var loadingImage: ImageView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var listenerRegistration: ListenerRegistration

    private fun resolveDependentId(): String {
        return activity?.intent?.getStringExtra("dependentId")
            ?: dependentViewModel.dependent.value?.id
            ?: getString(R.string.devminds_text)
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            setStatusBarColor(requireActivity().window, statusBarColor("notch"))
            loadingOverlay.visibility = View.VISIBLE
            Animations.startPulseAnimation(loadingImage)
        } else {
            Animations.stopPulseAnimation()
            loadingOverlay.visibility = View.GONE
        }
    }

    // função apenas para atualizar o adapater (tela do dependente)
    private fun updateAdapter(tasks: List<Task>) {
        updateAdapterGeneric(
            oldList = taskList,
            newList = tasks,
            adapter = adapter,
            areItemsTheSame = { old, new -> old.id == new.id },
            areContentsTheSame = { old, new -> old == new }
        )
    }

    fun syncFirestoreToApp() {
        FirestoreHelper.getDependentById(resolveDependentId()) { dependent ->
            dependent?.let {
                val dependentesRef = FirestoreHelper.getDb()
                    .collection("dependents")
                    .document(it.id)
                    .collection("tasks")

                dependentesRef.get().addOnSuccessListener { snapshot ->
                    val tasks = snapshot.documents.mapNotNull { doc -> doc.toObject(Task::class.java) }
                    updateAdapter(tasks)

                    it.tasks.clear()
                    it.tasks.addAll(tasks)

                    dependentViewModel.setDependent(it)

                    taskList.clear()
                    taskList.addAll(tasks)
//                    adapter.notifyItemRangeChanged(0, taskList.size) // não funciona para deletes
//                    adapter.notifyDataSetChanged() // muito pesado
                }
            }
        }
    }

    fun refreshPage(swipeRefresh: SwipeRefreshLayout) {
        swipeRefresh.setOnRefreshListener {
            syncFirestoreToApp()
            // encerra o efeito de refresh
            swipeRefresh.isRefreshing = false
        }
    }

    // envia uma notificação para o dependente usando um listener
    fun listenForDependentTasks(dependentId: String): ListenerRegistration {
        val db = FirebaseFirestore.getInstance()
        val taskRef = db.collection("dependents")
            .document(dependentId)
            .collection("tasks")

        return taskRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("Firestore", "Erro ao escutar mudanças de tarefas", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (change in snapshot.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            DialogUtils.showMessage(requireContext(), getString(R.string.new_task_text))
                            Log.d("Firestore", "Task adicionada: ${change.document.id}")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            DialogUtils.showMessage(requireContext(), getString(R.string.modified_task_text))
                            Log.d("Firestore", "Task modificada: ${change.document.id}")
                        }
                        DocumentChange.Type.REMOVED -> {
                            DialogUtils.showMessage(requireContext(), getString(R.string.removed_task_text))
                            Log.d("Firestore", "Task removida: ${change.document.id}")
                        }
                    }
                    // atualiza o adapter
                    val tasks = snapshot.documents.mapNotNull { doc -> doc.toObject(Task::class.java) }
                    updateAdapter(tasks)
                }
            }
        }
    }

    // remove o listener para evitar vazamento de memória
    override fun onStop() {
        super.onStop()
        listenerRegistration.remove()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        loadingOverlay = view.findViewById(R.id.loadingOverlay)
        loadingImage = view.findViewById(R.id.loadingImage)

        showLoading(true) // mostra loading

        clearNavHistory()

        syncFirestoreToApp()

        toolbar = view.findViewById(R.id.topBar)
        toolbar.navigationIcon = null // esconde o botão de voltar

        title = view.findViewById(R.id.title)

        // cabeçalho
        dependentViewModel.dependent.observe(viewLifecycleOwner) { dependent ->
            dependent?.let {
                // nome do dependent
                val welcome = getString(R.string.welcome_text) + dependent.name
                title.text = welcome

                val dependentId = it.id
                listenerRegistration = listenForDependentTasks(dependentId)

                // foto do dependent
                photo = view.findViewById(R.id.dependentPhoto)!! // foto
                dependent.photo.let { url ->
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.user_photo) // imagem padrão
                        .error(R.drawable.user_photo_error) // em caso de erro
                        .circleCrop() // arredonda a imagem
                        .into(photo)
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    showLoading(false)
                }, 200)
            }
        }

        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu
        // esconde o item de voltar para o início
        menu.findItem(R.id.action_homepage).isVisible = false

        // menu suspenso (3 pontos)
        menuItemView = toolbar.findViewById(R.id.more_options)

        // botão de menu suspenso
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    // mostra o menu suspenso
                    PopupMenu.show(context, menuItemView, this)
                    true
                }
                else -> false
            }
        }

        // cria uma tarefa não é ativa para dependente
        btnNewTask = view.findViewById(R.id.btnAddTask)
        btnNewTask.visibility = View.INVISIBLE

        // atualiza o adapter
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        refreshPage(swipeRefresh)

        // lista das tarefas
        recyclerTasks = view.findViewById(R.id.recyclerTasks)
        recyclerTasks.layoutManager = LinearLayoutManager(context)
        recycler = recyclerTasks

        // manuseia a lista de casas
        adapter = Utils.createTaskAdapterDep(
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
            dependentViewModel = dependentViewModel,
            userViewModel = userViewModel,
            taskViewModel = taskViewModel,
            context = context
        )
        recycler.adapter = adapter
    }
}
