package com.x8bit.bitwarden.ui.platform.manager.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.x8bit.bitwarden.BuildConfig
import com.x8bit.bitwarden.R
import com.x8bit.bitwarden.data.platform.annotation.OmitFromCoverage
import com.x8bit.bitwarden.ui.platform.util.toFormattedPattern
import java.io.File
import java.time.Clock

/**
 * The authority used for pulling in photos from the camera.
 *
 * Note: This must match the file provider authority in the manifest.
 */
private const val FILE_PROVIDER_AUTHORITY: String = "${BuildConfig.APPLICATION_ID}.fileprovider"

/**
 * Temporary file name for a camera image.
 */
private const val TEMP_CAMERA_IMAGE_NAME: String = "temp_camera_image.jpg"

/**
 * This directory must also be declared in file_paths.xml
 */
private const val TEMP_CAMERA_IMAGE_DIR: String = "camera_temp"

/**
 * The default implementation of the [IntentManager] for simplifying the handling of Android
 * Intents within a given context.
 */
@Suppress("TooManyFunctions")
@OmitFromCoverage
class IntentManagerImpl(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) : IntentManager {

    override fun exitApplication() {
        // Note that we fire an explicit Intent rather than try to cast to an Activity and call
        // finish to avoid assumptions about what kind of context we have.
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        startActivity(intent)
    }

    override fun startActivity(intent: Intent) {
        context.startActivity(intent)
    }

    @Composable
    override fun launchActivityForResult(
        onResult: (ActivityResult) -> Unit,
    ): ManagedActivityResultLauncher<Intent, ActivityResult> =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = onResult,
        )

    override fun startCustomTabsActivity(uri: Uri) {
        CustomTabsIntent
            .Builder()
            .build()
            .launchUrl(context, uri)
    }

    override fun startSystemAutofillSettingsActivity(): Boolean =
        try {
            val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
                .apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }

    override fun startApplicationDetailsSettingsActivity() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:" + context.packageName)
        startActivity(intent = intent)
    }

    override fun launchUri(uri: Uri) {
        val newUri = if (uri.scheme == null) {
            uri.buildUpon().scheme("https").build()
        } else {
            uri.normalizeScheme()
        }
        startActivity(Intent(Intent.ACTION_VIEW, newUri))
    }

    override fun shareText(text: String) {
        val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, null))
    }

    override fun getFileDataFromIntent(activityResult: ActivityResult): IntentManager.FileData? {
        if (activityResult.resultCode != Activity.RESULT_OK) return null
        val uri = activityResult.data?.data
        return if (uri != null) getLocalFileData(uri) else getCameraFileData()
    }

    override fun createFileChooserIntent(withCameraIntents: Boolean): Intent {
        val chooserIntent = Intent.createChooser(
            Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*"),
            ContextCompat.getString(context, R.string.file_source),
        )

        if (withCameraIntents) {
            val tmpDir = File(context.filesDir, TEMP_CAMERA_IMAGE_DIR)
            val file = File(tmpDir, TEMP_CAMERA_IMAGE_NAME)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val outputFileUri = FileProvider.getUriForFile(
                context,
                FILE_PROVIDER_AUTHORITY,
                file,
            )

            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS,
                getCameraIntents(outputFileUri).toTypedArray(),
            )
        }

        return chooserIntent
    }

    private fun getCameraFileData(): IntentManager.FileData {
        val tmpDir = File(context.filesDir, TEMP_CAMERA_IMAGE_DIR)
        val file = File(tmpDir, TEMP_CAMERA_IMAGE_NAME)
        val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
        val fileName = "photo_${clock.instant().toFormattedPattern(pattern = "yyyyMMddHHmmss")}.jpg"
        return IntentManager.FileData(
            fileName = fileName,
            uri = uri,
            sizeBytes = file.length(),
        )
    }

    private fun getLocalFileData(uri: Uri): IntentManager.FileData? =
        context
            .contentResolver
            .query(
                uri,
                arrayOf(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.SIZE,
                ),
                null,
                null,
                null,
            )
            ?.use { cursor ->
                if (!cursor.moveToFirst()) return@use null
                val fileName = cursor
                    .getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    .takeIf { it >= 0 }
                    ?.let { cursor.getString(it) }
                val fileSize = cursor
                    .getColumnIndex(MediaStore.MediaColumns.SIZE)
                    .takeIf { it >= 0 }
                    ?.let { cursor.getLong(it) }
                if (fileName == null || fileSize == null) return@use null
                IntentManager.FileData(
                    fileName = fileName,
                    uri = uri,
                    sizeBytes = fileSize,
                )
            }

    private fun getCameraIntents(outputUri: Uri): List<Intent> {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return context
            .packageManager
            .queryIntentActivities(captureIntent, PackageManager.MATCH_ALL)
            .map {
                val packageName = it.activityInfo.packageName
                Intent(captureIntent).apply {
                    component = ComponentName(packageName, it.activityInfo.name)
                    setPackage(packageName)
                    putExtra(MediaStore.EXTRA_OUTPUT, outputUri)
                }
            }
    }
}
