package ure.ui.modals

import ure.ui.modals.TitleScreenModalAction.*

/**
 * This interface describes an object which wants to receive callbacks from a modal.
 *
 * This is an abstract interface and can't be implemented directly; every subclass of UModal should have a matching
 * HearModalType interface for callback targets to implement.
 */
interface ModalCallback

@Deprecated("")
interface TitleScreenModalCallback : ModalCallback {

    fun hearModalTitleScreen(context: String, optional: String)
}

sealed class TitleScreenModalAction {
    data class NewWorld(val playerName: String) : TitleScreenModalAction()
    data class ContinueGame(val playerName: String) : TitleScreenModalAction()
    object Quit : TitleScreenModalAction()
}

@Deprecated("Legacy wrapper")
fun ((TitleScreenModalAction) -> Unit).wrapper() = object : TitleScreenModalCallback {
    override fun hearModalTitleScreen(context: String, optional: String) {
        when (context) {
            "NewWorld" ->
                this@wrapper(NewWorld(optional))
            "Continue" ->
                this@wrapper(ContinueGame(optional))
            "Quit" ->
                this@wrapper(Quit)
        }
    }
}
