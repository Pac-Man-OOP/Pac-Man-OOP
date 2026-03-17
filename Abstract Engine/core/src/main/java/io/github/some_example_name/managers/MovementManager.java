package io.github.some_example_name.managers;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import io.github.some_example_name.lifecycle.Activatable;
import io.github.some_example_name.movement.Movable;
import io.github.some_example_name.movement.behaviour.MovementBehaviour;
import io.github.some_example_name.movement.physics.MovementPhysics;

/**
 * Coordinates movement for all registered {@link Movable} entities.
 *
 * <p>Important design rule (Abstract Engine): this manager only coordinates.
 * The "dirty work" lives in {@link MovementBehaviour} (movement decision) and
 * {@link MovementPhysics} (integration/calculation).
 */
public class MovementManager {

    private final List<Movable> movableEntities = new ArrayList<>();
    private final Map<Movable, MovementBehaviour> behaviours = new IdentityHashMap<>();
    private final MovementPhysics physics = new MovementPhysics();

    /** Register an entity so it is included in {@link #updateAll(float)}. */
    public void registerEntity(Movable entity) {
        if (entity == null) return;
        if (!movableEntities.contains(entity)) {
            movableEntities.add(entity);
        }
    }

    /** Unregister an entity so it is no longer updated. */
    public void unregisterEntity(Movable entity) {
        if (entity == null) return;
        movableEntities.remove(entity);
        behaviours.remove(entity);
    }

    /** Assign a movement behaviour to an entity (Strategy Pattern). */
    public void assignBehaviour(Movable entity, MovementBehaviour behaviour) {
        if (entity == null) return;
        if (behaviour == null) {
            behaviours.remove(entity);
            entity.setMovementBehaviour(null);
            return;
        }
        behaviours.put(entity, behaviour);
        entity.setMovementBehaviour(behaviour);
    }

    /**
     * Update all registered entities.
     *
     * <ol>
     *   <li>Ask the entity's behaviour to update velocity/direction/state</li>
     *   <li>Delegate integration (position update) to MovementPhysics</li>
     * </ol>
     */
    public void updateAll(float deltaTime) {
        if (deltaTime <= 0) return;

        for (Movable entity : new ArrayList<>(movableEntities)) {
            if (entity == null) continue;
            if (!isActive(entity)) continue;

            MovementBehaviour behaviour = behaviours.get(entity);
            if (behaviour == null) {
                behaviour = entity.getMovementBehaviour();
            }

            if (behaviour != null) {
                behaviour.move(entity, deltaTime);
            }

            physics.integrate(entity, deltaTime);
        }
    }

    /** Exposes the physics helper (useful for configuration/testing). */
    public MovementPhysics getPhysics() {
        return physics;
    }

    /**
     * Allows subclasses to declare that a movable's per-frame update logic is
     * fully driven by this manager instead of the entity manager.
     */
    public boolean ownsEntityUpdateCycle(Movable entity) {
        return false;
    }

    private boolean isActive(Object candidate) {
        return !(candidate instanceof Activatable activatable) || activatable.isActive();
    }
}
