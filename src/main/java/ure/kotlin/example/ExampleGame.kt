package ure.kotlin.example

import org.apache.commons.logging.LogFactory
import ure.actors.UActorCzar
import ure.actors.UPlayer
import ure.areas.UArea
import ure.areas.UCartographer
import ure.examplegame.ExampleCartographer
import ure.kotlin.ui.modals.HearModalTitleScreen
import ure.math.UColor
import ure.render.URenderer
import ure.sys.*
import ure.terrain.UTerrainCzar
import ure.things.UThing
import ure.things.UThingCzar
import ure.ui.UCamera
import ure.ui.modals.UModalTitleScreen
import ure.ui.panels.*
import java.io.IOException
import java.util.logging.LogManager
import javax.inject.Inject

class ExampleGame : UREgame, HearModalTitleScreen {

    var renderer: URenderer? = null
        @Inject set
    var commander: UCommander? = null
        @Inject set
    var config: UConfig? = null
        @Inject set
    var terrainCzar: UTerrainCzar? = null
        @Inject set
    var thingCzar: UThingCzar? = null
        @Inject set
    var actorCzar: UActorCzar? = null
        @Inject set

    @Inject
    lateinit var cartographer: UCartographer

    var player: UPlayer? = null

    lateinit var area: UArea
    private lateinit var camera: UCamera
    lateinit var statusPanel: StatusPanel
    lateinit var hotbarPanel: HotbarPanel
    lateinit var scrollPanel: ScrollPanel
    lateinit var lensPanel: LensPanel
    lateinit var actorPanel: ActorPanel
    lateinit var window: UREWindow

    private val log = LogFactory.getLog(ExampleGame::class.java)

    init {
        // Set up logging before doing anything else, including dependency injection.  That way we'll
        // get proper logging for @Provides methods.
        try {
            val configInputStream = ExampleGame::class.java.getResourceAsStream("/logging.properties")
            LogManager.getLogManager().readConfiguration(configInputStream)
        } catch (ioe: IOException) {
            throw RuntimeException("Can't configure logger", ioe)
        }

        Injector.appComponent.inject(this)
    }

    private fun makeWindow() {
        window = UREWindow()
        camera = UCamera(0, 0, 1200, 800).apply {
            moveTo(area, 40, 20)
            window.setCamera(this)
        }

        val borderColor = UColor.DARKGRAY

        statusPanel = StatusPanel(10, 10, config!!.textColor, null, borderColor)

        statusPanel.addText("name", " ", 0, 0)
        statusPanel.addText("race", "Owl", 0, 1)
        statusPanel.addText("class", "Ornithologist", 0, 2)
        statusPanel.addText("turn", "T 1", 0, 5)
        statusPanel.addText("time", "", 0, 6)
        statusPanel.addText("location", "?", 0, 8)
        statusPanel.addText("lens", "", 0, 20)
        statusPanel.setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_BOTTOM, 8, 0.15f, 12, 10, 0f, 10)
        window.addPanel(statusPanel)

        actorPanel = ActorPanel(10, 10, config!!.textColor, null, borderColor)
        actorPanel.setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_FIT, 8, 0.15f, 12, 1, 1f, 9999)
        window.addPanel(actorPanel)

        lensPanel = LensPanel(camera, 0, 0, 12, 12, config!!.textColor, null, borderColor)
        lensPanel.setLayout(UPanel.XPOS_LEFT, UPanel.YPOS_TOP, 8, 0.15f, 12, 6, 0f, 6)
        window.addPanel(lensPanel)


        scrollPanel = ScrollPanel(12, 12, config!!.textColor, null, UColor(0.3f, 0.3f, 0.3f))
        scrollPanel.setLayout(UPanel.XPOS_FIT, UPanel.YPOS_BOTTOM, 0, 1f, 9999, 2, 0.18f, 11)
        scrollPanel.addLineFade(UColor(1.0f, 1.0f, 1.0f))
        scrollPanel.addLineFade(UColor(0.8f, 0.8f, 0.8f))
        scrollPanel.addLineFade(UColor(0.6f, 0.6f, 0.6f))
        scrollPanel.addLineFade(UColor(0.5f, 0.5f, 0.5f))
        scrollPanel.addLineFade(UColor(0.4f, 0.4f, 0.4f))
        scrollPanel.addLineFade(UColor(0.3f, 0.3f, 0.3f))
        scrollPanel.print("Welcome to UnRogueEngine!")
        scrollPanel.print("The universal java toolkit for roguelike games.")
        scrollPanel.print("Your journey begins...")
        window.addPanel(scrollPanel)

        hotbarPanel = HotbarPanel(config!!.textColor, config!!.panelBgColor)
        hotbarPanel.setLayout(UPanel.XPOS_FIT, UPanel.YPOS_TOP, 1, 1f, 9999, 2, 0f, 2)
        window.addPanel(hotbarPanel)

        window.doLayout()
        renderer!!.rootView = window

        commander!!.setStatusPanel(statusPanel)
        commander!!.setScrollPanel(scrollPanel)
        commander!!.registerCamera(camera)
    }

    fun startUp() {

        cartographer = ExampleCartographer()
        makeWindow()
        commander!!.registerComponents(this, window, player, renderer, thingCzar, actorCzar, cartographer)


        commander!!.registerScrollPrinter(scrollPanel)
        commander!!.addAnimator(camera)

        setupTitleScreen()

        commander!!.gameLoop()
    }

    override fun setupTitleScreen() {
        window.hidePanels()
        area = cartographer.titleArea
        camera.moveTo(area, 50, 50)
        commander!!.config.isVisibilityEnable = false
        commander!!.showModal(UModalTitleScreen(22, 22, this, "start", area))
    }

    override fun hearModalTitleScreen(context: String, optional: String) {
        if (context == "Credits" || context == "Quit") {
            commander!!.quitGame()
        } else {
            if (context == "New World") {
                cartographer.wipeWorld()
                continueGame(optional)
            } else {
                continueGame(optional)
            }
        }
    }

    fun continueGame(playername: String) {
        area.requestCloseOut()
        player = commander!!.loadPlayer()
        if (player == null) {
            player = makeNewPlayer(playername)
            log.debug("Getting the starting area")
            cartographer.startLoader()
            area = cartographer.makeStartArea()
            val startcell = area.randomOpenCell(player)
            player!!.setSaveLocation(area, startcell.x, startcell.y)
        } else {
            log.info("Loading existing player into " + player!!.getSaveAreaLabel())
            cartographer.startLoader()
            area = cartographer.getArea(player!!.getSaveAreaLabel())
        }
        commander!!.startGame(player, area)
        player!!.attachCamera(camera, config!!.cameraPinStyle)
        window.showPanels()
    }

    fun makeNewPlayer(playername: String): UPlayer {
        log.debug("Creating a brand new @Player")
        player = UPlayer("Player", UColor(0.1f, 0.1f, 0.4f), 2, 3)
        player!!.name = playername
        player!!.id = commander!!.generateNewID(player)

        var item: UThing? = thingCzar!!.getThingByName("small stone")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("trucker hat")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("apple")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("nylon backpack")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("flashlight")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("biscuit")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("lantern")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("butcher knife")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("hiking boots")
        item!!.moveTo(player!!)

        item = thingCzar!!.getThingByName("army helmet")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("confusion helmet")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("aluminum bat")
        item!!.moveTo(player!!)
        item = thingCzar!!.getThingByName("leather jacket")
        item!!.moveTo(player!!)
        item = thingCzar!!.getPile("gold coins", 100)
        item!!.moveTo(player!!)
        item = thingCzar!!.getPile("gold coins", 320)
        item!!.moveTo(player!!)
        return player!!
    }
}

fun main() {
    ExampleGame().startUp()
}
