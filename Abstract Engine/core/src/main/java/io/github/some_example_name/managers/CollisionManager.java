package io.github.some_example_name.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collections;


import io.github.some_example_name.collision.AabbSeparationResolver;
import io.github.some_example_name.collision.AllowAllCollisionFilter;
import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.ICollisionFilter;
import io.github.some_example_name.collision.ICollisionListener;
import io.github.some_example_name.collision.ICollisionResolver;
import io.github.some_example_name.lifecycle.Activatable;

/**
 * Detects collisions between registered {@link Collider} instances.
 *
 * <p>SRP: this class coordinates detection and lifecycle events.
 * Resolution and filtering are delegated to strategies.
 */
public class CollisionManager {

    private final List<Collider> colliders = new ArrayList<>();
    private final Set<CollisionPair> activeCollisions = new HashSet<>();

    private ICollisionFilter filter = new AllowAllCollisionFilter();
    private ICollisionResolver resolver = new AabbSeparationResolver();

    public void add(Collider c) {
        if (c == null) return;
        if (!colliders.contains(c)) {
            colliders.add(c);
        }
    }

    public void remove(Collider c) {
        if (c == null) return;
        colliders.remove(c);
        activeCollisions.removeIf(p -> p.a == c || p.b == c);
    }

    public void update() {
        checkCollisions();
    }

    /**
     * Set the collision filter policy (Open/Closed).
     */
    public void setFilter(ICollisionFilter filter) {
        if (filter == null) {
            this.filter = new AllowAllCollisionFilter();
            return;
        }
        this.filter = filter;
    }

    /**
     * Set the collision resolver policy.
     */
    public void setResolver(ICollisionResolver resolver) {
        if (resolver == null) {
            this.resolver = new AabbSeparationResolver();
            return;
        }
        this.resolver = resolver;
    }

    public List<Collider> getCollisions(Collider collider) {
        List<Collider> res = new ArrayList<>();
        if (collider == null) return res;

        if (!isUsable(collider)) return res;

        for (Collider other : colliders) {
            if (other == null || other == collider) continue;
            if (!isUsable(other)) continue;

            if (!filter.canCollide(collider, other)) continue;
            if (collider.getBounds().overlaps(other.getBounds())) {
                res.add(other);
            }
        }

        return res;
    }

    /**
     * Called when two colliders are detected to be overlapping.
     * Delegates to the resolver strategy if set, then fires collision events.
     */
    public void resolveCollision(Collider c1, Collider c2) {
        if (c1 == null || c2 == null) return;

        if (resolver != null) {
            resolver.resolve(c1, c2);
        }
    }

    private void fireStay(Collider self, Collider other) {
        ICollisionListener l = self.getListener();
        if (l != null) {
            l.onCollisionStay(other);
        }
    }

    public void checkCollisions() {
        Set<CollisionPair> current = new HashSet<>();
        List<Collider> snapshot = new ArrayList<>(colliders);

        for (int i = 0; i < snapshot.size(); i++) {
            Collider c1 = snapshot.get(i);
            if (!isUsable(c1)) continue;

            for (int j = i + 1; j < snapshot.size(); j++) {
                Collider c2 = snapshot.get(j);
                if (!isUsable(c2)) continue;

                if (!filter.canCollide(c1, c2)) continue;
                if (!c1.getBounds().overlaps(c2.getBounds())) continue;

                CollisionPair pair = new CollisionPair(c1, c2);
                current.add(pair);

                if (!activeCollisions.contains(pair)) {
                    resolveCollision(c1, c2);
                    fireEnter(c1, c2);
                    fireEnter(c2, c1);
                } else {
                    resolveCollision(c1, c2);
                    fireStay(c1, c2);
                    fireStay(c2, c1);
                }
            }
        }

        for (CollisionPair old : activeCollisions) {
            if (!current.contains(old)) {
                fireExit(old.a, old.b);
                fireExit(old.b, old.a);
            }
        }

        activeCollisions.clear();
        activeCollisions.addAll(current);
    }

    private boolean isUsable(Collider c) {
        if (c == null) return false;
        Object owner = c.getOwner();
        return owner != null && isActive(owner);
    }

    private boolean isActive(Object candidate) {
        return !(candidate instanceof Activatable activatable) || activatable.isActive();
    }

    private void fireEnter(Collider self, Collider other) {
        ICollisionListener l = self.getListener();
        if (l != null) l.onCollisionEnter(other);
    }

    private void fireExit(Collider self, Collider other) {
        ICollisionListener l = self.getListener();
        if (l != null) l.onCollisionExit(other);
    }

    /** Private nested helper to track collisions without adding a new public API class. */
    private static final class CollisionPair {
        final Collider a;
        final Collider b;

        CollisionPair(Collider c1, Collider c2) {
            int h1 = System.identityHashCode(c1);
            int h2 = System.identityHashCode(c2);
            if (h1 <= h2) {
                a = c1;
                b = c2;
            } else {
                a = c2;
                b = c1;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CollisionPair)) return false;
            CollisionPair p = (CollisionPair) o;
            return a == p.a && b == p.b;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(a) + System.identityHashCode(b);
        }
    }

    public List<Collider> getAllColliders() {return Collections.unmodifiableList(colliders);}

    public boolean isColliding(Collider c) {
        if (c == null) return false;
        for (CollisionPair p : activeCollisions) {
        if (p.a == c || p.b == c) return true;
        }
        return false;
    }

}
