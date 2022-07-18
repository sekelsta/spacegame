package sekelsta.game;

import java.util.*;
import sekelsta.game.entity.*;

public class World {
    private static final int spawnRadius = 10000;

    // TODO: Per-world initial seed
    Random random = new Random();
    Spaceship player;
    List<Mob> mobs;
    // For entities that don't need to update
    List<Entity> entities;

    public World(Controller playerController) {
        this.player = new Spaceship(0, 0, 0, this, playerController);
        this.mobs = new ArrayList<>();
        this.entities = new ArrayList<>();
        this.mobs.add(this.player);
    }

    public void update() {
        // TODO: asteroid spawn conditions
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
        // TODO: Random rotation and maybe velocity
        int size = random.nextInt(9) + 1;
        this.spawn(new Asteroid(spawnX, spawnY, spawnZ, this, size));

        for (Mob mob : mobs) {
            mob.update();
        }

        mobs.removeIf(mob -> mob.getPosition().distSquared(player.getPosition()) > 100 * spawnRadius * spawnRadius);
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
        this.mobs.add(mob);
        return mob;
    }
}
