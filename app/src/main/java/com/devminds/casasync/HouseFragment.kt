package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HouseFragment : Fragment(R.layout.fragment_house) {

    // dependentes mockados
    class Dependent(
        val id: String,
        val name: String
    )

    private val dependentList = mutableListOf<Dependent>(
        Dependent("1", "Dependente 1"),
        Dependent("2", "Dependente 2"),
        Dependent("3", "Dependente 3")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddDependent = view.findViewById<TextView>(R.id.btnAddDependent)
        btnAddDependent.setOnClickListener {

            // TODO lógica para add dependente
        }

        val recyclerDependent = view.findViewById<RecyclerView>(R.id.recyclerDependent)
        recyclerDependent.layoutManager = LinearLayoutManager(requireContext())

        recyclerDependent.adapter = GenericAdapter(
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
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }
}




