package io.github.some_example_name.lingoman.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.scenes.Scene;

public class MenuScene implements Scene {

    private static final Color BACKGROUND = new Color(0.07f, 0.10f, 0.13f, 1f);
    private static final Color BACKDROP_TOP = new Color(0.15f, 0.24f, 0.23f, 0.45f);
    private static final Color BACKDROP_BOTTOM = new Color(0.24f, 0.18f, 0.08f, 0.35f);
    private static final Color CARD_FILL = new Color(0.08f, 0.11f, 0.14f, 0.95f);
    private static final Color CARD_BORDER = new Color(0.94f, 0.78f, 0.28f, 1f);
    private static final Color OPTION_FILL = new Color(0.14f, 0.18f, 0.22f, 0.98f);
    private static final Color OPTION_BORDER = new Color(0.30f, 0.38f, 0.42f, 1f);
    private static final Color OPTION_SELECTED_FILL = new Color(0.96f, 0.79f, 0.26f, 1f);
    private static final Color OPTION_SELECTED_BORDER = new Color(1.00f, 0.90f, 0.52f, 1f);
    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.71f, 0.76f, 0.77f, 1f);
    private static final Color TEXT_DARK = new Color(0.11f, 0.10f, 0.08f, 1f);

    private EngineContext context;
    private final MenuOption[] options = {
        new MenuOption("Easy", "1 ghost", GameState.Difficulty.EASY),
        new MenuOption("Medium", "2 ghosts", GameState.Difficulty.MEDIUM),
        new MenuOption("Hard", "3 ghosts", GameState.Difficulty.HARD),
        new MenuOption("Quit", "Exit game", null)
    };
    private int selectedIndex = 0;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        System.out.println("[LingoMan] Menu entered");
    }

    @Override
    public void exit() {
        System.out.println("[LingoMan] Menu exit");
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_UP)) {
            selectedIndex = (selectedIndex - 1 + options.length) % options.length;
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_DOWN)) {
            selectedIndex = (selectedIndex + 1) % options.length;
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_CONFIRM)) {
            MenuOption selected = options[selectedIndex];
            if (selected.difficulty != null) {
                LingoSession.get().getGameState().clearCompletedWords();
                LingoSession.get().getGameState().setDifficulty(selected.difficulty);
                context.getSaveManager().save(LingoSession.SAVE_FILE);
                context.getSceneManager().setActiveScene(LingoSceneIds.GAME);
            } else {
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        context.getOutputManager().drawRect(0f, 330f, 640f, 150f, BACKDROP_TOP);
        context.getOutputManager().drawRect(0f, 0f, 640f, 105f, BACKDROP_BOTTOM);
        context.getOutputManager().drawPanel(100f, 88f, 440f, 286f, CARD_FILL, CARD_BORDER);

        context.getOutputManager().drawTextCenteredWithShadow("LINGO-MAN", 320f, 350f, TEXT_PRIMARY);
        context.getOutputManager().drawTextCentered("Educational arcade word hunt", 320f, 324f, TEXT_MUTED);
        context.getOutputManager().drawTextCentered("Choose a difficulty", 320f, 298f, TEXT_MUTED);

        for (int i = 0; i < options.length; i++) {
            boolean selected = i == selectedIndex;
            float y = 242f - (i * 44f);
            Color fill = selected ? OPTION_SELECTED_FILL : OPTION_FILL;
            Color border = selected ? OPTION_SELECTED_BORDER : OPTION_BORDER;
            Color labelColor = selected ? TEXT_DARK : TEXT_PRIMARY;
            Color hintColor = selected ? TEXT_DARK : TEXT_MUTED;
            context.getOutputManager().drawPanel(144f, y, 352f, 34f, fill, border);
            context.getOutputManager().drawText(options[i].label, 164f, y + 23f, labelColor);
            context.getOutputManager().drawTextRightAligned(options[i].hint, 476f, y + 23f, hintColor);
        }

        context.getOutputManager().drawTextCentered("W/S or arrow keys to move, ENTER to select", 320f, 54f, TEXT_MUTED);
    }

    @Override
    public void dispose() {
        System.out.println("[LingoMan] Menu disposed");
    }

    private static final class MenuOption {
        final String label;
        final String hint;
        final GameState.Difficulty difficulty;

        private MenuOption(String label, String hint, GameState.Difficulty difficulty) {
            this.label = label;
            this.hint = hint;
            this.difficulty = difficulty;
        }
    }
}
