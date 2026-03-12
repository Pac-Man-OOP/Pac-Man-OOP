package io.github.some_example_name.lingoman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.save.ISaveable;
import io.github.some_example_name.save.SaveData;

public final class LingoSession implements ISaveable {

    private static final LingoSession INSTANCE = new LingoSession();
    public static final String SAVE_FILE = "lingoman-session";
    private static final String SAVE_ID = "lingoman_session";
    private static final String COMPLETED_WORD_COUNT_KEY = "completed_word_count";
    private static final String COMPLETED_WORD_PREFIX = "completed_word_";

    private final GameState gameState = new GameState();
    private final Random random = new Random();

    private LingoSession() {
    }

    public static LingoSession get() {
        return INSTANCE;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Random getRandom() {
        return random;
    }

    @Override
    public String getSaveId() {
        return SAVE_ID;
    }

    @Override
    public SaveData writeSaveData() {
        SaveData data = new SaveData(getSaveId());
        List<String> completedWords = gameState.getCompletedWords();
        data.put(COMPLETED_WORD_COUNT_KEY, completedWords.size());
        for (int i = 0; i < completedWords.size(); i++) {
            data.put(COMPLETED_WORD_PREFIX + i, completedWords.get(i));
        }
        return data;
    }

    @Override
    public void readSaveData(SaveData saveData) {
        if (saveData == null) {
            return;
        }

        int wordCount = readInt(saveData, COMPLETED_WORD_COUNT_KEY, 0);
        List<String> restoredWords = new ArrayList<>();
        for (int i = 0; i < wordCount; i++) {
            Object value = saveData.get(COMPLETED_WORD_PREFIX + i);
            if (value instanceof String word && !word.isBlank()) {
                restoredWords.add(word);
            }
        }
        gameState.restoreCompletedWords(restoredWords);
    }

    private int readInt(SaveData saveData, String key, int fallback) {
        Object value = saveData.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }
}
