package io.github.some_example_name.lingoman.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.entity.NPCEntity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.OutputManager;

public class GhostEntity extends NPCEntity {

    private static final Color FROZEN_TINT = new Color(0.72f, 0.90f, 1.00f, 0.22f);
    private static final float RESPAWN_BLINK_INTERVAL = 0.14f;

    public enum GhostType {
        NORMAL,
        BOSS,
        WALL_BOMBER
    }

    private final Color color;
    private final GhostType type;
    private float frozenTimer;
    private float throwAnimationTimer;
    private float throwAnimationDuration;
    private float throwAimX = 0f;
    private float throwAimY = 1f;
    private float respawnProtectionTimer;

    public GhostEntity(String id, Color color, float x, float y, float size) {
        this(id, GhostType.NORMAL, color, x, y, size);
    }

    public GhostEntity(String id, GhostType type, Color color, float x, float y, float size) {
        super(id);
        this.type = type == null ? GhostType.NORMAL : type;
        this.color = color == null ? new Color(1f, 0.3f, 0.3f, 1f) : color;
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size);
    }

    @Override
    public void update(float dt) {
        if (dt <= 0f) {
            return;
        }

        if (throwAnimationTimer > 0f) {
            throwAnimationTimer = Math.max(0f, throwAnimationTimer - dt);
            if (throwAnimationTimer == 0f) {
                throwAnimationDuration = 0f;
            }
        }
        if (respawnProtectionTimer > 0f) {
            respawnProtectionTimer = Math.max(0f, respawnProtectionTimer - dt);
        }

        if (frozenTimer > 0f) {
            frozenTimer = Math.max(0f, frozenTimer - dt);
            setVx(0f);
            setVy(0f);
            return;
        }

        if (getMovementBehaviour() == null) {
            return;
        }

        getMovementBehaviour().move(this, dt);
        setX(getX() + getVx() * dt);
        setY(getY() + getVy() * dt);
    }

    @Override
    public void render(OutputManager outputManager) {
        if (isBlinkHidden(respawnProtectionTimer)) {
            return;
        }

        float throwProgress = throwAnimationProgress();
        float windup = smoothStep(phaseProgress(throwProgress, 0.00f, 0.34f));
        float release = smoothStep(phaseProgress(throwProgress, 0.16f, 0.56f));
        float recover = smoothStep(phaseProgress(throwProgress, 0.58f, 1.00f));
        float scale = switch (type) {
            case BOSS -> 1.38f;
            case WALL_BOMBER -> 1.20f;
            default -> 1.24f;
        };
        if (type == GhostType.WALL_BOMBER && throwAnimationTimer > 0f) {
            scale += 0.06f * windup * (1f - recover);
        }
        float drawWidth = getWidth() * scale;
        float drawHeight = getHeight() * scale;
        float drawX = getX() - (drawWidth - getWidth()) * 0.5f;
        float drawY = getY() - (drawHeight - getHeight()) * 0.5f;
        if (type == GhostType.WALL_BOMBER && throwAnimationTimer > 0f) {
            drawX += -throwAimX * getWidth() * 0.07f * windup
                + throwAimX * getWidth() * 0.04f * release
                - throwAimX * getWidth() * 0.02f * recover;
            drawY += -throwAimY * getHeight() * 0.05f * windup
                + throwAimY * getHeight() * 0.03f * release
                - throwAimY * getHeight() * 0.01f * recover
                + (float) Math.sin(throwProgress * Math.PI) * getHeight() * 0.03f;
        }
        switch (type) {
            case BOSS -> outputManager.draw(LingoSprites.bossGhost(color), drawX, drawY, drawWidth, drawHeight);
            case WALL_BOMBER -> {
                outputManager.draw(LingoSprites.wallBomberGhost(color), drawX, drawY, drawWidth, drawHeight);
                if (throwAnimationTimer > 0f) {
                    float bombVisibility = 1f - smoothStep(phaseProgress(throwProgress, 0.18f, 0.34f));
                    if (bombVisibility > 0f) {
                        float throwReach = -0.08f * windup + 0.22f * release - 0.06f * recover;
                        float sideOffset = 0.10f;
                        float perpX = -throwAimY;
                        float perpY = throwAimX;
                        float bombSize = Math.max(8f, getWidth() * (0.34f + 0.05f * windup)) * bombVisibility;
                        float bombCenterX = drawX + drawWidth * 0.5f
                            + throwAimX * drawWidth * (0.06f + throwReach)
                            + perpX * drawWidth * sideOffset;
                        float bombCenterY = drawY + drawHeight * 0.52f
                            + throwAimY * drawHeight * (0.04f + throwReach * 0.7f)
                            + perpY * drawHeight * (sideOffset * 0.85f);
                        float bombX = bombCenterX - bombSize * 0.5f;
                        float bombY = bombCenterY - bombSize * 0.5f;
                        outputManager.drawRotated(LingoSprites.wallBomb(), bombX, bombY, bombSize, bombSize, -18f + 44f * release);
                    }
                }
            }
            default -> outputManager.draw(LingoSprites.ghost(color), drawX, drawY, drawWidth, drawHeight);
        }
        if (isFrozen()) {
            outputManager.drawRect(drawX, drawY, drawWidth, drawHeight, FROZEN_TINT);
        }
    }

    @Override
    public void onCollision(Entity other) {
        if (other instanceof WallEntity) {
            setVx(0f);
            setVy(0f);
        }
    }

    public GhostType getType() {
        return type;
    }

    public boolean isBoss() {
        return type == GhostType.BOSS;
    }

    public Color getColor() {
        return new Color(color);
    }

    public void freezeFor(float seconds) {
        frozenTimer = Math.max(frozenTimer, Math.max(0f, seconds));
        setVx(0f);
        setVy(0f);
    }

    public void clearFreeze() {
        frozenTimer = 0f;
    }

    public boolean isFrozen() {
        return frozenTimer > 0f;
    }

    public void setRespawnProtection(float seconds) {
        respawnProtectionTimer = Math.max(respawnProtectionTimer, Math.max(0f, seconds));
    }

    public void clearRespawnProtection() {
        respawnProtectionTimer = 0f;
    }

    public boolean isRespawnProtected() {
        return respawnProtectionTimer > 0f;
    }

    public void playThrowAnimation(float seconds, float targetCenterX, float targetCenterY) {
        if (type != GhostType.WALL_BOMBER) {
            return;
        }
        float duration = Math.max(0.12f, seconds);
        throwAnimationDuration = duration;
        throwAnimationTimer = duration;
        Vector2 direction = resolveThrowDirection(targetCenterX, targetCenterY);
        throwAimX = direction.x;
        throwAimY = direction.y;
    }

    public Vector2 getThrowOrigin(float targetCenterX, float targetCenterY) {
        Vector2 direction = resolveThrowDirection(targetCenterX, targetCenterY);
        float perpX = -direction.y;
        float perpY = direction.x;
        float centerX = getX() + getWidth() * 0.5f;
        float centerY = getY() + getHeight() * 0.54f;
        return new Vector2(
            centerX + direction.x * getWidth() * 0.32f + perpX * getWidth() * 0.12f,
            centerY + direction.y * getHeight() * 0.18f + perpY * getHeight() * 0.10f
        );
    }

    public void clearThrowAnimation() {
        throwAnimationTimer = 0f;
        throwAnimationDuration = 0f;
    }

    private float throwAnimationProgress() {
        if (throwAnimationTimer <= 0f || throwAnimationDuration <= 0f) {
            return 0f;
        }
        return 1f - throwAnimationTimer / throwAnimationDuration;
    }

    private Vector2 resolveThrowDirection(float targetCenterX, float targetCenterY) {
        Vector2 direction = new Vector2(
            targetCenterX - (getX() + getWidth() * 0.5f),
            targetCenterY - (getY() + getHeight() * 0.5f)
        );
        if (direction.isZero(0.001f)) {
            direction.set(0f, 1f);
        } else {
            direction.nor();
        }
        return direction;
    }

    private static float phaseProgress(float progress, float start, float end) {
        if (end <= start) {
            return progress >= end ? 1f : 0f;
        }
        return clamp01((progress - start) / (end - start));
    }

    private static float smoothStep(float value) {
        float clamped = clamp01(value);
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static boolean isBlinkHidden(float timer) {
        if (timer <= 0f) {
            return false;
        }
        return ((int) (timer / RESPAWN_BLINK_INTERVAL)) % 2 != 0;
    }
}
