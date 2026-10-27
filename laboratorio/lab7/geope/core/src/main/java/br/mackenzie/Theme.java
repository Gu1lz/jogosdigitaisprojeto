package br.mackenzie;

import com.badlogic.gdx.graphics.Color;

public final class Theme {

    private Theme() {}

    public static final float WORLD_W = 9f;
    public static final float WORLD_H = 16f;

    /** Nome unico do jogo (menu, preferencias). Mude so aqui. */
    public static final String GAME_TITLE = "HEAD RUSH";
    public static final String PREFS_NAME = "headrush";

    public static final Color VOID          = new Color(0.02f, 0.01f, 0.06f, 1f);
    public static final Color SKY_TOP       = new Color(0.05f, 0.02f, 0.14f, 1f);
    public static final Color SKY_BOTTOM    = new Color(0.22f, 0.04f, 0.28f, 1f);

    public static final Color CYAN          = new Color(0.10f, 0.95f, 1.00f, 1f);
    public static final Color MAGENTA       = new Color(1.00f, 0.15f, 0.70f, 1f);
    public static final Color YELLOW        = new Color(1.00f, 0.90f, 0.20f, 1f);
    public static final Color PURPLE        = new Color(0.55f, 0.25f, 0.95f, 1f);
    public static final Color RED           = new Color(1.00f, 0.25f, 0.30f, 1f);
    public static final Color WHITE         = new Color(1f, 1f, 1f, 1f);

    public static final Color BUILDING_FAR  = new Color(0.07f, 0.04f, 0.16f, 1f);
    public static final Color BUILDING_NEAR = new Color(0.04f, 0.02f, 0.10f, 1f);
    public static final Color BLOCK_BODY    = new Color(0.10f, 0.03f, 0.16f, 1f);
}