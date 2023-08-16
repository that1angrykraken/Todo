package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentEditListBinding
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.viewmodel.ListViewModel

class EditListFragment : DialogFragment() {

    companion object {
        const val TAG: String = "EditListFragment"
    }

    private lateinit var binding: FragmentEditListBinding
    private lateinit var viewModel: ListViewModel
    var list = ListInfo()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[ListViewModel::class.java]

        binding = FragmentEditListBinding.inflate(layoutInflater)
        if (binding.list == null) binding.list = list

        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        binding.buttonDone.setOnClickListener {
            viewModel.upsert(list)
            dialog.dismiss()
        }
        binding.toolbar.setNavigationOnClickListener { dialog.cancel() }
        dialog.window?.setWindowAnimations(R.style.DialogBottomUpAnimation)
        dialog.setContentView(binding.root)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        binding.textListName.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            binding.textListName.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

}