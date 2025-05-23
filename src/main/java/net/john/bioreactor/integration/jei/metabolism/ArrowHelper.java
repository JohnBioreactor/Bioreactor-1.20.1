package net.john.bioreactor.integration.jei.metabolism;

import net.john.bioreactor.Bioreactor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

import java.util.HashMap;
import java.util.Map;

public class ArrowHelper {
    private static final int TEX_W = 34;  // largeur réelle du PNG arrows.png
    private static final int TEX_H = 39;  // hauteur réelle de arrows.png
    private static final ResourceLocation ATLAS =
            new ResourceLocation(Bioreactor.MOD_ID,
                    "textures/gui/jei/arrows.png");

    /** UV absolues de chaque couleur (shaft + tip). */
    private static record Params(int sU,int sV,int sW,int sH,
                                 int tU,int tV,int tW,int tH) {}
    private static final Map<String,Params> COLORS = new HashMap<>();
    static {
        COLORS.put("blue",   new Params( 2, 0, 3,32,   0,32,7,7));
        COLORS.put("red",    new Params(11, 0, 3,32,   9,32,7,7));
        COLORS.put("black",  new Params(20, 0, 3,32,  18,32,7,7));
        COLORS.put("yellow", new Params(29, 0, 3,32,  27,32,7,7));
    }

    /**
     * Types : "straight","no_tip","curved"
     * Couleurs : "blue","red","black","yellow"
     */
    public static void draw(GuiGraphics g,
                            String type,
                            String color,
                            int x1,int y1,
                            int x2,int y2) {
        Params p = COLORS.get(color);
        if (p == null) return;

        switch (type) {
            case "straight" -> drawStraight(g,p,x1,y1,x2,y2);
            case "no_tip"   -> drawNoTip(   g,p,x1,y1,x2,y2);
            case "curved"   -> drawCurved(  g,p,x1,y1,x2,y2);
            default         -> {}
        }
    }

    /**
     * round_trip : deux horizontales (x1→x3 @ y1, x2→x3 @ y2), puis vertical @ x3,
     * enfin pointe en (x2,y2) vers la gauche si x2<x3, sinon vers la droite.
     */
    public static void draw(GuiGraphics g,
                            String type,
                            String color,
                            int x1, int y1,
                            int x2, int y2,
                            int x3) {
        if (!"round_trip".equals(type)) return;
        Params p = COLORS.get(color);
        if (p == null) return;

        // 1) Ligne H1 : de (x1,y1) → (x3,y1)
        drawTiltedShaft(g, p, x1, y1, x3, y1);

        // 2) Ligne H2 : de (x2,y2) → (x3,y2)
        drawTiltedShaft(g, p, x2, y2, x3, y2);

        // 3) Segment vertical (x3, y1-2) → (x3, y2) pour assurer la continuité sans trou
        drawVerticalShaft(g, p, x3, y1 - 2, x3, y2);

        // 4) Pointe en (x2,y2), orientée LEFT si x2<x3 sinon RIGHT
        float angle = (x2 < x3) ?  90f   // 0° = bas, +90° = gauche
                : -90f;  // -90° = droite
        drawTip(g, p, x2, y2 - 1, angle);
    }

    // ─── STRAIGHT ────────────────────────────────────────────────────────────────

    private static void drawStraight(GuiGraphics g, Params p,
                                     int x1,int y1,int x2,int y2) {
        if (y1==y2 && x1!=x2) {
            // horizontal
            drawTiltedShaft(g,p, x1,y1, x2,y2);
            float angle = (x2>x1) ? -90f : 90f;  // droite=–90, gauche=+90
            drawTip(g,p, x2,y2-1, angle);  // "-1" car pointe de flèche 1 pixel plus haut

        } else if (x1==x2 && y1!=y2) {
            // vertical
            drawVerticalShaft(g,p, x1,y1, x2,y2);
            float angle = (y2>y1) ?   0f : 180f; // bas=0°, haut=180°
            drawTip(g,p, x2,y2, angle);

        } else {
            // fallback
            drawCurved(g,p,x1,y1,x2,y2);
        }
    }

    // ─── CURVED (L‑Shape) ──────────────────────────────────────────────────────

    private static void drawCurved(GuiGraphics g, Params p,
                                   int x1,int y1,int x2,int y2) {
        // d’abord horizontal
        drawTiltedShaft(g,p, x1,y1, x2,y1);
        // puis vertical
        drawVerticalShaft(g,p, x2,y1-2, x2,y2); // "-2" pour faire commencer le segment vertical 2 pixel plus haut = continuité
        // pointe vers bas (angle=0)
        drawTip(g,p, x2,y2, 0f);
    }

    // ─── NO_TIP (droite only sans pointe) ─────────────────────────────────────

    private static void drawNoTip(GuiGraphics g, Params p,
                                  int x1,int y1,int x2,int y2) {
        if (y1==y2) {
            drawTiltedShaft(g,p, x1,y1, x2,y2);
        } else if (x1==x2) {
            drawVerticalShaft(g,p, x1,y1, x2,y2);
        }
    }

    // ─── SHAFT TILING ──────────────────────────────────────────────────────────

    /** Tuilage vertical (pas “tilted”). */
    private static void drawVerticalShaft(GuiGraphics g, Params p,
                                          int x, int y1,int x2,int y2) {
        int minY = Math.min(y1,y2), total = Math.abs(y2-y1), drawn=0;
        while(drawn<total) {
            int h = Math.min(p.sH, total-drawn);
            g.blit(ATLAS,
                    x - p.sW/2, minY+drawn,
                    p.sU,p.sV, p.sW,h,
                    TEX_W,TEX_H);
            drawn += h;
        }
    }

    /** Tuilage horizontal via rotation de -90°, centré fragment par fragment. */
    private static void drawTiltedShaft(GuiGraphics g, Params p,
                                        int x1, int y1,
                                        int x2, int y2) {
        int minX  = Math.min(x1, x2);
        int total = Math.abs(x2 - x1);
        int drawn = 0;
        while (drawn < total) {
            // longueur de ce fragment
            int w = Math.min(p.sH, total - drawn);

            // calcul du centre horizontal du fragment
            int cx = minX + drawn + w/2;
            int cy = y1;

            g.pose().pushPose();
            // 1) translation au centre du fragment
            g.pose().translate(cx, cy, 0);
            // 2) rotation -90° pour passer de vertical à horizontal
            g.pose().mulPose(new Quaternionf().rotateZ((float)Math.toRadians(-90)));
            // 3) dessin de la tuile centrée (épaisseur sW × longueur w)
            g.blit(
                    ATLAS,
                    -p.sW/2,  // offset X après rotation = -half épaisseur
                    -w/2,     // offset Y après rotation = -half longueur
                    p.sU, p.sV,
                    p.sW, w,  // width=shaftW, height=fragmentLength
                    TEX_W, TEX_H
            );
            g.pose().popPose();

            drawn += w;
        }
    }

    // ─── POINTE ────────────────────────────────────────────────────────────────

    /** Dessine la pointe, centrée, avec l’angle donné (deg). */
    private static void drawTip(GuiGraphics g, Params p,
                                int x, int y,
                                float angleDeg) {
        g.pose().pushPose();
        // translation AU CENTRE
        g.pose().translate(x, y, 0);
        // rotation (sprite de base vers le bas)
        g.pose().mulPose(new Quaternionf()
                .rotateZ((float)Math.toRadians(angleDeg)));
        // draw centré
        g.blit(ATLAS,
                -p.tW/2, -p.tH/2,
                p.tU,p.tV, p.tW,p.tH,
                TEX_W,TEX_H);
        g.pose().popPose();
    }
}
