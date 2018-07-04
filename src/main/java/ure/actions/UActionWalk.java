package ure.actions;

import ure.actors.UREActor;

public class UActionWalk extends UAction {

    public static String id = "WALK";

    int xdir, ydir;

    public UActionWalk(int xd, int yd) {
        xdir = xd;
        ydir = yd;
    }

    public float doneBy(UREActor actor) {
        actor.walkDir(xdir, ydir);
        float time = super.doneBy(actor);
        time = time * (1f / actor.myCell().terrain().moveSpeed(actor));
        return time;
    }
}
