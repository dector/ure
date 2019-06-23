package ure.ui.modals

/**
 * Implement this interface to receive callbacks from a ModalChoices.
 */
interface HearModalChoices : ModalCallback {

    fun hearModalChoices(context: String, choice: String)
}
