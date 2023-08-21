package seamonster.kraken.todo.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import seamonster.kraken.todo.databinding.FragmentEditListBinding
import seamonster.kraken.todo.model.TasksList
import seamonster.kraken.todo.viewmodel.EditListViewModel
import seamonster.kraken.todo.viewmodel.PageViewModel

class EditListFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG: String = "EditListFragment"
    }

    private lateinit var binding: FragmentEditListBinding
    private val viewModel: EditListViewModel by viewModels()
    private lateinit var mainViewModel: PageViewModel
    var mode = 0 // 0 for adding, 1 for editing

    private fun getCurrentList(): TasksList{
         if (mode == 1){
            viewModel.currentList = mainViewModel.currentList.value!!
        }
        return viewModel.currentList
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(requireActivity())[PageViewModel::class.java]
        binding = FragmentEditListBinding.inflate(inflater, container, false)
        binding.list = getCurrentList()
        binding.buttonDone.setOnClickListener {
            viewModel.upsert()
            dismiss()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.textListName.requestFocus() // focus on text field
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) // show soft keyboard
            binding.textListName.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

}