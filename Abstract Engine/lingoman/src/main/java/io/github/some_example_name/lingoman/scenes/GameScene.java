package io.github.some_example_name.lingoman.scenes;

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
import io.github.some_example_name.lingoman.LingoSceneIds;
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
import io.github.some_example_name.movement.behaviour.MovementBehaviour;
import io.github.some_example_name.scenes.Scene;

public class GameScene implements Scene {

    private static final String PROFILE_FILE = "lingoman_progress.json";

    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.72f, 0.77f, 0.79f, 1f);
    private static final Color TEXT_ACCENT = new Color(0.98f, 0.84f, 0.36f, 1f);
    private static final Color TEXT_WARNING = new Color(1.00f, 0.64f, 0.47f, 1f);

    private static final float STATUS_MESSAGE_DURATION = 2.0f;
    private static final float INVULNERABLE_SECONDS = 1.25f;

    private EngineContext context;
    private final LingoWorld world = new LingoWorld();
    private final List<GhostEntity> ghosts = new ArrayList<>();

    private MazeLayout.Layout currentLayout;
    private PlayerEntity player;
    private float invulnerableTimer = 0f;
    private String statusMessage = "";
    private float statusMessageTimer = 0f;
    private boolean moveLoopPlaying = false;

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        if (LingoSession.get().consumeGameResumeRequest()) {
            moveLoopPlaying = false;
            context.getAudioManager().playMusic(LingoAudio.BGM_GAME, true);
            System.out.println("[LingoMan] Game resumed");
            return;
        }

        startNewGame();
        moveLoopPlaying = false;
        context.getAudioManager().playMusic(LingoAudio.BGM_GAME, true);
        System.out.println("[LingoMan] Game started");
    }

    @Override
    public void exit() {
        stopMovementAudio();
        context.getAudioManager().stopMusic();
        System.out.println("[LingoMan] Game exit");
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.GAME_MENU)) {
            context.getSceneManager().setActiveScene(LingoSceneIds.PAUSE);
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

        world.update(deltaTime);
        wrapTunnelTravellers();
        updateMovementAudio();

        GameState state = LingoSession.get().getGameState();
        if (state.hasCollectedAllLetters()) {
            stopGameplayAudio();
            state.addFoundWord(state.getTargetWord());
            context.getSaveManager().save(PROFILE_FILE);
            state.setLastResult("WORD COMPLETE");
            context.getAudioManager().playSound(LingoAudio.SFX_VICTORY, false);
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME_OVER);
        } else if (state.getLives() <= 0) {
            stopGameplayAudio();
            state.setLastResult("OUT OF LIVES");
            context.getAudioManager().playSound(LingoAudio.SFX_GAME_OVER, false);
            context.getSceneManager().setActiveScene(LingoSceneIds.GAME_OVER);
        }
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(0.05f, 0.12f, 0.07f, 1f);
        world.render(context.getOutputManager());

        GameState state = LingoSession.get().getGameState();
        context.getOutputManager().drawTextWithShadow("Target: " + state.getTargetWord(), 28f, 474f, TEXT_ACCENT);
        context.getOutputManager().drawTextWithShadow("Progress: " + state.getCollectedLettersDisplay(), 28f, 454f, TEXT_PRIMARY);

        context.getOutputManager().drawTextWithShadow("Lives: " + state.getLives(), 432f, 474f,
            state.getLives() <= 1 ? TEXT_WARNING : TEXT_PRIMARY);
        context.getOutputManager().drawTextWithShadow("Mode: " + state.getDifficulty(), 432f, 454f, TEXT_PRIMARY);

        context.getOutputManager().drawTextWithShadow("Move: WASD / Arrows", 28f, 18f, TEXT_PRIMARY);
        context.getOutputManager().drawTextWithShadow("Menu: M or ESC", 236f, 18f, TEXT_MUTED);

        if (!statusMessage.isBlank()) {
            context.getOutputManager().drawTextRightAlignedWithShadow(statusMessage, 610f, 18f, TEXT_WARNING);
        }
    }

    @Override
    public void dispose() {
        stopGameplayAudio();
        clearWorld();
        System.out.println("[LingoMan] Game disposed");
    }

    private void startNewGame() {
        GameState state = LingoSession.get().getGameState();
        Random rng = LingoSession.get().getRandom();

        state.resetLives();
        state.setLastResult("");
        state.setTargetWord(WordBank.randomWord(state.getDifficulty(), rng));

        currentLayout = MazeLayout.forDifficulty(state.getDifficulty());
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
        ghosts.clear();
        world.clear();
    }

    private void buildWalls() {
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

    private void buildPlayer() {
        GridPoint2 spawn = currentLayout.getPlayerSpawn();
        float size = entitySize();
        float x = MazeLayout.toWorldXCentered(currentLayout, spawn.x, size);
        float y = MazeLayout.toWorldYCentered(currentLayout, spawn.y, size);
        player = new PlayerEntity("player", context.getInputManager(), x, y, playerSpeed(), size);
        world.addCollidableEntity(player, new PlayerCollisionListener());
    }

    private void buildGhosts(GameState.Difficulty difficulty) {
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

    private void handlePlayerHit() {
        if (invulnerableTimer > 0f) {
            return;
        }

        GameState state = LingoSession.get().getGameState();
        state.loseLife();
        invulnerableTimer = INVULNERABLE_SECONDS;
        showStatus("Ouch! Lives left: " + state.getLives());
        context.getAudioManager().playSound(LingoAudio.SFX_HURT, false);
        resetPositions();
    }

    private void updateMovementAudio() {
        boolean shouldPlayMoveLoop = isMovementInputPressed();
        if (shouldPlayMoveLoop == moveLoopPlaying) {
            return;
        }

        moveLoopPlaying = shouldPlayMoveLoop;
        if (moveLoopPlaying) {
            context.getAudioManager().playLoopingSound(LingoAudio.SFX_MOVE);
        } else {
            context.getAudioManager().stopLoopingSound(LingoAudio.SFX_MOVE);
        }
    }

    private boolean isMovementInputPressed() {
        return context.getInputManager().isActionPressed(LingoInputActions.MOVE_LEFT)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_RIGHT)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_UP)
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_DOWN);
    }

    private void stopMovementAudio() {
        moveLoopPlaying = false;
        context.getAudioManager().stopLoopingSound(LingoAudio.SFX_MOVE);
    }

    private void stopGameplayAudio() {
        stopMovementAudio();
        context.getAudioManager().stopMusic();
    }

    private void wrapTunnelTravellers() {
        wrapTunnelTraveller(player);
        for (GhostEntity ghost : ghosts) {
            wrapTunnelTraveller(ghost);
        }
    }

    private void wrapTunnelTraveller(io.github.some_example_name.entity.Entity entity) {
        if (entity == null || currentLayout == null) {
            return;
        }

        float wrappedX = MazeLayout.wrapTunnelX(
            currentLayout, entity.getX(), entity.getY(), entity.getWidth(), entity.getHeight());
        if (wrappedX != entity.getX()) {
            entity.setX(wrappedX);
        }
    }

    private void showStatus(String message) {
        statusMessage = message == null ? "" : message;
        statusMessageTimer = STATUS_MESSAGE_DURATION;
    }

    private float entitySize() {
        return currentLayout.getTileSize() * 0.72f;
    }

    private float letterSize() {
        return currentLayout.getTileSize() * 0.55f;
    }

    private float playerSpeed() {
        return currentLayout.getTileSize() * 5.0f;
    }

    private float ghostSpeed(GameState.Difficulty difficulty) {
        float scale = switch (difficulty) {
            case MEDIUM -> 3.0f;
            case HARD -> 3.35f;
            default -> 2.6f;
        };
        return currentLayout.getTileSize() * scale;
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
                context.getAudioManager().playSound(LingoAudio.SFX_COLLECT_LETTER, false);
                world.removeEntity(letter);
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
