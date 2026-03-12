package io.github.some_example_name.lingoman.scenes;

import com.badlogic.gdx.Gdx;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.scenes.Scene;

public class MenuScene implements Scene {

    private EngineContext context;
    private final MenuOption[] options = {
        new MenuOption("Start Easy", GameState.Difficulty.EASY),
        new MenuOption("Start Medium", GameState.Difficulty.MEDIUM),
        new MenuOption("Start Hard", GameState.Difficulty.HARD),
        new MenuOption("Quit", null)
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
        context.getOutputManager().clearScreen(0.09f, 0.09f, 0.12f, 1f);
        context.getOutputManager().drawText("LINGO-MAN", 240f, 420f);
        context.getOutputManager().drawText("SELECT DIFFICULTY", 210f, 380f);

        for (int i = 0; i < options.length; i++) {
            String prefix = (i == selectedIndex) ? "> " : "  ";
            context.getOutputManager().drawText(prefix + options[i].label, 220f, 320f - (i * 35f));
        }
    }

    @Override
    public void dispose() {
        System.out.println("[LingoMan] Menu disposed");
    }

    private static final class MenuOption {
        final String label;
        final GameState.Difficulty difficulty;

        private MenuOption(String label, GameState.Difficulty difficulty) {
            this.label = label;
            this.difficulty = difficulty;
        }
    }
}
