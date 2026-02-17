package io.github.some_example_name.collision;

public interface ICollisionListener {
    void onCollisionEnter(Collider other);
    void onCollisionStay(Collider other);
    void onCollisionExit(Collider other);
}

