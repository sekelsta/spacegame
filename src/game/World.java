package sekelsta.game;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import sekelsta.engine.entity.*;
import sekelsta.engine.Particle;
import sekelsta.game.entity.*;
import sekelsta.game.network.*;
import shadowfox.math.Vector3f;

public class World implements IEntitySpace {
    private static final double spawnRadius = 1000;
    private static final int MOB_CAP = 400;

    public final Vector3f lightPos = new Vector3f(0, 0, 0);
    public final float sunRadius = 100;

    public final boolean authoritative;
    private long tick = 0;
    private boolean paused;

    private Random random = new Random();
    private List<Entity> mobs;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    private List<Entity> killed = new ArrayList<>();
    private List<Entity> spawned = new ArrayList<>();

    private List<Particle> particles = new ArrayList<>();

    // Players that died and haven't respawned yet
    private List<Spaceship> limbo = new ArrayList<>();

    public Spaceship localPlayer;

    private int nextID = 0;

    private Game game;

    private Map<Integer, List<Consumer<Entity>>> onSpawnFunctions = new HashMap<>();

    private List<List<Runnable>> delayedActions = new ArrayList<>();

    public World(Game game, boolean authoritative) {
        this.game = game;
        this.authoritative = authoritative;
        this.mobs = new ArrayList<>();
    }

    public void moveToSpawnPoint(Entity entity) {
        entity.setVelocity(0, 0, 0);
        entity.scaleAngularVelocity(0);
        float angle = random.nextFloat() * 2 * (float)Math.PI;
        float dist = 400;
        entity.teleport(dist * (float)Math.cos(angle), dist * (float)Math.sin(angle), 0);
        float yaw = random.nextFloat() * Entity.TAU;
        entity.snapToAngle(yaw, 0, 0);
    }

    public void spawnLocalPlayer(IController playerController) {
        this.localPlayer = new Spaceship(playerController);
        moveToSpawnPoint(localPlayer);
        localPlayer.skin = random.nextInt(Spaceship.NUM_SKINS);
        this.spawn(this.localPlayer);
    }

    public Spaceship respawn(Long connectionID) {
        assert(authoritative);
        Spaceship pawn = null;
        for (Spaceship ship : limbo) {
            if (ship.isControlledBy(connectionID)) {
                pawn = ship;
                break;
            }
        }
        moveToSpawnPoint(pawn);
        if (connectionID != null) {
            pawn.setController(new RemotePlayer(pawn, connectionID));
        }
        limbo.remove(pawn);
        spawn(pawn);
        return pawn;
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

    public void runDelayed(Runnable runnable, int delayTicks) {
        while (delayTicks >= delayedActions.size()) {
            delayedActions.add(new ArrayList<Runnable>());
        }
        delayedActions.get(delayTicks).add(runnable);
    }

    public void update() {
        if (paused) {
            return;
        }

        if (delayedActions.size() > 0) {
            for (Runnable action : delayedActions.get(0)) {
                action.run();
            }
            delayedActions.remove(0);
        }

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

        if (authoritative) {
            // Collide
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
                double d = mob.distSquared(0, 0, 0);
                if (d > 100 * spawnRadius * spawnRadius
                        && mob.mayDespawn()) {
                    remove(mob);
                }
                float warning_factor = 9;
                if (d < sunRadius * sunRadius) {
                    if (mob instanceof Spaceship) {
                        destroyShip((Spaceship)mob);
                    }
                    else {
                        remove(mob);
                    }
                }
                else if (d < warning_factor * sunRadius * sunRadius) {
                    double p = 1 - d / (warning_factor * sunRadius * sunRadius);
                    p *= mob.getCollisionRadius() * mob.getCollisionRadius() * mob.getCollisionRadius();
                    p /= 3;
                    int r = random.nextFloat() > p % 1? 0 : 1;
                    int numParticles = r + (int)p;
                    for (int i = 0; i < numParticles; ++i) {
                        int lifespan = random.nextInt(20) + 20;
                        float x = (float)mob.getX() + 0.5f * (random.nextFloat() - 0.5f) * (float)mob.getCollisionRadius();
                        float y = (float)mob.getY() + 0.5f * (random.nextFloat() - 0.5f) * (float)mob.getCollisionRadius();
                        float z = (float)mob.getZ() + 0.5f * (random.nextFloat() - 0.5f) * (float)mob.getCollisionRadius();
                        Particle particle = new Particle(x, y, z, lifespan);
                        Vector3f v = Vector3f.randomNonzero(random);
                        v.scale(0.1f);
                        particle.setVelocity(mob.getVelocityX() + v.x, mob.getVelocityY() + v.y, mob.getVelocityZ() + v.z);
                        addParticle(particle);
                    }
                }
            }
        }

        mobs.removeAll(killed);
        if (isNetworkServer()) {
            for (Entity mob : killed) {
                ServerRemoveEntity message = new ServerRemoveEntity(mob.getID());
                game.getNetworkManager().queueBroadcast(message);
                mob.enterWorld(null);
            }
        }
        killed.clear();


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

    public void clientSpawn(Entity entity) {
        if (authoritative) {
            spawn(entity);
        }
        else {
            ClientSpawnEntity message = new ClientSpawnEntity(entity);
            game.getNetworkManager().queueBroadcast(message);
        }
    }

    public Entity remove(Entity entity) {
        if (entity instanceof Spaceship && ((Spaceship)entity).isLocalPlayer()) {
            game.onLocalPlayerShipDestroyed();
        }

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

    @Override
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

    public void destroyShip(Spaceship ship) {
        assert(authoritative);
        ship.explode();

        if (isNetworkServer()) {
            ServerExplodeShip message = new ServerExplodeShip(ship.getID());
            game.getNetworkManager().queueBroadcast(message);
        }

        remove(ship);
        limbo.add(ship);
    }

    public boolean isDead(Entity entity) {
        if (killed.contains(entity)) {
            return true;
        }
        if (spawned.contains(entity)) {
            return false;
        }
        if (mobs.contains(entity)) {
            return false;
        }
        return true;
    }

    private boolean isNetworkServer() {
        return authoritative && game.getNetworkManager() != null;
    }
}
