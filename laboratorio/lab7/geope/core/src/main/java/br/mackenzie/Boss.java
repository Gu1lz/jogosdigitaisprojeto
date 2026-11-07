package br.mackenzie;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import java.util.Random;

public final class Boss {

    public static final int ST_HIDDEN = 0;
    public static final int ST_NORMAL = 1;
    public static final int ST_ATTACK = 2;
    public static final int ST_SCREAM = 3;
    public static final int ST_HIT    = 4;
    public static final int ST_DEATH  = 5;

    private static final float HIT_TIME    = 0.45f;
    private static final float ATTACK_TIME = 1.2f;
    private static final float SCREAM_TIME = 1.0f;
    private static final float DEATH_TIME  = 2.0f;

    private static final float BOSS_Y      = Theme.WORLD_H - 5.5f;
    private static final float BOSS_SIZE   = 4f;

    private static final int[] APPEAR_AT = { 6767 };

    private static final int CYCLE      = 200;
    private static final int HIT_OFFSET = 67;

    private final Random rnd = new Random();

    private int state;
    private float stateTimer;
    private float time;
    private int currentBossIndex = -1;
    private int lastHitScore;
    private int nextRandomAt;
    private boolean active;
    private boolean deathTriggered;

    private float pos, center, lastPos, prevH;
    private boolean first, doubleNext, finished;

    public Boss() {
        reset();
    }

    public void reset() {
        state = ST_HIDDEN;
        stateTimer = 0f;
        time = 0f;
        currentBossIndex = -1;
        lastHitScore = 0;
        nextRandomAt = 0;
        active = false;
        deathTriggered = false;
        finished = false;
        first = true;
        doubleNext = false;
        pos = 0f;
        lastPos = 0f;
        center = Theme.WORLD_W / 2f;
        prevH = 0f;
    }

    public void update(float delta, int score, float scroll) {
        time += delta;
        stateTimer += delta;

        int targetBoss = bossIndexForScore(score);

        if (targetBoss < 0) {
            if (state != ST_HIDDEN) state = ST_HIDDEN;
            active = false;
            return;
        }

        if (currentBossIndex != targetBoss) {
            currentBossIndex = targetBoss;
            lastHitScore = 0;
            deathTriggered = false;
            finished = false;
            active = true;
            beginPattern(scroll);
            setState(ST_ATTACK);
            nextRandomAt = score + rnd.nextInt(30, 60);
        }

        if (!deathTriggered && score >= nextBossAppear(targetBoss)) {
            deathTriggered = true;
            active = false;
            finished = true;
            setState(ST_DEATH);
            return;
        }

        if (deathTriggered) {
            if (state == ST_DEATH && stateTimer >= DEATH_TIME) {
                state = ST_HIDDEN;
            }
            return;
        }

        if (active && isHitScore(score) && score > lastHitScore) {
            lastHitScore = score;
            setState(ST_HIT);
        }

        if (active && state == ST_NORMAL && score >= nextRandomAt) {
            if (rnd.nextBoolean()) setState(ST_ATTACK);
            else setState(ST_SCREAM);
            nextRandomAt = score + rnd.nextInt(30, 60);
        }

        if (state == ST_ATTACK) {
            if (stateTimer >= ATTACK_TIME) setState(ST_NORMAL);
        } else if (state == ST_HIT) {
            if (stateTimer >= HIT_TIME) setState(ST_NORMAL);
        } else if (state == ST_SCREAM) {
            if (stateTimer >= SCREAM_TIME) setState(ST_NORMAL);
        }
    }

    private int bossIndexForScore(int score) {
        int idx = -1;
        for (int i = 0; i < APPEAR_AT.length; i++) {
            if (score >= APPEAR_AT[i]) idx = i;
        }
        return idx;
    }

    private int nextBossAppear(int idx) {
        if (idx + 1 < APPEAR_AT.length) return APPEAR_AT[idx + 1];
        return APPEAR_AT[idx] + 600;
    }

    private boolean isHitScore(int score) {
        int base = APPEAR_AT[currentBossIndex];
        return score >= base && (score - base) % CYCLE == HIT_OFFSET;
    }

    private void setState(int s) {
        state = s;
        stateTimer = 0f;
    }

    public void draw(SpriteBatch batch, Assets assets, float worldW) {
        if (state == ST_HIDDEN) return;
        if (currentBossIndex < 0) return;
        if (currentBossIndex >= assets.bosses.size) return;
        Assets.BossSprites sp = assets.bosses.get(currentBossIndex);
        TextureRegion frame = regionFor(sp);
        if (frame == null) return;
        float cx = worldW / 2f;
        batch.draw(frame, cx - BOSS_SIZE / 2f, BOSS_Y - BOSS_SIZE / 2f, BOSS_SIZE, BOSS_SIZE);
    }

    private TextureRegion regionFor(Assets.BossSprites sp) {
        if (state == ST_NORMAL) return sp.normal;
        if (state == ST_ATTACK) return sp.attack;
        if (state == ST_SCREAM) return sp.scream;
        if (state == ST_HIT)    return sp.hit;
        if (state == ST_DEATH)  return sp.death;
        return null;
    }

    public boolean controlsSpawn() {
        if (!active) return false;
        if (state == ST_HIDDEN) return false;
        if (state == ST_DEATH) return false;
        return true;
    }

    public boolean isVisible() {
        if (state == ST_HIDDEN) return false;
        return true;
    }

    public int state() {
        return state;
    }

    public int currentBossIndex() {
        return currentBossIndex;
    }

    public String currentBossName(Assets assets) {
        if (currentBossIndex < 0) return "";
        if (currentBossIndex >= assets.bosses.size) return "";
        return assets.bosses.get(currentBossIndex).name;
    }

    public float speedBonus() {
        if (active) return 1.5f;
        return 0f;
    }

    private void beginPattern(float scroll) {
        pos = scroll + 12f;
        lastPos = pos;
        center = Theme.WORLD_W / 2f;
        prevH = 0f;
        first = true;
        doubleNext = false;
        finished = false;
    }

    public void generateUntil(float limit, Array<Obstacle> out) {
        while (pos < limit && !finished) addRow(out);
    }

    private void addRow(Array<Obstacle> out) {
        float W = Theme.WORLD_W;
        float bound = CityScenery.BOUND;
        float hitR = 0.34f;

        float gap = MathUtils.random(2.0f, 2.8f);
        float rowH;
        if (rnd.nextFloat() < 0.65f) rowH = 0.9f;
        else rowH = 0.8f;

        boolean spike;
        if (rowH > 0.85f) spike = true;
        else spike = false;

        float lo = bound + gap / 2f + 0.05f;
        float hi = W - bound - gap / 2f - 0.05f;

        if (first) {
            center = W / 2f + (rnd.nextFloat() - 0.5f) * 1.5f;
        } else {
            float freeTime = Math.max(0.1f, (pos - lastPos - prevH) / 11f);
            float maxShift = Math.max(0.5f, 10f * freeTime - (gap - 2f * hitR));
            center += (rnd.nextFloat() * 2f - 1f) * maxShift;
        }
        center = MathUtils.clamp(center, lo, hi);

        float gapL = center - gap / 2f;
        float gapR = center + gap / 2f;
        if (gapL - bound > 0.05f) {
            out.add(new Obstacle(bound, pos, gapL - bound, rowH, spike));
        }
        if ((W - bound) - gapR > 0.05f) {
            out.add(new Obstacle(gapR, pos, (W - bound) - gapR, rowH, spike));
        }

        lastPos = pos;
        prevH = rowH;
        first = false;

        if (doubleNext) {
            pos += rowH + 0.6f;
            doubleNext = false;
        } else {
            pos += MathUtils.random(4.5f, 5.5f);
            if (rnd.nextFloat() < 0.25f) doubleNext = true;
            else doubleNext = false;
        }
    }
}