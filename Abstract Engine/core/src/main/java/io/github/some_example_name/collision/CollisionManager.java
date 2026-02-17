package io.github.some_example_name.collision;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollisionManager {
    private final List<Collider> colliders = new ArrayList<>();
    private final Set<CollisionPair> active = new HashSet<>(); // private nested helper
    private ICollisionFilter filter;

    public void add(Collider c) {
        if (c == null) return;
        if (!colliders.contains(c)) colliders.add(c);
    }

    public void remove(Collider c) {
        if (c == null) return;
        colliders.remove(c);
        active.removeIf(p -> p.a == c || p.b == c);
    }

    public void update() {
        checkCollision();
    }

    public void setFilter(ICollisionFilter filter) {
        this.filter = filter;
    }

    public List<Collider> getCollisions(Collider collider) {
        List<Collider> res = new ArrayList<>();
        if (collider == null) return res;

        for (Collider other : colliders) {
            if (other == null || other == collider) continue;

            if (filter != null && !filter.canCollide(collider, other)) continue;

            if (collider.getBounds().overlaps(other.getBounds())) {
                res.add(other);
            }
        }
        return res;
    }

    public void resolve(Collider c1, Collider c2) {
        ICollisionListener l = c1.getListener();
        if (l != null) l.onCollisionStay(c2);
    }

    private void checkCollision() {
        Set<CollisionPair> current = new HashSet<>();

        for (int i = 0; i < colliders.size(); i++) {
            Collider c1 = colliders.get(i);
            if (c1 == null) continue;

            for (int j = i + 1; j < colliders.size(); j++) {
                Collider c2 = colliders.get(j);
                if (c2 == null) continue;

                if (filter != null && !filter.canCollide(c1, c2)) continue;

                if (!c1.getBounds().overlaps(c2.getBounds())) continue;

                CollisionPair pair = new CollisionPair(c1, c2);
                current.add(pair);

                if (!active.contains(pair)) {
                    fireEnter(c1, c2);
                    fireEnter(c2, c1);
                } else {
                    resolve(c1, c2);
                    resolve(c2, c1);
                }
            }
        }

        for (CollisionPair old : active) {
            if (!current.contains(old)) {
                fireExit(old.a, old.b);
                fireExit(old.b, old.a);
            }
        }

        active.clear();
        active.addAll(current);
    }

    private void fireEnter(Collider self, Collider other) {
        ICollisionListener l = self.getListener();
        if (l != null) l.onCollisionEnter(other);
    }

    private void fireExit(Collider self, Collider other) {
        ICollisionListener l = self.getListener();
        if (l != null) l.onCollisionExit(other);
    }

    // Internal pair-key, not part of public engine API
    private static final class CollisionPair {
        final Collider a;
        final Collider b;

        CollisionPair(Collider c1, Collider c2) {
            int h1 = System.identityHashCode(c1);
            int h2 = System.identityHashCode(c2);
            if (h1 <= h2) { a = c1; b = c2; }
            else { a = c2; b = c1; }
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
}
