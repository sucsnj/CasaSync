package com.devminds.casasync.fragments

import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.GridLayoutManager
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.House
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.HouseViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.TransitionType
import com.devminds.casasync.utils.DialogUtils

class HouseFragment : BaseFragment(R.layout.fragment_house) {

    private lateinit var adapter: GenericAdapter<Dependent>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseViewModel: HouseViewModel by activityViewModels()
    private var currentHouse: House? = null
    private val dependentList = mutableListOf<Dependent>()
//    private val dependentList: MutableList<Dependent>
//        get() = currentHouse?.dependents ?: mutableListOf()
    private lateinit var toolbar: MaterialToolbar
    private lateinit var menu: Menu
    private lateinit var recyclerDependents: RecyclerView
    private var houseId: String? = null
    private lateinit var btnAddDependent: TextView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = requireContext()

        houseId = arguments?.getString("houseId")
        if (houseId.isNullOrBlank()) {
            DialogUtils.showMessage(context, "Erro: ID da casa não encontrado.")
            return
        }

        title = view.findViewById(R.id.title)
        toolbar = view.findViewById(R.id.topBar)
        // muda o título do cabeçalho
        houseViewModel.house.observe(viewLifecycleOwner) { house ->
            title.text = house?.name ?: "Casa"
        }
        // botão de voltar
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // menu suspenso (3 pontos)
        toolbar.inflateMenu(R.menu.topbar_menu)
        menu = toolbar.menu
        menu.findItem(R.id.more_options).isVisible = false
        // botões em menu suspenso
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_homepage -> {
                    replaceFragment(HomeFragment(), TransitionType.FADE) // muda para a home
                    true
                }
                else -> false
            }
        }
        // lista de dependentes
        recyclerDependents = view.findViewById(R.id.recyclerDependents)
        // recyclerDependents.layoutManager = GridLayoutManager(context, 2) // estilo grade
        // recyclerDependents.layoutManager = LinearLayoutManager(context) // estilo lista
        recyclerDependents.layoutManager = // estilo grade com espaçamento
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        // observa o usuário e a casa selecionada
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentHouse = user?.houses?.find { it.id == houseId }
            currentHouse?.let { houseViewModel.setHouse(it) }

            val userId = user?.id ?: return@observe
            houseId?.let { id ->
                val dependentsRef = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("houses")
                    .document(id)
                    .collection("dependentes")

                dependentsRef.get().addOnSuccessListener { snapshot ->
                    val dependents =
                        snapshot.documents.mapNotNull { it.toObject(Dependent::class.java) }

                    currentHouse?.dependents?.clear()
                    currentHouse?.dependents?.addAll(dependents)

                    dependentList.clear()
                    dependentList.addAll(dependents)

                    adapter.notifyDataSetChanged()
                }
            }

            // cria o adaptador
            adapter = Utils.createDependentAdapter(
                recycler = recyclerDependents,
                list = dependentList,
                fragmentFactory = { dependentId ->
                    DependentFragment().apply {
                        arguments = Bundle().apply {
                            putString("dependentId", dependentId)
                        }
                    }
                },
                fragmentManager = parentFragmentManager,
                itemOptions = getString(R.string.dependent_options),
                successRenameToast = getString(R.string.rename_success_dependent_toast),
                userViewModel = userViewModel,
                context = context
            )
            recyclerDependents.adapter = adapter
        }

        // botão de adicionar dependente
        btnAddDependent = view.findViewById(R.id.btnAddDependent)
        btnAddDependent.setOnClickListener {
            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(context).apply {
                hint = context.getString(R.string.dependent_name_prompt)
            }

            // teclado com delay
            delayEditText(inputName, context)

            layout.addView(inputName)
            // diálogo para adicionar dependente
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.btn_add_dependent))
                .setView(layout)
                .setCancelable(false)
                .setPositiveButton(getString(R.string.button_add)) { _, _ ->
                    val dependentName = inputName.text.toString().trim()
                    if (dependentName.isNotEmpty()) {
                        val newDependent = Dependent(
                            id = UUID.randomUUID().toString(),
                            name = dependentName
                        )
                        dependentList.add(newDependent)

                        houseViewModel.house.value?.dependents?.add(newDependent)
                        adapter.notifyItemInserted(dependentList.size - 1)

                        userViewModel.persistAndSyncUser(context)

                        DialogUtils.showMessage(context, "Dependente adicionado")
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show()
        }
    }
}
