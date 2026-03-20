package io.github.some_example_name.lingoman.model;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GameState {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private Difficulty difficulty = Difficulty.EASY;
    private String targetWord = "";
    private final Map<Character, Integer> targetCounts = new LinkedHashMap<>();
    private final Map<Character, Integer> collectedCounts = new HashMap<>();
    private final List<String> foundWords = new ArrayList<>();
    private int lives = 3;
    private String lastResult = "";

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public String getTargetWord() {
        return targetWord;
    }

    public void setTargetWord(String targetWord) {
        this.targetWord = targetWord == null ? "" : targetWord;
        targetCounts.clear();
        collectedCounts.clear();
        for (int i = 0; i < this.targetWord.length(); i++) {
            char letter = Character.toLowerCase(this.targetWord.charAt(i));
            targetCounts.put(letter, targetCounts.getOrDefault(letter, 0) + 1);
        }
    }

    public int getLives() {
        return lives;
    }

    public void resetLives() {
        lives = 3;
    }

    public void loseLife() {
        lives = Math.max(0, lives - 1);
    }

    public void collectLetter(char letter) {
        char normalized = Character.toLowerCase(letter);
        int target = targetCounts.getOrDefault(normalized, 0);
        if (target == 0) {
            return;
        }
        int current = collectedCounts.getOrDefault(normalized, 0);
        if (current < target) {
            collectedCounts.put(normalized, current + 1);
        }
    }

    public boolean hasCollectedAllLetters() {
        if (targetWord.isBlank()) {
            return false;
        }
        for (Map.Entry<Character, Integer> entry : targetCounts.entrySet()) {
            int collected = collectedCounts.getOrDefault(entry.getKey(), 0);
            if (collected < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public String getCollectedLettersDisplay() {
        if (targetWord.isBlank()) {
            return "";
        }
        Map<Character, Integer> usedCounts = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < targetWord.length(); i++) {
            char letter = targetWord.charAt(i);
            char normalized = Character.toLowerCase(letter);
            int used = usedCounts.getOrDefault(normalized, 0);
            int collected = collectedCounts.getOrDefault(normalized, 0);
            if (collected > used) {
                builder.append(letter);
                usedCounts.put(normalized, used + 1);
            } else {
                builder.append('_');
            }
        }
        return builder.toString();
    }

    public void addFoundWord(String word) {
        String normalized = normalizeWord(word);
        addUniqueFoundWord(normalized);
    }

    public List<String> getFoundWords() {
        return Collections.unmodifiableList(foundWords);
    }

    public int getFoundWordsCount() {
        return foundWords.size();
    }

    public void setFoundWords(List<String> words) {
        foundWords.clear();
        if (words == null) {
            return;
        }
        for (String word : words) {
            addUniqueFoundWord(normalizeWord(word));
        }
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult == null ? "" : lastResult;
    }

    public String getLastResult() {
        return lastResult;
    }

    private String normalizeWord(String word) {
        return word == null ? "" : word.trim().toUpperCase();
    }

    private void addUniqueFoundWord(String normalizedWord) {
        if (normalizedWord == null || normalizedWord.isEmpty() || foundWords.contains(normalizedWord)) {
            return;
        }
        foundWords.add(normalizedWord);
    }
}
