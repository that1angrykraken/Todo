package seamonster.kraken.todo.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import seamonster.kraken.todo.R
import seamonster.kraken.todo.activity.SignInActivity
import seamonster.kraken.todo.databinding.DialogOptionBinding
import seamonster.kraken.todo.viewmodel.UserViewModel

class OptionDialogFragment : DialogFragment() {
    companion object{
        const val TAG = "OptionDialogFragment"
    }

    private lateinit var binding: DialogOptionBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreate(savedInstanceState)
        val userViewModel = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        binding = DialogOptionBinding.inflate(layoutInflater)
        userViewModel.currentUserInfo.observeForever {
            binding.user = it
            Picasso.get().load(it.profilePhoto).into(binding.imgProfilePhoto)
        }
        initializeComponents()
        return Dialog(requireContext(), R.style.OptionDialog).apply {
            setContentView(binding.root)
            with(window!!){
                setBackgroundDrawableResource(R.drawable.round_bg)
                setGravity(Gravity.TOP)
                attributes.verticalMargin = 0.08f
            }
        }
    }

    private fun initializeComponents() {
        binding.buttonClose.setOnClickListener { dismiss() }
        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(requireContext(), SignInActivity::class.java))
        }
        binding.buttonSettings.setOnClickListener {
            dismiss()
        }
        binding.buttonFeedback.setOnClickListener {
            dismiss()
        }
        binding.chipEditInfo.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://myaccount.google.com")
            })
            dismiss()
        }
    }
}