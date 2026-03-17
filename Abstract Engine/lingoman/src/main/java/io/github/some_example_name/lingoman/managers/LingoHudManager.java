package io.github.some_example_name.lingoman.managers;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.managers.OutputManager;

public final class LingoHudManager {

    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.72f, 0.77f, 0.79f, 1f);
    private static final Color TEXT_ACCENT = new Color(0.98f, 0.84f, 0.36f, 1f);
    private static final Color TEXT_WARNING = new Color(1.00f, 0.64f, 0.47f, 1f);

    private static final float STATUS_MESSAGE_DURATION = 2.0f;

    private String statusMessage = "";
    private float statusMessageTimer = 0f;

    public void update(float deltaTime) {
        if (statusMessageTimer <= 0f) {
            return;
        }

        statusMessageTimer = Math.max(0f, statusMessageTimer - deltaTime);
        if (statusMessageTimer == 0f) {
            statusMessage = "";
        }
    }

    public void clear() {
        statusMessage = "";
        statusMessageTimer = 0f;
    }

    public void showStatus(String message) {
        statusMessage = message == null ? "" : message;
        statusMessageTimer = STATUS_MESSAGE_DURATION;
    }

    public void render(OutputManager outputManager, GameState state) {
        if (outputManager == null || state == null) {
            return;
        }

        outputManager.drawTextWithShadow("Target: " + state.getTargetWord(), 28f, 474f, TEXT_ACCENT);
        outputManager.drawTextWithShadow("Progress: " + state.getCollectedLettersDisplay(), 28f, 454f, TEXT_PRIMARY);

        outputManager.drawTextWithShadow("Lives: " + state.getLives(), 432f, 474f,
            state.getLives() <= 1 ? TEXT_WARNING : TEXT_PRIMARY);
        outputManager.drawTextWithShadow("Mode: " + state.getDifficulty(), 432f, 454f, TEXT_PRIMARY);

        outputManager.drawTextWithShadow("Move: WASD / Arrows", 28f, 18f, TEXT_PRIMARY);
        outputManager.drawTextWithShadow("Menu: M or ESC", 236f, 18f, TEXT_MUTED);

        if (!statusMessage.isBlank()) {
            outputManager.drawTextRightAlignedWithShadow(statusMessage, 610f, 18f, TEXT_WARNING);
        }
    }
}
