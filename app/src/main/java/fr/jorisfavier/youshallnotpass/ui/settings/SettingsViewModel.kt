package fr.jorisfavier.youshallnotpass.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import fr.jorisfavier.youshallnotpass.R
import fr.jorisfavier.youshallnotpass.manager.ICryptoManager
import fr.jorisfavier.youshallnotpass.manager.IFileManager
import fr.jorisfavier.youshallnotpass.repository.IItemRepository
import fr.jorisfavier.youshallnotpass.utils.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val itemRepository: IItemRepository,
    private val cryptoManager: ICryptoManager,
    private val fileManager: IFileManager
) : ViewModel() {

    val themeValues: Array<String>

    val themeEntries: IntArray

    init {
        val values = mutableListOf(
            AppCompatDelegate.MODE_NIGHT_NO.toString(),
            AppCompatDelegate.MODE_NIGHT_YES.toString()
        )

        val entries = mutableListOf(
            R.string.light,
            R.string.dark
        )
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            values.add(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY.toString())
            entries.add(R.string.battery_saver)
        } else {
            values.add(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM.toString())
            entries.add(R.string.system_default)
        }
        themeValues = values.toTypedArray()
        themeEntries = entries.toIntArray()
    }

    fun getDefaultThemeValue(currentNightMode: Int): String? {
        return if (!sharedPreferences.contains(SettingsFragment.THEME_PREFERENCE_KEY)) {
            when (currentNightMode) {
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.MODE_NIGHT_NO.toString()
                else -> AppCompatDelegate.MODE_NIGHT_YES.toString()
            }
        } else {
            null
        }
    }

    fun exportPasswords(@IdRes option: Int): Flow<Result<Intent>> {
        return flow {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val uri = if (option == R.id.ysnpExportRadioButton) {
                    createYsnpExport()
                } else {
                    createCsvExport()
                }
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                emit(Result.success(intent))
            } catch (e: Exception) {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Export all items into a csv file
     * @return an Uri to the csv file
     */
    private suspend fun createCsvExport(): Uri {
        return withContext(Dispatchers.Default) {
            val items = itemRepository.getAllItems()
            val res = StringBuilder()
            items.forEach {
                val itemPassword = cryptoManager.decryptData(it.password, it.initializationVector)
                res.append("${it.title},$itemPassword")
                res.append("\n")
            }
            fileManager.saveToCsv(res.toString())
        }
    }

    /**
     * Export all items into an encrypted file
     * @param password the key to encrypt the file's content
     * @return an Uri to the encrypted file
     */
    private suspend fun createYsnpExport(): Uri {
        return withContext(Dispatchers.Default) {
            val items = itemRepository.getAllItems()
            var data = items.map { it.toByteArray() }.reduce { acc, bytes -> acc + bytes }
            data = cryptoManager.encryptDataWithPassword("test", data)
            fileManager.saveToYsnpFile(data)
        }
    }
}