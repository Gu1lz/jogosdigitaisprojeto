package br.mackenzie;

public final class Obstacle {
    public final float x, pos, w, h;
    public final boolean spike;

    public Obstacle(float x, float pos, float w, float h, boolean spike) {
        this.x = x;
        this.pos = pos;
        this.w = w;
        this.h = h;
        this.spike = spike;
    }
}