package sekelsta.game;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import sekelsta.engine.entity.*;
import sekelsta.engine.Particle;
import sekelsta.game.entity.*;
import sekelsta.game.network.ServerSpawnEntity;
import sekelsta.game.network.ServerRemoveEntity;
import sekelsta.game.network.EntityUpdate;
import shadowfox.math.Vector3f;

public class World implements IEntitySpace {
    private static final double spawnRadius = 1000;
    private static final int MOB_CAP = 400;

    public final boolean authoritative;
    private long tick = 0;
    private boolean paused;

    private Random random = new Random();
    private List<Entity> mobs;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    private List<Entity> killed = new ArrayList<>();
    private List<Entity> spawned = new ArrayList<>();

    private List<Particle> particles = new ArrayList<>();

    public Spaceship localPlayer;

    private int nextID = 0;

    private Game game;

    private Map<Integer, List<Consumer<Entity>>> onSpawnFunctions = new HashMap<>();

    public World(Game game, boolean authoritative) {
        this.game = game;
        this.authoritative = authoritative;
        this.mobs = new ArrayList<>();
    }

    public void spawnLocalPlayer(IController playerController) {
        this.localPlayer = new Spaceship(0, -100, 0, playerController);
        localPlayer.skin = random.nextInt(Spaceship.NUM_SKINS);
        this.spawn(this.localPlayer);
    }

    public void togglePaused() {
        paused = !paused;
        if (game.getNetworkManager() != null) {
            paused = false;
        }
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
        if (isNetworkServer()) {
            for (Entity mob : spawned) {
                ServerSpawnEntity spawnMessage = new ServerSpawnEntity(mob);
                game.getNetworkManager().queueBroadcast(spawnMessage);
            }
        }
        spawned.clear();

        if (authoritative && mobs.size() < MOB_CAP) {
            Vector3f spawn = Vector3f.randomNonzero(new Vector3f(), random);
            spawn.scale((float)spawnRadius);
            Asteroid asteroid = new Asteroid(spawn.x, spawn.y, spawn.z, random);
            this.spawn(asteroid);
            asteroid.setRandomVelocity();
        }

        // Update
        for (Entity mob : mobs) {
            mob.update();
            if (isNetworkServer()) {
                EntityUpdate message = new EntityUpdate(mob);
                game.getNetworkManager().queueBroadcast(message);
            }
        }

        List<Entity> collidableMobs = mobs.stream().filter(mob -> mob instanceof ICollider).collect(Collectors.toList());
        for (Entity collider : collidableMobs) {
            for (Entity collidee : mobs) {
                double tolerance = collider.getCollisionRadius() + collidee.getCollisionRadius();
                tolerance *= tolerance;
                double distSq = collider.distSquared(collidee);
                if (distSq < tolerance && collider != collidee) {
                    ((ICollider)collider).collide(collidee);
                }
            }
        }

        // Despawn
        for (Entity mob : mobs) {
            if (mob.distSquared(0, 0, 0) > 100 * spawnRadius * spawnRadius
                    && mob.mayDespawn()) {
                remove(mob);
            }
        }
        mobs.removeAll(killed);
        if (isNetworkServer()) {
            for (Entity mob : killed) {
                ServerRemoveEntity message = new ServerRemoveEntity(mob.getID());
                game.getNetworkManager().queueBroadcast(message);
            }
        }
        killed.clear();

        // Update particles
        ArrayList<Particle> removedParticles = new ArrayList<>();
        for (Particle particle : particles) {
            particle.update();
            if (particle.isDead()) {
                removedParticles.add(particle);
            }
        }
        particles.removeAll(removedParticles);
        removedParticles.clear();


        tick += 1;
    }

    public List<Entity> getMobs() {
        return mobs;
    }

    public List<Particle> getParticles() {
        return particles;
    }

    public Spaceship getLocalPlayer() {
        return localPlayer;
    }

    public <T extends Entity> T spawn(T entity) {
        this.spawned.add(entity);
        entity.enterWorld(this);
        if (authoritative) {
            entity.setID(nextID);
            nextID += 1;
        }

        if (onSpawnFunctions.containsKey(entity.getID())) {
            for (Consumer<Entity> function : onSpawnFunctions.get(entity.getID())) {
                function.accept(entity);
            }
            onSpawnFunctions.remove(entity.getID());
        }

        return entity;
    }

    public Entity remove(Entity entity) {
        this.killed.add(entity);
        return entity;
    }

    public Particle addParticle(Particle particle) {
        particles.add(particle);
        return particle;
    }

    public void runWhenEntitySpawns(Consumer<Entity> function, int id) {
        for (Entity mob : mobs) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }
        for (Entity mob : spawned) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }

        if (onSpawnFunctions.containsKey(id)) {
            onSpawnFunctions.get(id).add(function);
        }
        else {
            List<Consumer<Entity>> f = new ArrayList<>();
            f.add(function);
            onSpawnFunctions.put(id, f);
        }
    }

    public Entity getEntityByID(int id) {
        for (Entity mob : mobs) {
            if (mob.getID() == id) {
                return mob;
            }
        }
        return null;
    }

    public Random getRandom() {
        return random;
    }

    public long getCurrentTick() {
        return tick;
    }

    public void setTickIfClient(long tick) {
        if (authoritative) {
            return;
        }
        if (game.getNetworkManager() == null) {
            return;
        }
        this.tick = tick;
    }

    private boolean isNetworkServer() {
        return authoritative && game.getNetworkManager() != null;
    }
}
