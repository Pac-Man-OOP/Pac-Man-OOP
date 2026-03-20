package io.github.some_example_name.lingoman.graphics;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public final class LingoSprites {

    private enum GhostStyle {
        NORMAL,
        BOSS,
        WALL_BOMBER
    }

    private static final int SPRITE_SIZE = 64;
    private static final int HEDGE_TILE_SIZE = 24;
    private static final int HEDGE_VARIANTS = 2;

    private static final Color PLAYER_OUTLINE = new Color(0.07f, 0.16f, 0.22f, 1f);
    private static final Color PLAYER_GOLD = new Color(0.96f, 0.78f, 0.23f, 1f);
    private static final Color PLAYER_GLOW = new Color(0.99f, 0.88f, 0.42f, 1f);
    private static final Color PLAYER_ACCENT = new Color(0.18f, 0.66f, 0.89f, 1f);
    private static final Color PLAYER_EYE = new Color(0.97f, 0.99f, 1f, 1f);
    private static final Color GHOST_EYE = new Color(0.96f, 0.98f, 1f, 1f);
    private static final Color GHOST_PUPIL = new Color(0.08f, 0.16f, 0.24f, 1f);
    private static final Color FLOOR_GRASS_DARK = new Color(0.10f, 0.30f, 0.04f, 1f);
    private static final Color FLOWER_BLUE = new Color(0.66f, 0.90f, 1.00f, 1f);
    private static final Color FLOWER_YELLOW = new Color(1.00f, 0.88f, 0.40f, 1f);
    private static final Color FLOWER_WHITE = new Color(0.96f, 0.99f, 0.98f, 1f);

    private static final Map<Integer, Texture> GHOST_TEXTURES = new HashMap<>();
    private static final Map<Integer, Texture> BOSS_GHOST_TEXTURES = new HashMap<>();
    private static final Map<Integer, Texture> WALL_BOMBER_GHOST_TEXTURES = new HashMap<>();
    private static Texture playerTexture;
    private static Texture freezePickupTexture;
    private static Texture shockPickupTexture;
    private static Texture fireballTexture;
    private static Texture wallBombTexture;
    private static Texture wallBlastTexture;
    private static final Texture[] HEDGE_WALL_TEXTURES = new Texture[HEDGE_VARIANTS];

    private LingoSprites() {
    }

    public static Texture player() {
        if (playerTexture == null) {
            playerTexture = createPlayerTexture();
        }
        return playerTexture;
    }

    public static Texture freezePickup() {
        if (freezePickupTexture == null) {
            freezePickupTexture = createFreezePickupTexture();
        }
        return freezePickupTexture;
    }

    public static Texture shockPickup() {
        if (shockPickupTexture == null) {
            shockPickupTexture = createShockPickupTexture();
        }
        return shockPickupTexture;
    }

    public static Texture fireball() {
        if (fireballTexture == null) {
            fireballTexture = createFireballTexture();
        }
        return fireballTexture;
    }

    public static Texture wallBomb() {
        if (wallBombTexture == null) {
            wallBombTexture = createWallBombTexture();
        }
        return wallBombTexture;
    }

    public static Texture wallBlast() {
        if (wallBlastTexture == null) {
            wallBlastTexture = createWallBlastTexture();
        }
        return wallBlastTexture;
    }

    public static Texture ghost(Color color) {
        return getGhostTexture(GHOST_TEXTURES, color, GhostStyle.NORMAL);
    }

    public static Texture bossGhost(Color color) {
        return getGhostTexture(BOSS_GHOST_TEXTURES, color, GhostStyle.BOSS);
    }

    public static Texture wallBomberGhost(Color color) {
        return getGhostTexture(WALL_BOMBER_GHOST_TEXTURES, color, GhostStyle.WALL_BOMBER);
    }

    public static Texture hedgeWall() {
        return hedgeWall(0);
    }

    public static Texture hedgeWall(int variant) {
        int index = Math.floorMod(variant, HEDGE_VARIANTS);
        if (HEDGE_WALL_TEXTURES[index] == null) {
            HEDGE_WALL_TEXTURES[index] = createHedgeWallTexture(index);
        }
        return HEDGE_WALL_TEXTURES[index];
    }

    public static void disposeAll() {
        if (playerTexture != null) {
            playerTexture.dispose();
            playerTexture = null;
        }
        if (freezePickupTexture != null) {
            freezePickupTexture.dispose();
            freezePickupTexture = null;
        }
        if (shockPickupTexture != null) {
            shockPickupTexture.dispose();
            shockPickupTexture = null;
        }
        if (fireballTexture != null) {
            fireballTexture.dispose();
            fireballTexture = null;
        }
        if (wallBombTexture != null) {
            wallBombTexture.dispose();
            wallBombTexture = null;
        }
        if (wallBlastTexture != null) {
            wallBlastTexture.dispose();
            wallBlastTexture = null;
        }
        for (int i = 0; i < HEDGE_WALL_TEXTURES.length; i++) {
            if (HEDGE_WALL_TEXTURES[i] != null) {
                HEDGE_WALL_TEXTURES[i].dispose();
                HEDGE_WALL_TEXTURES[i] = null;
            }
        }

        for (Texture texture : GHOST_TEXTURES.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        GHOST_TEXTURES.clear();

        for (Texture texture : BOSS_GHOST_TEXTURES.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        BOSS_GHOST_TEXTURES.clear();

        for (Texture texture : WALL_BOMBER_GHOST_TEXTURES.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        WALL_BOMBER_GHOST_TEXTURES.clear();
    }

    private static Texture createPlayerTexture() {
        return loadAssetTexture("lingoman/player_iggle.png");
    }

    private static Texture getGhostTexture(Map<Integer, Texture> cache, Color color, GhostStyle style) {
        Color body = color == null ? new Color(1f, 0.3f, 0.3f, 1f) : new Color(color);
        int key = Color.rgba8888(body);
        Texture texture = cache.get(key);
        if (texture != null) {
            return texture;
        }

        Texture created = createGhostTexture(body, style);
        cache.put(key, created);
        return created;
    }

    private static Texture createGhostTexture(Color bodyColor, GhostStyle style) {
        return switch (style) {
            case BOSS -> createBossFireTexture(bodyColor);
            case WALL_BOMBER -> createWallBomberTexture(bodyColor);
            default -> createCloudGhostTexture(bodyColor);
        };
    }

    private static Texture createCloudGhostTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body = mix(bodyColor, new Color(0.77f, 0.62f, 0.90f, 1f), 0.45f);
        Color outline = shade(body, 0.34f);
        Color highlight = withAlpha(Color.WHITE, 0.24f);

        int[][] puffs = {
            {20, 28, 11}, {32, 30, 13}, {44, 28, 11},
            {19, 40, 10}, {31, 42, 12}, {43, 40, 10},
            {25, 20, 8}, {38, 21, 8}
        };
        for (int[] puff : puffs) {
            fillCircle(pixmap, puff[0], puff[1], puff[2] + 2, outline);
        }
        for (int[] puff : puffs) {
            fillCircle(pixmap, puff[0], puff[1], puff[2], body);
        }
        fillCircle(pixmap, 22, 42, 8, highlight);

        fillCircle(pixmap, 32, 33, 11, GHOST_EYE);
        fillCircle(pixmap, 35, 31, 4, GHOST_PUPIL);
        fillRect(pixmap, 25, 23, 12, 2, outline);
        fillRect(pixmap, 37, 24, 4, 2, outline);
        return toTexture(pixmap);
    }

    private static Texture createBossFireTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body = mix(bodyColor, new Color(0.98f, 0.22f, 0.16f, 1f), 0.35f);
        Color outline = new Color(0.39f, 0.05f, 0.03f, 1f);
        Color hot = new Color(1.00f, 0.84f, 0.62f, 0.52f);

        fillCircle(pixmap, 32, 35, 18, outline);
        fillCircle(pixmap, 20, 31, 10, outline);
        fillCircle(pixmap, 44, 31, 10, outline);
        fillTriangle(pixmap, 17, 41, 10, 56, 23, 48, outline);
        fillTriangle(pixmap, 47, 41, 54, 56, 41, 48, outline);
        fillTriangle(pixmap, 22, 46, 27, 63, 31, 47, outline);
        fillTriangle(pixmap, 33, 47, 38, 63, 43, 47, outline);

        fillCircle(pixmap, 32, 35, 16, body);
        fillCircle(pixmap, 22, 31, 8, body);
        fillCircle(pixmap, 42, 31, 8, body);
        fillTriangle(pixmap, 19, 43, 23, 58, 29, 44, body);
        fillTriangle(pixmap, 35, 44, 41, 58, 45, 43, body);
        fillCircle(pixmap, 26, 44, 8, hot);

        fillTriangle(pixmap, 18, 41, 22, 55, 25, 41, new Color(0.95f, 0.86f, 0.74f, 1f));
        fillTriangle(pixmap, 39, 41, 42, 55, 46, 41, new Color(0.95f, 0.86f, 0.74f, 1f));

        fillCircle(pixmap, 31, 34, 8, GHOST_EYE);
        fillCircle(pixmap, 37, 34, 8, GHOST_EYE);
        fillTriangle(pixmap, 31, 29, 35, 36, 27, 35, GHOST_PUPIL);
        fillTriangle(pixmap, 38, 29, 42, 35, 34, 36, GHOST_PUPIL);
        return toTexture(pixmap);
    }

    private static Texture createWallBomberTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body = mix(bodyColor, new Color(0.67f, 0.54f, 0.81f, 1f), 0.35f);
        Color outline = shade(body, 0.42f);
        Color panel = new Color(0.39f, 0.27f, 0.51f, 1f);

        fillRect(pixmap, 14, 16, 36, 36, outline);
        fillRect(pixmap, 17, 19, 30, 30, body);
        fillRect(pixmap, 22, 24, 20, 20, panel);
        fillCircle(pixmap, 18, 34, 4, outline);
        fillCircle(pixmap, 46, 34, 4, outline);
        fillCircle(pixmap, 32, 18, 4, outline);
        fillCircle(pixmap, 32, 50, 4, outline);

        fillCircle(pixmap, 32, 34, 10, GHOST_EYE);
        fillCircle(pixmap, 35, 33, 4, GHOST_PUPIL);
        fillRect(pixmap, 25, 24, 14, 2, outline);
        fillRect(pixmap, 25, 43, 14, 2, outline);
        return toTexture(pixmap);
    }

    private static Texture createHedgeWallTexture(int variant) {
        String path = "lingoman/hedge_wall_" + Math.floorMod(variant, HEDGE_VARIANTS) + ".png";
        Texture texture = tryLoadAssetTexture(path);
        return texture != null ? texture : createProceduralHedgeWallTexture(variant);
    }

    private static Texture createFreezePickupTexture() {
        return loadAssetTexture("lingoman/freeze_pickup.png");
    }

    private static Texture createShockPickupTexture() {
        Texture texture = tryLoadAssetTexture("lingoman/shock_pickup.png");
        return texture != null ? texture : createProceduralShockPickupTexture();
    }

    private static Texture createFireballTexture() {
        Texture texture = tryLoadAssetTexture("lingoman/fireball.png");
        return texture != null ? texture : createProceduralFireballTexture();
    }

    private static Texture createWallBombTexture() {
        return loadAssetTexture("lingoman/wall_bomb.png");
    }

    private static Texture createWallBlastTexture() {
        return loadAssetTexture("lingoman/wall_blast.png");
    }

    private static Pixmap newTransparentPixmap() {
        return newTransparentPixmap(SPRITE_SIZE, SPRITE_SIZE);
    }

    private static Pixmap newTransparentPixmap(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0f);
        pixmap.fill();
        return pixmap;
    }

    private static Texture toTexture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    private static Texture loadAssetTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    private static Texture tryLoadAssetTexture(String path) {
        if (path == null || Gdx.files == null) {
            return null;
        }
        return Gdx.files.internal(path).exists() ? loadAssetTexture(path) : null;
    }

    private static Texture createProceduralHedgeWallTexture(int variant) {
        Pixmap pixmap = newTransparentPixmap(HEDGE_TILE_SIZE, HEDGE_TILE_SIZE);
        fillRect(pixmap, 0, 0, HEDGE_TILE_SIZE, HEDGE_TILE_SIZE, FLOOR_GRASS_DARK);
        drawGrassCloud(pixmap, 6, 8, 0.90f);
        drawGrassCloud(pixmap, 13, 11, 0.82f);
        drawGrassCloud(pixmap, 19, 7, 0.78f);
        drawLeafCluster(
            pixmap,
            9,
            15,
            new Color(0.08f, 0.27f, 0.04f, 1f),
            new Color(0.38f, 0.70f, 0.18f, 1f)
        );
        drawLeafCluster(
            pixmap,
            17,
            8,
            new Color(0.11f, 0.32f, 0.05f, 1f),
            new Color(0.31f, 0.62f, 0.14f, 1f)
        );
        addHedgeDecoration(pixmap, variant);
        return toTexture(pixmap);
    }

    private static Texture createProceduralShockPickupTexture() {
        Pixmap pixmap = newTransparentPixmap();
        Color halo = new Color(0.08f, 0.27f, 0.52f, 0.45f);
        Color orbOutline = new Color(0.07f, 0.20f, 0.36f, 1f);
        Color orbBody = new Color(0.19f, 0.66f, 0.96f, 1f);
        Color orbGlow = new Color(0.71f, 0.95f, 1.00f, 0.90f);
        Color bolt = new Color(0.96f, 0.99f, 1.00f, 1f);
        Color boltShadow = new Color(0.07f, 0.20f, 0.36f, 0.85f);

        fillCircle(pixmap, 32, 32, 24, halo);
        fillCircle(pixmap, 32, 32, 20, orbOutline);
        fillCircle(pixmap, 32, 32, 17, orbBody);
        fillCircle(pixmap, 27, 39, 7, withAlpha(orbGlow, 0.34f));
        fillCircle(pixmap, 37, 25, 5, withAlpha(orbGlow, 0.22f));

        fillTriangle(pixmap, 31, 47, 21, 31, 31, 31, boltShadow);
        fillRect(pixmap, 28, 29, 7, 10, boltShadow);
        fillTriangle(pixmap, 33, 39, 43, 18, 33, 18, boltShadow);

        fillTriangle(pixmap, 29, 49, 19, 33, 29, 33, bolt);
        fillRect(pixmap, 26, 31, 7, 10, bolt);
        fillTriangle(pixmap, 31, 41, 42, 20, 31, 20, bolt);
        return toTexture(pixmap);
    }

    private static Texture createProceduralFireballTexture() {
        Pixmap pixmap = newTransparentPixmap();
        Color halo = new Color(0.94f, 0.24f, 0.08f, 0.34f);
        Color outline = new Color(0.41f, 0.06f, 0.02f, 1f);
        Color core = new Color(1.00f, 0.62f, 0.16f, 1f);
        Color hot = new Color(1.00f, 0.90f, 0.68f, 0.94f);
        Color ember = new Color(0.86f, 0.12f, 0.05f, 0.92f);

        fillCircle(pixmap, 32, 32, 24, halo);
        fillCircle(pixmap, 32, 32, 17, outline);
        fillCircle(pixmap, 32, 32, 14, core);
        fillCircle(pixmap, 28, 36, 6, hot);
        fillCircle(pixmap, 36, 28, 5, hot);
        fillTriangle(pixmap, 17, 28, 7, 21, 18, 38, ember);
        fillTriangle(pixmap, 21, 18, 12, 11, 24, 27, ember);
        fillTriangle(pixmap, 40, 47, 52, 55, 38, 37, ember);
        fillTriangle(pixmap, 45, 19, 57, 12, 42, 31, ember);
        return toTexture(pixmap);
    }

    private static void fillTriangle(Pixmap pixmap, int x1, int y1, int x2, int y2, int x3, int y3, Color color) {
        setColor(pixmap, color);
        pixmap.fillTriangle(x1, y1, x2, y2, x3, y3);
    }

    private static void fillCircle(Pixmap pixmap, int x, int y, int radius, Color color) {
        setColor(pixmap, color);
        pixmap.fillCircle(x, y, radius);
    }

    private static void fillRect(Pixmap pixmap, int x, int y, int width, int height, Color color) {
        setColor(pixmap, color);
        pixmap.fillRectangle(x, y, width, height);
    }

    private static void drawFlower(Pixmap pixmap, int centerX, int centerY, Color petalColor, Color centerColor) {
        fillCircle(pixmap, centerX - 3, centerY, 2, petalColor);
        fillCircle(pixmap, centerX + 3, centerY, 2, petalColor);
        fillCircle(pixmap, centerX, centerY - 3, 2, petalColor);
        fillCircle(pixmap, centerX, centerY + 3, 2, petalColor);
        fillCircle(pixmap, centerX, centerY, 2, centerColor);
    }

    private static void drawLeafCluster(Pixmap pixmap, int centerX, int centerY, Color dark, Color light) {
        fillCircle(pixmap, centerX - 3, centerY, 4, dark);
        fillCircle(pixmap, centerX + 3, centerY + 1, 4, dark);
        fillCircle(pixmap, centerX, centerY - 4, 4, dark);
        fillCircle(pixmap, centerX - 2, centerY + 1, 2, light);
        fillCircle(pixmap, centerX + 2, centerY + 2, 2, light);
        fillCircle(pixmap, centerX, centerY - 2, 2, light);
    }

    private static void drawGrassCloud(Pixmap pixmap, int x, int y, float alpha) {
        Color shadow = withAlpha(new Color(0.08f, 0.24f, 0.04f, 1f), alpha);
        fillCircle(pixmap, x, y, 10, shadow);
        fillCircle(pixmap, x + 9, y + 2, 9, shadow);
        fillCircle(pixmap, x - 5, y + 3, 8, shadow);
    }

    private static void addHedgeDecoration(Pixmap pixmap, int variant) {
        switch (Math.floorMod(variant, HEDGE_VARIANTS)) {
            case 1 -> {
                drawFlower(pixmap, 5, 18, FLOWER_BLUE, FLOWER_YELLOW);
                drawFlower(pixmap, 17, 7, FLOWER_WHITE, FLOWER_YELLOW);
            }
            default -> {
            }
        }
    }

    private static void setColor(Pixmap pixmap, Color color) {
        pixmap.setColor(color == null ? Color.WHITE : color);
    }

    private static Color shade(Color color, float amount) {
        return new Color(color.r * (1f - amount), color.g * (1f - amount), color.b * (1f - amount), color.a);
    }

    private static Color mix(Color from, Color to, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        return new Color(
            from.r + (to.r - from.r) * clamped,
            from.g + (to.g - from.g) * clamped,
            from.b + (to.b - from.b) * clamped,
            from.a + (to.a - from.a) * clamped
        );
    }

    private static Color withAlpha(Color color, float alpha) {
        return new Color(color.r, color.g, color.b, alpha);
    }
}
