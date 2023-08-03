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
import seamonster.kraken.todo.viewmodel.AppViewModel

class PageFragment : Fragment(), TaskItemListener {
    private lateinit var binding: FragmentPageBinding
    private lateinit var adapter: TasksListAdapter
    private lateinit var viewModel: AppViewModel
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
        val dialog = EditTaskFragment()
        dialog.task = task
        dialog.show(parentFragmentManager, EditTaskFragment.TAG)
    }

    override fun onItemCompletedChanged(task: Task) {
        viewModel.upsert(task)
    }

    override fun onItemImportantChanged(task: Task) {
        viewModel.upsert(task)
    }
}