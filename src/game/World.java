package sekelsta.game;

import java.util.*;
import java.util.stream.Collectors;
import sekelsta.engine.entity.*;
import sekelsta.game.entity.*;
import sekelsta.math.Vector3f;

public class World {
    private static final double spawnRadius = 1000;

    private boolean paused;

    // TODO: Per-world initial seed
    private Random random = new Random();
    private List<Movable> mobs;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    private List<Movable> killed = new ArrayList<>();
    private List<Movable> spawned = new ArrayList<>();

    private Spaceship localPlayer;

    public World() {
        this.mobs = new ArrayList<>();
    }

    public void spawnLocalPlayer(Controller playerController) {
        this.localPlayer = new Spaceship(0, 0, 0, this, playerController);
        localPlayer.skin = random.nextInt(Spaceship.NUM_SKINS);
        this.spawn(this.localPlayer);
    }

    public void togglePaused() {
        paused = !paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void update() {
        if (paused) {
            return;
        }
        // Spawn
        mobs.addAll(spawned);
        spawned.clear();
        // TODO: asteroid spawn conditions
        if (true) {
            Vector3f spawn = Vector3f.randomNonzero(new Vector3f(), random);
            spawn.scale((float)spawnRadius);
            Asteroid asteroid = new Asteroid(spawn.x, spawn.y, spawn.z, this);
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
        mobs.removeIf(mob -> mob.distSquared(0, 0, 0) > 100 * spawnRadius * spawnRadius);
    }

    public List<Movable> getMobs() {
        return mobs;
    }

    public Spaceship getLocalPlayer() {
        return localPlayer;
    }

    public Movable spawn(Movable mob) {
        assert(mob != null);
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
