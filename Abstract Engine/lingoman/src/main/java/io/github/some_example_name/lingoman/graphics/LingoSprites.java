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

    /** Size of the procedural word icon in pixels. */
    private static final int ICON_SIZE = 40;

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
    private static final Map<String, Texture> WORD_ICON_TEXTURES = new HashMap<>();

    private static Texture playerTexture;
    private static Texture freezePickupTexture;
    private static Texture shockPickupTexture;
    private static Texture fireballTexture;
    private static Texture wallBombTexture;
    private static Texture wallBlastTexture;
    private static final Texture[] HEDGE_WALL_TEXTURES = new Texture[HEDGE_VARIANTS];

    private LingoSprites() {
    }

    // -------------------------------------------------------------------------
    // Existing public API — unchanged
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // NEW: Word icon — returns a small procedural pixel-art icon for the word
    //
    // Each word has a hand-crafted icon drawn with the same Pixmap primitives
    // (fillCircle, fillRect, fillTriangle) used throughout this class —
    // keeping the visual style consistent with all other sprites.
    //
    // Icons are cached after first creation so they are only generated once
    // per session (same lazy-init pattern used for all other textures).
    // -------------------------------------------------------------------------

    /**
     * Returns a small {@value #ICON_SIZE}×{@value #ICON_SIZE} pixel-art icon
     * representing the given word.  Falls back to a generic question-mark icon
     * for words not in the map.
     *
     * @param word the target word (case-insensitive)
     * @return a {@link Texture} — do NOT dispose it manually;
     *         call {@link #disposeAll()} during shutdown instead
     */
    public static Texture wordIcon(String word) {
        if (word == null || word.isBlank()) {
            return getOrCreateWordIcon("?", LingoSprites::createIconUnknown);
        }
        String key = word.trim().toUpperCase();
        return getOrCreateWordIcon(key, () -> createWordIconFor(key));
    }

    // -------------------------------------------------------------------------
    // Dispose
    // -------------------------------------------------------------------------

    public static void disposeAll() {
        if (playerTexture != null)       { playerTexture.dispose();       playerTexture = null; }
        if (freezePickupTexture != null) { freezePickupTexture.dispose(); freezePickupTexture = null; }
        if (shockPickupTexture != null)  { shockPickupTexture.dispose();  shockPickupTexture = null; }
        if (fireballTexture != null)     { fireballTexture.dispose();     fireballTexture = null; }
        if (wallBombTexture != null)     { wallBombTexture.dispose();     wallBombTexture = null; }
        if (wallBlastTexture != null)    { wallBlastTexture.dispose();    wallBlastTexture = null; }

        for (int i = 0; i < HEDGE_WALL_TEXTURES.length; i++) {
            if (HEDGE_WALL_TEXTURES[i] != null) {
                HEDGE_WALL_TEXTURES[i].dispose();
                HEDGE_WALL_TEXTURES[i] = null;
            }
        }
        disposeMap(GHOST_TEXTURES);
        disposeMap(BOSS_GHOST_TEXTURES);
        disposeMap(WALL_BOMBER_GHOST_TEXTURES);
        disposeWordIconMap();
    }

    private static void disposeMap(Map<Integer, Texture> map) {
        for (Texture t : map.values()) { if (t != null) t.dispose(); }
        map.clear();
    }

    private static void disposeWordIconMap() {
        for (Texture t : WORD_ICON_TEXTURES.values()) { if (t != null) t.dispose(); }
        WORD_ICON_TEXTURES.clear();
    }

    // -------------------------------------------------------------------------
    // Private helpers — existing
    // -------------------------------------------------------------------------

    private static Texture createPlayerTexture() {
        return loadAssetTexture("lingoman/player_iggle.png");
    }

    private static Texture getGhostTexture(Map<Integer, Texture> cache, Color color, GhostStyle style) {
        Color body = color == null ? new Color(1f, 0.3f, 0.3f, 1f) : new Color(color);
        int key = Color.rgba8888(body);
        Texture texture = cache.get(key);
        if (texture != null) return texture;
        Texture created = createGhostTexture(body, style);
        cache.put(key, created);
        return created;
    }

    private static Texture createGhostTexture(Color bodyColor, GhostStyle style) {
        return switch (style) {
            case BOSS         -> createBossFireTexture(bodyColor);
            case WALL_BOMBER  -> createWallBomberTexture(bodyColor);
            default           -> createCloudGhostTexture(bodyColor);
        };
    }

    private static Texture createCloudGhostTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body    = mix(bodyColor, new Color(0.77f, 0.62f, 0.90f, 1f), 0.45f);
        Color outline = shade(body, 0.34f);
        Color highlight = withAlpha(Color.WHITE, 0.24f);
        int[][] puffs = { {20,28,11},{32,30,13},{44,28,11},{19,40,10},{31,42,12},{43,40,10},{25,20,8},{38,21,8} };
        for (int[] p : puffs) fillCircle(pixmap, p[0], p[1], p[2]+2, outline);
        for (int[] p : puffs) fillCircle(pixmap, p[0], p[1], p[2],   body);
        fillCircle(pixmap, 22, 42, 8, highlight);
        fillCircle(pixmap, 32, 33, 11, GHOST_EYE);
        fillCircle(pixmap, 35, 31,  4, GHOST_PUPIL);
        fillRect(pixmap, 25, 23, 12, 2, outline);
        fillRect(pixmap, 37, 24,  4, 2, outline);
        return toTexture(pixmap);
    }

    private static Texture createBossFireTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body    = mix(bodyColor, new Color(0.98f, 0.22f, 0.16f, 1f), 0.35f);
        Color outline = new Color(0.39f, 0.05f, 0.03f, 1f);
        Color hot     = new Color(1.00f, 0.84f, 0.62f, 0.52f);
        fillCircle(pixmap, 32, 35, 18, outline);
        fillCircle(pixmap, 20, 31, 10, outline);
        fillCircle(pixmap, 44, 31, 10, outline);
        fillTriangle(pixmap, 17,41,10,56,23,48, outline);
        fillTriangle(pixmap, 47,41,54,56,41,48, outline);
        fillTriangle(pixmap, 22,46,27,63,31,47, outline);
        fillTriangle(pixmap, 33,47,38,63,43,47, outline);
        fillCircle(pixmap, 32, 35, 16, body);
        fillCircle(pixmap, 22, 31,  8, body);
        fillCircle(pixmap, 42, 31,  8, body);
        fillTriangle(pixmap, 19,43,23,58,29,44, body);
        fillTriangle(pixmap, 35,44,41,58,45,43, body);
        fillCircle(pixmap, 26, 44, 8, hot);
        fillTriangle(pixmap, 18,41,22,55,25,41, new Color(0.95f,0.86f,0.74f,1f));
        fillTriangle(pixmap, 39,41,42,55,46,41, new Color(0.95f,0.86f,0.74f,1f));
        fillCircle(pixmap, 31, 34,  8, GHOST_EYE);
        fillCircle(pixmap, 37, 34,  8, GHOST_EYE);
        fillTriangle(pixmap, 31,29,35,36,27,35, GHOST_PUPIL);
        fillTriangle(pixmap, 38,29,42,35,34,36, GHOST_PUPIL);
        return toTexture(pixmap);
    }

    private static Texture createWallBomberTexture(Color bodyColor) {
        Pixmap pixmap = newTransparentPixmap();
        Color body    = mix(bodyColor, new Color(0.67f, 0.54f, 0.81f, 1f), 0.35f);
        Color outline = shade(body, 0.42f);
        Color panel   = new Color(0.39f, 0.27f, 0.51f, 1f);
        fillRect(pixmap, 14, 16, 36, 36, outline);
        fillRect(pixmap, 17, 19, 30, 30, body);
        fillRect(pixmap, 22, 24, 20, 20, panel);
        fillCircle(pixmap, 18, 34, 4, outline);
        fillCircle(pixmap, 46, 34, 4, outline);
        fillCircle(pixmap, 32, 18, 4, outline);
        fillCircle(pixmap, 32, 50, 4, outline);
        fillCircle(pixmap, 32, 34, 10, GHOST_EYE);
        fillCircle(pixmap, 35, 33,  4, GHOST_PUPIL);
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

    private static Texture createWallBombTexture()  { return loadAssetTexture("lingoman/wall_bomb.png"); }
    private static Texture createWallBlastTexture() { return loadAssetTexture("lingoman/wall_blast.png"); }

    // -------------------------------------------------------------------------
    // NEW: Word icon dispatch + per-word drawing methods
    // -------------------------------------------------------------------------

    /** Cache lookup / creation for word icons. */
    private static Texture getOrCreateWordIcon(String key, java.util.function.Supplier<Texture> factory) {
        Texture cached = WORD_ICON_TEXTURES.get(key);
        if (cached != null) return cached;
        Texture created = factory.get();
        WORD_ICON_TEXTURES.put(key, created);
        return created;
    }

    /** Dispatches to the correct icon drawing method for each known word. */
    private static Texture createWordIconFor(String word) {
        return switch (word) {
            // ── Easy ──────────────────────────────────────────────────────────
            case "CAT"      -> createIconCat();
            case "DOG"      -> createIconDog();
            case "SUN"      -> createIconSun();
            case "MOON"     -> createIconMoon();
            case "STAR"     -> createIconStar();
            case "TREE"     -> createIconTree();
            case "BIRD"     -> createIconBird();
            case "FISH"     -> createIconFish();
            // ── Medium ────────────────────────────────────────────────────────
            case "ORANGE"   -> createIconOrange();
            case "PLANET"   -> createIconPlanet();
            case "PENCIL"   -> createIconPencil();
            case "FLOWER"   -> createIconFlower();
            case "ROCKET"   -> createIconRocket();
            case "WINDOW"   -> createIconWindow();
            // ── Hard ──────────────────────────────────────────────────────────
            case "LANGUAGE" -> createIconLanguage();
            case "ECLIPSES" -> createIconEclipses();
            case "ELEPHANT" -> createIconElephant();
            case "MAGNETIC" -> createIconMagnetic();
            case "NOTEBOOK" -> createIconNotebook();
            default         -> createIconUnknown();
        };
    }

    // ── Icon drawing helpers ──────────────────────────────────────────────────

    /** Dark panel background matching the game's HUD aesthetic. */
    private static Pixmap iconBase() {
        Pixmap p = newTransparentPixmap(ICON_SIZE, ICON_SIZE);
        // Dark rounded background consistent with the game's dark theme
        fillRect(p, 0, 0, ICON_SIZE, ICON_SIZE, new Color(0.08f, 0.10f, 0.14f, 0.92f));
        // Thin gold border matching panel borders used in other scenes
        Color border = new Color(0.94f, 0.78f, 0.28f, 1f);
        fillRect(p, 0, 0, ICON_SIZE, 2, border);               // bottom
        fillRect(p, 0, ICON_SIZE-2, ICON_SIZE, 2, border);     // top
        fillRect(p, 0, 0, 2, ICON_SIZE, border);               // left
        fillRect(p, ICON_SIZE-2, 0, 2, ICON_SIZE, border);     // right
        return p;
    }

    // CAT — two triangular ears, round head, whisker dots
    private static Texture createIconCat() {
        Pixmap p = iconBase();
        Color fur     = new Color(0.80f, 0.62f, 0.42f, 1f);
        Color outline = new Color(0.30f, 0.20f, 0.10f, 1f);
        Color eyeC    = new Color(0.30f, 0.82f, 0.36f, 1f);
        fillCircle(p, 20, 18, 12, outline);
        fillCircle(p, 20, 18, 10, fur);
        fillTriangle(p, 10,28,  6,36, 15,28, outline);  // left ear
        fillTriangle(p, 30,28, 34,36, 25,28, outline);
        fillTriangle(p, 11,28,  7,34, 15,28, fur);
        fillTriangle(p, 29,28, 33,34, 25,28, fur);
        fillCircle(p, 16, 20, 3, outline); fillCircle(p, 16, 20, 2, eyeC);
        fillCircle(p, 24, 20, 3, outline); fillCircle(p, 24, 20, 2, eyeC);
        // whisker dots
        fillCircle(p, 12, 15, 1, outline); fillCircle(p, 14, 13, 1, outline);
        fillCircle(p, 26, 15, 1, outline); fillCircle(p, 28, 13, 1, outline);
        return toTexture(p);
    }

    // DOG — floppy ears, tongue
    private static Texture createIconDog() {
        Pixmap p = iconBase();
        Color fur  = new Color(0.76f, 0.55f, 0.30f, 1f);
        Color dark = new Color(0.32f, 0.20f, 0.08f, 1f);
        Color eye  = new Color(0.18f, 0.12f, 0.06f, 1f);
        Color tongue = new Color(0.96f, 0.42f, 0.46f, 1f);
        fillCircle(p, 20, 20, 12, dark);
        fillCircle(p, 20, 20, 10, fur);
        fillRect(p, 8, 18, 5, 10, dark); fillRect(p, 9, 19, 3, 8, fur);   // left ear
        fillRect(p, 27, 18, 5, 10, dark); fillRect(p, 28, 19, 3, 8, fur); // right ear
        fillCircle(p, 16, 20, 3, dark); fillCircle(p, 16, 20, 2, eye);
        fillCircle(p, 24, 20, 3, dark); fillCircle(p, 24, 20, 2, eye);
        fillCircle(p, 20, 26, 4, dark); fillCircle(p, 20, 26, 3, new Color(0.50f,0.35f,0.20f,1f)); // nose
        fillCircle(p, 20, 30, 4, tongue);
        return toTexture(p);
    }

    // SUN — yellow circle + 8 rays
    private static Texture createIconSun() {
        Pixmap p = iconBase();
        Color ray    = new Color(0.98f, 0.84f, 0.20f, 1f);
        Color body   = new Color(0.99f, 0.95f, 0.40f, 1f);
        Color centre = new Color(1.00f, 0.98f, 0.72f, 1f);
        // Rays
        int[][] rays = {{20,4},{31,6},{36,16},{35,28},{26,34},{14,32},{9,21},{10,9}};
        for (int[] r : rays) fillCircle(p, r[0], r[1], 3, ray);
        fillCircle(p, 20, 19, 11, ray);
        fillCircle(p, 20, 19,  9, body);
        fillCircle(p, 18, 17,  4, centre);
        return toTexture(p);
    }

    // MOON — crescent using two circles
    private static Texture createIconMoon() {
        Pixmap p = iconBase();
        Color moon = new Color(0.95f, 0.92f, 0.70f, 1f);
        Color sky  = new Color(0.08f, 0.10f, 0.14f, 0.92f); // same as background
        Color star = new Color(0.98f, 0.96f, 0.82f, 1f);
        fillCircle(p, 18, 20, 13, moon);
        fillCircle(p, 24, 17, 11, sky);  // carves crescent
        fillCircle(p, 29, 10, 2, star);
        fillCircle(p, 33, 19, 1, star);
        fillCircle(p, 27, 28, 2, star);
        return toTexture(p);
    }

    // STAR — five-pointed using triangles
    private static Texture createIconStar() {
        Pixmap p = iconBase();
        Color body   = new Color(0.98f, 0.84f, 0.20f, 1f);
        Color outline= new Color(0.70f, 0.55f, 0.05f, 1f);
        // Approximated as a circle + 5 triangular spikes
        fillCircle(p, 20, 20, 8, outline);
        fillCircle(p, 20, 20, 6, body);
        int[][] tips = {{20,5},{33,16},{28,31},{12,31},{7,16}};
        for (int[] t : tips) {
            fillTriangle(p, 20,20, t[0],t[1], t[0]+2,t[1]+2, body);
        }
        fillCircle(p, 17, 16, 2, new Color(1f,0.98f,0.80f,0.8f)); // highlight
        return toTexture(p);
    }

    // TREE — brown trunk + green canopy
    private static Texture createIconTree() {
        Pixmap p = iconBase();
        Color trunk  = new Color(0.50f, 0.30f, 0.12f, 1f);
        Color darkG  = new Color(0.12f, 0.36f, 0.06f, 1f);
        Color midG   = new Color(0.22f, 0.58f, 0.12f, 1f);
        Color lightG = new Color(0.38f, 0.72f, 0.18f, 1f);
        fillRect(p, 17, 4, 6, 14, trunk);
        fillTriangle(p, 20,36, 6,18, 34,18, darkG);
        fillTriangle(p, 20,33, 8,19, 32,19, midG);
        fillCircle(p, 16, 25, 5, lightG);
        fillCircle(p, 24, 23, 5, lightG);
        return toTexture(p);
    }

    // BIRD — simple wing + beak
    private static Texture createIconBird() {
        Pixmap p = iconBase();
        Color body  = new Color(0.30f, 0.60f, 0.90f, 1f);
        Color dark  = new Color(0.10f, 0.25f, 0.50f, 1f);
        Color beak  = new Color(0.98f, 0.78f, 0.22f, 1f);
        Color eye   = new Color(0.08f, 0.08f, 0.10f, 1f);
        fillCircle(p, 22, 20, 10, dark);
        fillCircle(p, 22, 20,  8, body);
        fillTriangle(p,  6,24, 16,18, 14,28, dark); // left wing
        fillTriangle(p,  7,24, 16,19, 14,27, body);
        fillTriangle(p, 28,20, 36,18, 36,22, beak);
        fillCircle(p, 26, 17, 2, eye);
        return toTexture(p);
    }

    // FISH — oval body + tail fin
    private static Texture createIconFish() {
        Pixmap p = iconBase();
        Color body   = new Color(0.30f, 0.68f, 0.82f, 1f);
        Color outline= new Color(0.10f, 0.30f, 0.50f, 1f);
        Color fin    = new Color(0.20f, 0.52f, 0.72f, 1f);
        Color eye    = new Color(0.08f, 0.08f, 0.10f, 1f);
        Color scale  = new Color(0.50f, 0.84f, 0.95f, 0.55f);
        fillCircle(p, 22, 20, 12, outline);
        fillCircle(p, 22, 20,  9, body);
        fillTriangle(p, 10,20, 4,12, 4,28, outline); // tail
        fillTriangle(p, 11,20, 5,13, 5,27, fin);
        fillCircle(p, 28, 17,  3, outline); fillCircle(p, 28, 17, 2, eye);
        fillCircle(p, 18, 18,  3, scale);
        fillCircle(p, 23, 22,  2, scale);
        return toTexture(p);
    }

    // ORANGE — round citrus with leaf
    private static Texture createIconOrange() {
        Pixmap p = iconBase();
        Color peel   = new Color(0.95f, 0.56f, 0.10f, 1f);
        Color dark   = new Color(0.60f, 0.28f, 0.04f, 1f);
        Color highlight = new Color(1.00f, 0.82f, 0.50f, 0.70f);
        Color leaf   = new Color(0.22f, 0.60f, 0.12f, 1f);
        fillCircle(p, 20, 21, 13, dark);
        fillCircle(p, 20, 21, 11, peel);
        fillCircle(p, 15, 27,  5, highlight);
        fillRect(p, 19, 8, 3, 7, dark); // stem
        fillTriangle(p, 22,12, 30,8, 28,16, leaf);
        return toTexture(p);
    }

    // PLANET — sphere with ring
    private static Texture createIconPlanet() {
        Pixmap p = iconBase();
        Color globe  = new Color(0.28f, 0.50f, 0.88f, 1f);
        Color dark   = new Color(0.10f, 0.22f, 0.52f, 1f);
        Color ring   = new Color(0.78f, 0.60f, 0.22f, 0.85f);
        Color land   = new Color(0.28f, 0.68f, 0.28f, 1f);
        fillCircle(p, 20, 20, 11, dark);
        fillCircle(p, 20, 20,  9, globe);
        fillCircle(p, 16, 17,  3, land);
        fillCircle(p, 23, 23,  2, land);
        // Ring as thin horizontal ellipse approximation
        fillRect(p, 6, 19, 28, 4, withAlpha(ring, 0.50f));
        fillRect(p, 6, 20, 28, 2, ring);
        // Re-draw front half of globe over ring
        fillCircle(p, 20, 23, 9, globe);
        fillCircle(p, 16, 20, 3, land);
        return toTexture(p);
    }

    // PENCIL — yellow hexagonal body + tip
    private static Texture createIconPencil() {
        Pixmap p = iconBase();
        Color body  = new Color(0.98f, 0.84f, 0.20f, 1f);
        Color dark  = new Color(0.40f, 0.30f, 0.06f, 1f);
        Color tip   = new Color(0.90f, 0.70f, 0.50f, 1f);
        Color point = new Color(0.20f, 0.18f, 0.16f, 1f);
        Color eraser= new Color(0.96f, 0.52f, 0.58f, 1f);
        // Body (tilted rectangle)
        fillRect(p, 14, 8, 12, 22, dark);
        fillRect(p, 15, 9, 10, 20, body);
        // Tip
        fillTriangle(p, 14,30, 26,30, 20,38, dark);
        fillTriangle(p, 15,30, 25,30, 20,37, tip);
        fillTriangle(p, 18,34, 22,34, 20,38, point);
        // Eraser
        fillRect(p, 14, 6, 12, 4, dark);
        fillRect(p, 15, 7, 10, 2, eraser);
        return toTexture(p);
    }

    // FLOWER — centre + 6 petals
    private static Texture createIconFlower() {
        Pixmap p = iconBase();
        Color petal  = new Color(0.96f, 0.46f, 0.70f, 1f);
        Color outline= new Color(0.60f, 0.20f, 0.38f, 1f);
        Color centre = new Color(0.98f, 0.84f, 0.20f, 1f);
        Color stem   = new Color(0.22f, 0.58f, 0.12f, 1f);
        fillRect(p, 19, 4, 3, 10, stem);
        int[][] petals = {{20,18},{28,16},{30,24},{24,30},{14,30},{10,22},{10,14},{16,10}};
        for (int[] pt : petals) { fillCircle(p, pt[0], pt[1], 5, outline); }
        for (int[] pt : petals) { fillCircle(p, pt[0], pt[1], 4, petal); }
        fillCircle(p, 20, 22, 6, outline);
        fillCircle(p, 20, 22, 5, centre);
        return toTexture(p);
    }

    // ROCKET — white body + flame
    private static Texture createIconRocket() {
        Pixmap p = iconBase();
        Color body   = new Color(0.88f, 0.90f, 0.96f, 1f);
        Color dark   = new Color(0.30f, 0.34f, 0.46f, 1f);
        Color window = new Color(0.40f, 0.76f, 0.98f, 1f);
        Color flame1 = new Color(0.98f, 0.72f, 0.12f, 1f);
        Color flame2 = new Color(0.98f, 0.30f, 0.10f, 1f);
        // Body
        fillRect(p, 15, 10, 10, 22, dark);
        fillRect(p, 16, 11,  8, 20, body);
        // Nose
        fillTriangle(p, 15,10, 25,10, 20,4, dark);
        fillTriangle(p, 16,10, 24,10, 20,5, body);
        // Fins
        fillTriangle(p, 15,30, 10,36, 15,36, dark);
        fillTriangle(p, 25,30, 30,36, 25,36, dark);
        // Window
        fillCircle(p, 20, 18, 4, dark);
        fillCircle(p, 20, 18, 3, window);
        // Flame
        fillTriangle(p, 16,32, 24,32, 18,38, flame1);
        fillTriangle(p, 18,32, 22,32, 20,37, flame2);
        return toTexture(p);
    }

    // WINDOW — frame + cross dividers + sky inside
    private static Texture createIconWindow() {
        Pixmap p = iconBase();
        Color frame = new Color(0.62f, 0.44f, 0.24f, 1f);
        Color glass = new Color(0.56f, 0.80f, 0.96f, 0.80f);
        Color cloud = new Color(0.96f, 0.96f, 0.98f, 0.88f);
        Color sky   = new Color(0.50f, 0.72f, 0.92f, 1f);
        fillRect(p, 6, 6, 28, 28, frame);
        fillRect(p, 8, 8, 12, 12, glass); // top-left pane
        fillRect(p, 8, 8, 11, 11, sky);
        fillRect(p, 22, 8, 12, 12, glass); // top-right pane
        fillRect(p, 22, 8, 11, 11, sky);
        fillRect(p, 8, 22, 12, 12, glass); // bottom-left pane
        fillRect(p, 22, 22, 12, 12, glass); // bottom-right pane
        // Cloud in top-left pane
        fillCircle(p, 12, 12, 3, cloud);
        fillCircle(p, 15, 11, 3, cloud);
        return toTexture(p);
    }

    // LANGUAGE — speech bubble with letter inside
    private static Texture createIconLanguage() {
        Pixmap p = iconBase();
        Color bubble = new Color(0.26f, 0.72f, 0.56f, 1f);
        Color dark   = new Color(0.08f, 0.30f, 0.22f, 1f);
        Color letter = new Color(0.96f, 0.96f, 0.92f, 1f);
        fillCircle(p, 20, 18, 13, dark);
        fillCircle(p, 20, 18, 11, bubble);
        // Tail of speech bubble
        fillTriangle(p, 14,28, 20,29, 12,36, dark);
        fillTriangle(p, 15,28, 20,28, 13,34, bubble);
        // Letter 'A' approximation
        fillRect(p, 17, 12, 3, 12, letter);
        fillRect(p, 22, 12, 3, 12, letter);
        fillRect(p, 17, 17, 8, 3, letter);
        fillRect(p, 14, 22, 15, 2, letter);
        return toTexture(p);
    }

    // ECLIPSES — sun partially covered by moon
    private static Texture createIconEclipses() {
        Pixmap p = iconBase();
        Color sun    = new Color(0.98f, 0.82f, 0.18f, 1f);
        Color corona = new Color(0.98f, 0.64f, 0.10f, 0.60f);
        Color moon   = new Color(0.20f, 0.20f, 0.28f, 1f);
        Color glow   = new Color(0.98f, 0.90f, 0.50f, 0.40f);
        fillCircle(p, 18, 20, 14, corona);
        fillCircle(p, 18, 20, 11, sun);
        fillCircle(p, 24, 20, 12, new Color(0.10f,0.10f,0.16f,1f));
        fillCircle(p, 24, 20, 10, moon);
        // Corona rays around the gap
        fillCircle(p, 18, 7, 3, corona);
        fillCircle(p, 7, 18, 3, corona);
        fillCircle(p, 7, 28, 2, corona);
        return toTexture(p);
    }

    // ELEPHANT — large grey circle + trunk
    private static Texture createIconElephant() {
        Pixmap p = iconBase();
        Color body  = new Color(0.60f, 0.60f, 0.66f, 1f);
        Color dark  = new Color(0.30f, 0.30f, 0.36f, 1f);
        Color eye   = new Color(0.10f, 0.10f, 0.14f, 1f);
        Color tusk  = new Color(0.95f, 0.92f, 0.78f, 1f);
        // Head
        fillCircle(p, 20, 20, 13, dark);
        fillCircle(p, 20, 20, 11, body);
        // Ears
        fillCircle(p,  9, 20, 7, dark); fillCircle(p,  9, 20, 5, body);
        fillCircle(p, 31, 20, 7, dark); fillCircle(p, 31, 20, 5, body);
        // Trunk
        fillRect(p, 17, 31, 6, 8, dark);
        fillRect(p, 18, 32, 4, 7, body);
        fillRect(p, 14, 37, 5, 3, body); // trunk curl
        // Eye
        fillCircle(p, 24, 17, 3, dark); fillCircle(p, 24, 17, 2, eye);
        // Tusk
        fillTriangle(p, 24,28, 34,34, 26,30, tusk);
        return toTexture(p);
    }

    // MAGNETIC — horseshoe magnet with poles
    private static Texture createIconMagnetic() {
        Pixmap p = iconBase();
        Color metal = new Color(0.68f, 0.70f, 0.76f, 1f);
        Color dark  = new Color(0.28f, 0.30f, 0.36f, 1f);
        Color red   = new Color(0.90f, 0.20f, 0.20f, 1f);
        Color blue  = new Color(0.20f, 0.40f, 0.90f, 1f);
        // Horseshoe arc (two pillars + arc top)
        fillRect(p, 8,  8, 7, 24, dark);  fillRect(p, 9,  9, 5, 22, metal);
        fillRect(p, 25, 8, 7, 24, dark);  fillRect(p, 26, 9, 5, 22, metal);
        fillRect(p, 8,  8, 24, 7, dark);  fillRect(p, 9,  9, 22, 5, metal);
        // Pole colours
        fillRect(p, 8, 30, 7, 4, red);
        fillRect(p, 25, 30, 7, 4, blue);
        // Field lines (arcs approximated as circles)
        fillCircle(p, 19, 35, 8, withAlpha(red, 0.25f));
        fillCircle(p, 20, 35, 5, withAlpha(blue, 0.20f));
        return toTexture(p);
    }

    // NOTEBOOK — lined pages + spiral binding
    private static Texture createIconNotebook() {
        Pixmap p = iconBase();
        Color cover  = new Color(0.22f, 0.46f, 0.72f, 1f);
        Color page   = new Color(0.94f, 0.94f, 0.90f, 1f);
        Color line   = new Color(0.70f, 0.76f, 0.86f, 1f);
        Color spiral = new Color(0.56f, 0.36f, 0.16f, 1f);
        // Cover
        fillRect(p, 8, 5, 24, 30, new Color(0.14f,0.30f,0.52f,1f));
        fillRect(p, 9, 6, 22, 28, cover);
        // Pages
        fillRect(p, 10, 7, 18, 26, page);
        // Lines
        for (int y = 10; y < 30; y += 4) fillRect(p, 12, y, 14, 2, line);
        // Spiral binding
        for (int y = 8; y < 32; y += 5) fillCircle(p, 9, y, 2, spiral);
        return toTexture(p);
    }

    // Fallback — question mark
    private static Texture createIconUnknown() {
        Pixmap p = iconBase();
        Color c = new Color(0.94f, 0.78f, 0.28f, 1f);
        fillCircle(p, 20, 14, 7, c);
        fillCircle(p, 20, 14, 5, new Color(0.08f, 0.10f, 0.14f, 0.92f));
        fillRect(p, 18, 18, 4, 8, c);
        fillRect(p, 18, 28, 4, 4, c);
        return toTexture(p);
    }

    // -------------------------------------------------------------------------
    // Shared Pixmap utilities
    // -------------------------------------------------------------------------

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
        if (path == null || Gdx.files == null) return null;
        return Gdx.files.internal(path).exists() ? loadAssetTexture(path) : null;
    }

    private static Texture createProceduralHedgeWallTexture(int variant) {
        Pixmap pixmap = newTransparentPixmap(HEDGE_TILE_SIZE, HEDGE_TILE_SIZE);
        fillRect(pixmap, 0, 0, HEDGE_TILE_SIZE, HEDGE_TILE_SIZE, FLOOR_GRASS_DARK);
        drawGrassCloud(pixmap, 6, 8, 0.90f);
        drawGrassCloud(pixmap, 13, 11, 0.82f);
        drawGrassCloud(pixmap, 19, 7, 0.78f);
        drawLeafCluster(pixmap, 9, 15, new Color(0.08f,0.27f,0.04f,1f), new Color(0.38f,0.70f,0.18f,1f));
        drawLeafCluster(pixmap, 17, 8, new Color(0.11f,0.32f,0.05f,1f), new Color(0.31f,0.62f,0.14f,1f));
        addHedgeDecoration(pixmap, variant);
        return toTexture(pixmap);
    }

    private static Texture createProceduralShockPickupTexture() {
        Pixmap pixmap = newTransparentPixmap();
        Color halo = new Color(0.08f,0.27f,0.52f,0.45f);
        Color orbOutline = new Color(0.07f,0.20f,0.36f,1f);
        Color orbBody = new Color(0.19f,0.66f,0.96f,1f);
        Color orbGlow = new Color(0.71f,0.95f,1.00f,0.90f);
        Color bolt = new Color(0.96f,0.99f,1.00f,1f);
        Color boltShadow = new Color(0.07f,0.20f,0.36f,0.85f);
        fillCircle(pixmap,32,32,24,halo); fillCircle(pixmap,32,32,20,orbOutline);
        fillCircle(pixmap,32,32,17,orbBody); fillCircle(pixmap,27,39,7,withAlpha(orbGlow,0.34f));
        fillCircle(pixmap,37,25,5,withAlpha(orbGlow,0.22f));
        fillTriangle(pixmap,31,47,21,31,31,31,boltShadow); fillRect(pixmap,28,29,7,10,boltShadow);
        fillTriangle(pixmap,33,39,43,18,33,18,boltShadow);
        fillTriangle(pixmap,29,49,19,33,29,33,bolt); fillRect(pixmap,26,31,7,10,bolt);
        fillTriangle(pixmap,31,41,42,20,31,20,bolt);
        return toTexture(pixmap);
    }

    private static Texture createProceduralFireballTexture() {
        Pixmap pixmap = newTransparentPixmap();
        Color halo = new Color(0.94f,0.24f,0.08f,0.34f);
        Color outline = new Color(0.41f,0.06f,0.02f,1f);
        Color core = new Color(1.00f,0.62f,0.16f,1f);
        Color hot = new Color(1.00f,0.90f,0.68f,0.94f);
        Color ember = new Color(0.86f,0.12f,0.05f,0.92f);
        fillCircle(pixmap,32,32,24,halo); fillCircle(pixmap,32,32,17,outline);
        fillCircle(pixmap,32,32,14,core); fillCircle(pixmap,28,36,6,hot);
        fillCircle(pixmap,36,28,5,hot);
        fillTriangle(pixmap,17,28,7,21,18,38,ember); fillTriangle(pixmap,21,18,12,11,24,27,ember);
        fillTriangle(pixmap,40,47,52,55,38,37,ember); fillTriangle(pixmap,45,19,57,12,42,31,ember);
        return toTexture(pixmap);
    }

    private static void fillTriangle(Pixmap p,int x1,int y1,int x2,int y2,int x3,int y3,Color c) {
        setColor(p,c); p.fillTriangle(x1,y1,x2,y2,x3,y3);
    }
    private static void fillCircle(Pixmap p,int x,int y,int r,Color c) {
        setColor(p,c); p.fillCircle(x,y,r);
    }
    private static void fillRect(Pixmap p,int x,int y,int w,int h,Color c) {
        setColor(p,c); p.fillRectangle(x,y,w,h);
    }
    private static void drawFlower(Pixmap p,int cx,int cy,Color petal,Color centre) {
        fillCircle(p,cx-3,cy,2,petal); fillCircle(p,cx+3,cy,2,petal);
        fillCircle(p,cx,cy-3,2,petal); fillCircle(p,cx,cy+3,2,petal);
        fillCircle(p,cx,cy,2,centre);
    }
    private static void drawLeafCluster(Pixmap p,int cx,int cy,Color dark,Color light) {
        fillCircle(p,cx-3,cy,4,dark); fillCircle(p,cx+3,cy+1,4,dark); fillCircle(p,cx,cy-4,4,dark);
        fillCircle(p,cx-2,cy+1,2,light); fillCircle(p,cx+2,cy+2,2,light); fillCircle(p,cx,cy-2,2,light);
    }
    private static void drawGrassCloud(Pixmap p,int x,int y,float alpha) {
        Color s = withAlpha(new Color(0.08f,0.24f,0.04f,1f),alpha);
        fillCircle(p,x,y,10,s); fillCircle(p,x+9,y+2,9,s); fillCircle(p,x-5,y+3,8,s);
    }
    private static void addHedgeDecoration(Pixmap p,int variant) {
        switch (Math.floorMod(variant,HEDGE_VARIANTS)) {
            case 1 -> { drawFlower(p,5,18,FLOWER_BLUE,FLOWER_YELLOW); drawFlower(p,17,7,FLOWER_WHITE,FLOWER_YELLOW); }
            default -> {}
        }
    }
    private static void setColor(Pixmap p,Color c) { p.setColor(c==null?Color.WHITE:c); }
    private static Color shade(Color c,float a) {
        return new Color(c.r*(1f-a),c.g*(1f-a),c.b*(1f-a),c.a);
    }
    private static Color mix(Color f,Color t,float a) {
        float cl=Math.max(0f,Math.min(1f,a));
        return new Color(f.r+(t.r-f.r)*cl,f.g+(t.g-f.g)*cl,f.b+(t.b-f.b)*cl,f.a+(t.a-f.a)*cl);
    }
    private static Color withAlpha(Color c,float a) { return new Color(c.r,c.g,c.b,a); }
}