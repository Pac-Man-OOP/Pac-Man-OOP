package io.github.some_example_name.lingoman.world;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.ICollisionListener;
import io.github.some_example_name.entity.Entity;
import io.github.some_example_name.entity.EntityManager;
import io.github.some_example_name.lingoman.LingoCollisionFilter;
import io.github.some_example_name.managers.CollisionManager;
import io.github.some_example_name.managers.MovementManager;
import io.github.some_example_name.managers.OutputManager;
import io.github.some_example_name.movement.Movable;
import io.github.some_example_name.movement.behaviour.MovementBehaviour;

/**
 * Coordinates gameplay entities and keeps add/remove logic in one place.
 *
 * <p>The scene can focus on rules while this class manages engine registration
 * for rendering, movement, collisions, and deferred cleanup.
 */
public final class LingoWorld {

    private final EntityManager entityManager = new EntityManager();
    private final CollisionManager collisionManager = new CollisionManager();
    private final MovementManager movementManager = new MovementManager();
    private final Set<Entity> entities = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Entity, Collider> collidersByEntity = new IdentityHashMap<>();
    private final Set<Movable> movables = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
    private final List<Entity> pendingRemoval = new ArrayList<>();

    public LingoWorld() {
        collisionManager.setFilter(new LingoCollisionFilter());
    }

    public void addEntity(Entity entity) {
        if (entity == null) {
            return;
        }
        entity.setActive(true);
        if (entities.add(entity)) {
            entityManager.add(entity);
        }

        if (entity instanceof Movable movable && movables.add(movable)) {
            movementManager.registerEntity(movable);
        }
    }

    public Collider addCollidableEntity(Entity entity, ICollisionListener listener) {
        if (entity == null) {
            return null;
        }

        addEntity(entity);
        Collider existing = collidersByEntity.get(entity);
        if (existing != null) {
            existing.setListener(listener);
            return existing;
        }

        Collider collider = new Collider(entity, entity.getWidth(), entity.getHeight());
        collider.setListener(listener);
        collidersByEntity.put(entity, collider);
        collisionManager.add(collider);
        return collider;
    }

    public void assignBehaviour(Movable movable, MovementBehaviour behaviour) {
        if (movable == null) {
            return;
        }
        if (movables.add(movable)) {
            movementManager.registerEntity(movable);
        }
        movementManager.assignBehaviour(movable, behaviour);
    }

    public void removeEntity(Entity entity) {
        if (entity == null || pendingRemoval.contains(entity)) {
            return;
        }
        entity.setActive(false);
        pendingRemoval.add(entity);
    }

    public Collider getCollider(Entity entity) {
        return collidersByEntity.get(entity);
    }

    public void update(float deltaTime) {
        entityManager.update(deltaTime);
        movementManager.updateAll(deltaTime);
        collisionManager.update();
        flushPendingRemoval();
    }

    public void render(OutputManager outputManager) {
        entityManager.render(outputManager);
    }

    public void clear() {
        pendingRemoval.clear();

        for (Collider collider : new ArrayList<>(collidersByEntity.values())) {
            collisionManager.remove(collider);
        }
        entities.clear();
        collidersByEntity.clear();

        for (Movable movable : new ArrayList<>(movables)) {
            movementManager.unregisterEntity(movable);
        }
        movables.clear();

        entityManager.clear();
    }

    private void flushPendingRemoval() {
        if (pendingRemoval.isEmpty()) {
            return;
        }

        for (Entity entity : new ArrayList<>(pendingRemoval)) {
            Collider collider = collidersByEntity.remove(entity);
            if (collider != null) {
                collisionManager.remove(collider);
            }

            if (entity instanceof Movable movable && movables.remove(movable)) {
                movementManager.unregisterEntity(movable);
            }

            entities.remove(entity);
            entityManager.remove(entity);
        }

        pendingRemoval.clear();
    }
}
