
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import com.devminds.casasync.R

class TarefaAdapter(context: Context, private val lista: MutableList<Tarefa>)
    : ArrayAdapter<Tarefa>(context, 0, lista) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val tarefa = lista[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.tarefa, parent, false)

        val checkBox = view.findViewById<CheckBox>(R.id.checkBoxTarefa)
        checkBox.text = tarefa.titulo
        checkBox.isChecked = tarefa.concluida

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            tarefa.concluida = isChecked
        }

        return view
    }
}
