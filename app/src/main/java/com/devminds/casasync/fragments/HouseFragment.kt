package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.House
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.utils.Utils
import android.widget.Toast
import com.devminds.casasync.views.HouseViewModel
import com.devminds.casasync.views.UserViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.UUID
import com.devminds.casasync.TransitionType
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.fragments.HomeFragment

class HouseFragment : Fragment(R.layout.fragment_house) {

    private lateinit var adapter: GenericAdapter<Dependent>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseViewModel: HouseViewModel by activityViewModels()
    private var currentHouse: House? = null
    private val dependentList: MutableList<Dependent>
        get() = currentHouse?.dependents ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<MaterialToolbar>(R.id.topBar)
        houseViewModel.house.observe(viewLifecycleOwner) { house ->
            toolbar.title = house?.name ?: "Casa"
        }

        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        toolbar.inflateMenu(R.menu.topbar_menu)

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.more_options -> {
                    Toast.makeText(
                        context,
                        "implementando mais opções",
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                else -> false
            }
        }

        val recyclerDependents = view.findViewById<RecyclerView>(R.id.recyclerDependents)
        recyclerDependents.layoutManager = LinearLayoutManager(requireContext())

        val houseId = arguments?.getString("houseId")

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentHouse = user?.houses?.find { it.id == houseId }
            currentHouse?.let { houseViewModel.setHouse(it) }

            val recycler = recyclerDependents

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
                context = requireContext()
            )

            recycler.adapter = adapter
            val position = dependentList.indexOfFirst { it.id == houseId }
            if (position != -1) {
                adapter.notifyItemChanged(position)
            }
        }

        val btnAddDependent = view.findViewById<TextView>(R.id.btnAddDependent)
        btnAddDependent.setOnClickListener {

            val context = requireContext()

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 40, 50, 10)
            }

            val inputName = EditText(context).apply {
                hint = context.getString(R.string.dependent_name_prompt)
            }

            layout.addView(inputName)

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
                            JsonStorageManager.saveUser(requireContext(), it)
                        }
                    }
                }
                .setNegativeButton(getString(R.string.button_cancel), null)
                .show()
        }
    }
}
