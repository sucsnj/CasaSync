package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.House
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.utils.JsonStorageManager
import com.devminds.casasync.views.HouseViewModel
import com.devminds.casasync.views.UserViewModel
import java.util.UUID

class HouseFragment : Fragment(R.layout.fragment_house) {

    private lateinit var adapter: GenericAdapter<Dependent>
    private val userViewModel: UserViewModel by activityViewModels()
    private val houseViewModel: HouseViewModel by activityViewModels()
    private var currentHouse: House? = null
    private val dependentList: MutableList<Dependent>
        get() = currentHouse?.dependents ?: mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val houseName = view.findViewById<TextView>(R.id.houseName)

        val recyclerDependents = view.findViewById<RecyclerView>(R.id.recyclerDependents)
        recyclerDependents.layoutManager = LinearLayoutManager(requireContext())

        val houseId = arguments?.getString("houseId")

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            currentHouse = user?.houses?.find { it.id == houseId }
            currentHouse?.let { houseViewModel.setHouse(it) }

            adapter = GenericAdapter(
                items = dependentList,
                layoutResId = R.layout.item_generic,
                bind = { itemView, dependent ->
                    itemView.findViewById<TextView>(R.id.itemName).text = dependent.name
                },
                onItemClick = { selectedDependent ->
                    val fragment = DependentFragment().apply {
                        arguments = Bundle().apply {
                            putString("dependentId", selectedDependent.id)
                        }
                    }

                    parentFragmentManager.beginTransaction()
                        .setCustomTransition(TransitionType.SLIDE)
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }
            )

            recyclerDependents.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        houseViewModel.house.observe(viewLifecycleOwner) { house ->
            houseName.text = "Você está na casa, ${house?.name ?: "Bolsão"}"
        }

        val btnAddDependent = view.findViewById<TextView>(R.id.btnAddDependent)
        btnAddDependent.setOnClickListener {

            val context = requireContext()
            val input = EditText(context).apply {
                hint = "Nome do dependente"
            }

            AlertDialog.Builder(context)
                .setTitle("Adicionar novo dependente")
                .setView(input)
                .setPositiveButton("Adicionar") { _, _ ->
                    val dependentName = input.text.toString().trim()
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
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
