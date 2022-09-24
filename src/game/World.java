package sekelsta.game;

import java.util.*;
import java.util.stream.Collectors;
import sekelsta.engine.entity.*;
import sekelsta.game.entity.*;

public class World {
    private static final double spawnRadius = 1000;

    // TODO: Per-world initial seed
    Random random = new Random();
    Spaceship player;
    List<Movable> mobs;
    // For entities that don't need to update
    List<Entity> entities;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    List<Movable> killed = new ArrayList<>();
    List<Movable> spawned = new ArrayList<>();

    public World(Controller playerController) {
        this.player = new Spaceship(0, 0, 0, this, playerController);
        this.mobs = new ArrayList<>();
        this.entities = new ArrayList<>(); // TODO: Unused, remove / change w/ mobs
        this.mobs.add(this.player);
    }

    public void update() {
        // Spawn
        mobs.addAll(spawned);
        spawned.clear();
        // TODO: asteroid spawn conditions
        if (true) {
            double spawnX = 0;
            double spawnY = 0;
            double spawnZ = 0;
            boolean farEnough = false;
            while (!farEnough) {
                spawnX = (random.nextDouble() - 0.5) * 2 * spawnRadius;
                spawnY = (random.nextDouble() - 0.5) * 2 * spawnRadius;
                spawnZ = (random.nextDouble() - 0.5) * 2 * spawnRadius;

                farEnough = this.player.distSquared(spawnX, spawnY, spawnZ) > spawnRadius * spawnRadius / 100;
            }

            spawnX += this.player.getX();
            spawnY += this.player.getY();
            spawnZ += this.player.getZ();

            Asteroid asteroid = new Asteroid(spawnX, spawnY, spawnZ, this);
            asteroid.setRandomVelocity();
            this.spawn(asteroid);
        }

        for (Movable mob : mobs) {
            mob.update();
        }

        List<Movable> collidableMobs = mobs.stream().filter(mob -> mob instanceof ICollider).collect(Collectors.toList());
        for (Movable collider : collidableMobs) {
            for (Movable collidee : mobs) {
                double tolerance = collider.getCollisionRadius() + collidee.getCollisionRadius();
                tolerance *= tolerance;
                double distSq = collider.distSquared(collidee);
                if (distSq < tolerance && collider != collidee) {
                    ((ICollider)collider).collide(collidee);
                }
            }
        }

        // Done iterating, safe to remove
        mobs.removeAll(killed);
        killed.clear();
        // Despawn
        mobs.removeIf(mob -> mob.distSquared(player) > 100 * spawnRadius * spawnRadius);
    }

    public List<Movable> getMobs() {
        return mobs;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Spaceship getPlayer() {
        return player;
    }

    public Movable spawn(Movable mob) {
        this.spawned.add(mob);
        return mob;
    }

    public Movable kill(Movable mob) {
        this.killed.add(mob);
        return mob;
    }

    public Random getRandom() {
        return random;
    }
}
