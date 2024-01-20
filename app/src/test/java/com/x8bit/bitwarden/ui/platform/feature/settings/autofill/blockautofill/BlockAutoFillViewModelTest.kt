package com.x8bit.bitwarden.ui.platform.feature.settings.autofill.blockautofill

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.x8bit.bitwarden.data.platform.repository.SettingsRepository
import com.x8bit.bitwarden.ui.platform.base.BaseViewModelTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BlockAutoFillViewModelTest : BaseViewModelTest() {

    private val settingsRepository: SettingsRepository = mockk {
        every { blockedAutofillUris } returns listOf("blockedUri")
    }

    @Suppress("MaxLineLength")
    @Test
    fun `initial state with blocked URIs updates state to ViewState Content`() =
        runTest {
            val viewModel = createViewModel()
            val expectedState = BlockAutoFillState(
                viewState = BlockAutoFillState.ViewState.Content(
                    blockedUris = listOf("blockedUri"),
                ),
            )

            assertEquals(expectedState, viewModel.stateFlow.value)
        }

    @Suppress("MaxLineLength")
    @Test
    fun `initial state with empty blocked URIs maintains state as ViewState Empty`() =
        runTest {
            every { settingsRepository.blockedAutofillUris } returns emptyList()
            val viewModel = createViewModel()
            val expectedState = BlockAutoFillState(
                viewState = BlockAutoFillState.ViewState.Empty,
            )

            assertEquals(expectedState, viewModel.stateFlow.value)
        }

    @Test
    fun `on BackClick should emit NavigateBack`() = runTest {
        val viewModel = createViewModel()
        viewModel.eventFlow.test {
            viewModel.trySendAction(BlockAutoFillAction.BackClick)
            assertEquals(BlockAutoFillEvent.NavigateBack, awaitItem())
        }
    }

    private fun createViewModel(
        state: BlockAutoFillState? = DEFAULT_STATE,
    ): BlockAutoFillViewModel = BlockAutoFillViewModel(
        savedStateHandle = SavedStateHandle().apply { set("state", state) },
        settingsRepository = settingsRepository,
    )
}

private val DEFAULT_STATE: BlockAutoFillState = BlockAutoFillState(
    BlockAutoFillState.ViewState.Empty,
)
