package ure.terrain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ure.actors.UActor;
import ure.actors.UPlayer;
import ure.actors.actions.Interactable;
import ure.areas.UArea;
import ure.areas.UCell;
import ure.kotlin.sys.Injector;
import ure.math.URandom;
import ure.sys.Entity;
import ure.sys.UCommander;
import ure.things.UContainer;
import ure.ui.Icons.Icon;
import ure.ui.Icons.UIconCzar;
import ure.ui.sounds.Sound;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A real serializable instance of a terrain which exists in a single cell.  Subclass this to
 * create new subtypes of Terrain.  To extend base terrain, use TerrainDeco.
 *
 * For API reference when extending this class to create custom terrain types, see the method docs of
 * TerrainDeco.
 *
 */

public abstract class UTerrain implements Entity, Cloneable, Interactable {

    @Inject
    @JsonIgnore
    UCommander commander;
    @Inject
    @JsonIgnore
    UIconCzar iconCzar;
    @Inject
    @JsonIgnore
    URandom random;

    @JsonIgnore
    public UCell cell;

    public static final String TYPE = "";

    protected String name;
    protected long ID;
    protected String plural;
    protected String type;
    protected String walkmsg = "";
    protected String bonkmsg = "";
    protected String lookmsg = "";
    protected Icon icon;
    protected String category;
    protected HashMap<String,Integer> stats = new HashMap<>();

    protected boolean opaque;
    protected boolean spawnok;
    protected boolean breaklatch = false;
    protected boolean glow = false;
    protected float sunvis = 0.0f;
    protected float movespeed = 1.0f;
    protected HashSet<String> moveTypes;

    protected String ambientsound;
    protected float ambientsoundGain;
    protected Sound stepsound;


    public UTerrain() {
        Injector.getAppComponent().inject(this);
    }

    /**
     * Set up a new template object fresh from resource JSON deserializing, to make it cloneable.
     */
    public void initializeAsTemplate() {

    }
    /**
     * Set up a fresh clone from a template object.
     */
    public void initializeAsCloneFrom(UTerrain template) {
        icon = null;
        icon();
    }

    public void reconnect(UArea area, UCell cell) {
        // TODO: Back reference
        this.cell = cell;
        if (icon() != null)
            icon.setEntity(this);
    }

    public void closeOut() {
        icon = null;
        stats = null;
    }

    public ArrayList<String> UIdetails(String context) { return null; }

    public ArrayList<String> UItips(String context) {
        if (lookmsg != null) {
            if (lookmsg.length() > 0) {
                ArrayList<String> tips = new ArrayList<>();
                tips.add(lookmsg);
                return tips;
            }
        }
        return null;
    }

    public void becomeReal(UCell c) {
        cell = c;
    }

    public Icon icon() {
        if (icon == null) {
            icon = iconCzar.getIconByName(name);
            if (icon != null)
                icon.setEntity(this);
        }
        return icon;
    }

    public UContainer location() {
        return cell;
    }

    public void moveTriggerFrom(UActor actor, UCell cell) {
        if (passable(actor)) {
            actor.moveToCell(cell.areaX(), cell.areaY());
        } else {
            actor.walkFail(cell);
        }
    }

    public boolean preventMoveFrom(UActor actor) {
        return false;
    }

    public void walkedOnBy(UActor actor, UCell cell) {
        if (actor instanceof UPlayer) {
            if (walkmsg != null)
                commander.printScroll(this.getIcon(), getWalkmsg());
        }
    }

    public boolean isInteractable(UActor actor) {
        return false;
    }

    public float interactionFrom(UActor actor) {
        return 0f;
    }

    public UTerrain makeClone() {
        try {
            return (UTerrain) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean passable() {
        if (moveTypes == null)
            return false;
        if (moveTypes.size() < 1)
            return false;
        return true;
    }

    public boolean passable(HashSet<String> actorMoveTypes) {
        if (actorMoveTypes == null) return false;
        if (passable()) {
            for (String moveType : actorMoveTypes) {
                for (String myType : moveTypes) {
                    if (myType.equals(moveType))
                        return true;
                }
            }
        }
        return false;
    }

    public boolean passable(UActor actor) { return passable(actor.moveTypes()); }

    public boolean isOpaque() { return opaque; }
    public boolean isSpawnok() { return spawnok; }
    public String getName() { return name; }
    public String getPlural() { return plural != null ? plural : getName() + "s"; }
    public Icon getIcon() { return icon; }
    public String getCategory() { return category; }

    public int getStat(String stat) {
        if (getStats().containsKey(stat))
            return (int)(getStats().get(stat));
        return 0;
    }

    public void setStat(String stat, int value) {
        getStats().put(stat, value);
    }

    public String name() { return name; }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWalkmsg() {
        return walkmsg;
    }

    public void setWalkmsg(String walkmsg) {
        this.walkmsg = walkmsg;
    }

    public String getBonkmsg() {
        return bonkmsg;
    }

    public void setBonkmsg(String bonkmsg) {
        this.bonkmsg = bonkmsg;
    }
    public String getLookmsg() { return lookmsg; }
    public void setLookmsg(String s) { lookmsg = s; }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    public HashMap<String, Integer> getStats() {
        return stats;
    }

    public void setStats(HashMap<String, Integer> stats) {
        this.stats = stats;
    }

    public void setOpaque(boolean opaque) {
        this.opaque = opaque;
    }

    public boolean isBreaklatch() {
        return breaklatch;
    }

    public void setBreaklatch(boolean breaklatch) {
        this.breaklatch = breaklatch;
    }

    public boolean isGlow() {
        return glow;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public float getSunvis() {
        return sunvis;
    }

    public void setSunvis(float sunvis) {
        this.sunvis = sunvis;
    }

    public float getMovespeed() {
        return movespeed;
    }
    public float getMoveSpeed(UActor actor) {
        return movespeed;
    }

    public void setMovespeed(float movespeed) {
        this.movespeed = movespeed;
    }

    public HashSet<String> getMoveTypes() { return moveTypes; }
    public void setMoveTypes(HashSet<String> h) { moveTypes = h; }

    public String getAmbientsound() { return ambientsound; }
    public void setAmbientsound(String s) { ambientsound = s; }
    public float getAmbientsoundGain() { return ambientsoundGain; }
    public void setAmbientsoundGain(float f) { ambientsoundGain = f; }
    public Sound getStepsound() { return stepsound; }
    public void setStepsound(Sound s) { stepsound = s; }

    public long getID() { return ID; }
    public void setID(long newID) { ID = newID; }

    public UArea area() {
        if (cell != null)
            return cell.area();
        else
            return null;
    }
    public int areaX() {
        if (cell != null) return cell.areaX();
        return 0;
    }
    public int areaY() {
        if (cell != null) return cell.areaY();
        return 0;
    }
}
