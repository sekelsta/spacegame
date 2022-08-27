package sekelsta.game;

import java.util.*;
import java.util.stream.Collectors;
import sekelsta.engine.Position;
import sekelsta.game.entity.*;

public class World {
    private static final int spawnRadius = 10000;

    // TODO: Per-world initial seed
    Random random = new Random();
    Spaceship player;
    List<Mob> mobs;
    // For entities that don't need to update
    List<Entity> entities;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    List<Mob> killed = new ArrayList<>();
    List<Mob> spawned = new ArrayList<>();

    public World(Controller playerController) {
        this.player = new Spaceship(0, 0, 0, this, playerController);
        this.mobs = new ArrayList<>();
        this.entities = new ArrayList<>(); // TODO: Unused, remove / change w/ mobs
        this.mobs.add(this.player);
    }

    public void update() {
        // TODO: asteroid spawn conditions
        if (true) {
            int spawnX = 0;
            int spawnY = 0;
            int spawnZ = 0;
            boolean farEnough = false;
            while (!farEnough) {
                spawnX = random.nextInt(2 * spawnRadius) - spawnRadius;
                spawnY = random.nextInt(2 * spawnRadius) - spawnRadius;
                spawnZ = random.nextInt(2 * spawnRadius) - spawnRadius;

                farEnough = this.player.getPosition().distSquared(spawnX, spawnY, spawnZ) > spawnRadius * spawnRadius / 100;
            }

            spawnX += this.player.getPosition().getX();
            spawnY += this.player.getPosition().getY();
            spawnZ += this.player.getPosition().getZ();

            Asteroid asteroid = new Asteroid(spawnX, spawnY, spawnZ, this);
            asteroid.setRandomVelocity();
            this.spawn(asteroid);
        }

        for (Mob mob : mobs) {
            mob.update();
        }

        List<Mob> collidableMobs = mobs.stream().filter(mob -> mob.hasCollisions()).collect(Collectors.toList());
        for (Mob collider : collidableMobs) {
            for (Mob collidee : mobs) {
                int tolerance = collider.getCollisionRadius() + collidee.getCollisionRadius();
                tolerance *= tolerance;
                long distSq = collider.getPosition().distSquared(collidee.getPosition());
                if (distSq < tolerance && collider != collidee) {
                    collider.collide(collidee);
                }
            }
        }

        // Done iterating, safe to remove
        mobs.removeAll(killed);
        killed.clear();
        // Despawn
        mobs.removeIf(mob -> mob.getPosition().distSquared(player.getPosition()) > 100 * spawnRadius * spawnRadius);
        // Spawn
        mobs.addAll(spawned);
        spawned.clear();
    }

    public List<Mob> getMobs() {
        return mobs;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public Spaceship getPlayer() {
        return player;
    }

    public Mob spawn(Mob mob) {
        this.spawned.add(mob);
        return mob;
    }

    public Mob kill(Mob mob) {
        this.killed.add(mob);
        return mob;
    }

    public Random getRandom() {
        return random;
    }
}
