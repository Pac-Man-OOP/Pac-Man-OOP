package io.github.some_example_name.lingoman.entity;

import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.OutputManager;

public class WallEntity extends Entity {

    private static final com.badlogic.gdx.graphics.Color HEDGE_SHADOW =
        new com.badlogic.gdx.graphics.Color(0.04f, 0.10f, 0.03f, 0.34f);
    private static final com.badlogic.gdx.graphics.Color HEDGE_TOP_LIGHT =
        new com.badlogic.gdx.graphics.Color(0.82f, 0.94f, 0.72f, 0.10f);

    public WallEntity(String id, float x, float y, float size) {
        super(id);
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size);
    }

    @Override
    public boolean isStatic() { return true; }

    @Override
    public void render(OutputManager outputManager) {
        float overlap = Math.max(0.45f, getWidth() * 0.02f);
        float shadowDrop = Math.max(1f, getHeight() * 0.10f);
        int col = Math.round(getX() / Math.max(1f, getWidth()));
        int row = Math.round(getY() / Math.max(1f, getHeight()));
        int variant = Math.floorMod(col * 31 + row * 17, 4);
        outputManager.drawTinted(
            LingoSprites.hedgeWall(variant),
            getX() - overlap + shadowDrop * 0.10f,
            getY() - overlap - shadowDrop,
            getWidth() + overlap * 2f,
            getHeight() + overlap * 2f,
            HEDGE_SHADOW
        );
        outputManager.draw(
            LingoSprites.hedgeWall(variant),
            getX() - overlap,
            getY() - overlap,
            getWidth() + overlap * 2f,
            getHeight() + overlap * 2f
        );
        outputManager.drawRect(
            getX() + getWidth() * 0.12f,
            getY() + getHeight() * 0.74f,
            getWidth() * 0.76f,
            Math.max(1.2f, getHeight() * 0.10f),
            HEDGE_TOP_LIGHT
        );
    }
}
