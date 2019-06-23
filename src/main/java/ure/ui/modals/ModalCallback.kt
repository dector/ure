package ure.ui.modals

/**
 * This interface describes an object which wants to receive callbacks from a modal.
 *
 * This is an abstract interface and can't be implemented directly; every subclass of UModal should have a matching
 * HearModalType interface for callback targets to implement.
 */
interface ModalCallback

sealed class TitleScreenModalAction {
    data class NewWorld(val playerName: String) : TitleScreenModalAction()
    data class ContinueGame(val playerName: String) : TitleScreenModalAction()
    object Quit : TitleScreenModalAction()
}
