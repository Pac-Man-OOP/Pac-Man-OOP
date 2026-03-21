package io.github.some_example_name.lingoman.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GameState {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private Difficulty difficulty = Difficulty.EASY;
    private String targetWord = "";
    private int collectedIndex = 0;
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
        this.targetWord = targetWord == null ? "" : targetWord.trim().toUpperCase();
        collectedIndex = 0;
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

    public boolean isNextExpectedLetter(char letter) {
        if (targetWord.isBlank() || collectedIndex >= targetWord.length()) {
            return false;
        }
        return Character.toUpperCase(letter) == targetWord.charAt(collectedIndex);
    }

    public boolean collectNextLetter(char letter) {
        if (!isNextExpectedLetter(letter)) {
            return false;
        }
        collectedIndex++;
        return true;
    }

    public char getNextExpectedLetter() {
        if (targetWord.isBlank() || collectedIndex >= targetWord.length()) {
            return '\0';
        }
        return targetWord.charAt(collectedIndex);
    }

    public int getCollectedIndex() {
        return collectedIndex;
    }

    public boolean hasCollectedAllLetters() {
        return !targetWord.isBlank() && collectedIndex >= targetWord.length();
    }

    public String getCollectedLettersDisplay() {
        if (targetWord.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < targetWord.length(); i++) {
            if (i < collectedIndex) {
                builder.append(targetWord.charAt(i));
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
