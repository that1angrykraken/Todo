package seamonster.kraken.todo.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import seamonster.kraken.todo.adapter.ListSelectorAdapter
import seamonster.kraken.todo.databinding.FragmentListSelectorBinding
import seamonster.kraken.todo.listener.ListItemListener
import seamonster.kraken.todo.viewmodel.AppViewModel
import java.util.ArrayList

class ListSelectorFragment : BottomSheetDialogFragment(), ListItemListener {

    private lateinit var binding: FragmentListSelectorBinding
    private lateinit var viewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        binding = FragmentListSelectorBinding.inflate(inflater, container, false)
        initLists()
        initButtonCreateList()
        return binding.root
    }

    private fun initLists() {
        val adapter = ListSelectorAdapter(ArrayList(), viewModel.currentList.value!!, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        viewModel.lists.observeForever {
            adapter.data = it
            adapter.notifyItemInserted(adapter.itemCount-1)
        }
    }

    private fun initButtonCreateList() {
        binding.buttonCreateNew.setOnClickListener {
            val dialog = CreateListFragment()
            dialog.show(parentFragmentManager, "CreateListFragment")
        }
    }

    companion object {
        const val TAG = "ListSelectorFragment"
    }

    override fun onItemClicked(id: Int) {
        viewModel.setCurrentList(id)
        dismiss()
    }
}