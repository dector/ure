package ure.examplegame;

import ure.actors.UActorCzar;
import ure.actors.UPlayer;
import ure.areas.UArea;
import ure.areas.UCartographer;
import ure.areas.UCell;
import ure.math.UColor;
import ure.render.URenderer;
import ure.render.URendererOGL;
import ure.sys.Injector;
import ure.sys.UCommander;
import ure.sys.UREGame;
import ure.sys.UTimeListener;
import ure.terrain.UTerrainCzar;
import ure.things.UThing;
import ure.things.UThingCzar;
import ure.ui.*;
import ure.ui.modals.HearModalTitleScreen;
import ure.ui.modals.UModalTitleScreen;
import ure.ui.panels.UActorPanel;
import ure.ui.panels.ULensPanel;
import ure.ui.panels.UScrollPanel;
import ure.ui.panels.UStatusPanel;

import javax.inject.Inject;

public class ExampleGame implements UREGame, HearModalTitleScreen, UTimeListener {

    static UArea area;
    static UCamera camera;
    static UPlayer player;
    static UStatusPanel statusPanel;
    static UScrollPanel scrollPanel;
    static ULensPanel lensPanel;
    static UActorPanel actorPanel;
    static URenderer renderer;

    @Inject
    UCommander commander;
    @Inject
    UTerrainCzar terrainCzar;
    @Inject
    UThingCzar thingCzar;
    @Inject
    UActorCzar actorCzar;
    @Inject
    UCartographer cartographer;

    public ExampleGame() {
        Injector.getAppComponent().inject(this);
    }

    private void makeWindow() {

        View rootView = new View();

        camera = new UCamera(renderer, 0, 0, 1200, 800);
        camera.moveTo(area, 40,20);
        rootView.addChild(camera);

        UColor borderColor = UColor.COLOR_DARKGRAY;

        statusPanel = new UStatusPanel(200, 200, 10, 10, commander.config.getTextColor(), UColor.COLOR_BLACK, borderColor);
        statusPanel.addText("name", "Kaffo",0,0);
        statusPanel.addText("race", "Owl",0,1);
        statusPanel.addText("class", "Ornithologist",0,2);
        statusPanel.addText("turn", "T 1", 0, 5);
        statusPanel.addText("time", "", 0, 6);
        statusPanel.addText("location", "?", 0, 8);
        statusPanel.addText("lens", "", 0, 20);
        statusPanel.setPosition(1200,0);
        rootView.addChild(statusPanel);

        actorPanel = new UActorPanel(200,600,10,10,commander.config.getTextColor(), UColor.COLOR_BLACK, borderColor);
        actorPanel.setPosition(1200,200);
        rootView.addChild(actorPanel);

        lensPanel = new ULensPanel(camera, 0, 0, 200, 200, 12, 12, commander.config.getTextColor(), UColor.COLOR_BLACK, borderColor);
        lensPanel.setPosition(1200,800);
        rootView.addChild(lensPanel);

        scrollPanel = new UScrollPanel(1200, 200, 12, 12, commander.config.getTextColor(), new UColor(0f,0f,0f), new UColor(0.3f,0.3f,0.3f));
        scrollPanel.addLineFade(new UColor(1.0f, 1.0f, 1.0f));
        scrollPanel.addLineFade(new UColor(0.6f, 0.6f, 0.6f));
        scrollPanel.addLineFade(new UColor(0.4f, 0.4f, 0.4f));
        scrollPanel.addLineFade(new UColor(0.3f, 0.3f, 0.3f));
        scrollPanel.setPosition(0,800);
        scrollPanel.setBounds(0,800,1200,200);
        scrollPanel.print("Welcome to UnRogueEngine!");
        scrollPanel.print("The universal java toolkit for roguelike games.");
        scrollPanel.print("Your journey begins...");
        rootView.addChild(scrollPanel);

        renderer.setRootView(rootView);



        commander.setStatusPanel(statusPanel);
        commander.setScrollPanel(scrollPanel);
        commander.registerModalCamera(camera);
        commander.config.setUiFrameGlyphs(null);
    }

    public void startUp()  {
        renderer = new URendererOGL();
        renderer.initialize();

        cartographer = new ExampleCartographer();

        commander.registerComponents(this, player, renderer, thingCzar, actorCzar, cartographer);

        makeWindow();

        commander.registerScrollPrinter(scrollPanel);
        commander.registerTimeListener(this);
        commander.addAnimator(camera);

        setupTitleScreen();

        commander.gameLoop();
    }

    public void setupTitleScreen() {
        scrollPanel.hide();
        lensPanel.hide();
        statusPanel.hide();
        actorPanel.hide();
        cartographer.setupRegions();
        area = cartographer.getTitleArea();
        if (player != null) {
            player.setCameraPinStyle(0);
            player.moveToCell(area, 50, 50);
            player = null;
        }
        camera.moveTo(area, 50, 50);
        commander.config.setVisibilityEnable(false);
        commander.showModal(new UModalTitleScreen(35, 20, this, "start", UColor.COLOR_BLACK, area));

    }

    public void hearTimeTick(UCommander cmdr) {
        if (statusPanel != null && !statusPanel.isHidden()) {
            statusPanel.setText("turn", "T " + Integer.toString(commander.getTurn()));
            statusPanel.setText("time", commander.timeString(true, " "));
            statusPanel.setText("location", commander.cartographer.describeLabel(commander.player().area().getLabel()));
        }
        if (actorPanel != null && !actorPanel.isHidden() && commander.player() != null) {
            actorPanel.updateActors((UPlayer)commander.player());
        }
    }

    public void hearModalTitleScreen(String context) {
        if (context.equals("Credits") || context.equals("Quit")) {
            commander.quitGame();
        } else {
            if (context.equals("New World")) {
                cartographer.wipeWorld();
            }
            continueGame();
        }
    }

    public void continueGame() {
        System.out.println("Creating the @Player");
        player = new UPlayer("Player", '@', UColor.COLOR_WHITE, true, new UColor(0.3f, 0.3f, 0.6f), 3, 4);
        commander.setPlayer(player);
        System.out.println("Un-hiding interface panels");
        statusPanel.unHide();
        lensPanel.unHide();
        scrollPanel.unHide();
        actorPanel.unHide();
        cartographer.startLoader();
        area = cartographer.getStartArea();
        UCell startcell = area.randomOpenCell(player);
        commander.config.setVisibilityEnable(true);
        player.attachCamera(camera, UCamera.PINSTYLE_SOFT);
        player.moveToCell(area, startcell.x, startcell.y);
        player.startActing();
        //cartographer.playerLeftArea(player, null);

        UThing item = thingCzar.getThingByName("rock");
        item.moveTo(player);
        item = thingCzar.getThingByName("trucker hat");
        item.moveTo(player);
        item = thingCzar.getThingByName("torch");
        item.moveTo(player);
        item = thingCzar.getThingByName("apple");
        item.moveTo(player);
        item = thingCzar.getThingByName("apple");
        item.moveTo(player);
        item = thingCzar.getThingByName("flashlight");
        item.moveTo(player);
        item = thingCzar.getThingByName("biscuit");
        item.moveTo(player);
        item = thingCzar.getThingByName("lantern");
        item.moveTo(player);
        item = thingCzar.getThingByName("butcher knife");
        item.moveTo(player);
        item = thingCzar.getThingByName("gold statue");
        item.moveTo(player);
        item = thingCzar.getThingByName("skull");
        item.moveTo(player);
    }
}
