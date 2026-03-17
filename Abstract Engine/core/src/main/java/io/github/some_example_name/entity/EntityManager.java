package io.github.some_example_name.entity;

import io.github.some_example_name.managers.OutputManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public final class EntityManager {

  private final List<Entity> entities = new ArrayList<>();
  private final List<Entity> pendingAdd = new ArrayList<>();
  private final List<Entity> pendingRemove = new ArrayList<>();
  private final Set<Entity> externallyManagedUpdates = Collections.newSetFromMap(new IdentityHashMap<>());
  private boolean locked = false;

  public void add(Entity e) {
    if (e == null)
      return;
    if (locked)
      pendingAdd.add(e);
    else
      entities.add(e);
  }

  public void remove(Entity e) {
    if (e == null)
      return;
    if (locked)
      pendingRemove.add(e);
    else {
      entities.remove(e);
      externallyManagedUpdates.remove(e);
    }
  }

  public List<Entity> getAll() {
    return Collections.unmodifiableList(entities);
  }

  public void clear() {
    if (locked) {
      pendingRemove.addAll(entities);
      pendingAdd.clear();
      return;
    }
    entities.clear();
    pendingAdd.clear();
    pendingRemove.clear();
    externallyManagedUpdates.clear();
  }

  public void setExternallyManagedUpdate(Entity entity, boolean externallyManaged) {
    if (entity == null) {
      return;
    }
    if (externallyManaged) {
      externallyManagedUpdates.add(entity);
    } else {
      externallyManagedUpdates.remove(entity);
    }
  }

  public void update(float dt) {
    locked = true;

    for (int i = 0; i < entities.size(); i++) {
      Entity e = entities.get(i);
      if (e.isActive() && !externallyManagedUpdates.contains(e))
        e.update(dt);
    }

    locked = false;
    flushQueues();

    entities.removeIf(e -> !e.isActive());
    externallyManagedUpdates.retainAll(entities);
  }

  public void render(OutputManager out) {
    locked = true;

    for (int i = 0; i < entities.size(); i++) {
      Entity e = entities.get(i);
      if (e.isActive())
        e.render(out);
    }

    locked = false;
    flushQueues();
  }

  private void flushQueues() {
    if (!pendingRemove.isEmpty()) {
      entities.removeAll(pendingRemove);
      externallyManagedUpdates.removeAll(pendingRemove);
      pendingRemove.clear();
    }
    if (!pendingAdd.isEmpty()) {
      entities.addAll(pendingAdd);
      pendingAdd.clear();
    }
  }
  public Entity getById(String id) {
    if (id == null) {
      return null;
    }
    for (Entity e : entities) {
      if (id.equals(e.getId()))
        return e;
    }
    return null;
  }
}
