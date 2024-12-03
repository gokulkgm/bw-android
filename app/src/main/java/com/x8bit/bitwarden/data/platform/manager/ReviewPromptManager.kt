package com.x8bit.bitwarden.data.platform.manager

/**
 * Responsible for managing whether or not the app review prompt should be shown.
 */
interface ReviewPromptManager {
    /**
     * Increments the add action count for the active user.
     */
    fun incrementAddActionCount()

    /**
     * Increments the copy action count for the active user.
     */
    fun incrementCopyActionCount()

    /**
     * Increments the create action count for the active user.
     */
    fun incrementCreateActionCount()

    /**
     * Returns a boolean value indicating whether or not the user should be prompted to
     * review the app.
     */
    fun shouldPromptForAppReview(): Boolean
}
