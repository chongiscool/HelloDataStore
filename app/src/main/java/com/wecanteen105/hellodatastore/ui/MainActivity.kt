package com.wecanteen105.hellodatastore.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.wecanteen105.hellodatastore.data.SortOrder
import com.wecanteen105.hellodatastore.data.TasksRepository
import com.wecanteen105.hellodatastore.data.UserPreferencesRepository
import com.wecanteen105.hellodatastore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val adapter = TasksAdapter()

    private lateinit var viewModel: TasksViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(
            this,
            TasksViewModelFactory(TasksRepository, UserPreferencesRepository(dataStore))
        )[TasksViewModel::class.java]

        setupRecyclerView()
        
//        setupOnCheckedChangeListeners()
//        observePreferenceChanges()
        viewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
            updateTaskFilters(initialSetupEvent.sortOrder, initialSetupEvent.showCompleted)
            setupOnCheckedChangeListeners()
            observePreferenceChanges()
        }

    }

    private fun observePreferenceChanges() {
        viewModel.tasksUiModel.observe(this) { tasksUiModel ->
            adapter.submitList(tasksUiModel.tasks)
            updateTaskFilters(tasksUiModel.sortOrder, tasksUiModel.showCompleted)
        }
    }

    private fun setupRecyclerView() {
        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)

        binding.list.adapter = adapter
    }

    private fun setupOnCheckedChangeListeners() {
        binding.showCompletedSwitch.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "showCompletedSwitch: $checked ")
            viewModel.showCompletedTasks(checked)
        }
        setupSortChangeListeners()
    }
    
    private fun setupSortChangeListeners() {
        binding.sortDeadline.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "sortDeadline: $checked ")
            viewModel.enableSortByDeadline(checked)
        }
        binding.sortPriority.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "sortPriority: $checked ")
            viewModel.enableSortByPriority(checked)
        }
    }

    private fun updateTaskFilters(sortOrder: SortOrder, showCompleted: Boolean) {
        with(binding) {
            sortDeadline.isChecked =
                sortOrder == SortOrder.BY_DEADLINE || sortOrder == SortOrder.BY_DEADLINE_AND_PRIORITY
            sortPriority.isChecked =
                sortOrder == SortOrder.BY_PRIORITY || sortOrder == SortOrder.BY_DEADLINE_AND_PRIORITY
            showCompletedSwitch.isChecked = showCompleted
        }
    }
}

private const val TAG = "MainAty"
private const val USER_PREFERENCES_NAME = "user_preferences"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        // Since we're migrating from SharedPreferences, add a migration based on the
        // SharedPreferences name
        listOf(SharedPreferencesMigration(context, USER_PREFERENCES_NAME))
    }
)

