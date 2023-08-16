package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isEmpty
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.FragmentFeedbackBinding
import seamonster.kraken.todo.viewmodel.FeedbackViewModel

class FeedbackFragment : DialogFragment() {
    companion object {
        const val TAG = "FeedbackFragment"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentFeedbackBinding.inflate(layoutInflater)
        val viewModel: FeedbackViewModel by viewModels()
        return Dialog(requireContext(), R.style.DialogTheme).apply {
            window?.setWindowAnimations(R.style.DialogBottomUpAnimation)
            binding.feedback = viewModel.feedback
            binding.toolbar.setNavigationOnClickListener { cancel() }
            binding.buttonSend.setOnClickListener {
                if (binding.feedbackTitle.editText?.length() == 0 || binding.feedbackDetail.editText?.length() == 0) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.fields_empty), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.sendFeedback()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.feedback_sent), Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
            }
            setContentView(binding.root)
        }
    }

}