package io.github.some_example_name.lingoman.scenes;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.scenes.Scene;

public class GameOverScene implements Scene {

    private static final Color BACKGROUND = new Color(0.09f, 0.08f, 0.10f, 1f);
    private static final Color PANEL_FILL = new Color(0.10f, 0.10f, 0.12f, 0.96f);
    private static final Color PANEL_BORDER = new Color(0.96f, 0.80f, 0.34f, 1f);
    private static final Color SUCCESS = new Color(0.62f, 0.92f, 0.55f, 1f);
    private static final Color FAILURE = new Color(1.00f, 0.62f, 0.47f, 1f);
    private static final Color TEXT_PRIMARY = new Color(0.95f, 0.95f, 0.90f, 1f);
    private static final Color TEXT_MUTED = new Color(0.72f, 0.72f, 0.70f, 1f);

    private EngineContext context;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        System.out.println("[LingoMan] Game over");
    }

    @Override
    public void exit() {
        System.out.println("[LingoMan] Game over exit");
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_RESTART)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME);
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.MENU);
        }
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        String result = LingoSession.get().getGameState().getLastResult();
        if (result == null || result.isBlank()) {
            result = "GAME OVER";
        }
        String word = LingoSession.get().getGameState().getTargetWord();
        String difficulty = LingoSession.get().getGameState().getDifficulty().name();
        boolean success = "WORD COMPLETE".equalsIgnoreCase(result);
        Color resultColor = success ? SUCCESS : FAILURE;

        context.getOutputManager().drawRect(0f, 332f, 640f, 148f, new Color(0.18f, 0.10f, 0.08f, 0.35f));
        context.getOutputManager().drawPanel(108f, 134f, 424f, 190f, PANEL_FILL, PANEL_BORDER);
        context.getOutputManager().drawTextCenteredWithShadow("ROUND OVER", 320f, 298f, TEXT_MUTED);
        context.getOutputManager().drawTextCenteredWithShadow(result, 320f, 262f, resultColor);
        context.getOutputManager().drawTextCentered("Word: " + word + "    Mode: " + difficulty, 320f, 220f, TEXT_PRIMARY);
        context.getOutputManager().drawTextCentered("R restart    M or ESC menu", 320f, 182f, TEXT_MUTED);
    }

    @Override
    public void dispose() {
        System.out.println("[LingoMan] Game over disposed");
    }
}
