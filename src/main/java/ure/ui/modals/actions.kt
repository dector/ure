package ure.ui.modals

sealed class TitleScreenModalAction {
    data class NewWorld(val playerName: String) : TitleScreenModalAction()
    data class ContinueGame(val playerName: String) : TitleScreenModalAction()
    object Quit : TitleScreenModalAction()
}
