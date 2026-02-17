package io.github.some_example_name.collision;

public interface ICollisionFilter {
    boolean canCollide(Collider a, Collider b);
}
