package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentCreateListBinding
import seamonster.kraken.todo.model.TaskList
import seamonster.kraken.todo.viewmodel.AppViewModel

class CreateListFragment : DialogFragment() {

    companion object{
        const val TAG: String = "CreateListFragment"
    }

    private lateinit var binding: FragmentCreateListBinding
    private lateinit var viewModel: AppViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel = ViewModelProvider(requireActivity())[AppViewModel::class.java]

        binding = FragmentCreateListBinding.inflate(layoutInflater)
        val dialog = Dialog(requireContext(), R.style.DialogTheme)

        binding.buttonDone.setOnClickListener {
            val list = TaskList()
            list.name = binding.textListName.editText?.text.toString()
            rename(list)
            viewModel.upsertList(list)
            dialog.dismiss()
        }
        binding.toolbar.setNavigationOnClickListener { dialog.cancel() }
        dialog.window?.setWindowAnimations(R.style.DialogAnimation)
        dialog.setContentView(binding.root)
        return dialog
    }

    private fun rename(l: TaskList, count: Int = 1) {
        if(l.name.isEmpty()) l.name = getString(R.string.new_list)
        if (viewModel.lists.value!!.any { it.name == l.name }) {
            Log.d(TAG, "rename: exist")
            val len = l.name.length
            if (count > 1) l.name = l.name.removeRange(len - 3, len)
            l.name += "($count)"
            rename(l, count + 1)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.textListName.requestFocus()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            binding.textListName.windowInsetsController?.show(WindowInsetsCompat.Type.ime())
        else{
            val inputMethodManager =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(binding.textListName, InputMethodManager.SHOW_IMPLICIT)
        }
//        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

}