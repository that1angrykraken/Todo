package seamonster.kraken.todo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.TaskItemBinding
import seamonster.kraken.todo.listener.TaskItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.AppUtil

class TasksListAdapter(private val listener: TaskItemListener) :
    RecyclerView.Adapter<TasksListAdapter.ViewHolder>() {

    var data: List<Task> = ArrayList()
        set(value) {
            val result: DiffUtil.DiffResult = DiffUtil.calculateDiff(CallBack(field, value))
            field = value
            result.dispatchUpdatesTo(this)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = data[holder.adapterPosition]
        with(holder.binding) {
            t = task
            if (task.dateTime != null) {
                chipDateTime.text =
                    AppUtil.convertDateTime(
                        (listener as Fragment).requireContext(),
                        task.dateTime!!
                    )
            }
            if (task.uncompleted) {
                textTitle.setTextAppearance(R.style.TaskTitleUncompleted)
            } else {
                textTitle.setTextAppearance(R.style.TaskTitle)
            }
            root.setOnClickListener { listener.onItemClick(task) }
            checkboxStar.setOnClickListener { listener.onItemImportantChanged(task) }
            checkboxCompleted.setOnClickListener { listener.onItemCompletedChanged(task) }
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

class CallBack(private val oldData: List<Task>, private val newData: List<Task>) :
    DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldData.size
    }

    override fun getNewListSize(): Int {
        return newData.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition].id == newData[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldData[oldItemPosition] == newData[newItemPosition]
    }
}