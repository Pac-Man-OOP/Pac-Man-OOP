package io.github.some_example_name.lingoman.entity;

import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.lingoman.graphics.LingoSprites;
import io.github.some_example_name.managers.OutputManager;

public class ShockPickupEntity extends Entity {

    public ShockPickupEntity(String id, float x, float y, float size) {
        super(id);
        setX(x);
        setY(y);
        setWidth(size);
        setHeight(size);
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
        float scale = 1.08f;
        float drawWidth = getWidth() * scale;
        float drawHeight = getHeight() * scale;
        float drawX = getX() - (drawWidth - getWidth()) * 0.5f;
        float drawY = getY() - (drawHeight - getHeight()) * 0.5f;
        outputManager.draw(LingoSprites.shockPickup(), drawX, drawY, drawWidth, drawHeight);
    }
}
