package io.github.some_example_name.entity;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.lifecycle.Activatable;
import io.github.some_example_name.managers.OutputManager;

public abstract class Entity implements Activatable {
  private final String id;
  private final Rectangle bounds = new Rectangle();

  protected Entity(String id) { this.id = id; }

  public String getId() { return id; }

  private float x, y;
  private float vx, vy;
  private float width = 32, height = 32;

  private boolean active = true;

  public void update(float dt) {}

  // Replaced Batch with OutputManager
  public void render(OutputManager outputManager) {}

  public void onCollision(Entity other) {}

  public Rectangle bounds() {
    bounds.set(getX(), getY(), getWidth(), getHeight());
    return bounds;
  }

  // Convenience getters/setters for subclasses
  public float getX() { return x; }
  public void setX(float x) { this.x = x; }

  public float getY() { return y; }
  public void setY(float y) { this.y = y; }

  public float getVx() { return vx; }
  public void setVx(float vx) { this.vx = vx; }

  public float getVy() { return vy; }
  public void setVy(float vy) { this.vy = vy; }

  public float getWidth() { return width; }
  public void setWidth(float width) { this.width = width; }

  public float getHeight() { return height; }
  public void setHeight(float height) { this.height = height; }

  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }

  /** 
   * Returns true if this entity should not be moved.
   * For example, walls stay in place during collision resolution.
  */
  public boolean isStatic() { return false; }

  /**
   * Returns true if this entity only needs overlap events and should not
   * physically push other entities during collision resolution.
   */
  public boolean isTrigger() { return false; }
}
