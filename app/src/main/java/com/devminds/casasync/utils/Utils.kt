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
import com.devminds.casasync.MainActivity
import com.devminds.casasync.fragments.LoginFragment
import com.devminds.casasync.parts.User
import com.devminds.casasync.views.DependentViewModel

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
                        context.getString(R.string.viewPass),
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
                                    val itemPasscodeView = item.passcode
                                    AlertDialog.Builder(context)
                                        .setTitle(context.getString(R.string.viewPass))
                                        .setCancelable(true)
                                        .setMessage(itemPasscodeView)
                                        .show()
                                }

                                2 -> {
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

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser()
                                                }

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
                                            TaskFragment().cancelAllTaskNotifications(context, item)

                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
                                                recycler.adapter?.notifyItemRemoved(index)

                                                userViewModel.user.value?.let { user ->
                                                    // remover o task do user
                                                    user.houses.find { it.id == item.houseId }?.let { house ->
                                                        house.dependents.find { it.id == item.dependentId }?.let { dep ->
                                                            dep.tasks.removeAll { it.id == item.id }
                                                        }
                                                    }
                                                    userViewModel.deleteTask(item.houseId, item.dependentId, item.id)
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

                                2 -> {
                                    taskViewModel.task.value?.let { task -> 
                                        val previsionDate = task.previsionDate
                                        val previsionHour = task.previsionHour

                                        TaskFragment().cancelAllTaskNotifications(context, item)

                                        item.finishDate = DateUtils.date(0).fullDate
                                        item.previsionDate = previsionDate
                                        item.previsionHour = previsionHour
                                    }

                                    recycler.adapter?.notifyItemChanged(position)

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

    fun createTaskAdapterDep(
        recycler: RecyclerView,
        list: MutableList<Task>,
        fragmentFactory: (String) -> Fragment,
        fragmentManager: FragmentManager,
        itemOptions: String,
        successRenameToast: String,
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

                                                dependentViewModel.dependent.value?.let {
                                                    dependentViewModel.persistAndSyncDependent()
                                                }

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
                                            TaskFragment().cancelAllTaskNotifications(context, item)

                                            val index = list.indexOfFirst { it.id == item.id }
                                            if (index != -1) {
                                                list.removeAt(index)
                                                recycler.adapter?.notifyItemRemoved(index)

                                                dependentViewModel.dependent.value?.let { dependent ->
                                                    // remover o task do dependente
                                                    dependent.tasks.removeAll { it.id == item.id }

                                                    dependentViewModel.deleteTask(item.id)
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

                                2 -> {
                                    taskViewModel.task.value?.let { task ->
                                        val previsionDate = task.previsionDate
                                        val previsionHour = task.previsionHour

                                        TaskFragment().cancelAllTaskNotifications(context, item)

                                        item.finishDate = DateUtils.date(0).fullDate
                                        item.previsionDate = previsionDate
                                        item.previsionHour = previsionHour
                                    }

                                    recycler.adapter?.notifyItemChanged(position)

                                    dependentViewModel.dependent.value?.let {
                                        dependentViewModel.persistAndSyncDependent()
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

    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // checa se há um usuário logado
    fun isLogged(context: Context): Boolean {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("logged_user_id", null)

        return if (!userId.isNullOrEmpty()) {
            val intent = Intent(context, HomeActivity::class.java)
            intent.putExtra("userId", userId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            Log.d("LoginFragment", "Usuário já está logado.")
            true
        } else {
            Log.d("LoginFragment", "Nenhum usuário salvo nas prefs.")
            false
        }
    }

    // salva o id do usuário nas shared preferences
    fun saveUserToPrefs(context: Context, user: User) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString(
                "logged_user_id",
                user.id)
        }
    }

    fun saveDependentToPrefs(context: Context, dependent: Dependent) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString(
                "logged_user_id",
                dependent.id)
        }
    }

    fun checkIfUserIsLoggedIn(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getString("logged_user_id", null)

        if (!userId.isNullOrEmpty()) {
            // Usuário já está logado, tenta carregar os dados dele
            LoginFragment().loginWithUserId(userId)
        } else {
            // Nenhum usuário logado, segue com o fluxo normal de login
            Log.d("LoginFragment", "Nenhum usuário salvo nas prefs.")
        }
    }

    // remove usuário do SharedPreferences
    fun logout(context: Context) {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit { clear() }

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is Activity) {
            context.finish()
        }
    }
}
