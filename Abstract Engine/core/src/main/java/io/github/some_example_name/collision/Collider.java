package io.github.some_example_name.collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.entity.Entity;

public class Collider {
    private ICollisionListener listener;
    private Entity owner;
    private float width;
    private float height;

    public Collider(Entity owner, float width, float height) {
        this.owner = owner;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds() {
        Vector2 pos = owner.getPosition();
        return new Rectangle(pos.x, pos.y, width, height);
    }

    public ICollisionListener getListener() {
        return listener;
    }

    public Entity getOwner() {
        return owner;
    }

    public void setListener(ICollisionListener listener) {
        this.listener = listener;
    }
}
