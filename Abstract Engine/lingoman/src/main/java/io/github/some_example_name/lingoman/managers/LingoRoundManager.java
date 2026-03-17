package io.github.some_example_name.lingoman.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.EntityCollisionListenerAdapter;
import io.github.some_example_name.collision.ICollisionListener;
import io.github.some_example_name.lingoman.LingoAudio;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.entity.GhostEntity;
import io.github.some_example_name.lingoman.entity.LetterEntity;
import io.github.some_example_name.lingoman.entity.PlayerEntity;
import io.github.some_example_name.lingoman.entity.WallEntity;
import io.github.some_example_name.lingoman.level.MazeLayout;
import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.lingoman.model.WordBank;
import io.github.some_example_name.lingoman.movement.MazeFollowBehaviour;
import io.github.some_example_name.lingoman.movement.MazePatrolBehaviour;
import io.github.some_example_name.lingoman.movement.MazeSeekBehaviour;
import io.github.some_example_name.lingoman.movement.MazeWanderBehaviour;
import io.github.some_example_name.lingoman.world.LingoWorld;
import io.github.some_example_name.managers.OutputManager;
import io.github.some_example_name.movement.behaviour.MovementBehaviour;

public final class LingoRoundManager {

    public enum RoundOutcome {
        IN_PROGRESS,
        WON,
        LOST;

        public boolean isTerminal() {
            return this != IN_PROGRESS;
        }
    }

    private static final float INVULNERABLE_SECONDS = 1.25f;

    private EngineContext context;
    private final LingoWorld world = new LingoWorld();
    private final List<GhostEntity> ghosts = new ArrayList<>();

    private MazeLayout.Layout currentLayout;
    private PlayerEntity player;
    private float invulnerableTimer = 0f;

    public void initialize(EngineContext context) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        this.context = context;
    }

    public void startNewRound(LingoHudManager hudManager) {
        ensureInitialized();

        GameState state = LingoSession.get().getGameState();
        Random rng = LingoSession.get().getRandom();
        MazeLayout.Layout nextLayout = MazeLayout.forDifficulty(state.getDifficulty());

        clearWorld();
        state.resetLives();
        state.setLastResult("");
        state.setTargetWord(WordBank.randomWord(state.getDifficulty(), rng));

        currentLayout = nextLayout;
        invulnerableTimer = 0f;
        if (hudManager != null) {
            hudManager.clear();
        }

        buildLevel(state, hudManager);
    }

    public RoundOutcome update(float deltaTime, String profileFile) {
        if (invulnerableTimer > 0f) {
            invulnerableTimer = Math.max(0f, invulnerableTimer - deltaTime);
        }

        world.update(deltaTime);
        return resolveRoundOutcome(profileFile);
    }

    public void render(OutputManager outputManager) {
        world.render(outputManager);
    }

    public boolean isMovementInputPressed() {
        ensureInitialized();
        return context.getInputManager().isActionPressed(LingoInputActions.MOVE_LEFT)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_RIGHT)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_UP)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_DOWN);
    }

    public void dispose() {
        clearWorld();
    }

    private RoundOutcome resolveRoundOutcome(String profileFile) {
        ensureInitialized();

        GameState state = LingoSession.get().getGameState();
        if (state.hasCollectedAllLetters()) {
            state.addFoundWord(state.getTargetWord());
            context.getSaveManager().save(profileFile);
            state.setLastResult("WORD COMPLETE");
            context.getAudioManager().playSound(LingoAudio.SFX_VICTORY, false);
            return RoundOutcome.WON;
        }
        if (state.getLives() <= 0) {
            state.setLastResult("OUT OF LIVES");
            context.getAudioManager().playSound(LingoAudio.SFX_GAME_OVER, false);
            return RoundOutcome.LOST;
        }
        return RoundOutcome.IN_PROGRESS;
    }

    private void buildLevel(GameState state, LingoHudManager hudManager) {
        requireLayout();

        buildWalls();
        buildPlayer(hudManager);
        buildGhosts(state.getDifficulty());
        buildLetters(state.getTargetWord());
    }

    private void clearWorld() {
        ghosts.clear();
        world.clear();
        player = null;
        currentLayout = null;
        invulnerableTimer = 0f;
    }

    private void buildWalls() {
        requireLayout();
        int id = 0;
        for (int row = 0; row < currentLayout.getRows(); row++) {
            for (int col = 0; col < currentLayout.getCols(); col++) {
                if (!MazeLayout.isWall(currentLayout, col, row)) {
                    continue;
                }

                float x = MazeLayout.toWorldX(currentLayout, col);
                float y = MazeLayout.toWorldY(currentLayout, row);
                WallEntity wall = new WallEntity("wall_" + id++, x, y, currentLayout.getTileSize());
                world.addCollidableEntity(wall, null);
            }
        }
    }

    private void buildPlayer(LingoHudManager hudManager) {
        requireLayout();
        GridPoint2 spawn = currentLayout.getPlayerSpawn();
        float size = entitySize();
        float x = MazeLayout.toWorldXCentered(currentLayout, spawn.x, size);
        float y = MazeLayout.toWorldYCentered(currentLayout, spawn.y, size);
        player = new PlayerEntity("player", context.getInputManager(), x, y, playerSpeed(), size);
        world.addCollidableEntity(player, new PlayerCollisionListener(hudManager));
    }

    private void buildGhosts(GameState.Difficulty difficulty) {
        requireLayout();
        int ghostCount = switch (difficulty) {
            case MEDIUM -> 2;
            case HARD -> 4;
            default -> 1;
        };

        float speed = ghostSpeed(difficulty);
        float size = entitySize();
        GridPoint2[] spawns = currentLayout.getGhostSpawns();

        List<Color> colors = List.of(
            new Color(1.0f, 0.35f, 0.30f, 1f),
            new Color(0.90f, 0.20f, 0.90f, 1f),
            new Color(0.35f, 0.90f, 0.45f, 1f),
            new Color(0.95f, 0.70f, 0.22f, 1f)
        );

        for (int i = 0; i < ghostCount; i++) {
            GridPoint2 spawn = spawns[i % spawns.length];
            float x = MazeLayout.toWorldXCentered(currentLayout, spawn.x, size);
            float y = MazeLayout.toWorldYCentered(currentLayout, spawn.y, size);
            GhostEntity ghost = new GhostEntity("ghost_" + i, colors.get(i % colors.size()), x, y, size);
            ghosts.add(ghost);
            world.addCollidableEntity(ghost, new EntityCollisionListenerAdapter(ghost));
            world.assignBehaviour(ghost, buildGhostBehaviour(i, speed));
        }
    }

    private void buildLetters(String targetWord) {
        requireLayout();
        if (targetWord == null || targetWord.isBlank()) {
            return;
        }

        List<GridPoint2> openCells = MazeLayout.collectReachableOpenCells(currentLayout, currentLayout.getPlayerSpawn());
        openCells.removeIf(this::isSpawnCell);
        Collections.shuffle(openCells, LingoSession.get().getRandom());

        float size = letterSize();
        int placed = 0;
        for (int i = 0; i < targetWord.length(); i++) {
            if (placed >= openCells.size()) {
                break;
            }

            char letter = targetWord.charAt(i);
            GridPoint2 cell = openCells.get(placed++);
            float x = MazeLayout.toWorldXCentered(currentLayout, cell.x, size);
            float y = MazeLayout.toWorldYCentered(currentLayout, cell.y, size);
            LetterEntity entity = new LetterEntity("letter_" + i, letter, x, y, size);
            world.addCollidableEntity(entity, null);
        }
    }

    private boolean isSpawnCell(GridPoint2 cell) {
        if (cell == null) {
            return false;
        }
        if (cell.equals(currentLayout.getPlayerSpawn())) {
            return true;
        }
        for (GridPoint2 spawn : currentLayout.getGhostSpawns()) {
            if (cell.equals(spawn)) {
                return true;
            }
        }
        return false;
    }

    private MovementBehaviour buildGhostBehaviour(int index, float speed) {
        List<GridPoint2> route = currentLayout.getWaypointRoute();
        float threshold = Math.max(2f, currentLayout.getTileSize() * 0.22f);
        return switch (index) {
            case 0 -> new MazeSeekBehaviour(currentLayout, player, speed);
            case 1 -> new MazePatrolBehaviour(currentLayout, route, speed * 0.96f, threshold);
            case 2 -> new MazeFollowBehaviour(currentLayout, player, speed * 0.92f, 4, threshold);
            default -> new MazeWanderBehaviour(currentLayout, speed * 0.88f);
        };
    }

    private void resetPositions() {
        GridPoint2 playerSpawn = currentLayout.getPlayerSpawn();
        player.setX(MazeLayout.toWorldXCentered(currentLayout, playerSpawn.x, player.getWidth()));
        player.setY(MazeLayout.toWorldYCentered(currentLayout, playerSpawn.y, player.getHeight()));
        player.setVx(0f);
        player.setVy(0f);

        GridPoint2[] spawns = currentLayout.getGhostSpawns();
        for (int i = 0; i < ghosts.size(); i++) {
            GhostEntity ghost = ghosts.get(i);
            GridPoint2 spawn = spawns[i % spawns.length];
            ghost.setX(MazeLayout.toWorldXCentered(currentLayout, spawn.x, ghost.getWidth()));
            ghost.setY(MazeLayout.toWorldYCentered(currentLayout, spawn.y, ghost.getHeight()));
            ghost.setVx(0f);
            ghost.setVy(0f);
        }

        float speed = ghostSpeed(LingoSession.get().getGameState().getDifficulty());
        for (int i = 0; i < ghosts.size(); i++) {
            world.assignBehaviour(ghosts.get(i), buildGhostBehaviour(i, speed));
        }
    }

    private void handlePlayerHit(LingoHudManager hudManager) {
        if (invulnerableTimer > 0f) {
            return;
        }

        GameState state = LingoSession.get().getGameState();
        state.loseLife();
        invulnerableTimer = INVULNERABLE_SECONDS;
        if (hudManager != null) {
            hudManager.showStatus("Ouch! Lives left: " + state.getLives());
        }
        context.getAudioManager().playSound(LingoAudio.SFX_HURT, false);
        resetPositions();
    }

    private float entitySize() {
        requireLayout();
        return currentLayout.getTileSize() * 0.72f;
    }

    private float letterSize() {
        requireLayout();
        return currentLayout.getTileSize() * 0.55f;
    }

    private float playerSpeed() {
        return currentLayout.getTileSize() * 5.0f;
    }

    private float ghostSpeed(GameState.Difficulty difficulty) {
        requireLayout();
        float scale = switch (difficulty) {
            case MEDIUM -> 3.0f;
            case HARD -> 3.35f;
            default -> 2.6f;
        };
        return currentLayout.getTileSize() * scale;
    }

    private void ensureInitialized() {
        if (context == null) {
            throw new IllegalStateException("LingoRoundManager has not been initialized");
        }
    }

    private void requireLayout() {
        if (currentLayout == null) {
            throw new IllegalStateException("No layout is active for the current round");
        }
    }

    private final class PlayerCollisionListener implements ICollisionListener {

        private final LingoHudManager hudManager;

        private PlayerCollisionListener(LingoHudManager hudManager) {
            this.hudManager = hudManager;
        }

        @Override
        public void onCollisionEnter(Collider other) {
            if (other == null) {
                return;
            }

            Object owner = other.getOwner();
            if (owner instanceof LetterEntity letter) {
                if (!letter.isActive()) {
                    return;
                }

                LingoSession.get().getGameState().collectLetter(letter.getLetter());
                context.getAudioManager().playSound(LingoAudio.SFX_COLLECT_LETTER, false);
                world.removeEntity(letter);
                if (hudManager != null) {
                    hudManager.showStatus("Collected: " + letter.getLetter());
                }
            } else if (owner instanceof GhostEntity) {
                handlePlayerHit(hudManager);
            }
        }

        @Override
        public void onCollisionStay(Collider other) {
        }

        @Override
        public void onCollisionExit(Collider other) {
        }
    }
}
