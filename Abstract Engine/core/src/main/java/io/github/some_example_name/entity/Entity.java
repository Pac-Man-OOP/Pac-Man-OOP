package io.github.some_example_name.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import io.github.some_example_name.collision.Collider;

public abstract class Entity {
    protected String id;
    protected Vector2 position;
    protected boolean isActive;
    protected String tag;

    protected Entity(String id, Vector2 position) {
        this.id = id;
        this.position = new Vector2(position);
        this.isActive = true;
    }

    public abstract void update(float deltaTime);

    public abstract void render(SpriteBatch batch);

    public String getId() {
        return id;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 pos) {
        this.position.set(pos);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void dispose() {
        // default no-op
    }

    public Collider getCollider() {
        return null; // temp: override in concrete entities, or store a field if your team decides
    }

    // UML shows isStatic(): boolean, keep as a method
    public boolean isStatic() {
        return false;
    }
}
