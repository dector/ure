package ure.kotlin.ui.modals

/**
 * This interface describes an object which wants to receive callbacks from a modal.
 *
 * This is an abstract interface and can't be implemented directly; every subclass of UModal should have a matching
 * HearModalType interface for callback targets to implement.
 */
interface HearModal

interface HearModalTitleScreen : HearModal {

    fun hearModalTitleScreen(context: String, optional: String)

}
