package com.x8bit.bitwarden.data.platform.manager

import com.x8bit.bitwarden.data.auth.datasource.disk.AuthDiskSource
import com.x8bit.bitwarden.data.autofill.accessibility.manager.AccessibilityEnabledManager
import com.x8bit.bitwarden.data.autofill.manager.AutofillEnabledManager
import com.x8bit.bitwarden.data.platform.datasource.disk.SettingsDiskSource
import com.x8bit.bitwarden.ui.platform.util.orZero

private const val ADD_ACTION_REQUIREMENT = 3
private const val COPY_ACTION_REQUIREMENT = 3
private const val CREATE_ACTION_REQUIREMENT = 3

/**
 * Default implementation of [ReviewPromptManager].
 */
class ReviewPromptManagerImpl(
    private val authDiskSource: AuthDiskSource,
    private val settingsDiskSource: SettingsDiskSource,
    private val autofillEnabledManager: AutofillEnabledManager,
    private val accessibilityEnabledManager: AccessibilityEnabledManager,
) : ReviewPromptManager {

    override fun incrementAddActionCount() {
        val activeUserId = authDiskSource.userState?.activeUserId ?: return
        if (isMinimumAddActionsMet(activeUserId)) return
        val currentValue = settingsDiskSource.getAddActionCount(activeUserId) ?: 0
        settingsDiskSource.storeAddActionCount(
            userId = activeUserId,
            count = currentValue + 1,
        )
    }

    override fun incrementCopyActionCount() {
        val activeUserId = authDiskSource.userState?.activeUserId ?: return
        if (isMinimumCopyActionsMet(activeUserId)) return
        val currentValue = settingsDiskSource.getCopyActionCount(activeUserId) ?: 0
        settingsDiskSource.storeCopyActionCount(
            userId = activeUserId,
            count = currentValue + 1,
        )
    }

    override fun incrementCreateActionCount() {
        val activeUserId = authDiskSource.userState?.activeUserId ?: return
        if (isMinimumCreateActionsMet(activeUserId)) return
        val currentValue = settingsDiskSource.getCreateActionCount(activeUserId) ?: 0
        settingsDiskSource.storeCreateActionCount(
            userId = activeUserId,
            count = currentValue + 1,
        )
    }

    override fun shouldPromptForAppReview(): Boolean {
        val activeUserId = authDiskSource.userState?.activeUserId ?: return false
        val promptHasNotBeenShown =
            settingsDiskSource.getUserHasBeenPromptedForReview(activeUserId) != true
        val autofillEnabled = autofillEnabledManager.isAutofillEnabledStateFlow.value
        val accessibilityEnabled = accessibilityEnabledManager.isAccessibilityEnabledStateFlow.value
        val minAddActionsMet = isMinimumAddActionsMet(activeUserId)
        val minCopyActionsMet = isMinimumCopyActionsMet(activeUserId)
        val minCreateActionsMet = isMinimumCreateActionsMet(activeUserId)
        return (autofillEnabled || accessibilityEnabled) &&
            (minAddActionsMet || minCopyActionsMet || minCreateActionsMet) &&
            promptHasNotBeenShown
    }

    private fun isMinimumAddActionsMet(userId: String): Boolean {
        return settingsDiskSource.getAddActionCount(userId).orZero() >= ADD_ACTION_REQUIREMENT
    }

    private fun isMinimumCopyActionsMet(userId: String): Boolean {
        return settingsDiskSource.getCopyActionCount(userId).orZero() >= COPY_ACTION_REQUIREMENT
    }

    private fun isMinimumCreateActionsMet(userId: String): Boolean {
        return settingsDiskSource.getCreateActionCount(userId).orZero() >= CREATE_ACTION_REQUIREMENT
    }
}
