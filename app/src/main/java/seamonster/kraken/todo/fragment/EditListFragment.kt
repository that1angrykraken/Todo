package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentEditListBinding
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.viewmodel.AppViewModel

class EditListFragment : DialogFragment() {

    companion object {
        const val TAG: String = "EditListFragment"
    }

    private lateinit var binding: FragmentEditListBinding
    private lateinit var viewModel: AppViewModel
    var list = ListInfo()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]

        binding = FragmentEditListBinding.inflate(layoutInflater)
        if(binding.list == null) binding.list = list

        val dialog = Dialog(requireContext(), R.style.DialogTheme)
        binding.buttonDone.setOnClickListener {
            list = binding.list!!
            if (isValidName(list)) {
                viewModel.upsertList(list)
                dialog.dismiss()
            }
            else {
                binding.textListName.error = getString(R.string.invalid_list_name)
            }
        }
        binding.toolbar.setNavigationOnClickListener { dialog.cancel() }
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog.setContentView(binding.root)
        return dialog
    }

    private fun isValidName(l: ListInfo) : Boolean{
        return !viewModel.lists.value!!.any { it.name == l.name } && l.name.isNotEmpty()
    }

    override fun onStart() {
        super.onStart()
        binding.textListName.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            binding.textListName.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        else {
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.textListName, InputMethodManager.SHOW_IMPLICIT)
//        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

}