package io.github.some_example_name.collision;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.entity.Entity;

/**
 * Minimal AABB positional separation resolver.
 *
 * <p>This keeps entities from staying overlapped, which helps prevent repeated
 * collision triggers and jitter.
 */
public final class AabbSeparationResolver implements ICollisionResolver {

    @Override
    public void resolve(Collider a, Collider b) {
        if (a == null || b == null) return;

        Rectangle ra = a.getBounds();
        Rectangle rb = b.getBounds();

        float overlapX = Math.min(ra.x + ra.width, rb.x + rb.width) - Math.max(ra.x, rb.x);
        float overlapY = Math.min(ra.y + ra.height, rb.y + rb.height) - Math.max(ra.y, rb.y);

        if (overlapX <= 0f || overlapY <= 0f) return;

        Entity ea = a.getOwner();
        Entity eb = b.getOwner();

        if (ea.isTrigger() || eb.isTrigger()) return;

        boolean aStatic = ea.isStatic();
        boolean bStatic = eb.isStatic();

        // If both are static, nothing to resolve
        if (aStatic && bStatic) return;

        if (overlapX < overlapY) {
            if (!aStatic && !bStatic) {
                // Both movable — split equally
                float separation = overlapX * 0.5f;
                if (ra.x < rb.x) {
                    ea.setX(ea.getX() - separation);
                    eb.setX(eb.getX() + separation);
                } else {
                    ea.setX(ea.getX() + separation);
                    eb.setX(eb.getX() - separation);
                }
            } else if (!aStatic) {
                // Only push a the full amount
                ea.setX(ra.x < rb.x ? ea.getX() - overlapX : ea.getX() + overlapX);
            } else {
                // Only push b the full amount
                eb.setX(rb.x < ra.x ? eb.getX() - overlapX : eb.getX() + overlapX);
            }
            return;
        }

        if (!aStatic && !bStatic) {
            float separation = overlapY * 0.5f;
            if (ra.y < rb.y) {
                ea.setY(ea.getY() - separation);
                eb.setY(eb.getY() + separation);
            } else {
                ea.setY(ea.getY() + separation);
                eb.setY(eb.getY() - separation);
            }
        } else if (!aStatic) {
            ea.setY(ra.y < rb.y ? ea.getY() - overlapY : ea.getY() + overlapY);
        } else {
            eb.setY(rb.y < ra.y ? eb.getY() - overlapY : eb.getY() + overlapY);
        }
    }
}
