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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.EditTaskActivity
import seamonster.kraken.todo.adapter.TasksListAdapter
import seamonster.kraken.todo.databinding.FragmentPageBinding
import seamonster.kraken.todo.listener.TaskItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.viewmodel.AppViewModel
import java.util.ArrayList

class PageFragment : Fragment(), TaskItemListener {
    private lateinit var binding: FragmentPageBinding
    private lateinit var adapter: TasksListAdapter
    private lateinit var viewModel: AppViewModel
    private var currentIndex: Int = -1
    private var pageIndex: Int? = 0

    companion object {
        const val TAG: String = "PageFragment"

        private const val ARG_PAGE_INDEX = "ARG_PAGE_INDEX"

        fun newInstance(pageIndex: Int): PageFragment {
            val args = Bundle()
            val fragment = PageFragment()
            args.putInt(ARG_PAGE_INDEX, pageIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        pageIndex = arguments?.getInt(ARG_PAGE_INDEX, 0)
        viewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        binding = FragmentPageBinding.inflate(layoutInflater, container, false)
        initRecyclerView()
        initEmptyText()
        return binding.root
    }

    private fun initEmptyText() {
        if (pageIndex == 2) binding.textView.text = getText(R.string.no_completed_tasks)
    }

    private fun initRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TasksListAdapter(ArrayList(), this)
        binding.recyclerView.adapter = adapter
        when (pageIndex) {
            2 -> viewModel.completedTasks.observeForever { updateUI(it) }
            1 -> viewModel.activeTasks.observeForever { updateUI(it) }
            0 -> viewModel.importantTasks.observeForever { updateUI(it) }
        }
    }

    private fun updateUI(tasks: List<Task>) {
        Log.d(TAG, "onCreateView: tasks size = ${tasks.size}")

        binding.textView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        val lastDataSize = adapter.data.size
        adapter.data = tasks
        when (viewModel.lastAction) {
            0 -> adapter.notifyDataSetChanged()
            1 -> {
                adapter.notifyItemRangeRemoved(0, lastDataSize)
                adapter.notifyItemRangeInserted(0, tasks.size)
            } // refresh the list
            2 -> adapter.notifyItemChanged(currentIndex)
            3 -> adapter.notifyItemRemoved(currentIndex)
        }

    }

    private val launcherShowTaskDetail: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val data = it.data
                if (data != null) {

                    val task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        data.getSerializableExtra("t", Task::class.java) // API 33+
                    else
                        data.getSerializableExtra("t") as Task

                    val action = data.getIntExtra("a", -1)
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
        launcherShowTaskDetail.launch(intent)
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