package io.github.some_example_name.lingoman.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.OutputManager;

public class BossFireballEntity extends Entity {

    private static final Color FIREBALL_GLOW = new Color(1.00f, 0.42f, 0.12f, 0.34f);
    private static final Color FIREBALL_TRAIL = new Color(1.00f, 0.78f, 0.40f, 0.18f);

    private final float size;
    private float remainingLifetime;
    private float rotationDegrees;

    public BossFireballEntity(
        String id,
        float startCenterX,
        float startCenterY,
        float targetCenterX,
        float targetCenterY,
        float size,
        float speed,
        float lifetimeSeconds
    ) {
        super(id);
        this.size = Math.max(10f, size);
        remainingLifetime = Math.max(0.20f, lifetimeSeconds);

        Vector2 direction = new Vector2(targetCenterX - startCenterX, targetCenterY - startCenterY);
        if (direction.isZero(0.001f)) {
            direction.set(1f, 0f);
        } else {
            direction.nor();
        }

        setWidth(this.size);
        setHeight(this.size);
        setX(startCenterX - this.size * 0.5f);
        setY(startCenterY - this.size * 0.5f);
        setVx(direction.x * Math.max(1f, speed));
        setVy(direction.y * Math.max(1f, speed));
        rotationDegrees = direction.angleDeg();
    }

    @Override
    public void update(float dt) {
        if (dt <= 0f || !isActive()) {
            return;
        }

        remainingLifetime -= dt;
        if (remainingLifetime <= 0f) {
            expire();
            return;
        }

        setX(getX() + getVx() * dt);
        setY(getY() + getVy() * dt);
        rotationDegrees += 540f * dt;
    }

    @Override
    public boolean isTrigger() {
        return true;
    }

    @Override
    public void render(OutputManager outputManager) {
        float glowPadding = size * 0.58f;
        float trailSize = size * 0.88f;
        float centerX = getX() + getWidth() * 0.5f;
        float centerY = getY() + getHeight() * 0.5f;

        outputManager.drawTinted(
            LingoSprites.fireball(),
            getX() - glowPadding * 0.5f,
            getY() - glowPadding * 0.5f,
            getWidth() + glowPadding,
            getHeight() + glowPadding,
            FIREBALL_GLOW
        );
        outputManager.drawTinted(
            LingoSprites.fireball(),
            centerX - trailSize * 0.5f - getVx() * 0.04f,
            centerY - trailSize * 0.5f - getVy() * 0.04f,
            trailSize,
            trailSize,
            FIREBALL_TRAIL
        );
        outputManager.drawRotated(LingoSprites.fireball(), getX(), getY(), getWidth(), getHeight(), rotationDegrees);
    }

    public void expire() {
        setActive(false);
    }
}
