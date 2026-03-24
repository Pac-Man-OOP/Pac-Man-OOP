package io.github.some_example_name.lingoman.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameState.
 *
 * Purpose:
 * - verify letter collection must follow the correct order
 * - verify found words are normalized and unique
 * - verify lives do not go below zero
 */
class GameStateTest {

    @Test
    void collectsLettersInOrderOnly() {
        GameState state = new GameState();
        state.setTargetWord("cat");

        assertEquals('C', state.getNextExpectedLetter());

        // Wrong first letter should be rejected
        assertFalse(state.collectNextLetter('A'));
        assertEquals(0, state.getCollectedIndex());

        // Correct sequence should advance progress
        assertTrue(state.collectNextLetter('C'));
        assertEquals(1, state.getCollectedIndex());
        assertEquals("C__", state.getCollectedLettersDisplay());

        assertTrue(state.collectNextLetter('A'));
        assertEquals("CA_", state.getCollectedLettersDisplay());

        assertTrue(state.collectNextLetter('T'));
        assertTrue(state.hasCollectedAllLetters());
        assertEquals("CAT", state.getCollectedLettersDisplay());
    }

    @Test
    void foundWords_areNormalizedAndUnique() {
        GameState state = new GameState();

        state.addFoundWord("cat");
        state.addFoundWord(" CAT ");
        state.addFoundWord("dog");

        // Words should be stored in uppercase and duplicates should be ignored
        assertEquals(List.of("CAT", "DOG"), state.getFoundWords());
    }

    @Test
    void lives_doNotGoBelowZero() {
        GameState state = new GameState();

        // Lose more lives than available
        state.loseLife();
        state.loseLife();
        state.loseLife();
        state.loseLife();

        assertEquals(0, state.getLives());
    }
}