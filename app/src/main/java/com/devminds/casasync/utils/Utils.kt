package com.devminds.casasync.utils

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
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.devminds.casasync.FirestoreHelper
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
import com.google.firebase.firestore.FirebaseFirestore

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
    fun renameDialogItem(
        activity: Activity,
        itemName: String
    ): Pair<View, EditText> { // retorna um par, uma View e um EditText
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_rename_item, null)
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
            bind = { itemView, item, position, viewHolder ->

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false
                    val activity = context

                    val options = arrayOf(
                        activity.getString(R.string.rename_dialog),
                        activity.getString(R.string.delete_dialog)
                    )

                    AlertDialog.Builder(activity)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = renameDialogItem(
                                        activity,
                                        item.name
                                    )
                                    val dialogNameEdit = AlertDialog.Builder(activity)
                                        .setTitle(activity.getString(R.string.rename_dialog))
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .setPositiveButton(activity.getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                recycler.adapter?.notifyItemChanged(position)

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser(context)
                                                }

                                                DialogUtils.showMessage(
                                                    activity,
                                                    successRenameToast
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            activity.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .create()
                                    dialogNameEdit.setOnShowListener {
                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(activity, 100)
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

                                                userViewModel.user.value?.let {
                                                    userViewModel.deleteHouse(context, item.id)
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
            bind = { itemView, item, position, viewHolder ->

                // esconde a imagem
                val imageView = itemView.findViewById<ImageView>(R.id.itemImage)
                imageView.visibility = View.GONE
                
                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false
                    val activity = context

                    val options = arrayOf(
                        activity.getString(R.string.rename_dialog),
                        activity.getString(R.string.delete_dialog)
                    )

                    AlertDialog.Builder(activity)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = renameDialogItem(
                                        activity,
                                        item.name
                                    )
                                    val dialogNameEdit = AlertDialog.Builder(activity)
                                        .setTitle(activity.getString(R.string.rename_dialog))
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .setPositiveButton(activity.getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                recycler.adapter?.notifyItemChanged(position)

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser(context)
                                                }

                                                DialogUtils.showMessage(
                                                    activity,
                                                    successRenameToast
                                                )
                                            }
                                        }
                                        .setNegativeButton(
                                            activity.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .create()
                                    dialogNameEdit.setOnShowListener {
                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(activity, 100)
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

                                                userViewModel.user.value?.let {
                                                    // userViewModel.persistAndSyncUser(context)
                                                    userViewModel.deleteDependent(context, item.houseId)
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
            bind = { itemView, item, position, viewHolder ->

                val textView = itemView.findViewById<TextView>(R.id.itemName)
                textView.text = item.name

                itemView.setOnLongClickListener {
                    if (context !is Activity) return@setOnLongClickListener false
                    val activity = context

                    // lógica para fazer aparecer o "Concluir tarefa"
                    options = if (item.finishDate != null) {
                        arrayOf(
                            activity.getString(R.string.rename_dialog),
                            activity.getString(R.string.delete_dialog)
                        )
                    } else {
                        arrayOf(
                            activity.getString(R.string.rename_dialog),
                            activity.getString(R.string.delete_dialog),
                            activity.getString(R.string.finish_dialog)
                        )
                    }

                    AlertDialog.Builder(activity)
                        .setTitle("$itemOptions ${item.name}")
                        .setItems(options) { _, which ->
                            when (which) {
                                0 -> {
                                    val (dialogView, editTextDialog) = renameDialogItem(
                                        activity,
                                        item.name
                                    )
                                    val dialogNameEdit = AlertDialog.Builder(activity)
                                        .setTitle(activity.getString(R.string.rename_dialog))
                                        .setView(dialogView)
                                        .setCancelable(false)
                                        .setPositiveButton(activity.getString(R.string.accept_dialog)) { _, _ ->
                                            val newName = editTextDialog.text.toString().trim()
                                            if (newName.isNotEmpty()) {
                                                item.name = newName
                                                // JsonStorageManager.saveUser(
                                                //     activity,
                                                //     userViewModel.user.value!!
                                                // )
                                                recycler.adapter?.notifyItemChanged(position)

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser(context)
                                                }

                                                DialogUtils.showMessage(
                                                    activity,
                                                    successRenameToast
                                                )
                                                TaskAlarmReceiver().scheduleNotification(
                                                    context,
                                                    item.id,
                                                    item.name,
                                                    "Menos de uma hora para ser concluída",
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
                                                    "Menos de um dia para ser concluída",
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
                                            activity.getString(R.string.cancel_dialog),
                                            null
                                        )
                                        .create()
                                    dialogNameEdit.setOnShowListener {
                                        editTextDialog.requestFocus()
                                        editTextDialog.keyboardDelay(activity, 100)
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

                                                userViewModel.user.value?.let {
                                                    userViewModel.persistAndSyncUser(context)
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
                                        userViewModel.persistAndSyncUser(context)
                                    }

                                    DialogUtils.showMessage(context, "Tarefa concluída")
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
            true
        } else {
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
        prefs.edit().clear().apply()

        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        if (context is Activity) {
            context.finish()
        }
    }
}
