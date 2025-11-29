package com.devminds.casasync.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.GenericAdapter
import com.devminds.casasync.HomeActivity
import com.devminds.casasync.R
import com.devminds.casasync.TransitionType
import com.devminds.casasync.fragments.TaskFragment
import com.devminds.casasync.parts.Dependent
import com.devminds.casasync.parts.House
import com.devminds.casasync.parts.Task
import com.devminds.casasync.setCustomTransition
import com.devminds.casasync.views.UserViewModel
import com.devminds.casasync.views.TaskViewModel
import androidx.core.content.edit
import com.devminds.casasync.FirestoreHelper
import com.devminds.casasync.MainActivity
import com.devminds.casasync.fragments.LoginFragment
import com.devminds.casasync.parts.User
import com.devminds.casasync.views.DependentViewModel
import android.text.InputType
import androidx.core.content.ContextCompat
import android.view.MotionEvent

import com.google.android.material.textfield.TextInputEditText

// classe utilitária
object Utils {

    private lateinit var options: Array<String>

    // função para converter dp para pixels
    fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale).toInt()
    }

    // chamar teclado com delay para TextView
    private fun TextView.keyboardDelay(context: Context, delay: Long) {
        if (context is Activity) {
            this.requestFocus()
            this.postDelayed({
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }, delay)
        }
    }

    // precisa de uma variável activity e uma String pro itemName
    @SuppressLint("InflateParams")
    fun renameDialogItem(
        activity: Activity,
        itemName: String
    ): Pair<View, EditText> { // retorna um par, uma View e um EditText
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_rename_item, null, false)
        val editText = dialogView.findViewById<EditText>(R.id.newNameItem)
        editText.setText(itemName)
        editText.setSelection(0, itemName.length)
        return Pair(dialogView, editText)
    }

    fun createHouseAdapter(
        recycler: RecyclerView,
        list: MutableList<House>,
        fragmentFactory: (String) -> Fragment,
        fragmentManager: FragmentManager,
        itemOptions: String,
        successRenameToast: String,
        userViewModel: UserViewModel,
        context: Context
    ): GenericAdapter<House> {
        return GenericAdapter(
            items = list,
            layoutResId = R.layout.item_generic,
            bind = { itemView, item, position, _ ->

                // manipula a imagem do recycler (item_generic)
                val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
                imageView.setImageResource(R.drawable.ico_house)

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false

                    val options = arrayOf(
                        context.getString(R.string.rename_dialog),
                        context.getString(R.string.delete_dialog)
                    )

                    AlertDialog.Builder(context)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = renameDialogItem(
                                        context,
                                        item.name
                                    )
                                    val dialogNameEdit = AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.rename_dialog))
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .setPositiveButton(context.getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                recycler.adapter?.notifyItemChanged(position)

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser()
                                                }

                                                DialogUtils.showMessage(
                                                    context,
                                                    successRenameToast
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            context.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .create()
                                    dialogNameEdit.setOnShowListener {
                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(context, 100)
                                    }
                                    dialogNameEdit.show()
                                }

                                1 -> {
                                    val itemNameDelete = item.name
                                    AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.delete_dialog))
                                        .setCancelable(false)
                                        .setMessage(
                                            context.getString(R.string.confirm_delete_dialog) +
                                                    itemNameDelete +
                                                    context.getString(R.string.question_mark)
                                        )
                                        .setPositiveButton(context.getString(R.string.delete_dialog)) { _, _ ->
                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
                                                recycler.adapter?.notifyItemRemoved(index)

                                                userViewModel.user.value?.let { user ->
                                                    user.houses.removeAll { it.id == item.id }

                                                    userViewModel.deleteHouse(item.id)

                                                    // deleta os dependentes associados à casa na collection de dependents
                                                    FirestoreHelper.deleteHouseAndDependents(item.id)
                                                }

                                                DialogUtils.showMessage(context,
                                                    itemNameDelete + context.getString(R.string.success_delete_dialog)
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            context.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .show()
                                }

                            }
                        }
                        .show()
                    true
                }
            },
            onItemClick = { selectedItem ->
                val targetFragment = fragmentFactory(selectedItem.id)

                fragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }

    fun createDependentAdapter(
        recycler: RecyclerView,
        list: MutableList<Dependent>,
        fragmentFactory: (String) -> Fragment,
        fragmentManager: FragmentManager,
        itemOptions: String,
        successRenameToast: String,
        userViewModel: UserViewModel,
        dependentViewModel: DependentViewModel,
        context: Context
    ): GenericAdapter<Dependent> {
        return GenericAdapter(
            items = list,
            layoutResId = R.layout.item_generic,
            bind = { itemView, item, position, _ ->

                // esconde a imagem
                val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
                imageView.visibility = View.GONE
                
                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false

                    val options = arrayOf(
                         context.getString(R.string.rename_dialog),
                        "Login: ${item.email}", // mostra o login
                        "Senha: ${item.passcode}", // mostra a senha
                        context.getString(R.string.delete_dialog)
                    )

                    AlertDialog.Builder(context)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                         val (dialogView, editTextDialog) = renameDialogItem(
                                             context,
                                             item.name
                                         )
                                         val dialogNameEdit = AlertDialog.Builder(context)
                                             .setTitle(context.getString(R.string.rename_dialog))
                                             .setView(dialogView)
                                             .setCancelable(false)
                                             .setPositiveButton(context.getString(R.string.accept_dialog)) { _, _ ->
                                                 val newName = editTextDialog.text.toString().trim()
                                                 if (newName.isNotEmpty()) {
                                                     item.name = newName
                                                     recycler.adapter?.notifyItemChanged(position)

                                                     userViewModel.user.value?.let {
                                                         userViewModel.persistAndSyncUser()
                                                     }

                                                     // renomeia o dependente na collection dependents
                                                     FirestoreHelper.renameDependent(item.id, newName)

                                                     DialogUtils.showMessage(
                                                         context,
                                                         successRenameToast
                                                     )
                                                 }
                                             }
                                             .setNegativeButton(
                                                 context.getString(R.string.cancel_dialog),
                                                 null
                                             )
                                             .create()
                                         dialogNameEdit.setOnShowListener {
                                             editTextDialog.requestFocus()
                                             editTextDialog.keyboardDelay(context, 100)
                                         }
                                         dialogNameEdit.show()
                                }

                                1 -> {
                                    // referência ao login
                                    return@setItems
                                }

                                2 -> {
                                    // referência a senha
                                    return@setItems
                                }

                                // 1 -> {
                                //     AlertDialog.Builder(context)
                                //         .setTitle(item.name)
                                //         .setMessage("Senha: ${item.passcode}")
                                //         .setCancelable(true)
                                //         .setPositiveButton("Fechar", null)
                                //         .show()

                                    // lógica desativada para uso futuro
                                    // edittext para permitir edição da senha com olho de tandera
                                    // val dialogView = LayoutInflater.from(context)
                                    //     .inflate(R.layout.password_dialog, null)
                                    // val editText = dialogView.findViewById<TextInputEditText>(R.id.passcodeEditText)
                                    // editText.setText(item.passcode)
                                    // editText.setSelection(editText.text?.length ?: 0)

                                    // AlertDialog.Builder(context)
                                    //     .setTitle(item.name)
                                    //     .setView(dialogView) // o edittext para editar a senha
                                    //     .setCancelable(true)
                                    //     .setPositiveButton(context.getString(R.string.accept_dialog)) { _, _ ->
                                    //         val newPasscode = editText.text.toString().trim()
                                    //         if (newPasscode.isNotEmpty()) {
                                    //             item.passcode = newPasscode
                                    //             recycler.adapter?.notifyItemChanged(position)

                                    //             // ainda não persiste no firestore
                                    //             userViewModel.persistAndSyncUser()
                                    //             dependentViewModel.persistAndSyncDependent()

                                    //             DialogUtils.showMessage(
                                    //                 context,
                                    //                 "Senha atualizada com sucesso!"
                                    //             )
                                    //         }
                                    //     }
                                    //     .setNegativeButton(context.getString(R.string.cancel_dialog), null)
                                    //     .show()
                                // }

                                3 -> {
                                    val itemNameDelete = item.name
                                    AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.delete_dialog))
                                        .setCancelable(false)
                                        .setMessage(
                                            context.getString(R.string.confirm_delete_dialog) +
                                                    itemNameDelete +
                                                    context.getString(R.string.question_mark)
                                        )
                                        .setPositiveButton(context.getString(R.string.delete_dialog)) { _, _ ->
                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
                                                recycler.adapter?.notifyItemRemoved(index)

                                                userViewModel.user.value?.let { user ->
                                                    // remover o dependente do user
                                                    user.houses.find { it.id == item.houseId }?.let { house ->
                                                        house.dependents.removeAll { it.id == item.id }
                                                    }
                                                    
                                                    userViewModel.deleteDependent(item.houseId, item.id)
                                                    FirestoreHelper.deleteDependentForDependent(item.id)
                                                }

                                                DialogUtils.showMessage(context,
                                                    itemNameDelete + context.getString(R.string.success_delete_dialog)
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            context.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .show()
                                }

                            }
                        }
                        .show()
                    true
                }
            },
            onItemClick = { selectedItem ->
                val targetFragment = fragmentFactory(selectedItem.id)

                fragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }

    fun createTaskAdapter(
        recycler: RecyclerView,
        list: MutableList<Task>,
        fragmentFactory: (String) -> Fragment,
        fragmentManager: FragmentManager,
        itemOptions: String,
        successRenameToast: String,
        userViewModel: UserViewModel,
        dependentViewModel: DependentViewModel,
        taskViewModel: TaskViewModel, // precisa para acessar o TaskViewModel com as datas e horas
        context: Context
    ): GenericAdapter<Task> {
        return GenericAdapter(
            items = list,
            layoutResId = R.layout.item_generic,
            bind = { itemView, item, position, _ ->

                // manipula a imagem do recycler (item_generic)
                val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
                imageView.setImageResource(R.drawable.ico_task)

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false

                    // lógica para fazer aparecer o "Concluir tarefa"
                    options = if (item.finishDate != null) {
                        arrayOf(
                            context.getString(R.string.rename_dialog),
                            context.getString(R.string.delete_dialog)
                        )
                    } else {
                        arrayOf(
                            context.getString(R.string.rename_dialog),
                            context.getString(R.string.delete_dialog),
                            context.getString(R.string.finish_dialog)
                        )
                    }

                    AlertDialog.Builder(context)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = renameDialogItem(
                                        context,
                                        item.name
                                    )
                                    val dialogNameEdit = AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.rename_dialog))
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .setPositiveButton(context.getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                recycler.adapter?.notifyItemChanged(position)

                                                dependentViewModel.updateTask(item)
                                                userViewModel.updateTask(item.houseId, item.dependentId, item)

                                                userViewModel.persistAndSyncUser()
                                                dependentViewModel.persistAndSyncDependent()

                                                DialogUtils.showMessage(
                                                    context,
                                                    successRenameToast
                                                )
                                                TaskAlarmReceiver().scheduleNotification(
                                                    context,
                                                    item.id,
                                                    item.name,
                                                    context.getString(R.string.less_than_one_hour),
                                                    DateUtils.minusHour(
                                                        item.previsionDate,
                                                        item.previsionHour,
                                                        1
                                                    ),
                                                    "hour"
                                                )
                                                TaskAlarmReceiver().scheduleNotification(
                                                    context,
                                                    item.id,
                                                    item.name,
                                                    context.getString(R.string.less_than_one_day),
                                                    DateUtils.minusDay(
                                                        item.previsionDate,
                                                        item.previsionHour,
                                                        1
                                                    ),
                                                    "day"
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            context.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .create()
                                    dialogNameEdit.setOnShowListener {
                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(context, 100)
                                    }
                                    dialogNameEdit.show()
                                }

                                1 -> {
                                    val itemNameDelete = item.name
                                    AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.delete_dialog))
                                        .setCancelable(false)
                                        .setMessage(
                                            context.getString(R.string.confirm_delete_dialog) +
                                                    itemNameDelete +
                                                    context.getString(R.string.question_mark)
                                        )
                                        .setPositiveButton(context.getString(R.string.delete_dialog)) { _, _ ->
                                            TaskAlarmReceiver().cancelAllTaskNotifications(context, item)

                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
                                                recycler.adapter?.notifyItemRemoved(index)

                                                // Atualiza nos dois lados
                                                dependentViewModel.deleteTask(item.id)
                                                userViewModel.deleteTask(item.houseId, item.dependentId, item.id)

                                                // Persiste
                                                userViewModel.persistAndSyncUser()
                                                dependentViewModel.persistAndSyncDependent()

                                                DialogUtils.showMessage(context,
                                                    itemNameDelete + context.getString(R.string.success_delete_dialog)
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            context.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .show()
                                }

                                2 -> {
                                    item.finishDate = DateUtils.date(0).fullDate
                                    recycler.adapter?.notifyItemChanged(position)

                                    // Atualiza nos dois lados
                                    dependentViewModel.updateTask(item)
                                    userViewModel.updateTask(item.houseId, item.dependentId, item)

                                    // Persiste
                                    userViewModel.persistAndSyncUser()
                                    dependentViewModel.persistAndSyncDependent()

                                    DialogUtils.showMessage(context,
                                        context.getString(R.string.task_finished))
                                }
                            }
                        }
                        .show()
                    true
                }
            },
            onItemClick = { selectedItem ->
                val targetFragment = fragmentFactory(selectedItem.id)

                fragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }

    fun createTaskAdapterDep(
        list: MutableList<Task>,
        fragmentFactory: (String) -> Fragment,
        fragmentManager: FragmentManager,
        itemOptions: String,
        dependentViewModel: DependentViewModel,
        userViewModel: UserViewModel,
        taskViewModel: TaskViewModel,
        context: Context
    ): GenericAdapter<Task> {
        return GenericAdapter(
            items = list,
            layoutResId = R.layout.item_generic,
            bind = { itemView, item, _, _ ->

                // manipula a imagem do recycler (item_generic)
                val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
                imageView.setImageResource(R.drawable.ico_task)

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false

                    // lógica para fazer aparecer o "Concluir tarefa"
                    options = if (item.finishDate != null) {
                        arrayOf(
                            context.getString(R.string.open)
                        )
                    } else {
                        arrayOf(
                            context.getString(R.string.open),
                            context.getString(R.string.finish_dialog)
                        )
                    }

                    AlertDialog.Builder(context)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    fragmentManager.beginTransaction()
                                        .setCustomTransition(TransitionType.SLIDE)
                                        .replace(R.id.fragment_container, TaskFragment())
                                        .addToBackStack(null)
                                        .commit()
                                }

                                1 -> {
                                    val date = DateUtils.date(0).fullDate // data atual

                                    // Atualiza o objeto da task
                                    item.finishDate = date

                                    // Atualiza no ViewModel do dependent
                                    dependentViewModel.updateTask(item)

                                    // Atualiza também no Firestore (dependents + users)
                                    val houseId = item.houseId
                                    val depId = item.dependentId
                                    userViewModel.updateTask(houseId, depId, item)

                                    // Persiste nos dois lados
                                    userViewModel.persistAndSyncUser()
                                    dependentViewModel.persistAndSyncDependent()

                                    taskViewModel.task.value?.let { task ->
                                        val previsionDate = task.previsionDate
                                        val previsionHour = task.previsionHour

                                        TaskAlarmReceiver().cancelAllTaskNotifications(context, item)

                                        item.finishDate = DateUtils.date(0).fullDate
                                        item.previsionDate = previsionDate
                                        item.previsionHour = previsionHour
                                    }

                                    userViewModel.user.value?.let {
                                        userViewModel.persistAndSyncUser()
                                    }

                                    DialogUtils.showMessage(context,
                                        context.getString(R.string.task_finished))
                                }
                            }
                        }
                        .show()
                    true
                }
            },
            onItemClick = { selectedItem ->
                val targetFragment = fragmentFactory(selectedItem.id)

                fragmentManager.beginTransaction()
                    .setCustomTransition(TransitionType.SLIDE)
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit()
            }
        )
    }

    // checa conexão com a internet
    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // checa se há um usuário logado
    fun isLogged(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val id = prefs.getString("logged_id", null)
        val role = prefs.getString("logged_role", null)
        // var idRole = id // será modificado para procurar no Firestore

        // procurar usuário no firestore
        if (role == "admin" && id != null) {
            FirestoreHelper.getUserById(id) { user ->
                if (user == null) {
                    DialogUtils.showMessage(context, "Usuário não encontrado. Fazendo logout.")
                    Log.d("Utils", "Nenhum usuário encontrado no Firestore com o ID: $id")
                    logout(context)
                }
            }
        } else if (role == "dependent" && id != null) {
            FirestoreHelper.getDependentById(id) { dependent ->
                if (dependent == null) {
                    DialogUtils.showMessage(context, "Dependente não encontrado. Fazendo logout.")
                    Log.d("Utils", "Nenhum dependente encontrado no Firestore com o ID: $id")
                    logout(context)
                }
            }
        }

        return if (!id.isNullOrEmpty() && !role.isNullOrEmpty()) {
            val intent = Intent(context, HomeActivity::class.java).apply {
                when (role) {
                    "admin" -> putExtra("userId", id)
                    "dependent" -> putExtra("dependentId", id)
                }
                putExtra("role", role)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            Log.d("LoginFragment", "Usuário já está logado como $role.")
            true
        } else {
            Log.d("LoginFragment", "Nenhum usuário salvo nas prefs.")
            false
        }
    }

    // salva o id logado nas shared preferences
    fun saveLoginToPrefs(context: Context, id: String, role: String) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("logged_id", id)
            putString("logged_role", role) // "admin" ou "dependent"
        }
    }

    // remove usuário do SharedPreferences
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            remove("logged_id")
            remove("logged_role")
        }

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is Activity) {
            context.finish()
        }
    }

    fun login(context: Context, userViewModel: UserViewModel, user: User) {
        DialogUtils.dismissActiveBanner()

        userViewModel.setUser(user)
        userViewModel.persistAndSyncUser()

        val intent = Intent(context, HomeActivity::class.java).apply {
            putExtra("userId", user.id)
            putExtra("role", "admin")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        val biometric = Biometric()
        biometric.saveBiometricAuthUser(context, user.id, "admin")
        biometric.lastLoggedUser(context, user.id, "admin")

        saveLoginToPrefs(context, user.id, "admin")
    }
}
