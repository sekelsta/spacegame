package sekelsta.game;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import sekelsta.engine.entity.*;
import sekelsta.game.entity.*;
import sekelsta.game.network.ServerSpawnEntity;
import sekelsta.game.network.ServerRemoveEntity;
import sekelsta.game.network.MobUpdate;
import sekelsta.math.Vector3f;

public class World implements IEntitySpace {
    private static final double spawnRadius = 1000;
    private static final int MOB_CAP = 400;

    public final boolean authoritative;
    private long tick = 0;
    private boolean paused;

    private Random random = new Random();
    private List<Movable> mobs;

    // Mobs to add/remove, to avoid concurrent modififation while updating
    private List<Movable> killed = new ArrayList<>();
    private List<Movable> spawned = new ArrayList<>();

    public Spaceship localPlayer;

    private int nextID = 0;

    private Game game;

    private Map<Integer, List<Consumer<Movable>>> onSpawnFunctions = new HashMap<>();

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
            for (Movable mob : spawned) {
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

        for (Movable mob : mobs) {
            mob.update();
            if (isNetworkServer()) {
                MobUpdate message = new MobUpdate(mob);
                game.getNetworkManager().queueBroadcast(message);
            }
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

        // Despawn
        for (Movable mob : mobs) {
            if (mob.distSquared(0, 0, 0) > 100 * spawnRadius * spawnRadius
                    && mob.mayDespawn()) {
                remove(mob);
            }
        }

        // Done iterating, safe to remove
        mobs.removeAll(killed);
        if (isNetworkServer()) {
            for (Movable mob : killed) {
                ServerRemoveEntity message = new ServerRemoveEntity(mob.getID());
                game.getNetworkManager().queueBroadcast(message);
            }
        }
        killed.clear();
        tick += 1;
    }

    public List<Movable> getMobs() {
        return mobs;
    }

    public Spaceship getLocalPlayer() {
        return localPlayer;
    }

    public <T extends Entity> T spawn(T entity) {
        if (entity instanceof Movable) {
            Movable mob = (Movable)entity;
            spawnMovable(mob);
            return entity;
        }
        throw new RuntimeException("TODO: not yet implemented");
    }

    private Movable spawnMovable(Movable mob) {
        this.spawned.add(mob);
        mob.enterWorld(this);
        if (authoritative) {
            mob.setID(nextID);
            nextID += 1;
        }

        if (onSpawnFunctions.containsKey(mob.getID())) {
            for (Consumer<Movable> function : onSpawnFunctions.get(mob.getID())) {
                function.accept(mob);
            }
            onSpawnFunctions.remove(mob.getID());
        }

        return mob;
    }

    public Movable remove(Movable mob) {
        this.killed.add(mob);
        return mob;
    }

    public void runWhenMovableSpawns(Consumer<Movable> function, int id) {
        for (Movable mob : mobs) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }
        for (Movable mob : spawned) {
            if (mob.getID() == id) {
                function.accept(mob);
                return;
            }
        }

        if (onSpawnFunctions.containsKey(id)) {
            onSpawnFunctions.get(id).add(function);
        }
        else {
            List<Consumer<Movable>> f = new ArrayList<>();
            f.add(function);
            onSpawnFunctions.put(id, f);
        }
    }

    public Movable getMovableByID(int id) {
        for (Movable mob : mobs) {
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
