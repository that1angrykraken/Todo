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
import seamonster.kraken.todo.viewmodel.AppViewModel

class ListActionFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG: String = "ListActionFragment"
    }

    private lateinit var viewModel: AppViewModel
    private lateinit var binding: FragmentListActionBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentListActionBinding.inflate(inflater, container, false)
        initializeComponents()
        return binding.root
    }

    private fun initializeComponents() {
        viewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]
        initButtonRenameList()
        initButtonDeleteList()
        initButtonDeleteCompletedTasks()
    }

    private fun initButtonRenameList() {
        binding.buttonRenameList.isEnabled = viewModel.currentList.value!! > 1
    }

    private fun initButtonDeleteList() {
        binding.buttonDeleteList.isEnabled = viewModel.currentList.value!! > 1
        // show confirmation dialog
        binding.buttonDeleteList.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_list_dialog_title))
                .setMessage(getString(R.string.delete_list_dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                    viewModel.deleteList(ListInfo(viewModel.currentList.value!!))
                    viewModel.setCurrentList(1)
                    dismiss()
                }
                .setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ ->
                    /* do nothing */
                }
                .show()
        }
    }

    private fun initButtonDeleteCompletedTasks() {
        val tasks = viewModel.completedTasks.value
        binding.buttonDeleteCompletedTasks.isEnabled = !tasks.isNullOrEmpty()

        binding.buttonDeleteCompletedTasks.setOnClickListener {
            // show confirmation dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_dialog_title))
                .setMessage(getString(R.string.delete_dialog_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ ->
                    viewModel.delete(*tasks!!.toTypedArray())
                    dismiss()
                }
                .setNegativeButton(getString(R.string.dialog_negative_button)) { _, _ ->
                    /* do nothing */
                }
                .show()
        }
    }
}