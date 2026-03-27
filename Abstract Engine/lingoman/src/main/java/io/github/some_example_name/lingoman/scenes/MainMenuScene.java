package io.github.some_example_name.lingoman.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.some_example_name.EngineContext;
import io.github.some_example_name.lingoman.LingoAudio;
import io.github.some_example_name.lingoman.LingoInputActions;
import io.github.some_example_name.lingoman.LingoSceneIds;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.scenes.Scene;

public class MainMenuScene implements Scene {

    private static final float SCREEN_WIDTH = 640f;
    private static final float SCREEN_HEIGHT = 480f;
    private static final float IMAGE_MAX_WIDTH = 620f;
    private static final float IMAGE_MAX_HEIGHT = 350f;
    private static final float START_BUTTON_X = 166f;
    private static final float START_BUTTON_Y = 36f;
    private static final float START_BUTTON_WIDTH = 308f;
    private static final float START_BUTTON_HEIGHT = 54f;

    private static final Color BACKGROUND = new Color(0.03f, 0.08f, 0.18f, 1f);
    private static final Color IMAGE_FRAME = new Color(0.16f, 0.82f, 0.95f, 0.60f);
    private static final Color IMAGE_SHADOW = new Color(0.02f, 0.04f, 0.10f, 0.82f);
    private static final Color IMAGE_GLOW = new Color(0.18f, 0.60f, 0.96f, 0.16f);
    private static final Color TRANSPARENT = new Color(0f, 0f, 0f, 0f);
    private static final Color TEXT_MUTED = new Color(0.72f, 0.95f, 0.98f, 1f);
    private static final Color START_FILL = new Color(0.05f, 0.18f, 0.42f, 0.96f);
    private static final Color START_TEXT = new Color(0.44f, 0.86f, 1.00f, 1f);
    private static final Color START_SHADOW = new Color(0.03f, 0.05f, 0.11f, 0.95f);

    private EngineContext context;
    private float pulseTimer;
    private final Color pulsingBorder = new Color();
    private final Color pulsingFill = new Color();

    @Override
    public void initialize(EngineContext context) {
        this.context = context;
    }

    @Override
    public void enter() {
        pulseTimer = 0f;
        context.getAudioManager().playMusic(LingoAudio.BGM_MENU, true);
    }

    @Override
    public void exit() {
    }

    @Override
    public void handleInput() {
        if (context.getInputManager().isActionJustPressed(LingoInputActions.MENU_CONFIRM) || isStartClicked()) {
            context.getAudioManager().playSound(LingoAudio.SFX_MENU_NAVIGATE, false);
            context.getSceneManager().setActiveScene(LingoSceneIds.MENU);
        }
    }

    @Override
    public void update(float deltaTime) {
        pulseTimer += deltaTime;
    }

    @Override
    public void render() {
        context.getOutputManager().clearScreen(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        drawStartArt();
        drawStartPrompt();
        context.getOutputManager().drawTextCentered("PRESS ENTER TO START", SCREEN_WIDTH * 0.5f, 28f, TEXT_MUTED);
    }

    @Override
    public void dispose() {
    }

    private void drawStartArt() {
        Texture art = LingoSprites.startMenu();
        float widthScale = IMAGE_MAX_WIDTH / art.getWidth();
        float heightScale = IMAGE_MAX_HEIGHT / art.getHeight();
        float scale = Math.min(widthScale, heightScale);
        float drawWidth = art.getWidth() * scale;
        float drawHeight = art.getHeight() * scale;
        float x = (SCREEN_WIDTH - drawWidth) * 0.5f;
        float y = 110f;

        context.getOutputManager().drawRect(x - 16f, y - 16f, drawWidth + 32f, drawHeight + 32f, IMAGE_GLOW);
        context.getOutputManager().drawRect(x + 10f, y - 10f, drawWidth, drawHeight, IMAGE_SHADOW);
        context.getOutputManager().drawPanel(x - 4f, y - 4f, drawWidth + 8f, drawHeight + 8f, TRANSPARENT, IMAGE_FRAME);
        context.getOutputManager().draw(art, x, y, drawWidth, drawHeight);
    }

    private void drawStartPrompt() {
        float pulse = 0.5f + 0.5f * MathUtils.sin(pulseTimer * 3.6f);
        pulsingBorder.set(
            MathUtils.lerp(0.24f, 0.44f, pulse),
            MathUtils.lerp(0.72f, 0.95f, pulse),
            MathUtils.lerp(0.88f, 1.00f, pulse),
            1f
        );
        pulsingFill.set(
            MathUtils.lerp(START_FILL.r, 0.10f, pulse * 0.30f),
            MathUtils.lerp(START_FILL.g, 0.28f, pulse * 0.30f),
            MathUtils.lerp(START_FILL.b, 0.58f, pulse * 0.20f),
            START_FILL.a
        );

        context.getOutputManager().drawRect(162f, 32f, 316f, 62f, START_SHADOW);
        context.getOutputManager().drawPanel(
            START_BUTTON_X,
            START_BUTTON_Y,
            START_BUTTON_WIDTH,
            START_BUTTON_HEIGHT,
            4f,
            pulsingFill,
            pulsingBorder
        );
        context.getOutputManager().drawTextCenteredScaled("START", SCREEN_WIDTH * 0.5f, 73f, START_TEXT, 1.28f);
    }

    private boolean isStartClicked() {
        if (!context.getInputManager().isMouseClicked()) {
            return false;
        }

        Vector2 mouse = context.getInputManager().getMousePosition();
        float worldX = mouse.x;
        float worldY = SCREEN_HEIGHT - mouse.y;
        return worldX >= START_BUTTON_X
            && worldX <= START_BUTTON_X + START_BUTTON_WIDTH
            && worldY >= START_BUTTON_Y
            && worldY <= START_BUTTON_Y + START_BUTTON_HEIGHT;
    }
}
