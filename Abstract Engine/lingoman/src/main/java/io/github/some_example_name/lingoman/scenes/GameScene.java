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
import io.github.some_example_name.lingoman.LingoAudio;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.LingoSession;
import io.github.some_example_name.lingoman.entity.BossFireballEntity;
import io.github.some_example_name.lingoman.entity.FreezePickupEntity;
import io.github.some_example_name.lingoman.entity.GhostEntity;
import io.github.some_example_name.lingoman.entity.LetterEntity;
import io.github.some_example_name.lingoman.entity.PlayerEntity;
import io.github.some_example_name.lingoman.entity.ShockPickupEntity;
import io.github.some_example_name.lingoman.entity.WallBombEntity;
import io.github.some_example_name.lingoman.entity.WallEntity;
import io.github.some_example_name.lingoman.level.MazeLayout;
import io.github.some_example_name.lingoman.model.GameState;
import io.github.some_example_name.lingoman.model.WordBank;
import io.github.some_example_name.lingoman.movement.MazeSeekBehaviour;
import io.github.some_example_name.lingoman.movement.MazeWanderBehaviour;
import io.github.some_example_name.lingoman.world.LingoWorld;
import io.github.some_example_name.movement.behaviour.MovementBehaviour;
import io.github.some_example_name.scenes.Scene;

public class GameScene implements Scene {

    private static final Color BACKGROUND_OUTER = new Color(0f, 0f, 0f, 1f);

    private enum GhostMovePattern {
        SEEK,
        WANDER,
        STATIONARY
    }

    private enum ShotPattern {
        NONE,
        BOMB_DROP,
        FIREBALL
    }

    private static final class GhostLoadout {
        private final GhostEntity.GhostType type;
        private final GhostMovePattern movePattern;
        private final Color color;
        private final float speedMultiplier;
        private final float sizeMultiplier;
        private final ShotPattern shotPattern;
        private final float shotCooldownSeconds;

        private GhostLoadout(
            GhostEntity.GhostType type,
            GhostMovePattern movePattern,
            Color color,
            float speedMultiplier,
            float sizeMultiplier,
            ShotPattern shotPattern,
            float shotCooldownSeconds
        ) {
            this.type = type;
            this.movePattern = movePattern;
            this.color = color == null ? new Color(1f, 0.3f, 0.3f, 1f) : new Color(color);
            this.speedMultiplier = speedMultiplier;
            this.sizeMultiplier = sizeMultiplier;
            this.shotPattern = shotPattern;
            this.shotCooldownSeconds = shotCooldownSeconds;
        }

        private boolean canShoot() {
            return shotPattern != ShotPattern.NONE;
        }
    }

    private static final String PROFILE_FILE = "lingoman_progress.json";

    private static final Color TEXT_PRIMARY = new Color(0.96f, 0.96f, 0.92f, 1f);
    private static final Color TEXT_MUTED = new Color(0.72f, 0.77f, 0.79f, 1f);
    private static final Color TEXT_ACCENT = new Color(0.98f, 0.84f, 0.36f, 1f);
    private static final Color TEXT_WARNING = new Color(1.00f, 0.64f, 0.47f, 1f);

    private static final float STATUS_MESSAGE_DURATION = 2.0f;
    private static final float RESPAWN_IMMUNITY_SECONDS = 5.0f;
    private static final float BOMB_THROW_SECONDS = 0.82f;
    private static final float BOMB_BLAST_SECONDS = 0.18f;
    private static final float FIREBALL_SPEED = 182f;
    private static final float FIREBALL_LIFETIME_SECONDS = 1.55f;
    private static final float FIREBALL_ATTACK_RANGE_TILES = 6.0f;
    private static final float BOMB_TARGET_PLAYER_CHANCE = 0.70f;
    private static final int BOMB_NEAR_PLAYER_RADIUS = 3;
    private static final float FREEZE_SECONDS = 4.5f;
    private static final float SHOCK_SECONDS = 6.0f;

    private EngineContext context;
    private final LingoWorld world = new LingoWorld();
    private final List<GhostEntity> ghosts = new ArrayList<>();
    private final List<WallBombEntity> wallBombs = new ArrayList<>();
    private final List<BossFireballEntity> bossFireballs = new ArrayList<>();
    private final Map<GhostEntity, Float> ghostShotCooldowns = new IdentityHashMap<>();
    private final Map<GhostEntity, GhostLoadout> ghostLoadouts = new IdentityHashMap<>();
    private final Map<GhostEntity, GridPoint2> ghostSpawnCells = new IdentityHashMap<>();
    private final List<GridPoint2> bombDropCells = new ArrayList<>();

    private MazeLayout.Layout currentLayout;
    private PlayerEntity player;
    private FreezePickupEntity freezePickup;
    private ShockPickupEntity shockPickup;
    private float invulnerableTimer = 0f;
    private String statusMessage = "";
    private float statusMessageTimer = 0f;
    private boolean moveLoopPlaying = false;
    private int bombSequence = 0;
    private int fireballSequence = 0;

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
        context.getOutputManager().clearScreen(BACKGROUND_OUTER.r, BACKGROUND_OUTER.g, BACKGROUND_OUTER.b, BACKGROUND_OUTER.a);
        world.render(context.getOutputManager());

        GameState state = LingoSession.get().getGameState();

        context.getOutputManager().drawTextWithShadow("Progress: " + state.getCollectedLettersDisplay(), 28f, 474f, TEXT_PRIMARY);
        char nextLetter = state.getNextExpectedLetter();
        String nextHint = nextLetter == '\0' ? "Next: -" : "Next: " + nextLetter;
        context.getOutputManager().drawTextWithShadow(nextHint, 28f, 454f, TEXT_WARNING);

        context.getOutputManager().drawTextWithShadow("Lives: " + state.getLives(), 432f, 474f,
            state.getLives() <= 1 ? TEXT_WARNING : TEXT_PRIMARY);
        context.getOutputManager().drawTextWithShadow("Mode: " + state.getDifficulty(), 432f, 454f, TEXT_PRIMARY);

        context.getOutputManager().drawTextWithShadow("Move: WASD / Arrows", 28f, 18f, TEXT_PRIMARY);
        context.getOutputManager().drawTextWithShadow("Dash: SPACE", 246f, 18f, TEXT_PRIMARY);
        context.getOutputManager().drawTextRightAlignedWithShadow("Menu: M or ESC", 610f, 18f, TEXT_MUTED);

        if (!statusMessage.isBlank()) {
            context.getOutputManager().drawTextCenteredScaled(
                statusMessage,
                320f,
                474f,
                TEXT_WARNING,
                0.8f
            );
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
        bombDropCells.addAll(collectBombDropCells());
        List<GridPoint2> itemCells = collectItemCells();
        buildLetters(state.getTargetWord(), itemCells);
        buildFreezePickup(itemCells);
        buildShockPickup(itemCells);
    }

    private void clearWorld() {
        ghosts.clear();
        wallBombs.clear();
        bossFireballs.clear();
        ghostShotCooldowns.clear();
        ghostLoadouts.clear();
        ghostSpawnCells.clear();
        bombDropCells.clear();
        freezePickup = null;
        shockPickup = null;
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
        float speed = ghostSpeed(difficulty);
        GridPoint2[] spawns = currentLayout.getGhostSpawns();
        List<GhostLoadout> loadouts = buildGhostLoadouts(difficulty);
        int pathSpawnIndex = 0;

        for (int i = 0; i < loadouts.size(); i++) {
            GhostLoadout loadout = loadouts.get(i);
            GridPoint2 spawn = loadout.type == GhostEntity.GhostType.WALL_BOMBER
                ? findWallBomberCell()
                : spawns[pathSpawnIndex++ % spawns.length];
            float size = entitySize() * loadout.sizeMultiplier;
            float x = MazeLayout.toWorldXCentered(currentLayout, spawn.x, size);
            float y = MazeLayout.toWorldYCentered(currentLayout, spawn.y, size);
            GhostEntity ghost = new GhostEntity("ghost_" + i, loadout.type, loadout.color, x, y, size);
            ghosts.add(ghost);
            ghostLoadouts.put(ghost, loadout);
            ghostSpawnCells.put(ghost, new GridPoint2(spawn));
            if (loadout.type == GhostEntity.GhostType.WALL_BOMBER) {
                world.addEntity(ghost);
            } else {
                world.addCollidableEntity(ghost, new EntityCollisionListenerAdapter(ghost));
            }
            MovementBehaviour behaviour = buildGhostBehaviour(loadout, speed);
            if (behaviour != null) {
                world.assignBehaviour(ghost, behaviour);
            }
            if (loadout.canShoot()) {
                ghostShotCooldowns.put(ghost, initialShotCooldown(loadout, ghostShotCooldowns.size()));
            }
        }
    }

    private List<GridPoint2> collectItemCells() {
        List<GridPoint2> openCells = MazeLayout.collectReachableOpenCells(currentLayout, currentLayout.getPlayerSpawn());
        openCells.removeIf(this::isSpawnCell);
        Collections.shuffle(openCells, LingoSession.get().getRandom());
        return openCells;
    }

    private List<GridPoint2> collectBombDropCells() {
        List<GridPoint2> openCells = MazeLayout.collectReachableOpenCells(currentLayout, currentLayout.getPlayerSpawn());
        openCells.removeIf(this::isSpawnCell);
        return openCells;
    }

    private void buildLetters(String targetWord, List<GridPoint2> openCells) {
        if (targetWord == null || targetWord.isBlank()) {
            return;
        }

        float size = letterSize();
        for (int i = 0; i < targetWord.length(); i++) {
            if (openCells == null || openCells.isEmpty()) {
                break;
            }

            char letter = targetWord.charAt(i);
            GridPoint2 cell = openCells.remove(0);
            float x = MazeLayout.toWorldXCentered(currentLayout, cell.x, size);
            float y = MazeLayout.toWorldYCentered(currentLayout, cell.y, size);
            LetterEntity entity = new LetterEntity("letter_" + i, letter, x, y, size);
            world.addCollidableEntity(entity, null);
        }
    }

    private void buildFreezePickup(List<GridPoint2> openCells) {
        if (openCells == null || openCells.isEmpty()) {
            freezePickup = null;
            return;
        }

        float size = Math.max(currentLayout.getTileSize() * 0.62f, 14f);
        GridPoint2 cell = openCells.remove(0);
        float x = MazeLayout.toWorldXCentered(currentLayout, cell.x, size);
        float y = MazeLayout.toWorldYCentered(currentLayout, cell.y, size);
        freezePickup = new FreezePickupEntity("freeze_pickup", x, y, size);
        world.addCollidableEntity(freezePickup, null);
    }

    private void buildShockPickup(List<GridPoint2> openCells) {
        if (openCells == null || openCells.isEmpty()) {
            shockPickup = null;
            return;
        }

        float size = Math.max(currentLayout.getTileSize() * 0.62f, 14f);
        GridPoint2 cell = openCells.remove(0);
        float x = MazeLayout.toWorldXCentered(currentLayout, cell.x, size);
        float y = MazeLayout.toWorldYCentered(currentLayout, cell.y, size);
        shockPickup = new ShockPickupEntity("shock_pickup", x, y, size);
        world.addCollidableEntity(shockPickup, null);
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

    private GridPoint2 findWallBomberCell() {
        int centerCol = currentLayout.getCols() / 2;
        int centerRow = currentLayout.getRows() / 2;
        GridPoint2 best = null;
        int bestScore = Integer.MAX_VALUE;

        for (int row = 1; row < currentLayout.getRows() - 1; row++) {
            for (int col = 1; col < currentLayout.getCols() - 1; col++) {
                if (!MazeLayout.isWall(currentLayout, col, row)) {
                    continue;
                }
                if (!hasAdjacentOpenCell(col, row)) {
                    continue;
                }

                int score = Math.abs(col - centerCol) * 2 + Math.abs(row - centerRow);
                if (score < bestScore) {
                    bestScore = score;
                    best = new GridPoint2(col, row);
                }
            }
        }

        return best == null ? new GridPoint2(centerCol, centerRow) : best;
    }

    private boolean hasAdjacentOpenCell(int col, int row) {
        return MazeLayout.isOpen(currentLayout, col + 1, row)
            || MazeLayout.isOpen(currentLayout, col - 1, row)
            || MazeLayout.isOpen(currentLayout, col, row + 1)
            || MazeLayout.isOpen(currentLayout, col, row - 1);
    }

    private MovementBehaviour buildGhostBehaviour(GhostLoadout loadout, float speed) {
        float threshold = Math.max(2f, currentLayout.getTileSize() * 0.22f);
        float adjustedSpeed = speed * (loadout == null ? 1f : loadout.speedMultiplier);
        GhostMovePattern pattern = loadout == null ? GhostMovePattern.SEEK : loadout.movePattern;
        return switch (pattern) {
            case WANDER -> new MazeWanderBehaviour(currentLayout, adjustedSpeed);
            case STATIONARY -> null;
            default -> new MazeSeekBehaviour(currentLayout, player, adjustedSpeed);
        };
    }

    private void resetPositions() {
        GridPoint2 playerSpawn = currentLayout.getPlayerSpawn();
        player.setX(MazeLayout.toWorldXCentered(currentLayout, playerSpawn.x, player.getWidth()));
        player.setY(MazeLayout.toWorldYCentered(currentLayout, playerSpawn.y, player.getHeight()));
        player.setVx(0f);
        player.setVy(0f);
        player.clearDash();
        player.clearShock();
        player.clearRespawnProtection();
        player.setRespawnProtection(RESPAWN_IMMUNITY_SECONDS);

        clearBombs();
        clearFireballs();

        for (GhostEntity ghost : ghosts) {
            resetGhostToSpawn(ghost);
        }
        resetGhostShotCooldowns();
    }

    private void respawnGhost(GhostEntity ghost) {
        resetGhostToSpawn(ghost);
        ghost.setRespawnProtection(RESPAWN_IMMUNITY_SECONDS);

        GhostLoadout loadout = ghostLoadouts.get(ghost);
        if (loadout != null && loadout.canShoot()) {
            ghostShotCooldowns.put(ghost, shotCooldown(loadout));
        }
    }

    private void resetGhostToSpawn(GhostEntity ghost) {
        if (ghost == null) {
            return;
        }

        GridPoint2 spawn = ghostSpawnCells.get(ghost);
        if (spawn == null) {
            return;
        }

        ghost.setX(MazeLayout.toWorldXCentered(currentLayout, spawn.x, ghost.getWidth()));
        ghost.setY(MazeLayout.toWorldYCentered(currentLayout, spawn.y, ghost.getHeight()));
        ghost.setVx(0f);
        ghost.setVy(0f);
        ghost.clearFreeze();
        ghost.clearThrowAnimation();
        ghost.clearRespawnProtection();
        refreshGhostBehaviour(ghost);
    }

    private void refreshGhostBehaviour(GhostEntity ghost) {
        if (ghost == null) {
            return;
        }

        MovementBehaviour behaviour = buildGhostBehaviour(
            ghostLoadouts.get(ghost),
            ghostSpeed(LingoSession.get().getGameState().getDifficulty())
        );
        if (behaviour != null) {
            world.assignBehaviour(ghost, behaviour);
        } else {
            ghost.setMovementBehaviour(null);
        }
    }

    private void handlePlayerHit() {
        if (invulnerableTimer > 0f) {
            return;
        }

        GameState state = LingoSession.get().getGameState();
        state.loseLife();
        invulnerableTimer = RESPAWN_IMMUNITY_SECONDS;
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
        return (player != null && player.isDashing())
            || context.getInputManager().isActionPressed(LingoInputActions.MOVE_LEFT)
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
        return currentLayout.getTileSize() * 0.78f;
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

    private void updateGhostShooting(float deltaTime) {
        if (ghostShotCooldowns.isEmpty() || invulnerableTimer > 0f || player == null) {
            return;
        }

        for (GhostEntity ghost : ghosts) {
            GhostLoadout loadout = ghostLoadouts.get(ghost);
            if (!ghost.isActive() || ghost.isFrozen() || loadout == null || !ghostShotCooldowns.containsKey(ghost)) {
                continue;
            }

            float remaining = ghostShotCooldowns.get(ghost) - deltaTime;
            if (remaining > 0f) {
                ghostShotCooldowns.put(ghost, remaining);
                continue;
            }

            boolean spawned = spawnGhostAttack(ghost, loadout);
            ghostShotCooldowns.put(ghost, spawned ? shotCooldown(loadout) : 0.45f);
            if (spawned) {
                break;
            }
        }
    }

    private boolean spawnGhostAttack(GhostEntity ghost, GhostLoadout loadout) {
        if (ghost == null || loadout == null || !loadout.canShoot() || player == null) {
            return false;
        }
        if (loadout.shotPattern == ShotPattern.BOMB_DROP) {
            return spawnWallBomb(ghost);
        }
        if (loadout.shotPattern == ShotPattern.FIREBALL) {
            return spawnBossFireball(ghost);
        }
        return false;
    }

    private boolean spawnWallBomb(GhostEntity bomber) {
        if (bomber == null || bombDropCells.isEmpty() || hasActiveWallBomb()) {
            return false;
        }

        GridPoint2 targetCell = selectBombTargetCell();
        if (targetCell == null) {
            return false;
        }

        float bombSize = Math.max(currentLayout.getTileSize() * 0.34f, 9f);
        float blastSize = Math.max(currentLayout.getTileSize() * 0.84f, 16f);
        Vector2 targetCenter = MazeLayout.toCellCenter(currentLayout, targetCell);
        Vector2 throwOrigin = bomber.getThrowOrigin(targetCenter.x, targetCenter.y);
        float startCenterX = throwOrigin.x;
        float startCenterY = throwOrigin.y;

        bomber.playThrowAnimation(BOMB_THROW_SECONDS, targetCenter.x, targetCenter.y);

        WallBombEntity bomb = new WallBombEntity(
            "wall_bomb_" + bombSequence++,
            startCenterX,
            startCenterY,
            targetCenter.x,
            targetCenter.y,
            bombSize,
            blastSize,
            BOMB_THROW_SECONDS,
            BOMB_BLAST_SECONDS
        );
        wallBombs.add(bomb);
        world.addCollidableEntity(bomb, new WallBombCollisionListener(bomb));
        return true;
    }

    private boolean spawnBossFireball(GhostEntity boss) {
        if (boss == null || player == null || hasActiveBossFireball()) {
            return false;
        }

        float playerCenterX = player.getX() + player.getWidth() * 0.5f;
        float playerCenterY = player.getY() + player.getHeight() * 0.5f;
        float bossCenterX = boss.getX() + boss.getWidth() * 0.5f;
        float bossCenterY = boss.getY() + boss.getHeight() * 0.5f;
        Vector2 direction = new Vector2(playerCenterX - bossCenterX, playerCenterY - bossCenterY);
        float targetDistance = direction.len();
        float attackRange = currentLayout.getTileSize() * FIREBALL_ATTACK_RANGE_TILES;
        if (targetDistance > attackRange) {
            return false;
        }
        if (direction.isZero(0.001f)) {
            direction.set(1f, 0f);
        } else {
            direction.nor();
        }

        float startCenterX = bossCenterX + direction.x * boss.getWidth() * 0.40f;
        float startCenterY = bossCenterY + direction.y * boss.getHeight() * 0.24f;
        float fireballSize = Math.max(currentLayout.getTileSize() * 0.42f, 12f);
        float fireballSpeed = Math.max(FIREBALL_SPEED, currentLayout.getTileSize() * 6.4f);

        BossFireballEntity fireball = new BossFireballEntity(
            "boss_fireball_" + fireballSequence++,
            startCenterX,
            startCenterY,
            playerCenterX,
            playerCenterY,
            fireballSize,
            fireballSpeed,
            FIREBALL_LIFETIME_SECONDS
        );
        bossFireballs.add(fireball);
        world.addCollidableEntity(fireball, new BossFireballCollisionListener(fireball));
        return true;
    }

    private GridPoint2 selectBombTargetCell() {
        Random rng = LingoSession.get().getRandom();
        GridPoint2 playerCell = player == null
            ? null
            : MazeLayout.toCell(currentLayout, player.getX(), player.getY(), player.getWidth());
        boolean canTargetPlayer = playerCell != null && MazeLayout.isOpen(currentLayout, playerCell.x, playerCell.y);

        if (canTargetPlayer && rng.nextFloat() < BOMB_TARGET_PLAYER_CHANCE) {
            return new GridPoint2(playerCell);
        }

        GridPoint2 nearbyPathCell = chooseNearbyBombTargetCell(playerCell, rng);
        if (nearbyPathCell != null) {
            return nearbyPathCell;
        }

        GridPoint2 randomOpenCell = chooseRandomBombTargetCell(playerCell, rng);
        return randomOpenCell != null ? randomOpenCell : (canTargetPlayer ? new GridPoint2(playerCell) : null);
    }

    private GridPoint2 chooseNearbyBombTargetCell(GridPoint2 playerCell, Random rng) {
        if (playerCell == null) {
            return null;
        }

        List<GridPoint2> nearbyCells = new ArrayList<>();
        List<GridPoint2> closestCells = new ArrayList<>();
        int closestDistance = Integer.MAX_VALUE;

        for (GridPoint2 cell : bombDropCells) {
            if (cell == null || cell.equals(playerCell) || !MazeLayout.isOpen(currentLayout, cell.x, cell.y)) {
                continue;
            }

            int distance = manhattanDistance(cell, playerCell);
            if (distance <= BOMB_NEAR_PLAYER_RADIUS) {
                nearbyCells.add(cell);
            }
            if (distance < closestDistance) {
                closestDistance = distance;
                closestCells.clear();
                closestCells.add(cell);
            } else if (distance == closestDistance) {
                closestCells.add(cell);
            }
        }

        if (!nearbyCells.isEmpty()) {
            GridPoint2 chosen = nearbyCells.get(rng.nextInt(nearbyCells.size()));
            return new GridPoint2(chosen);
        }
        if (!closestCells.isEmpty()) {
            GridPoint2 chosen = closestCells.get(rng.nextInt(closestCells.size()));
            return new GridPoint2(chosen);
        }
        return null;
    }

    private GridPoint2 chooseRandomBombTargetCell(GridPoint2 excludedCell, Random rng) {
        List<GridPoint2> shuffled = new ArrayList<>(bombDropCells);
        Collections.shuffle(shuffled, rng);
        for (GridPoint2 cell : shuffled) {
            if (cell == null || !MazeLayout.isOpen(currentLayout, cell.x, cell.y)) {
                continue;
            }
            if (excludedCell != null && shuffled.size() > 1 && excludedCell.equals(cell)) {
                continue;
            }
            return new GridPoint2(cell);
        }
        return null;
    }

    private int manhattanDistance(GridPoint2 a, GridPoint2 b) {
        if (a == null || b == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    private void cleanupInactiveBombs() {
        if (wallBombs.isEmpty()) {
            return;
        }

        List<WallBombEntity> expired = new ArrayList<>();
        for (WallBombEntity bomb : wallBombs) {
            if (bomb == null || bomb.isActive()) {
                continue;
            }

            expired.add(bomb);
            world.removeEntity(bomb);
        }

        if (!expired.isEmpty()) {
            wallBombs.removeAll(expired);
        }
    }

    private void cleanupInactiveFireballs() {
        if (bossFireballs.isEmpty()) {
            return;
        }

        List<BossFireballEntity> expired = new ArrayList<>();
        for (BossFireballEntity fireball : bossFireballs) {
            if (fireball == null || fireball.isActive()) {
                continue;
            }

            expired.add(fireball);
            world.removeEntity(fireball);
        }

        if (!expired.isEmpty()) {
            bossFireballs.removeAll(expired);
        }
    }

    private boolean hasActiveWallBomb() {
        for (WallBombEntity bomb : wallBombs) {
            if (bomb != null && bomb.isActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasActiveBossFireball() {
        for (BossFireballEntity fireball : bossFireballs) {
            if (fireball != null && fireball.isActive()) {
                return true;
            }
        }
        return false;
    }

    private void clearBombs() {
        if (wallBombs.isEmpty()) {
            return;
        }

        for (WallBombEntity bomb : new ArrayList<>(wallBombs)) {
            bomb.expire();
            world.removeEntity(bomb);
        }
        wallBombs.clear();
    }

    private void clearFireballs() {
        if (bossFireballs.isEmpty()) {
            return;
        }

        for (BossFireballEntity fireball : new ArrayList<>(bossFireballs)) {
            fireball.expire();
            world.removeEntity(fireball);
        }
        bossFireballs.clear();
    }

    private void resetGhostShotCooldowns() {
        ghostShotCooldowns.clear();
        int shooterIndex = 0;
        for (GhostEntity ghost : ghosts) {
            GhostLoadout loadout = ghostLoadouts.get(ghost);
            if (loadout == null || !loadout.canShoot()) {
                continue;
            }
            ghostShotCooldowns.put(ghost, initialShotCooldown(loadout, shooterIndex++));
        }
    }

    private float initialShotCooldown(GhostLoadout loadout, int shooterIndex) {
        return shotCooldown(loadout) + shooterIndex * 0.6f;
    }

    private float shotCooldown(GhostLoadout loadout) {
        return loadout == null ? Float.POSITIVE_INFINITY : loadout.shotCooldownSeconds;
    }

    private List<GhostLoadout> buildGhostLoadouts(GameState.Difficulty difficulty) {
        return switch (difficulty) {
            case MEDIUM -> List.of(
                new GhostLoadout(
                    GhostEntity.GhostType.NORMAL,
                    GhostMovePattern.SEEK,
                    new Color(1.00f, 0.35f, 0.30f, 1f),
                    1.00f,
                    1.05f,
                    ShotPattern.NONE,
                    0f
                ),
                new GhostLoadout(
                    GhostEntity.GhostType.WALL_BOMBER,
                    GhostMovePattern.STATIONARY,
                    new Color(0.65f, 0.48f, 0.78f, 1f),
                    0f,
                    1.04f,
                    ShotPattern.BOMB_DROP,
                    3.10f
                )
            );
            case HARD -> List.of(
                new GhostLoadout(
                    GhostEntity.GhostType.BOSS,
                    GhostMovePattern.SEEK,
                    new Color(0.98f, 0.24f, 0.18f, 1f),
                    1.12f,
                    1.28f,
                    ShotPattern.FIREBALL,
                    2.20f
                ),
                new GhostLoadout(
                    GhostEntity.GhostType.NORMAL,
                    GhostMovePattern.WANDER,
                    new Color(0.67f, 0.47f, 0.82f, 1f),
                    0.96f,
                    1.06f,
                    ShotPattern.NONE,
                    0f
                ),
                new GhostLoadout(
                    GhostEntity.GhostType.WALL_BOMBER,
                    GhostMovePattern.STATIONARY,
                    new Color(0.65f, 0.48f, 0.78f, 1f),
                    0f,
                    1.02f,
                    ShotPattern.BOMB_DROP,
                    3.10f
                )
            );
            default -> List.of(
                new GhostLoadout(
                    GhostEntity.GhostType.NORMAL,
                    GhostMovePattern.SEEK,
                    new Color(1.00f, 0.35f, 0.30f, 1f),
                    1.00f,
                    1.05f,
                    ShotPattern.NONE,
                    0f
                )
            );
        };
    }

    private void activateFreeze() {
        for (GhostEntity ghost : ghosts) {
            ghost.freezeFor(FREEZE_SECONDS);
        }
        showStatus("Freeze!");
    }

    private void activateShock() {
        if (player == null) {
            return;
        }
        player.activateShock(SHOCK_SECONDS);
        showStatus("Lightning!");
    }

    private void handlePlayerGhostContact(GhostEntity ghost) {
        if (ghost == null || !ghost.isActive()) {
            return;
        }
        if (player != null && player.hasShockPower()) {
            if (!ghost.isRespawnProtected()) {
                respawnGhost(ghost);
                showStatus(ghost.isBoss() ? "Boss electrocuted!" : "Ghost electrocuted!");
            }
            return;
        }
        if (ghost.isFrozen()) {
            return;
        }
        handlePlayerHit();
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

                GameState state = LingoSession.get().getGameState();
                char picked = Character.toUpperCase(letter.getLetter());

                if (state.collectNextLetter(picked)) {
                    context.getAudioManager().playSound(LingoAudio.SFX_COLLECT_LETTER, false);
                    world.removeEntity(letter);
                    showStatus("Correct: " + picked);
                } else {
                    context.getAudioManager().playSound(LingoAudio.SFX_WRONG_LETTER, false);
                    letter.triggerWrongFlash();

                    char expected = state.getNextExpectedLetter();
                    if (expected != '\0') {
                        showStatus("Wrong letter, find: " + expected);
                    } else {
                        showStatus("Wrong letter");
                    }
                }
            } else if (owner instanceof FreezePickupEntity pickup) {
                if (!pickup.isActive()) {
                    return;
                }

                world.removeEntity(pickup);
                freezePickup = null;
                activateFreeze();
            } else if (owner instanceof ShockPickupEntity pickup) {
                if (!pickup.isActive()) {
                    return;
                }

                world.removeEntity(pickup);
                shockPickup = null;
                activateShock();
            } else if (owner instanceof GhostEntity ghost) {
                handlePlayerGhostContact(ghost);
            }
        }

        @Override
        public void onCollisionStay(Collider other) {
            if (other == null) {
                return;
            }
            Object owner = other.getOwner();
            if (owner instanceof GhostEntity ghost) {
                handlePlayerGhostContact(ghost);
            }
        }

        @Override
        public void onCollisionExit(Collider other) {
        }
    }

    private final class WallBombCollisionListener implements ICollisionListener {

        private final WallBombEntity bomb;

        private WallBombCollisionListener(WallBombEntity bomb) {
            this.bomb = bomb;
        }

        @Override
        public void onCollisionEnter(Collider other) {
            handleBombContact(other);
        }

        @Override
        public void onCollisionStay(Collider other) {
            handleBombContact(other);
        }

        private void handleBombContact(Collider other) {
            if (other == null || bomb == null || !bomb.isActive()) {
                return;
            }
            if (!bomb.isExploding()) {
                return;
            }

            Object owner = other.getOwner();
            if (owner instanceof PlayerEntity) {
                bomb.expire();
                world.removeEntity(bomb);
                handlePlayerHit();
                return;
            }
            if (!(owner instanceof GhostEntity ghost)) {
                return;
            }
            if (ghost.getType() != GhostEntity.GhostType.NORMAL && ghost.getType() != GhostEntity.GhostType.BOSS) {
                return;
            }
            if (ghost.isRespawnProtected()) {
                return;
            }

            respawnGhost(ghost);
            bomb.expire();
            world.removeEntity(bomb);
            showStatus(ghost.isBoss() ? "Boss hit by bomb!" : "Ghost hit by bomb!");
        }

        @Override
        public void onCollisionExit(Collider other) {
        }
    }

    private final class BossFireballCollisionListener implements ICollisionListener {

        private final BossFireballEntity fireball;

        private BossFireballCollisionListener(BossFireballEntity fireball) {
            this.fireball = fireball;
        }

        @Override
        public void onCollisionEnter(Collider other) {
            handleFireballContact(other);
        }

        @Override
        public void onCollisionStay(Collider other) {
            handleFireballContact(other);
        }

        private void handleFireballContact(Collider other) {
            if (other == null || fireball == null || !fireball.isActive()) {
                return;
            }

            Object owner = other.getOwner();
            if (owner instanceof WallEntity) {
                fireball.expire();
                world.removeEntity(fireball);
                return;
            }
            if (owner instanceof PlayerEntity) {
                fireball.expire();
                world.removeEntity(fireball);
                handlePlayerHit();
            }
        }

        @Override
        public void onCollisionExit(Collider other) {
        }
    }
}
