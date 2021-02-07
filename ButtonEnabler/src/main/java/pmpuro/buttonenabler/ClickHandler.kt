package pmpuro.buttonenabler

import kotlinx.coroutines.flow.StateFlow

interface ClickHandler {
    fun launchClick()
    suspend fun click()
    val enabled: StateFlow<Boolean>
}
