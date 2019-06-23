package ure.sample

import ure.actors.UActorCzar
import ure.actors.UPlayer
import ure.areas.UArea
import ure.areas.UCartographer
import ure.di.Injector
import ure.examplegame.ExampleCartographer
import ure.log.configureLog
import ure.log.getLog
import ure.math.UColor
import ure.render.URenderer
import ure.sys.UCommander
import ure.sys.UConfig
import ure.sys.UREWindow
import ure.sys.UREgame
import ure.terrain.UTerrainCzar
import ure.things.UThingCzar
import ure.ui.UCamera
import ure.ui.modals.HearModalTitleScreen
import ure.ui.modals.UModalTitleScreen
import ure.ui.panels.*
import javax.inject.Inject

/**
 * Sample game. Usage:
 *
 * ```
 * SampleGame().launch()
 * ```
 */
class SampleGame : UREgame, HearModalTitleScreen {

    private val log = getLog()

    init {
        // Set up logging before doing anything else, including dependency injection.  That way we'll
        // get proper logging for @Provides methods.
        configureLog(SampleGame::class)
    }

    @Inject
    lateinit var renderer: URenderer

    @Inject
    lateinit var commander: UCommander

    @Inject
    lateinit var config: UConfig

    @Inject
    lateinit var terrainCzar: UTerrainCzar

    @Inject
    lateinit var thingCzar: UThingCzar

    @Inject
    lateinit var actorCzar: UActorCzar

    @Inject
    lateinit var cartographer: UCartographer

    private lateinit var player: UPlayer

    private lateinit var area: UArea

    private lateinit var window: UREWindow
    private lateinit var camera: UCamera

    private lateinit var scrollPanel: ScrollPanel

    init {
        Injector.appComponent.inject(this)
    }

    fun launch() {
        initUI()

        cartographer = ExampleCartographer()

        commander.registerComponents(this, window, null, renderer, thingCzar, actorCzar, cartographer)
        commander.registerScrollPrinter(scrollPanel)
        commander.addAnimator(camera)

        setupTitleScreen()

        commander.gameLoop()
    }

    private fun initUI() {
        window = UREWindow()
        camera = UCamera(0, 0, 1200, 800).apply {
            moveTo(area, 40, 20)
            window.setCamera(this)
        }

        val borderColor = UColor.DARKGRAY

        val statusPanel = StatusPanel(10, 10, config.textColor, null, borderColor).apply {
            addText("name", " ", 0, 0)
            addText("race", "Owl", 0, 1)
            addText("class", "Ornithologist", 0, 2)
            addText("turn", "T 1", 0, 5)
            addText("time", "", 0, 6)
            addText("location", "?", 0, 8)
            addText("lens", "", 0, 20)
            setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_BOTTOM, 8, 0.15f, 12, 10, 0f, 10)

            window.addPanel(this)
        }

        ActorPanel(10, 10, config.textColor, null, borderColor).apply {
            setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_FIT, 8, 0.15f, 12, 1, 1f, 9999)

            window.addPanel(this)
        }

        LensPanel(camera, 0, 0, 12, 12, config.textColor, null, borderColor).apply {
            setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_TOP, 8, 0.15f, 12, 6, 0f, 6)
        }.also { window.addPanel(it) }

        scrollPanel = ScrollPanel(12, 12, config.textColor, null, UColor(0.3f, 0.3f, 0.3f)).apply {
            setLayout(UPanel.XPOS_FIT, UPanel.YPOS_BOTTOM, 0, 1f, 9999, 2, 0.18f, 11)
            addLineFade(UColor(1.0f, 1.0f, 1.0f))
            addLineFade(UColor(0.8f, 0.8f, 0.8f))
            addLineFade(UColor(0.6f, 0.6f, 0.6f))
            addLineFade(UColor(0.5f, 0.5f, 0.5f))
            addLineFade(UColor(0.4f, 0.4f, 0.4f))
            addLineFade(UColor(0.3f, 0.3f, 0.3f))

            print("Welcome to UnRogueEngine!")
            print("The universal java toolkit for roguelike games.")
            print("Your journey begins...")

            window.addPanel(this)
        }

        HotbarPanel(config.textColor, config.panelBgColor).apply {
            setLayout(UPanel.XPOS_FIT, UPanel.YPOS_TOP, 1, 1f, 9999, 2, 0f, 2)

            window.addPanel(this)
        }

        window.doLayout()
        renderer.rootView = window

        commander.setStatusPanel(statusPanel)
        commander.setScrollPanel(scrollPanel)
        commander.registerCamera(camera)
    }

    override fun setupTitleScreen() {
        window.hidePanels()

        area = cartographer.titleArea

        camera.moveTo(area, 50, 50)

        commander.config.isVisibilityEnable = false
        commander.showModal(UModalTitleScreen(22, 22, this, "start", area))
    }

    override fun hearModalTitleScreen(context: String, optional: String) {
        if (context == "Credits" || context == "Quit") {
            commander.quitGame()
        } else {
            if (context == "New World") {
                cartographer.wipeWorld()
                continueGame(optional)
            } else {
                continueGame(optional)
            }
        }
    }

    private fun continueGame(playerName: String) {
        area.requestCloseOut()
        player = run {
            var player = commander.loadPlayer()

            if (player == null) {
                player = createNewPlayer(playerName)

                log.debug("Getting the starting area")

                area = cartographer.run {
                    startLoader()
                    makeStartArea()
                }

                val startCell = area.randomOpenCell(player)
                player.setSaveLocation(area, startCell.x, startCell.y)
            } else {
                log.info("Loading existing player into " + player.getSaveAreaLabel())

                cartographer.startLoader()
                area = cartographer.getArea(player.getSaveAreaLabel())
            }

            player
        }

        commander.startGame(player, area)
        player.attachCamera(camera, config.cameraPinStyle)
        window.showPanels()
    }

    private fun createNewPlayer(playerName: String): UPlayer {
        log.debug("Creating a brand new @Player with name '$playerName'")

        val player = UPlayer("Player", UColor(0.1f, 0.1f, 0.4f), 2, 3).apply {
            name = playerName
            id = commander.generateNewID(this)
        }

        // Initial Things
        listOf(
            "small stone",
            "trucker hat",
            "apple",
            "nylon backpack",
            "flashlight",
            "biscuit",
            "lantern",
            "butcher knife",
            "hiking boots",
            "army helmet",
            "confusion helmet",
            "aluminum bat",
            "leather jacket"
        )
            .mapNotNull { thingCzar.getThingByName(it) }
            .forEach { it.moveTo(player) }

        // Initial coins
        mapOf(
            "gold coins" to 100,
            "gold coins" to 320
        ).mapNotNull { (name, amount) ->
            thingCzar.getPile(name, amount)
        }.forEach { it.moveTo(player) }

        return player
    }
}

fun main() {
    SampleGame().launch()
}
