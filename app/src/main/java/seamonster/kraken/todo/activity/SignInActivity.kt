package seamonster.kraken.todo.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import seamonster.kraken.todo.R
import seamonster.kraken.todo.databinding.ActivitySignInBinding
import seamonster.kraken.todo.util.AppUtil

class SignInActivity : AppCompatActivity() {
    companion object {
        const val TAG = "SignInActivity"
    }

    private lateinit var binding: ActivitySignInBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initGoogleSignIn()
        binding.buttonSignIn.setOnClickListener { signIn() }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null) signIn()
        else updateUI(1)
    }

    private fun signIn() {
        updateUI(-1)
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    signInLauncher.launch(intentSenderRequest)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .addOnFailureListener { e -> writeLogAndUpdateUI(e) }
    }

    private fun initGoogleSignIn() {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        auth = Firebase.auth
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode != RESULT_CANCELED) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(it.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null) {
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    updateUI(1)
                                    Log.d(TAG, "signIn: Successfully")
                                } else {
                                    writeLogAndUpdateUI(task.exception)
                                }
                            }
                            .addOnFailureListener { e -> writeLogAndUpdateUI(e) }
                    }
                } catch (e: ApiException) {
                    writeLogAndUpdateUI(e)
                }
            } else {
                writeLogAndUpdateUI()
            }
        }

    private fun writeLogAndUpdateUI(e: Exception? = null) {
        updateUI(0)
        Log.e(TAG, "signIn: Failed", e)
    }

    private fun updateUI(result: Int) {
        when (result) {
            1 -> {
                binding.textSignInState.text =
                    getString(R.string.signing_in, auth.currentUser?.displayName)
                startActivity(Intent(this, MainActivity::class.java))
            }

            0 -> {
                binding.textSignInState.text = getString(R.string.sign_in_required)
                binding.buttonSignIn.visibility = View.VISIBLE
                binding.indicatorLoading.visibility = View.GONE
            }

            -1 -> {
                binding.textSignInState.text = getText(R.string.please_wait)
                binding.indicatorLoading.visibility = View.VISIBLE
                binding.buttonSignIn.visibility = View.GONE
            }
        }
    }

}