package seamonster.kraken.todo.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentListActionBinding
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.viewmodel.ListViewModel
import seamonster.kraken.todo.viewmodel.TaskViewModel

class ListActionFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG: String = "ListActionFragment"
    }

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var listViewModel: ListViewModel
    private lateinit var binding: FragmentListActionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListActionBinding.inflate(inflater, container, false)
        initializeComponents()
        return binding.root
    }

    private fun initializeComponents() {
        taskViewModel = ViewModelProvider(requireActivity())[TaskViewModel::class.java]
        listViewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]
        initButtonRenameList()
        initButtonDeleteList()
        initButtonDeleteCompletedTasks()
    }

    private fun initButtonRenameList() {
        val id = taskViewModel.currentList.value!!.id
        binding.buttonRenameList.isEnabled = (id != "0")
        binding.buttonRenameList.setOnClickListener {
            val dialog = EditListFragment()
            dialog.list = taskViewModel.currentList.value!!
            dialog.show(parentFragmentManager, EditListFragment.TAG)
            dismiss()
        }
    }

    private fun initButtonDeleteList() {
        val id = taskViewModel.currentList.value!!.id
        binding.buttonDeleteList.isEnabled = (id != "0")
        // show confirmation dialog
        binding.buttonDeleteList.setOnClickListener {
            dismiss()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_list_dialog_title))
                .setMessage(getString(R.string.delete_list_dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                    listViewModel.delete(ListInfo().also { it.id = id })
                    val defaultList = listViewModel.getLists.value?.find { it.id == "0" }!!
                    taskViewModel.setCurrentList(defaultList)
                }
                .setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ ->
                    /* do nothing */
                }
                .show()
        }
    }

    private fun initButtonDeleteCompletedTasks() {
        val tasks = taskViewModel.completedTasks.value
        binding.buttonDeleteCompletedTasks.isEnabled = !tasks.isNullOrEmpty()

        binding.buttonDeleteCompletedTasks.setOnClickListener {
            // show confirmation dialog
            dismiss()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_dialog_title))
                .setMessage(getString(R.string.delete_dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                    taskViewModel.delete(*tasks!!.toTypedArray())
                }
                .setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ ->
                    /* do nothing */
                }
                .show()
        }
    }
}