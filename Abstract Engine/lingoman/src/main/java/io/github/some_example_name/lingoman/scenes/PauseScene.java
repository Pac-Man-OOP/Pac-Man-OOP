package io.github.some_example_name.lingoman.scenes;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoAudio;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.scenes.Scene;

public class PauseScene implements Scene {

    private static final Color BACKGROUND = new Color(0.06f, 0.08f, 0.12f, 1f);
    private static final Color PANEL_FILL = new Color(0.09f, 0.11f, 0.16f, 0.96f);
    private static final Color PANEL_BORDER = new Color(0.94f, 0.78f, 0.28f, 1f);
    private static final Color OPTION_FILL = new Color(0.15f, 0.18f, 0.22f, 0.98f);
    private static final Color OPTION_BORDER = new Color(0.31f, 0.37f, 0.42f, 1f);
    private static final Color OPTION_SELECTED_FILL = new Color(0.96f, 0.79f, 0.26f, 1f);
    private static final Color OPTION_SELECTED_BORDER = new Color(1.00f, 0.89f, 0.47f, 1f);
    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.71f, 0.76f, 0.77f, 1f);
    private static final Color TEXT_DARK = new Color(0.10f, 0.10f, 0.08f, 1f);

    private static final String[] OPTIONS = { "Resume", "Settings", "Menu" };

    private EngineContext context;
    private int selectedIndex;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        selectedIndex = 0;
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_UP)) {
            selectedIndex = (selectedIndex - 1 + OPTIONS.length) % OPTIONS.length;
            context.getAudioManager().playSound(LingoAudio.SFX_MENU_NAVIGATE, false);
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_DOWN)) {
            selectedIndex = (selectedIndex + 1) % OPTIONS.length;
            context.getAudioManager().playSound(LingoAudio.SFX_MENU_NAVIGATE, false);
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)) {
            resumeGame();
            return;
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_CONFIRM)) {
            switch (selectedIndex) {
                case 0 -> resumeGame();
                case 1 -> {
                    LingoSession.get().setSettingsReturnSceneId(LingoSceneIds.PAUSE);
                    context.getSceneManager().setActiveScene(LingoSceneIds.SETTINGS);
                }
                default -> context.getSceneManager().setActiveScene(LingoSceneIds.MENU);
            }
        }
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        context.getOutputManager().drawRect(0f, 336f, 640f, 144f, new Color(0.20f, 0.12f, 0.08f, 0.26f));
        context.getOutputManager().drawPanel(124f, 118f, 392f, 214f, PANEL_FILL, PANEL_BORDER);

        context.getOutputManager().drawTextCenteredWithShadow("PAUSED", 320f, 298f, TEXT_PRIMARY);
        context.getOutputManager().drawTextCentered("Resume or open settings", 320f, 274f, TEXT_MUTED);

        for (int i = 0; i < OPTIONS.length; i++) {
            boolean selected = i == selectedIndex;
            float y = 228f - (i * 42f);
            Color fill = selected ? OPTION_SELECTED_FILL : OPTION_FILL;
            Color border = selected ? OPTION_SELECTED_BORDER : OPTION_BORDER;
            Color labelColor = selected ? TEXT_DARK : TEXT_PRIMARY;

            context.getOutputManager().drawPanel(170f, y, 300f, 32f, fill, border);
            context.getOutputManager().drawTextCentered(OPTIONS[i], 320f, y + 22f, labelColor);
        }

        context.getOutputManager().drawTextCentered("W/S or UP/DOWN: move    ENTER: select    M or ESC: resume", 320f, 86f, TEXT_MUTED);
    }

    @Override
    public void dispose() {
    }

    private void resumeGame() {
        LingoSession.get().requestGameResume();
        context.getSceneManager().setActiveScene(LingoSceneIds.GAME);
    }
}
