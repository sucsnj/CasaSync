package com.devminds.casasync.fragments

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import com.devminds.casasync.utils.Utils
import com.devminds.casasync.views.HouseViewModel
import com.devminds.casasync.views.UserViewModel
import java.util.UUID
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo

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
                bind = { itemView, dependent, position, viewHolder ->
                                val editText = itemView.findViewById<EditText>(R.id.itemName)
                                editText.setText(dependent.name)
                                editText.isEnabled = false
                                editText.isFocusable = false

                                    itemView.setOnLongClickListener {
                                        editText.isEnabled = true
                                        editText.isFocusableInTouchMode = true
                                        editText.requestFocus()
                                        editText.setSelection(editText.text.length)

                                        val imm = itemView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

                                        editText.setOnEditorActionListener { _, actionId, _ ->
                                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                                val newName = editText.text.toString().trim()
                                                if (newName.isNotEmpty()) {
                                                    dependent.name = newName
                                                    JsonStorageManager.saveUser(itemView.context, userViewModel.user.value!!)
                                                    adapter.notifyItemChanged(position)
                                                    Toast.makeText(itemView.context, "Dependente renomeado", Toast.LENGTH_SHORT).show()
                                                }
                                                editText.isEnabled = false
                                                editText.isFocusable = false
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        true
                                    }
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
                },
                onItemLongClick = { selectedDependent ->
                    val options = arrayOf("Renomear", "Apagar")

                    AlertDialog.Builder(requireContext())
                        .setTitle("Opções para o dependente \"${selectedDependent.name}\"")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    Toast.makeText(
                                        requireContext(),
                                        "Renomear dependente (em breve)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                1 -> {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Apagar Dependente")
                                        .setMessage("Tem certeza que deseja apagar o dependente \"${selectedDependent.name}\"?")
                                        .setPositiveButton("Apagar") { _, _ ->
                                            val index = dependentList.indexOfFirst { it.id == selectedDependent.id }
                                            if (index != -1) {
                                                dependentList.removeAt(index)
                                                adapter.notifyItemRemoved(index)

                                                // salva o usuário com dependente removido
                                                userViewModel.user.value?.let {
                                                    JsonStorageManager.saveUser(requireContext(), it)
                                                }

                                                Toast.makeText(
                                                    requireContext(),
                                                    "Dependente apagado com sucesso",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                        .setNegativeButton("Cancelar", null)
                                        .show()
                                }
                            }
                        }
                        .show()
                    true
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
