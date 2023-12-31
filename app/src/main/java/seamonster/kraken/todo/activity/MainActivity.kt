package seamonster.kraken.todo.activity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import seamonster.kraken.todo.R
import seamonster.kraken.todo.adapter.PagerAdapter
import seamonster.kraken.todo.databinding.ActivityMainBinding
import seamonster.kraken.todo.databinding.DialogRequestPermissionBinding
import seamonster.kraken.todo.fragment.EditTaskFragment
import seamonster.kraken.todo.fragment.ListActionFragment
import seamonster.kraken.todo.fragment.ListSelectorFragment
import seamonster.kraken.todo.fragment.OptionDialogFragment
import seamonster.kraken.todo.repository.LocalData
import seamonster.kraken.todo.util.AppUtil
import seamonster.kraken.todo.viewmodel.PageViewModel
import seamonster.kraken.todo.viewmodel.UserViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val userViewModel: UserViewModel by viewModels()
    private val pageViewModel: PageViewModel by viewModels()
    private lateinit var localData: LocalData

    override fun onDestroy() {
        super.onDestroy()
        ViewModelStore().clear()
    }

    companion object {
        const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeComponents()
        localData = LocalData(this)
    }

    override fun onStart() {
        super.onStart()
        initCurrentUser()
    }

    private fun initCurrentUser() {
        userViewModel.currentUser.observeForever {
            if (it != null) {
                Picasso.get().load(it.photoUrl).into(binding.buttonCurrentUser)
                initButtonCurrentUser()
                requestPermission()
            } else {
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }

    private fun initButtonCurrentUser() {
        binding.buttonCurrentUser.setOnClickListener {
            OptionDialogFragment().show(supportFragmentManager, OptionDialogFragment.TAG)
        }
    }

    private fun requestPermission() {
        val util = AppUtil(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    util.startReminder()
                    util.scheduleTasks()
                    util.startBackgroundService()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    lifecycleScope.launch {
                        localData.skipDialog.collect { if (it) showRequestPermissionDialog() }
                    }
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            util.startReminder()
            util.scheduleTasks()
            util.startBackgroundService()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        requestPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showRequestPermissionDialog() {
        val dialog = Dialog(this, R.style.DialogTheme)
        val dialogBinding = DialogRequestPermissionBinding.inflate(layoutInflater)
        dialogBinding.buttonOK.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            dialog.dismiss()
        }
        dialogBinding.buttonCancel.setOnClickListener {
            lifecycleScope.launch {
                localData.update(false)
            }
            showPermissionDeniedDialog()
            dialog.cancel()
        }
        dialog.setContentView(dialogBinding.root)
        dialog.show()
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(this).setTitle(getText(R.string.notification_permission_require_title))
            .setMessage(getText(R.string.notification_permission_require_message))
            .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ -> }
            .show()
    }

    private fun initializeComponents() {
        initButtonSelectList()
        initTabs()
        initPager()
        initFabAddTask()
        initButtonListAction()
        initChipUpcomingFilter()
        showTaskFromNotification()
    }

    private fun showTaskFromNotification() {
        val task = AppUtil.getTaskFromBundle(intent.extras)
        if (task != null) {
            intent.putExtras(Bundle()) // empty extras
            pageViewModel.currentTask = task
            showEditTaskDialog()
        }
    }

    private fun initButtonListAction() {
        binding.buttonListAction.setOnClickListener {
            val bottomSheet = ListActionFragment()
            bottomSheet.show(supportFragmentManager, ListActionFragment.TAG)
        }
    }

    private fun initChipUpcomingFilter() {
        val upcoming = intent.extras?.getBoolean("upcoming") ?: false
        if (upcoming) {
            binding.checkboxUpcomingFilter.isChecked = true
            pageViewModel.setUpcomingFilter(true)
        }
        binding.checkboxUpcomingFilter.setOnClickListener {
            pageViewModel.setUpcomingFilter(binding.checkboxUpcomingFilter.isChecked)
        }
    }

    private fun initButtonSelectList() {
        pageViewModel.currentList.observeForever { list ->
            binding.list = list
        }
        binding.buttonSelectList.setOnClickListener {
            val bottomSheet = ListSelectorFragment()
            bottomSheet.show(supportFragmentManager, ListSelectorFragment.TAG)
        }
    }

    private fun initFabAddTask() {
        binding.fabAddTask.setOnClickListener { showEditTaskDialog() }
    }

    private fun showEditTaskDialog() {
        val dialog = EditTaskFragment()
        dialog.show(supportFragmentManager, EditTaskFragment.TAG)
    }

    private fun initPager() {
        val adapter = PagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.offscreenPageLimit = 2
        val listener = object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))

                if (position <= 1) showButtons() else hideButtons()
            }
        }
        binding.viewPager2.registerOnPageChangeCallback(listener)
        binding.viewPager2.currentItem = 1
    }

    private fun showButtons() {
        binding.run {
            if (fabAddTask.visibility == View.GONE) {
                buttonSelectList.maxWidth =
                    (buttonSelectList.maxWidth - checkboxUpcomingFilter.width)
                fabAddTask.visibility = View.VISIBLE
                checkboxUpcomingFilter.visibility = View.VISIBLE
            }
        }
    }

    private fun hideButtons() {
        binding.run {
            buttonSelectList.maxWidth = (buttonSelectList.maxWidth + checkboxUpcomingFilter.width)
            fabAddTask.visibility = View.GONE
            checkboxUpcomingFilter.visibility = View.GONE
        }
    }

    private fun initTabs() {
        binding.tabLayout.run {
            addTab(
                newTab().setText(getText(R.string.important))
                    .setIcon(R.drawable.round_star_border_48)
            )
            addTab(newTab().setText(getText(R.string.toolbar_title)))
            addTab(newTab().setText(getText(R.string.completed)))

            val listener = object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab != null) {
                        binding.viewPager2.setCurrentItem(tab.position, true)
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            }
            addOnTabSelectedListener(listener)
        }
    }

}