package seamonster.kraken.todo.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class LocalDataRepo(private val context: Context) {
    companion object {
        private val skipRequestPermissionDialog =
            booleanPreferencesKey("skipRequestPermissionDialog")
    }

    val skipDialog = context.dataStore.data.map {
        it[skipRequestPermissionDialog] ?: true
    }

    suspend fun update(b: Boolean) = context.dataStore.edit {
        it[skipRequestPermissionDialog] = b
    }
}