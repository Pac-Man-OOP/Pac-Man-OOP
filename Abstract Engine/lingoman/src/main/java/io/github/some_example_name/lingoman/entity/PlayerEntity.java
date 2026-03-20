package io.github.some_example_name.lingoman.entity;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.entity.DynamicEntity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.InputManager;
import io.github.some_example_name.managers.OutputManager;

public class PlayerEntity extends DynamicEntity {
    private static final float DASH_DURATION_SECONDS = 0.19f;
    private static final float DASH_SPEED_MULTIPLIER = 2.95f;
    private static final float RESPAWN_BLINK_INTERVAL = 0.14f;
    private static final Color PLAYER_SHADOW = new Color(0f, 0f, 0f, 0.42f);
    private static final Color PLAYER_GLOW = new Color(0.30f, 0.82f, 1.00f, 0.30f);
    private static final Color PLAYER_EDGE = new Color(1.00f, 0.92f, 0.58f, 0.22f);
    private static final Color PLAYER_DASH_GLOW = new Color(1.00f, 0.95f, 0.68f, 0.38f);
    private static final Color PLAYER_SHOCK_GLOW = new Color(0.34f, 0.90f, 1.00f, 0.42f);
    private static final Color PLAYER_SHOCK_BODY = new Color(0.52f, 0.95f, 1.00f, 0.34f);
    private static final Color PLAYER_SHOCK_ARC = new Color(0.88f, 0.99f, 1.00f, 0.86f);
    private static final Color PLAYER_SHOCK_HALO = new Color(0.21f, 0.78f, 1.00f, 0.34f);

    private final InputManager input;
    private final float moveSpeed;
    private float dashTimer;
    private float dashDirectionX;
    private float dashDirectionY;
    private float lastMoveDirectionX;
    private float lastMoveDirectionY;
    private float respawnProtectionTimer;
    private float shockTimer;

    public PlayerEntity(String id, InputManager input, float x, float y, float moveSpeed, float size) {
        super(id);
        this.input = input;
        this.moveSpeed = moveSpeed;
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size);
    }

    @Override
    public void update(float dt) {
        float safeDt = Math.max(0f, dt);
        dashTimer = Math.max(0f, dashTimer - safeDt);
        respawnProtectionTimer = Math.max(0f, respawnProtectionTimer - safeDt);
        shockTimer = Math.max(0f, shockTimer - safeDt);

        float vx = 0f;
        float vy = 0f;

        if (input != null) {
            if (input.isActionPressed(LingoInputActions.MOVE_LEFT)) {
                vx -= moveSpeed;
            }
            if (input.isActionPressed(LingoInputActions.MOVE_RIGHT)) {
                vx += moveSpeed;
            }
            if (input.isActionPressed(LingoInputActions.MOVE_UP)) {
                vy += moveSpeed;
            }
            if (input.isActionPressed(LingoInputActions.MOVE_DOWN)) {
                vy -= moveSpeed;
            }

            if (input.isActionJustPressed(LingoInputActions.DASH) && dashTimer <= 0f) {
                float dashX = vx;
                float dashY = vy;
                if (isNearZero(dashX, dashY)) {
                    dashX = lastMoveDirectionX;
                    dashY = lastMoveDirectionY;
                }
                if (!isNearZero(dashX, dashY)) {
                    float invLength = inverseLength(dashX, dashY);
                    dashDirectionX = dashX * invLength;
                    dashDirectionY = dashY * invLength;
                    dashTimer = DASH_DURATION_SECONDS;
                }
            }
        }

        if (!isNearZero(vx, vy)) {
            float invLength = inverseLength(vx, vy);
            lastMoveDirectionX = vx * invLength;
            lastMoveDirectionY = vy * invLength;
        }

        if (dashTimer > 0f) {
            setVx(dashDirectionX * moveSpeed * DASH_SPEED_MULTIPLIER);
            setVy(dashDirectionY * moveSpeed * DASH_SPEED_MULTIPLIER);
            return;
        }

        setVx(vx);
        setVy(vy);
    }

    @Override
    public void render(OutputManager outputManager) {
        if (isBlinkHidden(respawnProtectionTimer)) {
            return;
        }

        float scale = isDashing() ? 1.34f : 1.30f;
        float drawWidth = getWidth() * scale;
        float drawHeight = getHeight() * scale;
        float drawX = getX() - (drawWidth - getWidth()) * 0.5f;
        float drawY = getY() - (drawHeight - getHeight()) * 0.5f;

        float shadowOffsetX = getWidth() * 0.05f;
        float shadowOffsetY = getHeight() * 0.06f;
        float glowPadding = getWidth() * (isDashing() ? 0.26f : 0.20f);
        float edgePadding = getWidth() * 0.10f;

        outputManager.drawTinted(
            LingoSprites.player(),
            drawX + shadowOffsetX,
            drawY - shadowOffsetY,
            drawWidth,
            drawHeight,
            PLAYER_SHADOW
        );
        outputManager.drawTinted(
            LingoSprites.player(),
            drawX - glowPadding * 0.5f,
            drawY - glowPadding * 0.5f,
            drawWidth + glowPadding,
            drawHeight + glowPadding,
            isDashing() ? PLAYER_DASH_GLOW : PLAYER_GLOW
        );
        outputManager.drawTinted(
            LingoSprites.player(),
            drawX - edgePadding * 0.5f,
            drawY - edgePadding * 0.5f,
            drawWidth + edgePadding,
            drawHeight + edgePadding,
            PLAYER_EDGE
        );
        if (hasShockPower()) {
            renderShockEffect(outputManager, drawX, drawY, drawWidth, drawHeight);
        }
        outputManager.draw(LingoSprites.player(), drawX, drawY, drawWidth, drawHeight);
        if (hasShockPower()) {
            float bodyPulse = 0.70f + 0.30f * oscillate(shockTimer * 11f);
            float bodyPadding = getWidth() * 0.06f;
            Color bodyTint = withAlpha(PLAYER_SHOCK_BODY, PLAYER_SHOCK_BODY.a * bodyPulse);
            outputManager.drawTinted(
                LingoSprites.player(),
                drawX - bodyPadding * 0.5f,
                drawY - bodyPadding * 0.5f,
                drawWidth + bodyPadding,
                drawHeight + bodyPadding,
                bodyTint
            );
        }
    }

    public boolean isDashing() {
        return dashTimer > 0f;
    }

    public void clearDash() {
        dashTimer = 0f;
        dashDirectionX = 0f;
        dashDirectionY = 0f;
        lastMoveDirectionX = 0f;
        lastMoveDirectionY = 0f;
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

    public void activateShock(float seconds) {
        shockTimer = Math.max(shockTimer, Math.max(0f, seconds));
    }

    public void clearShock() {
        shockTimer = 0f;
    }

    public boolean hasShockPower() {
        return shockTimer > 0f;
    }

    private static boolean isNearZero(float x, float y) {
        return Math.abs(x) < 0.001f && Math.abs(y) < 0.001f;
    }

    private static float inverseLength(float x, float y) {
        float length = (float) Math.sqrt(x * x + y * y);
        return length <= 0.0001f ? 0f : 1f / length;
    }

    private void renderShockEffect(OutputManager outputManager, float drawX, float drawY, float drawWidth, float drawHeight) {
        float phase = shockTimer * 10f;
        float pulse = 0.55f + 0.45f * oscillate(phase);
        float orbit = getWidth() * (0.18f + 0.08f * pulse);
        float haloPadding = getWidth() * (0.42f + 0.12f * pulse);
        float sparkSize = Math.max(drawWidth, drawHeight) * (0.70f + 0.12f * pulse);

        outputManager.drawTinted(
            LingoSprites.shockPickup(),
            drawX - haloPadding * 0.5f,
            drawY - haloPadding * 0.5f,
            drawWidth + haloPadding,
            drawHeight + haloPadding,
            withAlpha(PLAYER_SHOCK_HALO, PLAYER_SHOCK_HALO.a * (0.75f + 0.25f * pulse))
        );
        outputManager.drawTinted(
            LingoSprites.player(),
            drawX - haloPadding * 0.28f,
            drawY - haloPadding * 0.28f,
            drawWidth + haloPadding * 0.56f,
            drawHeight + haloPadding * 0.56f,
            withAlpha(PLAYER_SHOCK_GLOW, PLAYER_SHOCK_GLOW.a * (0.82f + 0.18f * pulse))
        );

        float leftX = drawX - sparkSize * 0.42f - orbit;
        float leftY = drawY + drawHeight * 0.18f + (float) Math.sin(phase) * getHeight() * 0.10f;
        float rightX = drawX + drawWidth - sparkSize * 0.58f + orbit;
        float rightY = drawY + drawHeight * 0.14f - (float) Math.sin(phase * 1.2f) * getHeight() * 0.09f;
        float topX = drawX + drawWidth * 0.5f - sparkSize * 0.5f + (float) Math.cos(phase * 0.85f) * getWidth() * 0.08f;
        float topY = drawY + drawHeight - sparkSize * 0.28f + getHeight() * 0.16f;

        Color arcTint = withAlpha(PLAYER_SHOCK_ARC, PLAYER_SHOCK_ARC.a * (0.78f + 0.22f * pulse));
        outputManager.drawTinted(LingoSprites.shockPickup(), leftX, leftY, sparkSize, sparkSize, arcTint);
        outputManager.drawTinted(
            LingoSprites.shockPickup(),
            rightX,
            rightY,
            sparkSize * 0.96f,
            sparkSize * 0.96f,
            withAlpha(PLAYER_SHOCK_ARC, PLAYER_SHOCK_ARC.a * (0.64f + 0.20f * pulse))
        );
        outputManager.drawTinted(
            LingoSprites.shockPickup(),
            topX,
            topY,
            sparkSize * 0.88f,
            sparkSize * 0.88f,
            withAlpha(PLAYER_SHOCK_ARC, PLAYER_SHOCK_ARC.a * (0.52f + 0.18f * pulse))
        );
    }

    private static float oscillate(float value) {
        return 0.5f + 0.5f * (float) Math.sin(value);
    }

    private static Color withAlpha(Color color, float alpha) {
        return new Color(color.r, color.g, color.b, Math.max(0f, Math.min(1f, alpha)));
    }

    private static boolean isBlinkHidden(float timer) {
        if (timer <= 0f) {
            return false;
        }
        return ((int) (timer / RESPAWN_BLINK_INTERVAL)) % 2 != 0;
    }
}
