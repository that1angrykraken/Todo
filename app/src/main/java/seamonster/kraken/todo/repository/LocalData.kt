package seamonster.kraken.todo.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore("settings")

class LocalData(private val context: Context) {
    companion object {
        private val skipRequestPermissionDialog =
            booleanPreferencesKey("skipRequestPermissionDialog")
        private val language =
            stringPreferencesKey("lang")
    }

    val skipDialog = context.dataStore.data.map {
        it[skipRequestPermissionDialog] ?: true
    }

    suspend fun update(b: Boolean) = context.dataStore.edit {
        it[skipRequestPermissionDialog] = b
    }

    suspend fun update(lang: String) = context.dataStore.edit {
        it[language] = lang
    }
}