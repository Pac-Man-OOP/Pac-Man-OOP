package io.github.some_example_name.lingoman.scenes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.EntityCollisionListenerAdapter;
import io.github.some_example_name.collision.ICollisionListener;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.entity.EntityManager;
import io.github.some_example_name.lingoman.LingoCollisionFilter;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.entity.GhostEntity;
import io.github.some_example_name.lingoman.entity.LetterEntity;
import io.github.some_example_name.lingoman.entity.PlayerEntity;
import io.github.some_example_name.lingoman.entity.WallEntity;
import io.github.some_example_name.lingoman.level.MazeLayout;
import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.lingoman.model.WordBank;
import io.github.some_example_name.managers.CollisionManager;
import io.github.some_example_name.managers.MovementManager;
import io.github.some_example_name.movement.Movable;
import io.github.some_example_name.movement.behaviour.FollowPathBehaviour;
import io.github.some_example_name.movement.behaviour.MovementBehaviour;
import io.github.some_example_name.movement.behaviour.PatrolBehaviour;
import io.github.some_example_name.movement.behaviour.SeekTargetBehaviour;
import io.github.some_example_name.scenes.Scene;

public class GameScene implements Scene {

    private static final float PLAYER_SPEED = 140f;
    private static final float GHOST_SPEED_EASY = 70f;
    private static final float GHOST_SPEED_MEDIUM = 85f;
    private static final float GHOST_SPEED_HARD = 95f;
    private static final float LETTER_SIZE = 22f;
    private static final float STATUS_MESSAGE_DURATION = 2.0f;
    private static final float INVULNERABLE_SECONDS = 1.25f;

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(1, 13);
    private static final GridPoint2[] GHOST_SPAWNS = {
        new GridPoint2(18, 1),
        new GridPoint2(18, 13),
        new GridPoint2(1, 1)
    };

    private EngineContext context;
    private final EntityManager entityManager = new EntityManager();
    private final CollisionManager collisionManager = new CollisionManager();
    private final MovementManager movementManager = new MovementManager();

    private final List<Collider> colliders = new ArrayList<>();
    private final Map<Entity, Collider> entityColliders = new IdentityHashMap<>();
    private final List<Movable> movables = new ArrayList<>();
    private final List<GhostEntity> ghosts = new ArrayList<>();

    private PlayerEntity player;
    private float invulnerableTimer = 0f;
    private String statusMessage = "";
    private float statusMessageTimer = 0f;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
        collisionManager.setFilter(new LingoCollisionFilter());
    }

    @Override
    public void enter() {
        startNewGame();
        System.out.println("[LingoMan] Game started");
    }

    @Override
    public void exit() {
        System.out.println("[LingoMan] Game exit");
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.MENU);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (statusMessageTimer > 0f) {
            statusMessageTimer = Math.max(0f, statusMessageTimer - deltaTime);
            if (statusMessageTimer == 0f) {
                statusMessage = "";
            }
        }
        if (invulnerableTimer > 0f) {
            invulnerableTimer = Math.max(0f, invulnerableTimer - deltaTime);
        }

        entityManager.update(deltaTime);
        movementManager.updateAll(deltaTime);
        collisionManager.update();

        GameState state = LingoSession.get().getGameState();
        if (state.hasCollectedAllLetters()) {
            state.addCompletedWord(state.getTargetWord());
            state.setLastResult("WORD COMPLETE");
            context.getSaveManager().save(LingoSession.SAVE_FILE);
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME_OVER);
        } else if (state.getLives() <= 0) {
            state.setLastResult("OUT OF LIVES");
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME_OVER);
        }
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(0.05f, 0.12f, 0.07f, 1f);
        entityManager.render(context.getOutputManager());

        GameState state = LingoSession.get().getGameState();
        context.getOutputManager().drawText("TARGET: " + state.getTargetWord(), 16f, 456f);
        context.getOutputManager().drawText("COLLECTED: " + state.getCollectedLettersDisplay(), 16f, 428f);
        context.getOutputManager().drawText("LIVES: " + state.getLives(), 16f, 400f);
        context.getOutputManager().drawText("DIFFICULTY: " + state.getDifficulty(), 16f, 372f);
        context.getOutputManager().drawText("MOVE: WASD/ARROWS", 16f, 320f);
        context.getOutputManager().drawText("ESC or M: MENU", 16f, 292f);
        if (!statusMessage.isBlank()) {
            context.getOutputManager().drawText("STATUS: " + statusMessage, 16f, 344f);
        }
    }

    @Override
    public void dispose() {
        clearWorld();
        System.out.println("[LingoMan] Game disposed");
    }

    private void startNewGame() {
        GameState state = LingoSession.get().getGameState();
        Random rng = LingoSession.get().getRandom();

        state.resetLives();
        state.setLastResult("");
        state.setTargetWord(WordBank.randomWord(state.getDifficulty(), rng));

        invulnerableTimer = 0f;
        statusMessage = "";
        statusMessageTimer = 0f;

        buildLevel(state);
    }

    private void buildLevel(GameState state) {
        clearWorld();

        buildWalls();
        buildPlayer();
        buildGhosts(state.getDifficulty());
        buildLetters(state.getTargetWord());
    }

    private void clearWorld() {
        for (Collider collider : colliders) {
            collisionManager.remove(collider);
        }
        colliders.clear();
        entityColliders.clear();

        for (Movable movable : movables) {
            movementManager.unregisterEntity(movable);
        }
        movables.clear();
        ghosts.clear();

        entityManager.clear();
    }

    private void buildWalls() {
        int id = 0;
        for (int row = 0; row < MazeLayout.ROWS; row++) {
            for (int col = 0; col < MazeLayout.COLS; col++) {
                if (!MazeLayout.isWall(col, row)) {
                    continue;
                }
                float x = MazeLayout.toWorldX(col);
                float y = MazeLayout.toWorldY(row);
                WallEntity wall = new WallEntity("wall_" + id++, x, y, MazeLayout.TILE_SIZE);
                entityManager.add(wall);
                registerCollider(wall, MazeLayout.TILE_SIZE, MazeLayout.TILE_SIZE, null);
            }
        }
    }

    private void buildPlayer() {
        float x = MazeLayout.toWorldXCentered(PLAYER_SPAWN.x, 28f);
        float y = MazeLayout.toWorldYCentered(PLAYER_SPAWN.y, 28f);
        player = new PlayerEntity("player", context.getInputManager(), x, y, PLAYER_SPEED);
        entityManager.add(player);
        registerMovable(player);
        registerCollider(player, player.getWidth(), player.getHeight(), new PlayerCollisionListener());
    }

    private void buildGhosts(GameState.Difficulty difficulty) {
        int ghostCount = switch (difficulty) {
            case MEDIUM -> 2;
            case HARD -> 3;
            default -> 1;
        };

        float speed = switch (difficulty) {
            case MEDIUM -> GHOST_SPEED_MEDIUM;
            case HARD -> GHOST_SPEED_HARD;
            default -> GHOST_SPEED_EASY;
        };

        List<Color> colors = List.of(
            new Color(1.0f, 0.35f, 0.30f, 1f),
            new Color(0.90f, 0.20f, 0.90f, 1f),
            new Color(0.35f, 0.90f, 0.45f, 1f)
        );

        for (int i = 0; i < ghostCount; i++) {
            GridPoint2 spawn = GHOST_SPAWNS[i % GHOST_SPAWNS.length];
            float x = MazeLayout.toWorldXCentered(spawn.x, 28f);
            float y = MazeLayout.toWorldYCentered(spawn.y, 28f);
            GhostEntity ghost = new GhostEntity("ghost_" + i, colors.get(i % colors.size()), x, y);
            ghosts.add(ghost);
            entityManager.add(ghost);
            registerMovable(ghost);
            movementManager.assignBehaviour(ghost, buildGhostBehaviour(i, speed));
            registerCollider(ghost, ghost.getWidth(), ghost.getHeight(), new EntityCollisionListenerAdapter(ghost));
        }
    }

    private void buildLetters(String targetWord) {
        if (targetWord == null || targetWord.isBlank()) {
            return;
        }

        List<GridPoint2> openCells = MazeLayout.collectOpenCells();
        openCells.removeIf(cell -> isSpawnCell(cell));
        Collections.shuffle(openCells, LingoSession.get().getRandom());

        int placed = 0;
        for (int i = 0; i < targetWord.length(); i++) {
            if (placed >= openCells.size()) {
                break;
            }
            char letter = targetWord.charAt(i);
            GridPoint2 cell = openCells.get(placed++);
            float x = MazeLayout.toWorldXCentered(cell.x, LETTER_SIZE);
            float y = MazeLayout.toWorldYCentered(cell.y, LETTER_SIZE);
            LetterEntity entity = new LetterEntity("letter_" + i, letter, x, y, LETTER_SIZE);
            entityManager.add(entity);
            registerCollider(entity, entity.getWidth(), entity.getHeight(), null);
        }
    }

    private boolean isSpawnCell(GridPoint2 cell) {
        if (cell == null) {
            return false;
        }
        if (cell.equals(PLAYER_SPAWN)) {
            return true;
        }
        for (GridPoint2 spawn : GHOST_SPAWNS) {
            if (cell.equals(spawn)) {
                return true;
            }
        }
        return false;
    }

    private MovementBehaviour buildGhostBehaviour(int index, float speed) {
        List<Vector2> route = List.of(
            new Vector2(MazeLayout.toWorldXCentered(3, 28f), MazeLayout.toWorldYCentered(3, 28f)),
            new Vector2(MazeLayout.toWorldXCentered(16, 28f), MazeLayout.toWorldYCentered(3, 28f)),
            new Vector2(MazeLayout.toWorldXCentered(16, 28f), MazeLayout.toWorldYCentered(11, 28f)),
            new Vector2(MazeLayout.toWorldXCentered(3, 28f), MazeLayout.toWorldYCentered(11, 28f))
        );

        return switch (index) {
            case 0 -> new SeekTargetBehaviour(player, speed, 6f);
            case 1 -> new PatrolBehaviour(route, speed * 0.9f, 6f);
            default -> new FollowPathBehaviour(route, speed * 0.85f, 6f, true);
        };
    }

    private void resetPositions() {
        player.setX(MazeLayout.toWorldXCentered(PLAYER_SPAWN.x, player.getWidth()));
        player.setY(MazeLayout.toWorldYCentered(PLAYER_SPAWN.y, player.getHeight()));
        player.setVx(0f);
        player.setVy(0f);

        for (int i = 0; i < ghosts.size(); i++) {
            GhostEntity ghost = ghosts.get(i);
            GridPoint2 spawn = GHOST_SPAWNS[i % GHOST_SPAWNS.length];
            ghost.setX(MazeLayout.toWorldXCentered(spawn.x, ghost.getWidth()));
            ghost.setY(MazeLayout.toWorldYCentered(spawn.y, ghost.getHeight()));
            ghost.setVx(0f);
            ghost.setVy(0f);
        }
    }

    private void registerMovable(Movable movable) {
        if (movable == null) {
            return;
        }
        movables.add(movable);
        movementManager.registerEntity(movable);
    }

    private void registerCollider(Entity owner, float width, float height, ICollisionListener listener) {
        Collider collider = new Collider(owner, width, height);
        collider.setListener(listener);
        collisionManager.add(collider);
        colliders.add(collider);
        entityColliders.put(owner, collider);
    }

    private void handlePlayerHit() {
        if (invulnerableTimer > 0f) {
            return;
        }
        GameState state = LingoSession.get().getGameState();
        state.loseLife();
        invulnerableTimer = INVULNERABLE_SECONDS;
        showStatus("Ouch! Lives left: " + state.getLives());
        resetPositions();
    }

    private void showStatus(String message) {
        statusMessage = message == null ? "" : message;
        statusMessageTimer = STATUS_MESSAGE_DURATION;
    }

    private final class PlayerCollisionListener implements ICollisionListener {

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
                letter.setActive(false);
                Collider letterCollider = entityColliders.remove(letter);
                if (letterCollider != null) {
                    collisionManager.remove(letterCollider);
                    colliders.remove(letterCollider);
                }
                showStatus("Collected: " + letter.getLetter());
            } else if (owner instanceof GhostEntity) {
                handlePlayerHit();
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
