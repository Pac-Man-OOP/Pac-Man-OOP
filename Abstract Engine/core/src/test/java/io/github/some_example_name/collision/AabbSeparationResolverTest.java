package io.github.some_example_name.collision;

import io.github.some_example_name.entity.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for AabbSeparationResolver.
 *
 * Purpose:
 * - verify trigger entities do not physically move things
 * - verify static vs dynamic separation behaves correctly
 */
class AabbSeparationResolverTest {

    @Test
    void triggerCollision_doesNotMoveEntities() {
        AabbSeparationResolver resolver = new AabbSeparationResolver();

        TestEntity trigger = new TestEntity("trigger", 0, 0, 10, 10, false, true);
        TestEntity other = new TestEntity("other", 5, 0, 10, 10, false, false);

        Collider a = new Collider(trigger, 10, 10);
        Collider b = new Collider(other, 10, 10);

        // Trigger collisions should not physically separate entities
        resolver.resolve(a, b);

        assertEquals(0f, trigger.getX());
        assertEquals(5f, other.getX());
    }

    @Test
    void staticVsDynamic_onlyDynamicMoves() {
        AabbSeparationResolver resolver = new AabbSeparationResolver();

        TestEntity wall = new TestEntity("wall", 0, 0, 10, 10, true, false);
        TestEntity player = new TestEntity("player", 5, 0, 10, 10, false, false);

        Collider a = new Collider(wall, 10, 10);
        Collider b = new Collider(player, 10, 10);

        // With one static and one dynamic body, only the dynamic one should move
        resolver.resolve(a, b);

        assertEquals(0f, wall.getX());
        assertEquals(10f, player.getX());
    }

    /**
     * Minimal entity for testing static and trigger behaviour.
     */
    private static final class TestEntity extends Entity {
        private final boolean isStatic;
        private final boolean isTrigger;

        TestEntity(String id, float x, float y, float width, float height, boolean isStatic, boolean isTrigger) {
            super(id);
            setX(x);
            setY(y);
            setWidth(width);
            setHeight(height);
            this.isStatic = isStatic;
            this.isTrigger = isTrigger;
        }

        @Override
        public boolean isStatic() {
            return isStatic;
        }

        @Override
        public boolean isTrigger() {
            return isTrigger;
        }
    }
}