package com.devminds.casasync.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class HouseFragment : BaseFragment(R.layout.fragment_house) {

    private lateinit var adapter: GenericAdapter<Dependent>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseViewModel: HouseViewModel by activityViewModels()
    private var currentHouse: House? = null
    private val dependentList: MutableList<Dependent>
        get() = currentHouse?.dependents ?: mutableListOf()
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
        recyclerDependents.layoutManager = LinearLayoutManager(context)

        houseId = arguments?.getString("houseId")
        // observa o usuário e a casa selecionada
        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentHouse = user?.houses?.find { it.id == houseId }
            currentHouse?.let { houseViewModel.setHouse(it) }
            // lista de dependentes
            val recycler = recyclerDependents
            // cria o adaptador
            adapter = Utils.createDependentAdapter(
                recycler = recyclerDependents,
                list = dependentList, // lista que será usada no adaptador
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
            // aplica o adaptador à lista de dependentes
            recycler.adapter = adapter
            val position = dependentList.indexOfFirst { it.id == houseId }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
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
            inputName.postDelayed({
                inputName.requestFocus() // traz o foco

                // levanta o teclado
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(inputName, InputMethodManager.SHOW_IMPLICIT)
                inputName.setSelection(0) // texto selecionado
            },200)

            layout.addView(inputName)
            // diálogo para adicionar dependente
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.btn_add_dependent))
                .setView(layout)
                .setPositiveButton(getString(R.string.button_add)) { _, _ ->
                    val dependentName = inputName.text.toString().trim()
                    if (dependentName.isNotEmpty()) {
                        val newDependent = Dependent(
                            id = UUID.randomUUID().toString(),
                            name = dependentName
                        )
                        houseViewModel.house.value?.dependents?.add(newDependent)
                        adapter.notifyItemInserted(dependentList.size - 1)

                        // persiste o usuário em json
                        val user = userViewModel.user.value
                        user?.let {
                            JsonStorageManager.saveUser(context, it)
                        }
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show()
        }
    }
}
