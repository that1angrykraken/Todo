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
//            checkName()
            viewModel.upsert(list)
            dialog.dismiss()
        }
        binding.toolbar.setNavigationOnClickListener { dialog.cancel() }
        dialog.window?.setWindowAnimations(R.style.DialogBottomUpAnimation)
        dialog.setContentView(binding.root)
        return dialog
    }

//    private fun checkName(count: Int = 1) {
//        if (list.name.isEmpty()) list.name = getString(R.string.default_list_name)
//        val b = viewModel.getLists.value!!.any { it.name.contentEquals(list.name) }
//        if (b) {
//            if (count == 1) list.name = list.name + " "
//            val l = list.name.length
//            list.name = list.name.replaceRange(l - 1, l, " $count")
//            checkName(count + 1)
//        } else {
//        }
//    }

    override fun onStart() {
        super.onStart()
        binding.textListName.requestFocus()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            binding.textListName.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        else {
//            val inputMethodManager =
//                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            inputMethodManager.showSoftInput(binding.textListName, InputMethodManager.SHOW_IMPLICIT)
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

}