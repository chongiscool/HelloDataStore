/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wecanteen105.hellodatastore.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import com.wecanteen105.hellodatastore.UserPrefs
import com.wecanteen105.hellodatastore.UserPrefs.SortOrder
import com.wecanteen105.hellodatastore.copy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

/**
 * Class that handles saving and retrieving user preferences
 */
class UserPreferencesRepository(
    private val userPrefsStore: DataStore<UserPrefs>,
) {
    private val TAG:String = "UserPreferencesRepo"

    /**
     * for initializing existing preferences
     */
    suspend fun fetchInitialPreferences() = userPrefsStore.data.first()

    val userPrefsFlow:Flow<UserPrefs> = userPrefsStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(TAG, "Error reading sort order preferneces.", exception)
                emit(UserPrefs.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun updateShowCompleted(showCompleted:Boolean){
        userPrefsStore.updateData { preferences ->
//            preferences.toBuilder().setShowCompleted(showCompleted).build()
            preferences.copy {  this.showCompleted = showCompleted  }
        }
    }

    suspend fun enableSortByDeadline(enable: Boolean) {
        // updateData handles data transactionally, ensuring that if the sort is updated at the same
        // time from another thread, we won't have conflicts
        userPrefsStore.updateData { preferences ->
            val currentOrder = preferences.sortOrder
            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_PRIORITY) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_DEADLINE
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_PRIORITY
                    } else {
                        SortOrder.NONE
                    }
                }
            preferences.copy {
                this.sortOrder = newSortOrder
            }
        }
    }

    suspend fun enableSortByPriority(enable: Boolean) {
        userPrefsStore.updateData { preferences ->
            val currentOrder = preferences.sortOrder
            val newSortOrder =
                if (enable) {
                    if (currentOrder == SortOrder.BY_DEADLINE) {
                        SortOrder.BY_DEADLINE_AND_PRIORITY
                    } else {
                        SortOrder.BY_PRIORITY
                    }
                } else {
                    if (currentOrder == SortOrder.BY_DEADLINE_AND_PRIORITY) {
                        SortOrder.BY_DEADLINE
                    } else {
                        SortOrder.NONE
                    }
                }
            preferences.copy {
                this.sortOrder = newSortOrder
            }
        }
    }

//    companion object {
//        @Volatile
//        private var INSTANCE: UserPreferencesRepository? = null
//
//        fun getInstance(context: Context): UserPreferencesRepository {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE?.let {
//                    return it
//                }
//
//                val instance = UserPreferencesRepository(context)
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
}
