package ure.areas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ure.actors.UActor;
import ure.actors.UPlayer;
import ure.actors.actions.UAction;
import ure.kotlin.sys.Injector;
import ure.math.Dimap;
import ure.math.UColor;
import ure.math.URandom;
import ure.sys.Entity;
import ure.sys.UCommander;
import ure.sys.UConfig;
import ure.sys.events.TimeTickEvent;
import ure.terrain.Stairs;
import ure.terrain.UTerrain;
import ure.terrain.UTerrainCzar;
import ure.things.UThing;
import ure.ui.ULight;
import ure.ui.modals.UModal;
import ure.ui.modals.UModalTooltip;
import ure.ui.particles.UParticle;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 *
 * UArea implements a self-contained 2D map of roguelike playfield cells.
 *
 */

public class UArea implements Serializable {

    @Inject
    @JsonIgnore
    public UCommander commander;
    @Inject
    @JsonIgnore
    public UConfig config;
    @Inject
    @JsonIgnore
    public UCartographer cartographer;
    @Inject
    @JsonIgnore
    public UTerrainCzar terrainCzar;
    @Inject
    @JsonIgnore
    public EventBus bus;

    protected String label;

    protected UCell[][] cells;
    public int xsize, ysize;
    protected HashSet<ULight> lights = new HashSet<>();
    protected HashSet<UActor> actors = new HashSet<>();
    @JsonIgnore
    protected HashSet<UParticle> particles = new HashSet<>();


    @Inject
    @JsonIgnore
    URandom random;

    protected UColor sunColor = new UColor(130,50,25);
    protected float clouds = 0.2f;

    protected ArrayList<Integer> sunColorLerpMarkers = new ArrayList<>();
    protected ArrayList<UColor> sunColorLerps = new ArrayList<>();
    protected HashMap<Integer,String> sunCycleMessages = new HashMap<>();
    protected int sunCycleLastAnnounceMarker;
    public boolean sunVisible;

    protected UColor fogColor = UColor.LIGHTBLUE;
    protected int fogDistance = 18;
    protected int fogDistanceFull = 35;

    String backgroundMusic;

    @JsonIgnore
    public boolean closed = false;
    @JsonIgnore
    public boolean closeRequested = false;
    @JsonIgnore
    public double closeRequestedTime;

    @JsonIgnore
    public ArrayList<Stairs> stairsLinks;

    @JsonIgnore
    HashMap<String,Dimap> dimaps;

    private Log log = LogFactory.getLog(UArea.class);

    public UArea() {
        Injector.getAppComponent().inject(this);
        bus.register(this);
        config.addDefaultSunCycle(this);
        dimaps = new HashMap<>();
    }
    public UArea(int thexsize, int theysize, String defaultTerrain) {
        this();
        initialize(thexsize,theysize,defaultTerrain);
    }

    public void initialize(int xsize, int ysize, String defaultTerrain) {
        this.xsize = xsize;
        this.ysize = ysize;
        cells = new UCell[xsize][ysize];
        for (int i=0;i<xsize;i++) {
            for (int j=0;j<ysize;j++)
                cells[i][j] = new UCell(this, i, j, terrainCzar.getTerrainByName(defaultTerrain));
        }
    }

    public boolean canBePersisted() {
        return true;
    }

    public void requestCloseOut() {
        closeRequested = true;
        closeRequestedTime = System.nanoTime();
    }

    public void closeOut() {
        closed = true;
        if (actors != null)
            for (UActor actor : actors)
                commander.unregisterActor(actor);
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (cells[x][y] != null) {
                    cells[x][y].closeOut();
                    cells[x][y] = null;
                }
            }
        }
        setLights(null);
        setActors(null);
        setCells(null);
        label = "closed (" + label + ")";
        log.debug(label);
    }

    /**
     * Do what's necessary to reconnect all internal references in our data model after deserialization.
     */
    public void reconnect() {
        reconnectCells();
        reconnectLights();
        reconnectActors();
        reconnectParticles();
    }

    protected void reconnectCells() {
        for (int x=0; x<cells.length; x++)
            for (int y=0; y<cells[x].length; y++) {
                cells[x][y].reconnect(this);
            }
    }

    protected void reconnectLights() {
        for (ULight light : lights) {
            light.reconnect(this);
        }
    }

    protected void reconnectActors() {
        // These will have been filled in by the deserializer, but we want them to be the same reference
        // that we see in our cell contents.
        actors = new HashSet<>();
        for (int x=0; x<cells.length; x++)
            for (int y=0; y<cells[x].length; y++) {
                for (UActor actor : cells[x][y].contents.getActors()) {
                    actors.add(actor);
                    commander.registerActor(actor);
                }
            }
    }

    protected void reconnectParticles() {
        for (UParticle particle : particles) {
            particle.reconnect(this);
        }
    }

    public HashSet<ULight> lights() {
        return getLights();
    }

    public void addLight(ULight light) {
        getLights().add(light);
    }

    public void removeLight(ULight light) {
        getLights().remove(light);
    }

    public void resetSunColorLerps() {
        getSunColorLerps().clear();
        getSunColorLerpMarkers().clear();
    }
    public void addSunColorLerp(int minutes, UColor color) { addSunColorLerp(minutes, color, null); }
    public void addSunColorLerp(int minutes, UColor color, String msg) {
        getSunColorLerps().add(color);
        getSunColorLerpMarkers().add(minutes);
        if (msg != null) {
            getSunCycleMessages().put((Integer)minutes, msg);
        }
    }
    public void setSunColor(float r, float g, float b) {
        this.getSunColor().set(r, g, b);
    }

    public void adjustSunColor(int minutes) {
        UColor lerp1, lerp2;
        int min1, min2;
        min1 = 0;
        min2 = 0;
        lerp1 = null;
        lerp2 = null;
        boolean gotem = false;
        for (int x = 0; x< getSunColorLerps().size(); x++) {
            int min = (int) getSunColorLerpMarkers().get(x);
            if (minutes >= min) {
                lerp1 = getSunColorLerps().get(x);
                min1 = min;
                gotem = true;
            } else if (gotem) {
                lerp2 = getSunColorLerps().get(x);
                min2 = min;
                gotem = false;
            }
        }
        float ratio = (float)(minutes - min1) / (float)(min2 - min1);
        setSunColor(lerp1.fR() + (lerp2.fR() - lerp1.fR()) * ratio,
                     lerp1.fG() + (lerp2.fG() - lerp1.fG()) * ratio,
                         lerp1.fB() + (lerp2.fB() - lerp1.fB()) * ratio);
        String msg = getSunCycleMessages().get(min1);
        if (msg != null && commander.player() != null) {
            if (getSunCycleLastAnnounceMarker() != min1) {
                setSunCycleLastAnnounceMarker(min1);
                UCell pcell = cellAt(commander.player().areaX(), commander.player().areaY());
                if (pcell != null)
                    if (pcell.sunBrightness() > 0.1f)
                        commander.printScroll(msg, getSunColor());
            }
        }
    }

    public URegion region() {
        return cartographer.regionForArea(this);
    }
    public boolean isValidXY(int x, int y) {
        if ((x >= 0) && (y >= 0))
            if ((x < xsize) && (y < ysize))
                return true;
        return false;
    }

    public boolean canSeeThrough(int x, int y) {
        UTerrain t = terrainAt(x,y);
        if (t == null) return true;
        return !t.isOpaque();
    }
    public UTerrain terrainAt(int x, int y) {
        if (isValidXY(x, y))
            if (getCells()[x][y] != null)
                return getCells()[x][y].terrain();
        return null;
    }
    public boolean hasTerrainAt(int x, int y, String terrain) {
        if (isValidXY(x,y))
            if (getCells()[x][y] != null)
                if (getCells()[x][y].getTerrain().getName().equals(terrain))
                    return true;
        return false;
    }

    public void setTerrain(int x, int y, String t) {
        if (isValidXY(x, y)) {
            UTerrain terrain = terrainCzar.getTerrainByName(t);
            getCells()[x][y].useTerrain(terrain);
        }
    }

    public UCell cellAt(int x, int y) {
        if (isValidXY(x,y)) {
            if (getCells() == null) {
                throw new RuntimeException("ACK!!! getCells() was null for area " + label);
            }
            return getCells()[x][y];
        }
        return null;
    }

    public boolean blocksLight(int x, int y) {
        UTerrain t = terrainAt(x, y);
        if (t != null)
            return t.isOpaque();
        return true;
    }

    public void setSeen(int x, int y) {
        setSeen(x, y, true);
    }

    public void setSeen(int x, int y, boolean seen) {
        if (isValidXY(x, y))
            getCells()[x][y].setSeen(seen);

    }

    public boolean seenCell(int x, int y) {
        if (isValidXY(x, y))
            return getCells()[x][y].isSeen();
        return false;
    }

    public float sunBrightnessAt(int x, int y) {
        if (isValidXY(x,y))
            if (getCells()[x][y] != null)
                return getCells()[x][y].sunBrightness();
        return 0.0f;
    }

    public boolean willAcceptThing(UThing thing, int x, int y) {
        if (isValidXY(x, y))
            return getCells()[x][y].willAcceptThing(thing);
        return false;
    }

    /**
     * A thing moved into one of our cells; do any bookkeeping or notification we require.
     *
     * @param thing
     * @param x
     * @param y
     * @return
     */
    public void addedThing(UThing thing, int x, int y) {
        if (thing instanceof UActor) {
            getActors().add((UActor) thing);
            if (thing instanceof UPlayer) {
                wakeCheckAll(x,y);
            } else {
                ((UActor)thing).wakeCheck(x,y);
            }
        }
    }

    public void wakeCheckAll(int playerx, int playery) {
        for (UActor actor : getActors()) {
            actor.wakeCheck(playerx, playery);
        }
    }
    public void wakeAllNPCs() {
        for (UActor actor : getActors()) {
            actor.startActing();
        }
    }

    public UCell randomOpenCell(UThing thing) {
        UCell cell = null;
        boolean match = false;
        while (cell == null || !match) {
            cell = cellAt(random.i(xsize), random.i(ysize));
            match = cell.willAcceptThing(thing);
        }
        return cell;
    }

    public UCell randomOpenCell() {
        UCell cell = null;
        boolean match = false;
        int tries = 0;
        while ((cell == null || !match) && tries < 100) {
            cell = cellAt(random.i(xsize),random.i(ysize));
            UTerrain t = cell.terrain();
            if (t != null)
                match = t.passable();
            tries++;
        }
        if (match)
            return cell;
        return null;
    }

    public void hearRemoveThing(UThing thing) {
        getActors().remove(thing);
    }

    public ArrayList<UThing> thingsAt(int x, int y) {
        if (isValidXY(x,y)) {
            return getCells()[x][y].things();
        }
        return null;
    }
    public UActor actorAt(int x, int y) {
        if (isValidXY(x,y)) {
            if (cells[x][y] != null)
                return cells[x][y].actorAt();
        }
        return null;
    }

    public UCell findExitTo(String label) {
        if (closed) return null;
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (cells[x][y].terrain() instanceof Stairs) {
                    if (((Stairs)cells[x][y].terrain()).getLabel().equals(label)) {
                        return cells[x][y];
                    }
                }
            }
        }
        return null;
    }

    public void setLinks() {
        stairsLinks = new ArrayList<Stairs>();
        for (int x=0;x<xsize;x++) {
            for (int y=0;y<ysize;y++) {
                if (cells[x][y].terrain() instanceof Stairs) {
                    stairsLinks.add((Stairs)cells[x][y].terrain());
                }
            }
        }
    }
    public ArrayList<Stairs> stairsLinks() { return stairsLinks; }

    @Subscribe
    public void hearTimeTick(TimeTickEvent event) {
        if (this.closeRequested || this.closed)
            bus.unregister(this);
        else {
            log.trace(getLabel() + " tick");
            adjustSunColor(commander.daytimeMinutes());
        }
    }

    /**
     * Broadcast an action's occurence to everyone who can be aware of it, based on the position
     * of the actor.
     *
     * @param action
     */
    public void broadcastEvent(UAction action) {
        if (closed) {
            log.warn(action.id + " event in closed area " + label);
            return;
        }
        for (UActor actor : getActors()) {
            // TODO: some events should probably go further than just seeing?
            if (actor.canSee(action.actor)) {
                actor.hearEvent(action);
            }
        }
    }

    public void setLabel(String thelabel) {
        label = thelabel;
    }

    public void addParticle(UParticle particle) {
        if (commander.hasModal())
            return;
        if (isValidXY(particle.x, particle.y)) {
            cellAt(particle.x,particle.y).addParticle(particle);
            particle.reconnect(this);
            particles.add(particle);
        }
    }
    public void fizzleParticle(UParticle particle) {
        cellAt(particle.x,particle.y).fizzleParticle(particle);
        particles.remove(particle);
    }

    public void animationTick() {
        for (UParticle particle : (HashSet<UParticle>)particles.clone()) {
            particle.animationTick();
            if (particle.isFizzled())
                fizzleParticle(particle);
        }
        for (ULight light : lights) {
            light.animationTick();
        }
    }

    public Dimap dimapFor(String key) {
        if (dimaps.containsKey(key))
            return dimaps.get(key);
        return null;
    }

    public Dimap addDimap(String key, Dimap map) {
        dimaps.put(key,map);
        return map;
    }

    public UModal makeTooltipAt(int x, int y) {
        UCell cell = cellAt(x,y);
        if (cell != null) {
            Entity entity = null;
            if (cell.actorAt() != null) {
                entity = cell.actorAt();
            } else if (cell.things() != null) {
                if (cell.things().size() > 0)
                    entity = cell.things().get(0);
            }
            if (entity == null)
                entity = cell.terrain();
            if (entity != null) {
                if (entity.UItips("") != null) {
                    UModal tooltip = new UModalTooltip(entity);
                    return tooltip;
                }
            }
        }
        return null;
    }

    /**
     * Do what it takes to make this area ready for serialization.
     * Only handle things internal to me; the Cartographer will take care of the rest.
     *
     */
    public void freezeForPersist() {
        for (UActor actor : getActors()) {
            log.debug(label + ": sleeping " + actor.getName() + " for freeze");
            commander.unregisterActor(actor);
        }
        for (ULight light : (HashSet<ULight>)lights.clone()) {
            if (!light.isPermanent()) {
                removeLight(light);
            }
        }
    }

    public void wipe(String wipeTerrain) {
        freezeForPersist();
        lights = new HashSet<>();
        actors = new HashSet<>();
        for (int i=0;i<xsize;i++) {
            for (int j=0;j<ysize;j++) {
                cells[i][j] = new UCell(this, i, j, terrainCzar.getTerrainByName(wipeTerrain));
            }
        }
    }

    public String getLabel() {
        return label;
    }

    public UCell[][] getCells() {
        return cells;
    }

    public void setCells(UCell[][] cells) {
        this.cells = cells;
    }

    public HashSet<ULight> getLights() {
        return lights;
    }

    public void setLights(HashSet<ULight> lights) {
        this.lights = lights;
    }

    public HashSet<UActor> getActors() {
        return actors;
    }

    public void setActors(HashSet<UActor> actors) {
        this.actors = actors;
    }

    public HashSet<UParticle> getParticles() {
        return particles;
    }

    public void setParticles(HashSet<UParticle> particles) {
        this.particles = particles;
    }

    public UColor getSunColor() {
        return sunColor;
    }

    public void setSunColor(UColor sunColor) {
        this.sunColor = sunColor;
    }

    public boolean isSunVisible() { return sunVisible; }
    public void setSunVisible(boolean b) { sunVisible = b; }

    public float getClouds() {
        return clouds;
    }

    public void setClouds(float clouds) {
        this.clouds = clouds;
    }

    public ArrayList<Integer> getSunColorLerpMarkers() {
        return sunColorLerpMarkers;
    }

    public void setSunColorLerpMarkers(ArrayList<Integer> sunColorLerpMarkers) {
        this.sunColorLerpMarkers = sunColorLerpMarkers;
    }

    public ArrayList<UColor> getSunColorLerps() {
        return sunColorLerps;
    }

    public void setSunColorLerps(ArrayList<UColor> sunColorLerps) {
        this.sunColorLerps = sunColorLerps;
    }

    public HashMap<Integer, String> getSunCycleMessages() {
        return sunCycleMessages;
    }

    public void setSunCycleMessages(HashMap<Integer, String> sunCycleMessages) {
        this.sunCycleMessages = sunCycleMessages;
    }

    public int getSunCycleLastAnnounceMarker() {
        return sunCycleLastAnnounceMarker;
    }

    public void setSunCycleLastAnnounceMarker(int sunCycleLastAnnounceMarker) {
        this.sunCycleLastAnnounceMarker = sunCycleLastAnnounceMarker;
    }

    public UColor getFogColor() { return fogColor; }
    public void setFogColor(UColor c) { fogColor = c; }
    public int getFogDistance() { return fogDistance; }
    public void setFogDistance(int i) { fogDistance = i; }
    public int getFogDistanceFull() { return fogDistanceFull; }
    public void setFogDistanceFull(int i) { fogDistanceFull = i; }

    public void setBackgroundMusic(String b) { backgroundMusic = b; }
    public String getBackgroundMusic() { return backgroundMusic; }

}
