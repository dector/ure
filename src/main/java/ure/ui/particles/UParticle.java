package ure.ui.particles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ure.areas.UArea;
import ure.di.Injector;
import ure.math.UColor;
import ure.math.URandom;
import ure.render.URenderer;
import ure.sys.UAnimator;
import ure.sys.UConfig;
import ure.ui.UCamera;

import javax.inject.Inject;

public class UParticle implements UAnimator {

     int ticksLeft;
     int ticksInitial;

    @JsonIgnore
    UArea area;

    @Inject
    @JsonIgnore
    protected URenderer renderer;
    @Inject
    @JsonIgnore
    protected UConfig config;
    @Inject
    @JsonIgnore
    protected URandom random;

    public int x, y;
    int glyph;
    float fgR,fgG,fgB,bgR,bgG,bgB;
    boolean receiveLight;
    float alpha;
    float alphadecay;
    UColor colorbuffer;
    String glyphFrames;

    float offx, offy;
    float vecx, vecy;
    float vecdecay;
    float gravx, gravy;
    float gravvecx, gravvecy;

    public UParticle(int x, int y, int ticksLeft, UColor fgColor, float alpha, boolean receiveLight, float vecx, float vecy, float vecdecay, float gravx, float gravy) {
        Injector.getAppComponent().inject(this);
        this.x = x;
        this.y = y;
        this.ticksLeft = ticksLeft;
        ticksInitial = ticksLeft;
        glyph = '*';
        this.receiveLight = receiveLight;
        fgR = fgColor.fR();
        fgG = fgColor.fG();
        fgB = fgColor.fB();
        this.alpha = alpha;
        alphadecay = alpha / ticksLeft;
        colorbuffer = new UColor(fgR,fgG,fgB);
        this.vecx = vecx;
        this.vecy = vecy;
        this.vecdecay = vecdecay;
        this.gravx = gravx;
        this.gravy = gravy;
    }

    public int frame() { return ticksInitial-ticksLeft; }

    public void reconnect(UArea area) {
        this.area = area;
    }

    public void animationTick() {
        ticksLeft--;
        alpha -= alphadecay;
        offx += vecx;
        offy += vecy;
        vecx *= 1f-vecdecay;
        vecy *= 1f-vecdecay;
        gravvecx += gravx;
        gravvecy += gravy;
    }

    public boolean isFizzled() { return (ticksLeft < 0); }

    public int glyph() {
        if (glyphFrames != null) {
            return (int)glyphFrames.charAt(ticksInitial - ticksLeft);
        }
        return glyph;
    }
    public boolean isReceiveLight() { return receiveLight; }

    public void draw(UCamera camera, UColor light, float vis) {
        if (receiveLight)
            colorbuffer.illuminateWith(light, vis);
        else
            colorbuffer.set(fgR,fgG,fgB);
        colorbuffer.setAlpha(alpha * vis);
        renderer.drawGlyph(glyph(), (x - camera.leftEdge)*config.getTileWidth() + glyphX(), (y - camera.topEdge)*config.getTileHeight() + glyphY(), colorbuffer);
    }

    public int glyphX() { return (int)(offx + gravvecx); }
    public int glyphY() { return (int)(offy + gravvecy); }
}
