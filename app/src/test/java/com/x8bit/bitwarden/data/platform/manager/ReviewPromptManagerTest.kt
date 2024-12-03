package com.x8bit.bitwarden.data.platform.manager

import com.x8bit.bitwarden.data.auth.datasource.disk.model.UserStateJson
import com.x8bit.bitwarden.data.auth.datasource.disk.util.FakeAuthDiskSource
import com.x8bit.bitwarden.data.autofill.accessibility.manager.FakeAccessibilityEnabledManager
import com.x8bit.bitwarden.data.autofill.manager.AutofillEnabledManager
import com.x8bit.bitwarden.data.autofill.manager.AutofillEnabledManagerImpl
import com.x8bit.bitwarden.data.platform.datasource.disk.util.FakeSettingsDiskSource
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ReviewPromptManagerTest {

    private val autofillEnabledManager: AutofillEnabledManager = AutofillEnabledManagerImpl()
    private val fakeAccessibilityEnabledManager = FakeAccessibilityEnabledManager()
    private val fakeAuthDiskSource = FakeAuthDiskSource()
    private val fakeSettingsDiskSource = FakeSettingsDiskSource()

    private val reviewPromptManager = ReviewPromptManagerImpl(
        autofillEnabledManager = autofillEnabledManager,
        accessibilityEnabledManager = fakeAccessibilityEnabledManager,
        authDiskSource = fakeAuthDiskSource,
        settingsDiskSource = fakeSettingsDiskSource,
    )

    @Test
    fun `incrementAddActionCount increments stored value as expected`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        reviewPromptManager.incrementAddActionCount()
        assertEquals(
            1,
            fakeSettingsDiskSource.getAddActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementAddActionCount()
        assertEquals(
            2,
            fakeSettingsDiskSource.getAddActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementAddActionCount()
        assertEquals(
            3,
            fakeSettingsDiskSource.getAddActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementAddActionCount()
        // Should not increment over 3.
        assertEquals(
            3,
            fakeSettingsDiskSource.getAddActionCount(
                userId = USER_ID,
            ),
        )
    }

    @Test
    fun `incrementCopyActionCount increments stored value as expected`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        reviewPromptManager.incrementCopyActionCount()
        assertEquals(
            1,
            fakeSettingsDiskSource.getCopyActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCopyActionCount()
        assertEquals(
            2,
            fakeSettingsDiskSource.getCopyActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCopyActionCount()
        assertEquals(
            3,
            fakeSettingsDiskSource.getCopyActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCopyActionCount()
        assertEquals(
            3,
            fakeSettingsDiskSource.getCopyActionCount(
                userId = USER_ID,
            ),
        )
    }

    @Test
    fun `incrementCreateActionCount increments stored value as expected`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        reviewPromptManager.incrementCreateActionCount()
        assertEquals(
            1,
            fakeSettingsDiskSource.getCreateActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCreateActionCount()
        assertEquals(
            2,
            fakeSettingsDiskSource.getCreateActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCreateActionCount()
        assertEquals(
            3,
            fakeSettingsDiskSource.getCreateActionCount(
                userId = USER_ID,
            ),
        )
        reviewPromptManager.incrementCreateActionCount()
        assertEquals(
            3,
            fakeSettingsDiskSource.getCreateActionCount(
                userId = USER_ID,
            ),
        )
    }

    @Test
    fun `shouldPromptForAppReview should default to false if no active user`() {
        assertFalse(reviewPromptManager.shouldPromptForAppReview())
    }

    @Suppress("MaxLineLength")
    @Test
    fun `shouldPromptForAppReview should return true if one auto fill service is enabled and one actions requirement is met`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        fakeAccessibilityEnabledManager.isAccessibilityEnabled = true
        autofillEnabledManager.isAutofillEnabled = false
        fakeSettingsDiskSource.storeCopyActionCount(USER_ID, 0)
        fakeSettingsDiskSource.storeCreateActionCount(USER_ID, 0)
        fakeSettingsDiskSource.storeAddActionCount(USER_ID, 4)
        assertTrue(reviewPromptManager.shouldPromptForAppReview())
    }

    @Suppress("MaxLineLength")
    @Test
    fun `shouldPromptForAppReview should return false if prompt has been shown but other criteria is met`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        fakeAccessibilityEnabledManager.isAccessibilityEnabled = true
        fakeSettingsDiskSource.storeUserHasBeenPromptedForReview(USER_ID, true)
        fakeSettingsDiskSource.storeAddActionCount(USER_ID, 4)
        assertFalse(reviewPromptManager.shouldPromptForAppReview())
    }

    @Test
    fun `shouldPromptForAppReview should return false if no auto fill service is enabled`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        fakeAccessibilityEnabledManager.isAccessibilityEnabled = false
        autofillEnabledManager.isAutofillEnabled = false
        fakeSettingsDiskSource.storeCopyActionCount(USER_ID, 0)
        fakeSettingsDiskSource.storeCreateActionCount(USER_ID, 0)
        fakeSettingsDiskSource.storeAddActionCount(USER_ID, 4)
        assertFalse(reviewPromptManager.shouldPromptForAppReview())
    }

    @Test
    fun `shouldPromptForAppReview should return false if no action count is met`() {
        fakeAuthDiskSource.userState = MOCK_USER_STATE
        fakeAccessibilityEnabledManager.isAccessibilityEnabled = true
        autofillEnabledManager.isAutofillEnabled = true
        fakeSettingsDiskSource.storeCopyActionCount(USER_ID, 1)
        fakeSettingsDiskSource.storeCreateActionCount(USER_ID, 0)
        fakeSettingsDiskSource.storeAddActionCount(USER_ID, 2)
        assertFalse(reviewPromptManager.shouldPromptForAppReview())
    }
}

private const val USER_ID = "user_id"
private val MOCK_USER_STATE = mockk<UserStateJson>() {
    every { activeUserId } returns USER_ID
}
