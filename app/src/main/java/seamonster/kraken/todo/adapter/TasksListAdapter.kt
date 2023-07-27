package seamonster.kraken.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import seamonster.kraken.todo.databinding.TaskItemBinding
import seamonster.kraken.todo.listener.TaskItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.TextUtil

class TasksListAdapter(var data: List<Task>, private val listener: TaskItemListener) :
    RecyclerView.Adapter<TasksListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = data[holder.adapterPosition]
        with(holder.binding) {
            t = task
            chipDateTime.text =
                TextUtil.convertDateTime((listener as Fragment).requireContext(), task.dateTime)
            root.setOnClickListener { listener.onItemClick(task, holder.adapterPosition) }
            checkboxStar.setOnClickListener {
                listener.onItemImportantChanged(task, holder.adapterPosition)
            }
            checkboxCompleted.setOnClickListener {
                listener.onItemCompletedChanged(task, holder.adapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return data[position].id.toLong()
    }

    inner class ViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root)
}