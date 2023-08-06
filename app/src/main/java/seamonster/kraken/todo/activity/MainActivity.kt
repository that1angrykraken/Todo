package seamonster.kraken.todo.activity

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import seamonster.kraken.todo.R
import seamonster.kraken.todo.adapter.PagerAdapter
import seamonster.kraken.todo.databinding.ActivityMainBinding
import seamonster.kraken.todo.fragment.EditTaskFragment
import seamonster.kraken.todo.fragment.ListActionFragment
import seamonster.kraken.todo.fragment.ListSelectorFragment
import seamonster.kraken.todo.model.ListInfo
import seamonster.kraken.todo.model.Task
import seamonster.kraken.todo.util.AppUtil
import seamonster.kraken.todo.viewmodel.AppViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AppViewModel by viewModels()

    companion object {
        const val TAG: String = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeComponents()
    }

    private fun initializeComponents() {
        initDefaultList()
        initTabs()
        initPager()
        initFabAddTask()
        initChipUpcomingFilter()
        initButtonSelectList()
        initButtonListAction()
        initScheduler()
        showTaskFromNotification()
    }

    private fun initDefaultList() {
        val list = ListInfo(1).apply { name = getString(R.string.my_tasks) }
        viewModel.upsertList(list)
        viewModel.setCurrentList(list)
    }

    private fun showTaskFromNotification() {
        val task = AppUtil.getTaskFromBundle(intent.extras)
        if (task != null) {
            showEditTaskDialog(task)
        }
    }

    private fun initScheduler() {
        viewModel.allTasks.observeForever { tasks ->
            if (tasks.isNotEmpty()){
                AppUtil().scheduleNextTask(this, Task(0))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (!it) {
            val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.notification_permission_require_title))
                .setMessage(getString(R.string.notification_permission_require_message))
                .setPositiveButton(getString(R.string.dialog_positive_button)) { _, _ -> }
            dialog.show()
        }
    }

    private fun initButtonListAction() {
        binding.buttonListAction.setOnClickListener {
            val bottomSheet = ListActionFragment()
            bottomSheet.show(supportFragmentManager, ListActionFragment.TAG)
        }
    }

    private fun initChipUpcomingFilter() {
        binding.checkboxUpcomingFilter.setOnClickListener {
            viewModel.upcomingFilterEnabled.value = binding.checkboxUpcomingFilter.isChecked
        }
    }

    private fun initButtonSelectList() {
        viewModel.currentList.observeForever { list ->
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

    private fun showEditTaskDialog(task: Task = Task()) {
        val dialog = EditTaskFragment()
        dialog.task = task
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