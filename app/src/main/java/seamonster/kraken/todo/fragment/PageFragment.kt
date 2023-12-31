package seamonster.kraken.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import seamonster.kraken.todo.R
import seamonster.kraken.todo.adapter.TasksListAdapter
import seamonster.kraken.todo.databinding.FragmentPageBinding
import seamonster.kraken.todo.listener.TaskItemListener
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.viewmodel.PageViewModel

class PageFragment : Fragment(), TaskItemListener {
    private lateinit var binding: FragmentPageBinding
    private lateinit var adapter: TasksListAdapter
    private lateinit var viewModel: PageViewModel
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
        viewModel = ViewModelProvider(requireActivity())[PageViewModel::class.java]
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
        adapter = TasksListAdapter(this)
        binding.recyclerView.adapter = adapter
        when (pageIndex) {
            2 -> viewModel.completedTasks.observeForever { updateUI(it) }
            1 -> viewModel.activeTasks.observeForever { updateUI(it) }
            0 -> viewModel.importantTasks.observeForever { updateUI(it) }
        }
    }

    private fun updateUI(tasks: List<Task>) {
        binding.textView.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        adapter.data = tasks
    }

    override fun onItemClick(task: Task) {
        viewModel.currentTask = task
        val dialog = EditTaskFragment()
        dialog.show(parentFragmentManager, EditTaskFragment.TAG)
    }

    override fun onItemCompletedChange(task: Task) {
        viewModel.upsert(task)
    }

    override fun onItemImportantChange(task: Task) {
        viewModel.upsert(task)
    }
}