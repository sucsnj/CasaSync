package com.devminds.casasync

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HouseFragment : Fragment(R.layout.fragment_house) {

    // membros mockados
    class Member(
        val id: String,
        val name: String
    )

    private val memberList = mutableListOf<Member>(
        Member("1", "Membro 1"),
        Member("2", "Membro 2"),
        Member("3", "Membro 3")
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnAddMember = view.findViewById<TextView>(R.id.btnAddMember)
        btnAddMember.setOnClickListener {

            // TODO lógica para add membro
        }

        val recyclerMembers = view.findViewById<RecyclerView>(R.id.recyclerMembers)
        recyclerMembers.layoutManager = LinearLayoutManager(requireContext())

        recyclerMembers.adapter = GenericAdapter(
            items = memberList,
            layoutResId = R.layout.item_generic,
            bind = { itemView, member ->
                itemView.findViewById<TextView>(R.id.itemName).text = member.name
            },
            onItemClick = { selectedMember ->
                val fragment = DependentFragment().apply {
                    arguments = Bundle().apply {
                        putString("memberId", selectedMember.id)
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




