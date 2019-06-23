package ure.ui.modals

import ure.areas.UArea
import ure.commands.UCommand
import ure.editors.glyphed.GlyphedModal
import ure.editors.landed.LandedModal
import ure.math.UColor
import ure.sys.GLKey
import ure.ui.modals.widgets.Widget
import ure.ui.modals.widgets.WidgetListVert
import ure.ui.modals.widgets.WidgetRexImage
import ure.ui.modals.widgets.WidgetText

import java.io.File

class UModalTitleScreen(
    cellWidth: Int, cellHeight: Int,
    callback: HearModalTitleScreen,
    callbackContext: String,
    internal var area: UArea
) : UModal(callback, callbackContext), HearModalGetString, HearModalChoices {

    private var logoWidget: WidgetRexImage
    private var titleWidget: WidgetText
    private var menuWidget: WidgetListVert

    private var fakeTickCount: Int = 0

    init {
        setDimensions(cellWidth, cellHeight)

        escapable = false
        logoWidget = WidgetRexImage(this, 0, 0, "/ure_logo.xp")
        logoWidget.alpha = 0f

        addCenteredWidget(logoWidget)

        titleWidget = WidgetText(this, 0, 11, TITLE_MESSAGE)
        titleWidget.hidden = true

        addCenteredWidget(titleWidget)
        setBgColor(UColor(0.07f, 0.07f, 0.07f))

        val options: Array<String>
        val file = File(commander.savePath() + "player")
        options = if (!file.isFile)
            arrayOf("New World", "Edit", "Credits", "Quit")
        else
            arrayOf("Continue", "New World", "Edit", "Credits", "Quit")

        menuWidget = WidgetListVert(this, 0, 13, options)
        menuWidget.hidden = true
        menuWidget.dismissFlash = true

        addCenteredWidget(menuWidget)

        fakeTickCount = 0
        dismissFrameEnd = 0
        commander.speaker.playBGM(commander.config.titleMusic)
    }

    override fun hearCommand(command: UCommand?, k: GLKey) {
        if (logoWidget.alpha < 1f) {
            logoWidget.alpha = 1f
            return
        }
        super.hearCommand(command, k)
    }

    override fun mouseClick() {
        if (logoWidget.alpha < 1f) {
            logoWidget.alpha = 1f
            return
        }
        super.mouseClick()
    }

    override fun pressWidget(widget: Widget) {
        if (widget == menuWidget) {
            pickSelection(menuWidget.choice())
        }
    }

    private fun pickSelection(option: String) {
        when (option) {
            "New World" -> commander.showModal(
                UModalGetString("Name your character:", 15, 25, this, "name-new-world"))
            "Credits" -> {
                val modal = UModalNotify("URE: the unRoguelike Engine\n \nSpunky - meta\nMoycakes - openGL\nKapho - QA, content\nGilmore - misc").apply {
                    setPad(1, 1)
                    setTitle("credits")
                }
                commander.showModal(modal)
            }
            "Edit" -> commander.showModal(
                UModalChoices(null, arrayOf("LandEd", "VaultEd", "GlyphEd"), this, "edit"))
            else -> {
                dismiss()
                (callback as HearModalTitleScreen).hearModalTitleScreen(option, "")
            }
        }
    }

    override fun hearModalChoices(context: String, option: String) {
        when (option) {
            "LandEd" -> {
                dismiss()
                commander.showModal(LandedModal(area))
            }
            "VaultEd" ->
                commander.launchVaulted()
            "GlyphEd" ->
                commander.showModal(GlyphedModal())
        }
    }

    override fun hearModalGetString(context: String, input: String) {
        if (context == "name-new-world") {
            escape()
            (callback as HearModalTitleScreen).hearModalTitleScreen("New World", input)
        }
    }

    override fun animationTick() {
        super.animationTick()
        area.animationTick()

        logoWidget.alpha += 0.02f

        if (logoWidget.alpha > 1f) {
            logoWidget.alpha = 1f
            titleWidget.hidden = false
            menuWidget.hidden = false
        }

        fakeTickCount++

        if (fakeTickCount > 20) {
            fakeTickCount = 0
            commander.tickTime()
            commander.letActorsAct()
        }
    }
}

private const val TITLE_MESSAGE = "Example Quest : Curse of the Feature Creep"
