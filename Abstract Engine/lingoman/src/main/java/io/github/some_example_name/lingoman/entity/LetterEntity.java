package io.github.some_example_name.lingoman.entity;

import com.badlogic.gdx.graphics.Color;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.managers.OutputManager;

public class LetterEntity extends Entity {

    private static final float WRONG_FLASH_DURATION = 0.35f;
    private static final Color LETTER_COLOR = new Color(0.95f, 0.90f, 0.20f, 1f);
    private static final Color WRONG_OUTLINE = new Color(0.95f, 0.20f, 0.20f, 1f);

    private final char letter;
    private float wrongFlashTimer = 0f;

    public LetterEntity(String id, char letter, float x, float y, float size) {
        super(id);
        this.letter = Character.toUpperCase(letter);
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size);
    }

    public char getLetter() {
        return letter;
    }

    public void triggerWrongFlash() {
        wrongFlashTimer = WRONG_FLASH_DURATION;
    }

    public boolean isWrongFlashing() {
        return wrongFlashTimer > 0f;
    }

    @Override
    public void update(float deltaTime) {
        if (wrongFlashTimer > 0f) {
            wrongFlashTimer = Math.max(0f, wrongFlashTimer - deltaTime);
        }
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public boolean isTrigger() {
        return true;
    }

    @Override
    public void render(OutputManager outputManager) {
        if (isWrongFlashing()) {
            float outline = Math.max(2f, getWidth() * 0.08f);
            outputManager.drawRect(
                getX() - outline,
                getY() - outline,
                getWidth() + outline * 2f,
                getHeight() + outline * 2f,
                WRONG_OUTLINE
            );
        }

        outputManager.drawRect(getX(), getY(), getWidth(), getHeight(), LETTER_COLOR);

        float textScale = Math.max(0.42f, Math.min(0.68f, getWidth() / 22f));
        outputManager.drawTextCenteredScaled(
            String.valueOf(letter),
            getX() + getWidth() * 0.5f,
            getY() + getHeight() * 0.76f,
            new Color(0.10f, 0.08f, 0.02f,  1f),
            textScale
        );
    }
}
