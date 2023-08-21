package seamonster.kraken.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import seamonster.kraken.todo.adapter.ListAdapter
import seamonster.kraken.todo.databinding.FragmentListSelectorBinding
import seamonster.kraken.todo.listener.ListItemListener
import seamonster.kraken.todo.model.TasksList
import seamonster.kraken.todo.viewmodel.ListViewModel
import seamonster.kraken.todo.viewmodel.PageViewModel
import java.util.ArrayList

class ListSelectorFragment : BottomSheetDialogFragment(), ListItemListener {

    companion object {
        const val TAG = "ListSelectorFragment"
    }

    private lateinit var binding: FragmentListSelectorBinding
    private lateinit var listViewModel: ListViewModel
    private lateinit var pageViewModel: PageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        pageViewModel = ViewModelProvider(requireActivity())[PageViewModel::class.java]
        binding = FragmentListSelectorBinding.inflate(inflater, container, false)
        initLists()
        initButtonCreateList()
        return binding.root
    }

    private fun initLists() {
        val adapter = ListAdapter(ArrayList(), pageViewModel.currentList.value?.id!!, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        listViewModel.getLists.observeForever {
            adapter.data = it
            adapter.notifyDataSetChanged()
        }
    }

    private fun initButtonCreateList() {
        binding.buttonCreateNew.setOnClickListener {
            val dialog = EditListFragment()
            dialog.show(parentFragmentManager, "CreateListFragment")
        }
    }

    override fun onItemClicked(list: TasksList) {
        pageViewModel.setCurrentList(list)
        dismiss()
    }
}