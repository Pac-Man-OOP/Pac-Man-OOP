package io.github.some_example_name.lingoman.entity;

import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.OutputManager;

public class WallBombEntity extends Entity {

    private final float startCenterX;
    private final float startCenterY;
    private final float targetCenterX;
    private final float targetCenterY;
    private final float bombSize;
    private final float blastSize;
    private final float throwSeconds;
    private final float blastSeconds;
    private final float throwDistance;
    private final float arcHeight;
    private final Color shadowTint = new Color(0f, 0f, 0f, 0.24f);
    private final Color trailTint = new Color(1f, 1f, 1f, 0.12f);

    private float remainingLifetime;
    private boolean exploding;
    private float throwProgress;

    public WallBombEntity(
        String id,
        float startCenterX,
        float startCenterY,
        float targetCenterX,
        float targetCenterY,
        float bombSize,
        float blastSize,
        float throwSeconds,
        float blastSeconds
    ) {
        super(id);
        this.startCenterX = startCenterX;
        this.startCenterY = startCenterY;
        this.targetCenterX = targetCenterX;
        this.targetCenterY = targetCenterY;
        this.bombSize = Math.max(8f, bombSize);
        this.blastSize = Math.max(this.bombSize + 4f, blastSize);
        this.throwSeconds = Math.max(0.08f, throwSeconds);
        this.blastSeconds = Math.max(0.10f, blastSeconds);
        float deltaX = targetCenterX - startCenterX;
        float deltaY = targetCenterY - startCenterY;
        throwDistance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        arcHeight = Math.max(
            this.bombSize * 0.75f,
            Math.min(Math.max(this.bombSize, throwDistance * 0.22f), this.blastSize * 1.10f)
        );
        remainingLifetime = this.throwSeconds;
        setWidth(0f);
        setHeight(0f);
        updateThrowPosition(0f);
    }

    @Override
    public void update(float dt) {
        if (dt <= 0f || !isActive()) {
            return;
        }

        remainingLifetime -= dt;
        if (!exploding) {
            throwProgress = 1f - Math.max(0f, remainingLifetime) / throwSeconds;
            updateThrowPosition(throwProgress);
            if (remainingLifetime <= 0f) {
                startBlast();
            }
            return;
        }

        if (remainingLifetime <= 0f) {
            expire();
        }
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isTrigger() {
        return true;
    }

    @Override
    public void render(OutputManager outputManager) {
        if (!exploding) {
            float progress = clamp01(throwProgress);
            float travelProgress = easeOut(progress);
            float centerX = lerp(startCenterX, targetCenterX, travelProgress);
            float centerY = lerp(startCenterY, targetCenterY, travelProgress);
            float settleProgress = smoothStep(phaseProgress(progress, 0.16f, 1.00f));
            float arc = flightArc(progress);
            float screenZoomScale = lerp(2.80f, 0.92f, settleProgress);
            float drawSize = bombSize * (screenZoomScale + 0.18f * arc * (1f - settleProgress));
            float pathLift = arcHeight * (0.34f + 0.48f * (1f - settleProgress) + 0.18f * arc);
            float shadowSize = bombSize * lerp(0.56f, 1.05f, settleProgress);
            shadowTint.a = lerp(0.08f, 0.30f, settleProgress);
            outputManager.drawTinted(
                LingoSprites.wallBomb(),
                centerX - shadowSize * 0.5f,
                centerY - shadowSize * 0.5f,
                shadowSize,
                shadowSize,
                shadowTint
            );
            renderTrail(outputManager, progress, settleProgress);
            float drawX = centerX - drawSize * 0.5f;
            float drawY = centerY - drawSize * 0.5f + pathLift;
            float spin = 160f + travelProgress * (420f + throwDistance * 0.7f);
            outputManager.drawRotated(LingoSprites.wallBomb(), drawX, drawY, drawSize, drawSize, spin);
            return;
        }

        float elapsed = blastSeconds - Math.max(0f, remainingLifetime);
        float progress = Math.min(1f, elapsed / blastSeconds);
        float scale = 1.08f + 0.20f * progress;
        float drawWidth = blastSize * scale;
        float drawHeight = blastSize * scale;
        float drawX = targetCenterX - drawWidth * 0.5f;
        float drawY = targetCenterY - drawHeight * 0.5f;
        outputManager.draw(LingoSprites.wallBlast(), drawX, drawY, drawWidth, drawHeight);
    }

    public void expire() {
        setActive(false);
    }

    public boolean isExploding() {
        return exploding && isActive();
    }

    private void updateThrowPosition(float progress) {
        float clamped = clamp01(progress);
        float eased = easeOut(clamped);
        float centerX = lerp(startCenterX, targetCenterX, eased);
        float centerY = lerp(startCenterY, targetCenterY, eased);
        setX(centerX - bombSize * 0.5f);
        setY(centerY - bombSize * 0.5f);
    }

    private void startBlast() {
        exploding = true;
        remainingLifetime = blastSeconds;
        setX(targetCenterX - blastSize * 0.5f);
        setY(targetCenterY - blastSize * 0.5f);
        setWidth(blastSize);
        setHeight(blastSize);
    }

    private void renderTrail(OutputManager outputManager, float progress, float settleProgress) {
        for (int i = 3; i >= 1; i--) {
            float trailProgress = progress - i * 0.10f;
            if (trailProgress <= 0f) {
                continue;
            }

            float travel = easeOut(trailProgress);
            float centerX = lerp(startCenterX, targetCenterX, travel);
            float centerY = lerp(startCenterY, targetCenterY, travel);
            float trailSettle = smoothStep(phaseProgress(trailProgress, 0.16f, 1.00f));
            float arc = flightArc(trailProgress);
            float trailSize = bombSize * lerp(1.60f, 0.42f, trailSettle);
            trailTint.a = (0.05f + 0.04f * i) * (1f - progress * 0.28f) * (0.65f + 0.35f * (1f - settleProgress));
            outputManager.drawTinted(
                LingoSprites.wallBomb(),
                centerX - trailSize * 0.5f,
                centerY - trailSize * 0.5f + arcHeight * (0.30f + 0.36f * (1f - trailSettle) + 0.12f * arc),
                trailSize,
                trailSize,
                trailTint
            );
        }
    }

    private static float lerp(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    private static float flightArc(float progress) {
        float clamped = clamp01(progress);
        return 4f * clamped * (1f - clamped);
    }

    private static float easeOut(float progress) {
        float clamped = clamp01(progress);
        float inverse = 1f - clamped;
        return 1f - inverse * inverse * inverse;
    }

    private static float phaseProgress(float progress, float start, float end) {
        if (end <= start) {
            return progress >= end ? 1f : 0f;
        }
        return clamp01((progress - start) / (end - start));
    }

    private static float smoothStep(float progress) {
        float clamped = clamp01(progress);
        return clamped * clamped * (3f - 2f * clamped);
    }

    private static float clamp01(float progress) {
        return Math.max(0f, Math.min(1f, progress));
    }
}
