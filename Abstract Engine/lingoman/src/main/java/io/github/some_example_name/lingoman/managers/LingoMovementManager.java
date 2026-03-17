package io.github.some_example_name.lingoman.managers;

import io.github.some_example_name.lingoman.entity.GhostEntity;
import io.github.some_example_name.managers.MovementManager;
import io.github.some_example_name.movement.Movable;

public final class LingoMovementManager extends MovementManager {

    @Override
    public boolean ownsEntityUpdateCycle(Movable entity) {
        return entity instanceof GhostEntity;
    }
}
