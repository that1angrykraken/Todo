package seamonster.kraken.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.ListItemBinding
import seamonster.kraken.todo.listener.ListItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.model.TaskList
import java.util.ArrayList

class ListSelectorAdapter(
    var data: List<TaskList>,
    private val currentListId: Int,
    private val fragment: Fragment
) : RecyclerView.Adapter<ListSelectorAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            l = data[holder.adapterPosition]
            this.currentId = currentListId
            root.setOnClickListener { (fragment as ListItemListener).onItemClicked(l!!.id) }
        }
    }
}