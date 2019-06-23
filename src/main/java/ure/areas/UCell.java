package ure.areas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ure.actors.UActor;
import ure.actors.UPlayer;
import ure.actors.actions.UAction;
import ure.kotlin.sys.Injector;
import ure.sys.UCommander;
import ure.terrain.Stairs;
import ure.terrain.UTerrain;
import ure.things.UCollection;
import ure.things.UContainer;
import ure.things.UThing;
import ure.ui.Icons.Icon;
import ure.ui.particles.UParticle;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * UCell represents a single XY cell of an area.
 *
 * These are created on area creation along with a terrain.  They have no type and should
 * not need to be subclassed, however, a custom UCell class can be given to Area on creation.
 *
 */
public class UCell implements UContainer {

    @Inject
    @JsonIgnore
    UCommander commander;

    @JsonIgnore
    UArea area;
    @JsonIgnore
    ArrayList<UParticle> particles;

    public int x,y;

    protected UTerrain terrain;
    protected UCollection contents;
    protected boolean isSeen = false;

    public UCell() {
        Injector.getAppComponent().inject(this);
        particles = new ArrayList<UParticle>();
    }

    public UCell(UArea theArea, int thex, int they, UTerrain theTerrain) {
        this();
        setContents(new UCollection(this, "cell" + Integer.toString(thex) + ","+Integer.toString(they) + " area " + theArea.label));
        area = theArea;
        x = thex;
        y = they;
        useTerrain(theTerrain);
    }

    public void reconnect(UArea area) {
        this.area = area;
        terrain.reconnect(area, this);
        contents.reconnect(area, this);
    }

    public void closeOut() {
        if (terrain != null) terrain.closeOut();
        contents.closeOut();
        terrain = null;
        contents = null;
    }

    public float sunBrightness() {
        if (area.isSunVisible())
            return getTerrain().getSunvis();
        return 0f;
    }

    public void setSeen(boolean theseen) {
        isSeen = theseen;
    }
    public boolean isSeen() {
        return isSeen;
    }
    public UTerrain terrain() {
        return getTerrain();
    }

    public void addThing(UThing thing) {
        if (thing instanceof UActor) commander.registerActor((UActor)thing);
        getContents().add(thing);
    }
    public void removeThing(UThing thing) {
        if (thing instanceof UActor) commander.unregisterActor((UActor)thing);
        getContents().remove(thing);
        area.hearRemoveThing(thing);
    }
    public Iterator<UThing> iterator() {
        return getContents().iterator();
    }
    public ArrayList<UThing> things() { return getContents().getThings(); }

    public int containerType() { return UContainer.TYPE_CELL; }

    public void moveTriggerFrom(UActor actor) {
        if (actorAt() != null) {
            actorAt().moveTriggerFrom(actor);
        } else {
            getTerrain().moveTriggerFrom(actor, this);
        }
    }

    public void walkedOnBy(UActor actor) {
        if (actor instanceof UPlayer && getContents().hasThings()) {
            UThing thing = getContents().topThing();
            commander.printScroll(thing.getIcon(), thing.walkMsg(actor));
        }
        getTerrain().walkedOnBy(actor, this);
    }

    /**
     * URE assumes only one actor can be considered to be in a cell at one time, however,
     * we internally allow for a list of actors.
     *
     * @return
     */
    public UActor actorAt() {
        if (getContents().hasActors())
            return getContents().actor();
        return null;
    }

    /**
     * Get the thing 'on top' of the pile of things here.
     *
     * @return
     */
    public UThing topThingAt() {
        return getContents().topThing();
    }

    /**
     * Get all the things here gettable by this actor.
     */
    public ArrayList<UThing> gettableThingsAt(UActor actor) {
        ArrayList<UThing> gettables = new ArrayList<>();
        for (UThing thing : contents.getThings()) {
            if (thing.isMovableBy(actor)) {
                gettables.add(thing);
            }
        }
        return gettables;
    }

    public boolean willAcceptThing(UThing thing) {
        if (getTerrain() != null) {
            if (getTerrain().passable()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does anything in the cell prevent this action from being done?
     *
     * @param action
     * @return
     */
    public boolean preventAction(UAction action) {
        return false;
    }
    /**
     * Is there any thing here named this?
     *
     * @param thing
     * @return
     */
    public boolean hasA(String thing) {
        for (UThing t : getContents().getThings()) {
            if (t.getName().equals(thing))
                return true;
        }
        return false;
    }
    public int areaX() { return x; }
    public int areaY() { return y; }
    public UArea area() { return area; }
    public UCell cell() { return this; }

    /**
     * Take a terrain object and make it our terrain.
     *
     * @param t
     */
    public void useTerrain(UTerrain t) {
        setTerrain(t);
        t.becomeReal(this);
    }

    public void animationTick() {
        UActor actor = actorAt();
        if (actor != null)
            actor.animationTick();
        if (contents.hasThings()) {
            for (UThing thing : (ArrayList<UThing>)contents.getThings().clone()) {
                thing.animationTick();
            }
        }
    }

    public Icon mapIcon() {
        if (!isSeen()) return null;
        if (terrain instanceof Stairs)
            return terrain.icon();
        return null;
    }
    public String mapLabel() {
        if (!isSeen()) return null;
        if (terrain instanceof Stairs)
            return commander.cartographer.describeLabel(((Stairs)terrain).getLabel());
        return "?";
    }

    public void addParticle(UParticle particle) {
        particles.add(particle);
    }
    public void fizzleParticle(UParticle particle) {
        particles.remove(particle);
    }

    public UTerrain getTerrain() {
        return terrain;
    }

    public void setTerrain(UTerrain terrain) {
        this.terrain = terrain;
    }

    public UCollection getContents() {
        return contents;
    }

    public void setContents(UCollection contents) {
        this.contents = contents;
    }

    public void setArea(UArea area) {
        this.area = area;
    }

    public ArrayList<UParticle> getParticles() { return particles; }
}
