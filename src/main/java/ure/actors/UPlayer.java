package ure.actors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ure.areas.UCell;
import ure.math.UColor;
import ure.areas.UArea;
import ure.ui.Icons.Icon;
import ure.ui.ULight;
import ure.things.UThing;

import java.util.ArrayList;

/**
 * UPlayer implements a UActor whose actions are initiated by user input.  More than one can exist.
 *
 */
public class UPlayer extends UActor {

    public static final String TYPE = "player";

    public boolean awake = true;

    public String saveAreaLabel;
    public int saveAreaX;
    public int saveAreaY;
    public int saveTurn;
    public UColor selfLightColor;
    public int selfLight;
    public int selfLightFalloff;

    public ArrayList<UThing> hotbar;
    public ArrayList<Long> hotbarIDs;

    @JsonIgnore
    ULight light;

    private Log log = LogFactory.getLog(UPlayer.class);

    public UPlayer() {
        super();
    }

    public UPlayer(String thename, UColor selfLightColor, int selfLight, int selfLightFalloff) {
        super();
        initializeAsTemplate();
        this.selfLight = selfLight;
        this.selfLightFalloff = selfLightFalloff;
        this.selfLightColor = selfLightColor;
        setName(thename);
        if (selfLight > 0) {
            light = new ULight(selfLightColor, selfLightFalloff + selfLight, selfLight);
        }
        bodytype = "humanoid";
        body = commander.actorCzar.getNewBody(bodytype);
        hearingrange = config.getVolumeFalloffDistance();
        hotbar = new ArrayList<>();
    }

    @Override
    public Icon icon() {
        if (icon == null) {
            icon = iconCzar.getIconByName("player");
            icon.setEntity(this);
        }
        return icon;
    }

    @Override
    public void addActionTime(float v) {
        log.debug("adding action time to player: " + v);
        super.addActionTime(v);
    }

    public void reconnectThings() {
        for (UThing thing : contents.getThings()) {
            thing.setLocation(this);
            thing.reconnect(area(), this);
        }
        if (selfLight > 0) {
            light = new ULight(selfLightColor, selfLightFalloff + selfLight, selfLight);
        }
        hotbar = new ArrayList<>();
        for (Long id : hotbarIDs) {
            if (id == 0)
                hotbar.add(null);
            else {
                for (UThing t : things()) {
                    if (t.getID() == id) {
                        hotbar.add(t);
                    }
                }
            }
        }
    }

    @Override
    public void removeThing(UThing thing) {
        super.removeThing(thing);
        removeFromHotbar(thing);
    }

    @Override
    public void moveToCell(UArea area, int destX, int destY) {
        super.moveToCell(area,destX,destY);
        if (light != null) {
            light.moveTo(area,destX,destY);
        }
        if (area.terrainAt(destX,destY).isBreaklatch())
            commander.latchBreak();

    }

    /** Remove ourselves from the area into nowhere, to get ready for game terminate
     * Normal actors don't have to do this, but Player does because she prevents the area from freezing.
     */
    public void prepareToVanish() {
        commander.unregisterActor(this);
        location.removeThing(this);
    }

    @Override
    public boolean canSee(UThing thing) {
        int x = thing.areaX();
        int y = thing.areaY();
        if (camera.visibilityAt(x - camera.leftEdge, y - camera.topEdge) > config.getVisibilityThreshold())
            if (camera.lightAt(x-camera.leftEdge,y-camera.topEdge).grayscale() > config.getVisibilityThreshold())
            return true;
        return false;
    }

    @Override
    public void walkFail(UCell cell) {
        super.walkFail(cell);
        commander.latchBreak();
    }

    @Override
    public void closeOut() {

    }

    public void saveStateData() {
        saveAreaLabel = area().getLabel();
        saveAreaX = areaX();
        saveAreaY = areaY();
        saveTurn = commander.getTurn();
        hotbarIDs = new ArrayList<>();
        for (UThing t : hotbar) {
            if (t == null)
                hotbarIDs.add((long)0);
            else
                hotbarIDs.add(t.getID());
        }
    }

    public void setSaveLocation(UArea area, int x, int y) {
        saveAreaLabel = area.getLabel();
        saveAreaX = x;
        saveAreaY = y;
    }

    public void addToHotbar(UThing thing) {
        for (int i=0;i<10;i++) {
            if (i < hotbar.size()) {
                if (hotbar.get(i) == null) {
                    hotbar.set(i, thing);
                    return;
                }
            }
        }
        if (hotbar.size() < 10)
            hotbar.add(thing);
    }

    public void clearHotbarSlot(int slot) {
        if (hotbar.size() >= slot+1) {
            hotbar.set(slot, null);
        }
    }

    public void removeFromHotbar(UThing thing) {
        for (int i=0;i<hotbar.size();i++) {
            if (hotbar.get(i) == thing)
                hotbar.set(i, null);
        }
    }

    public boolean hasFreeHotbarSlot() {
        if (hotbar.size() < 10) return true;
        for (UThing t : hotbar)
            if (t == null) return true;
        return false;
    }

    public UThing getHotbarItem(int i) {
        if (i < hotbar.size())
            return hotbar.get(i);
        return null;
    }

    public void setSaveAreaLabel(String l) { saveAreaLabel = l; }
    public String getSaveAreaLabel() { return saveAreaLabel; }
    public void setSaveAreaX(int _x) { saveAreaX = _x; }
    public void setSaveAreaY(int _y) { saveAreaY = _y; }
    public int getSaveAreaX() { return saveAreaX; }
    public int getSaveAreaY() { return saveAreaY; }
    public int getSaveTurn() { return saveTurn; }
    public void setSaveTurn(int s) { saveTurn = s; }
    public UColor getSelfLightColor() { return selfLightColor; }
    public int getSelfLight() { return selfLight; }
    public int getSelfLightFalloff() { return selfLightFalloff; }
    public void setSelfLightColor(UColor c) { selfLightColor = c; }
    public void setSelfLight(int c) { selfLight = c; }
    public void setSelfLightFalloff(int c) { selfLightFalloff = c; }
    public ArrayList<UThing> getHotbar() { return hotbar; }
    public void setHotbar(ArrayList<UThing> al) { hotbar = al; }
}
