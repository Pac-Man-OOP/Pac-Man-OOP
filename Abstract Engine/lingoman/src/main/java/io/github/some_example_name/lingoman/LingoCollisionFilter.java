package io.github.some_example_name.lingoman;

import io.github.some_example_name.collision.Collider;
import io.github.some_example_name.collision.ICollisionFilter;
import io.github.some_example_name.lingoman.entity.BossFireballEntity;
import io.github.some_example_name.lingoman.entity.FreezePickupEntity;
import io.github.some_example_name.lingoman.entity.GhostEntity;
import io.github.some_example_name.lingoman.entity.LetterEntity;
import io.github.some_example_name.lingoman.entity.PlayerEntity;
import io.github.some_example_name.lingoman.entity.ShockPickupEntity;
import io.github.some_example_name.lingoman.entity.WallBombEntity;
import io.github.some_example_name.lingoman.entity.WallEntity;

public class LingoCollisionFilter implements ICollisionFilter {

    @Override
    public boolean canCollide(Collider a, Collider b) {
        if (a == null || b == null) {
            return false;
        }
        Object oa = a.getOwner();
        Object ob = b.getOwner();
        if (oa == null || ob == null) {
            return false;
        }

        if (isFrozenGhostPlayerPair(oa, ob)) {
            return false;
        }

        if (oa instanceof WallEntity && ob instanceof WallEntity) {
            return false;
        }
        if (oa instanceof GhostEntity && ob instanceof GhostEntity) {
            return false;
        }
        if (oa instanceof LetterEntity && ob instanceof LetterEntity) {
            return false;
        }
        if (oa instanceof FreezePickupEntity && ob instanceof FreezePickupEntity) {
            return false;
        }
        if (oa instanceof ShockPickupEntity && ob instanceof ShockPickupEntity) {
            return false;
        }
        if (oa instanceof BossFireballEntity && ob instanceof BossFireballEntity) {
            return false;
        }
        if (oa instanceof WallBombEntity && ob instanceof WallBombEntity) {
            return false;
        }

        if (oa instanceof LetterEntity && !(ob instanceof PlayerEntity)) {
            return false;
        }
        if (ob instanceof LetterEntity && !(oa instanceof PlayerEntity)) {
            return false;
        }
        if (oa instanceof FreezePickupEntity && !(ob instanceof PlayerEntity)) {
            return false;
        }
        if (ob instanceof FreezePickupEntity && !(oa instanceof PlayerEntity)) {
            return false;
        }
        if (oa instanceof ShockPickupEntity && !(ob instanceof PlayerEntity)) {
            return false;
        }
        if (ob instanceof ShockPickupEntity && !(oa instanceof PlayerEntity)) {
            return false;
        }
        if (oa instanceof BossFireballEntity) {
            return ob instanceof PlayerEntity || ob instanceof WallEntity;
        }
        if (ob instanceof BossFireballEntity) {
            return oa instanceof PlayerEntity || oa instanceof WallEntity;
        }
        if (oa instanceof WallBombEntity) {
            return ob instanceof GhostEntity || ob instanceof PlayerEntity;
        }
        if (ob instanceof WallBombEntity) {
            return oa instanceof GhostEntity || oa instanceof PlayerEntity;
        }

        return true;
    }

    private boolean isFrozenGhostPlayerPair(Object a, Object b) {
        return (a instanceof GhostEntity ghostA && ghostA.isFrozen()
            && b instanceof PlayerEntity playerB && !playerB.hasShockPower())
            || (b instanceof GhostEntity ghostB && ghostB.isFrozen()
            && a instanceof PlayerEntity playerA && !playerA.hasShockPower());
    }
}
