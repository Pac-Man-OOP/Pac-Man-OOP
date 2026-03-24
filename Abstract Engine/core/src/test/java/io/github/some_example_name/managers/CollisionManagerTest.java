package io.github.some_example_name.managers;

import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.ICollisionListener;
import io.github.some_example_name.collision.ICollisionResolver;
import io.github.some_example_name.entity.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CollisionManager.
 *
 * Purpose:
 * - verify ENTER, STAY, and EXIT collision events
 * - verify a collision pair is resolved only once per update
 * - verify inactive entities are ignored
 */
class CollisionManagerTest {

    @Test
    void stayCollision_resolvesOnlyOncePerPairPerUpdate() {
        // Create the manager under test
        CollisionManager manager = new CollisionManager();

        // CountingResolver lets us verify how many times physical resolution happened
        CountingResolver resolver = new CountingResolver();
        manager.setResolver(resolver);

        // Create two overlapping test entities
        TestEntity a = new TestEntity("a", 0, 0, 10, 10);
        TestEntity b = new TestEntity("b", 5, 0, 10, 10);

        // Attach colliders to those entities
        Collider ca = new Collider(a, 10, 10);
        Collider cb = new Collider(b, 10, 10);

        // ListenerSpy lets us count ENTER / STAY / EXIT calls
        ListenerSpy la = new ListenerSpy();
        ListenerSpy lb = new ListenerSpy();
        ca.setListener(la);
        cb.setListener(lb);

        manager.add(ca);
        manager.add(cb);

        // First update, collision should be treated as ENTER
        manager.update();

        assertEquals(1, resolver.calls);
        assertEquals(1, la.enterCount);
        assertEquals(1, lb.enterCount);
        assertEquals(0, la.stayCount);
        assertEquals(0, lb.stayCount);

        // Second update, same overlap should now be treated as STAY
        manager.update();

        assertEquals(2, resolver.calls);
        assertEquals(1, la.stayCount);
        assertEquals(1, lb.stayCount);
    }

    @Test
    void exitCollision_firesWhenEntitiesSeparate() {
        CollisionManager manager = new CollisionManager();

        TestEntity a = new TestEntity("a", 0, 0, 10, 10);
        TestEntity b = new TestEntity("b", 5, 0, 10, 10);

        Collider ca = new Collider(a, 10, 10);
        Collider cb = new Collider(b, 10, 10);

        ListenerSpy la = new ListenerSpy();
        ListenerSpy lb = new ListenerSpy();
        ca.setListener(la);
        cb.setListener(lb);

        manager.add(ca);
        manager.add(cb);

        // First update, collision exists so ENTER should occur
        manager.update();
        assertEquals(1, la.enterCount);
        assertEquals(0, la.exitCount);

        // Move one entity away, then update again to trigger EXIT
        b.setX(30);
        manager.update();

        assertEquals(1, la.exitCount);
        assertEquals(1, lb.exitCount);
    }

    @Test
    void inactiveEntity_isIgnored() {
        CollisionManager manager = new CollisionManager();

        TestEntity a = new TestEntity("a", 0, 0, 10, 10);
        TestEntity b = new TestEntity("b", 5, 0, 10, 10);

        // Mark entity b inactive so it should not participate in collision checks
        b.setActive(false);

        Collider ca = new Collider(a, 10, 10);
        Collider cb = new Collider(b, 10, 10);

        ListenerSpy la = new ListenerSpy();
        ListenerSpy lb = new ListenerSpy();
        ca.setListener(la);
        cb.setListener(lb);

        manager.add(ca);
        manager.add(cb);

        manager.update();

        assertEquals(0, la.enterCount);
        assertEquals(0, lb.enterCount);
        assertFalse(manager.isColliding(ca));
        assertFalse(manager.isColliding(cb));
    }

    /**
     * Minimal test entity used only for unit tests.
     * This avoids needing full player/ghost game objects.
     */
    private static final class TestEntity extends Entity {
        TestEntity(String id, float x, float y, float width, float height) {
            super(id);
            setX(x);
            setY(y);
            setWidth(width);
            setHeight(height);
        }
    }

    /**
     * Spy object that counts how many collision callbacks were triggered.
     */
    private static final class ListenerSpy implements ICollisionListener {
        int enterCount;
        int stayCount;
        int exitCount;

        @Override
        public void onCollisionEnter(Collider other) {
            enterCount++;
        }

        @Override
        public void onCollisionStay(Collider other) {
            stayCount++;
        }

        @Override
        public void onCollisionExit(Collider other) {
            exitCount++;
        }
    }

    /**
     * Fake resolver used to count how many times physical resolution is called.
     */
    private static final class CountingResolver implements ICollisionResolver {
        int calls;

        @Override
        public void resolve(Collider a, Collider b) {
            calls++;
        }
    }
}