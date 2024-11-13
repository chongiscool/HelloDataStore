package com.wecanteen105.hellodatastore.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.wecanteen105.hellodatastore.UserPrefs
import com.wecanteen105.hellodatastore.UserPrefs.SortOrder
import com.wecanteen105.hellodatastore.copy
import com.wecanteen105.hellodatastore.data.TasksRepository
import com.wecanteen105.hellodatastore.data.UserPreferencesRepository
import com.wecanteen105.hellodatastore.data.UserPreferencesSerializer
import com.wecanteen105.hellodatastore.databinding.ActivityMainBinding

private const val USER_PREFERENCES_NAME = "user_preferences"
private const val DATA_STORE_FILE_NAME = "user_prefs.pb"
private const val SORT_ORDER_KEY = "sort_order"

private val Context.userPrefsStore: DataStore<UserPrefs> by dataStore(
    fileName = DATA_STORE_FILE_NAME,
    serializer = UserPreferencesSerializer,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                context,
                USER_PREFERENCES_NAME,
            ) { sharedPrefs: SharedPreferencesView, currentData: UserPrefs ->
                if (currentData.sortOrder == SortOrder.UNSPECIFIED) {
//                    currentData.toBuilder().setSortOrder(
//                        SortOrder.valueOf(
//                            sharedPrefs.getString(SORT_ORDER_KEY, SortOrder.NONE.name)
//                                ?: SortOrder.NONE.name
//                        )
//                    ).build()
                    currentData.copy {
                        this.sortOrder = SortOrder.valueOf(
                            sharedPrefs.getString(SORT_ORDER_KEY, SortOrder.NONE.name)
                                ?: SortOrder.NONE.name
                        )
                    }
                } else {
                    currentData
                }
            }
        )
    }
)

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
            TasksViewModelFactory(
                TasksRepository,
                UserPreferencesRepository(
                    userPrefsStore,
                )
            )
        )[TasksViewModel::class.java]

        setupRecyclerView()

        // NOTE: this setup have no initial setup, when you open app again, have no chance to read existing proto datastore data
//        setupOnChangeListeners()
//        observePreferenceChanges()

        viewModel.initialSetupEvent.observe(this) { initialSetupEvent ->
            updateTaskFilters(initialSetupEvent.sortOrder, initialSetupEvent.showCompleted)
            setupOnChangeListeners()
            observePreferenceChanges()
        }
    }

    private fun observePreferenceChanges() {
        viewModel.tasksUiModel.observe(this) { tasksUiModel ->
            adapter.submitList(tasksUiModel.tasks)
            updateTaskFilters(tasksUiModel.sortOrder, tasksUiModel.showCompleted)
        }
    }

    private fun setupOnChangeListeners() {
        setupSortChangeListeners()
        binding.showCompletedSwitch.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "showCompletedSwitch checked: $checked")
            viewModel.showCompletedTasks(checked)
        }
    }

    private fun setupRecyclerView() {
        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)

        binding.list.adapter = adapter
    }

    private fun setupSortChangeListeners() {
        binding.sortDeadline.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "sortDeadline checked: $checked")
            viewModel.enableSortByDeadline(checked)
        }
        binding.sortPriority.setOnCheckedChangeListener { _, checked ->
            Log.d(TAG, "sortPriority: checked: $checked")
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