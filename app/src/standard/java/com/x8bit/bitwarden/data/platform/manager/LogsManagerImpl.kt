package com.x8bit.bitwarden.data.platform.manager

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.x8bit.bitwarden.BuildConfig
import com.x8bit.bitwarden.data.platform.annotation.OmitFromCoverage
import com.x8bit.bitwarden.data.platform.datasource.disk.legacy.LegacyAppCenterMigrator
import com.x8bit.bitwarden.data.platform.repository.SettingsRepository
import com.x8bit.bitwarden.data.platform.repository.model.Environment
import com.x8bit.bitwarden.data.platform.repository.model.InMemoryLogManager
import com.x8bit.bitwarden.data.platform.repository.model.LoggableResult
import timber.log.Timber

/**
 * [LogsManager] implementation for standard flavor builds.
 */
@OmitFromCoverage
class LogsManagerImpl(
    private val settingsRepository: SettingsRepository,
    legacyAppCenterMigrator: LegacyAppCenterMigrator,
    private val inMemoryLogManager: InMemoryLogManager,
) : LogsManager {

    private val nonfatalErrorTree: NonfatalErrorTree = NonfatalErrorTree()

    override var isEnabled: Boolean
        get() = settingsRepository.isCrashLoggingEnabled
        set(value) {
            settingsRepository.isCrashLoggingEnabled = value
            Firebase.crashlytics.isCrashlyticsCollectionEnabled = value
            if (BuildConfig.HAS_LOGS_ENABLED) {
                Timber.plant(Timber.DebugTree())
            }
            if (value) {
                Timber.plant(nonfatalErrorTree)
            } else if (Timber.forest().contains(nonfatalErrorTree)) {
                Timber.uproot(nonfatalErrorTree)
            }
        }

    override fun setUserData(userId: String?, environmentType: Environment.Type) {
        Firebase.crashlytics.setUserId(userId.orEmpty())
        Firebase.crashlytics.setCustomKey(
            if (userId == null) "PreAuthRegion" else "Region",
            environmentType.toString(),
        )
    }

    override fun trackNonFatalException(throwable: Throwable) {
        if (isEnabled) {
            Firebase.crashlytics.recordException(throwable)
        }
    }

    init {
        legacyAppCenterMigrator.migrateIfNecessary()
        isEnabled = settingsRepository.isCrashLoggingEnabled
        Timber.plant(LocalLoggerTree())
    }

    private inner class NonfatalErrorTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            t?.let { trackNonFatalException(BitwardenNonfatalException(message, it)) }
        }
    }

    private inner class LocalLoggerTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority == Log.WARN || priority == Log.ERROR) {
                inMemoryLogManager.registerLoggableResult(
                    LoggableResult(
                        message = message,
                        throwable = t,
                    )
                )
            }
        }
    }
}

private class BitwardenNonfatalException(
    message: String,
    throwable: Throwable,
) : Exception(message, throwable)
