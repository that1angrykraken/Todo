package seamonster.kraken.todo.fragment

import android.app.Activity.*
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import seamonster.kraken.todo.activity.EditTaskActivity
import seamonster.kraken.todo.adapter.TasksListAdapter
import seamonster.kraken.todo.databinding.FragmentPageBinding
import seamonster.kraken.todo.listener.TaskItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.viewmodel.AppViewModel
import java.util.ArrayList

class PageFragment(private val pageIndex: Int) : Fragment(), TaskItemListener {
    private lateinit var binding: FragmentPageBinding
    private lateinit var adapter: TasksListAdapter
    private val viewModel: AppViewModel by viewModels()
    private var currentIndex: Int = -1

    companion object {
        const val TAG: String = "Tasks Fragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPageBinding.inflate(layoutInflater, container, false)
        initRecyclerView()
        return binding.root
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TasksListAdapter(ArrayList(), this)
        binding.recyclerView.adapter = adapter
        when (pageIndex) {
            2 -> viewModel.completedTasks.observe(requireActivity()) { updateUI(it) }
            1 -> viewModel.activeTasks.observe(requireActivity()) { updateUI(it) }
            0 -> viewModel.importantTasks.observe(requireActivity()) { updateUI(it) }
        }
    }

    private fun updateUI(tasks: List<Task>) {
        Log.d(TAG, "onCreateView: tasks size = ${tasks.size}")

        binding.textView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        adapter.data = tasks
        when (viewModel.lastAction) {
            0 -> adapter.notifyItemInserted(adapter.itemCount - 1)
            1 -> adapter.notifyDataSetChanged() // refresh the list
            2 -> adapter.notifyItemChanged(currentIndex)
            3 -> adapter.notifyItemRemoved(currentIndex)
        }
    }

    private val launcherTaskDetail: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                if (data != null) {

                    val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        data.getSerializableExtra("t", Task::class.java) // API 33+
                    else
                        data.getSerializableExtra("t") as Task

                    val action = data.getIntExtra("a", 1)
                    viewModel.lastAction = action
                    if (task != null) {
                        if (action == 3) viewModel.delete(task) else viewModel.upsert(task)
                    }
                }
            }
        }

    override fun onItemClick(task: Task, position: Int) {
        currentIndex = position
        val intent = Intent(requireContext(), EditTaskActivity::class.java)
        intent.putExtra("t", task)
        launcherTaskDetail.launch(intent)
    }

    override fun onItemCompletedChanged(task: Task, position: Int) {
        currentIndex = position
        viewModel.lastAction = 3
        viewModel.upsert(task)

        Log.d(TAG, "onItemCompletedChanged: name: ${task.title} - desc ${task.desc}")
    }

    override fun onItemImportantChanged(task: Task, position: Int) {
        currentIndex = position
        when (pageIndex) {
            1, 2 -> viewModel.lastAction = -1
            0 -> viewModel.lastAction = 3
        }
        viewModel.upsert(task)
    }
}