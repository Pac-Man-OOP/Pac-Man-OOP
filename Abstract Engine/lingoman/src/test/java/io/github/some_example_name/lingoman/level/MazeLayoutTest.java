package io.github.some_example_name.lingoman.level;

import com.badlogic.gdx.math.GridPoint2;
import io.github.some_example_name.lingoman.model.GameState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MazeLayout helper logic.
 *
 * Purpose:
 * - verify the player spawn is valid and reachable
 * - verify pathfinding returns a valid adjacent next step
 * - verify same start/goal behaves correctly
 */
class MazeLayoutTest {

    @Test
    void playerSpawn_isReachableAndOpen() {
        MazeLayout.Layout layout = MazeLayout.forDifficulty(GameState.Difficulty.EASY);
        GridPoint2 spawn = layout.getPlayerSpawn();

        // Player spawn should be on an open tile
        assertTrue(MazeLayout.isOpen(layout, spawn.x, spawn.y));

        // All reachable open cells from spawn should include the spawn itself
        List<GridPoint2> reachable = MazeLayout.collectReachableOpenCells(layout, spawn);
        assertTrue(reachable.contains(spawn));
        assertFalse(reachable.isEmpty());
    }

    @Test
    void nextStepTowards_returnsAdjacentOpenCell() {
        MazeLayout.Layout layout = MazeLayout.forDifficulty(GameState.Difficulty.EASY);
        GridPoint2 start = layout.getPlayerSpawn();

        List<GridPoint2> reachable = MazeLayout.collectReachableOpenCells(layout, start);
        GridPoint2 goal = reachable.get(reachable.size() - 1);

        GridPoint2 next = MazeLayout.findNextStepTowards(layout, start, goal);

        assertNotNull(next);
        assertTrue(MazeLayout.isOpen(layout, next.x, next.y));

        // The next step should be exactly one tile away from the start
        int dx = Math.abs(next.x - start.x);
        int dy = Math.abs(next.y - start.y);
        assertEquals(1, dx + dy);
    }

    @Test
    void sameStartAndGoal_returnsGoal() {
        MazeLayout.Layout layout = MazeLayout.forDifficulty(GameState.Difficulty.EASY);
        GridPoint2 start = layout.getPlayerSpawn();

        // If start and goal are the same, the returned next step should be that same tile
        GridPoint2 next = MazeLayout.findNextStepTowards(layout, start, start);

        assertEquals(start, next);
    }
}