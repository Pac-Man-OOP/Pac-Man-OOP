package io.github.some_example_name.lingoman.scenes;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.model.WordBank;
import io.github.some_example_name.scenes.Scene;

public class FoundWordsScene implements Scene {

    private static final Color BACKGROUND = new Color(0.08f, 0.10f, 0.13f, 1f);
    private static final Color PANEL_FILL = new Color(0.09f, 0.12f, 0.15f, 0.95f);
    private static final Color PANEL_BORDER = new Color(0.94f, 0.78f, 0.28f, 1f);
    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.71f, 0.76f, 0.77f, 1f);
    private static final Color TEXT_ACCENT = new Color(0.98f, 0.84f, 0.36f, 1f);

    private static final int ROWS_PER_PAGE = 8;

    private EngineContext context;
    private int pageStartIndex;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        pageStartIndex = 0;
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput() {
        List<String> foundWords = LingoSession.get().getGameState().getFoundWords();
        int total = foundWords.size();

        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_UP)) {
            pageStartIndex = Math.max(0, pageStartIndex - ROWS_PER_PAGE);
        }
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_DOWN)) {
            int maxStart = Math.max(0, total - ROWS_PER_PAGE);
            pageStartIndex = Math.min(maxStart, pageStartIndex + ROWS_PER_PAGE);
        }

        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)
            || context.getInputManager().isActionJustPressed(LingoInputActions.MENU_CONFIRM)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.MENU);
        }
    }

    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        context.getOutputManager().drawPanel(68f, 54f, 504f, 364f, PANEL_FILL, PANEL_BORDER);

        List<String> foundWords = LingoSession.get().getGameState().getFoundWords();
        int total = foundWords.size();

        context.getOutputManager().drawTextCenteredWithShadow("FOUND WORDS", 320f, 390f, TEXT_PRIMARY);
        context.getOutputManager().drawTextCentered("Stored words and meanings", 320f, 368f, TEXT_MUTED);
        context.getOutputManager().drawText("Total: " + total, 88f, 348f, TEXT_ACCENT);

        if (total == 0) {
            context.getOutputManager().drawTextCentered("No words found yet. Finish a round to save one.", 320f, 232f, TEXT_MUTED);
        } else {
            int shown = 0;
            for (int i = pageStartIndex; i < total && shown < ROWS_PER_PAGE; i++) {
                String word = foundWords.get(i);
                String meaning = truncate(WordBank.meaningFor(word), 52);
                float y = 324f - (shown * 28f);
                context.getOutputManager().drawText(word, 88f, y, TEXT_PRIMARY);
                context.getOutputManager().drawText("- " + meaning, 188f, y, TEXT_MUTED);
                shown++;
            }
        }

        context.getOutputManager().drawTextCentered("UP/DOWN: page    ENTER, M or ESC: back", 320f, 70f, TEXT_MUTED);
    }

    @Override
    public void dispose() {
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }
}
