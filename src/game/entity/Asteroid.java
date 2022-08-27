package sekelsta.game.entity;

import java.util.Random;
import sekelsta.engine.Position;
import sekelsta.game.World;
import sekelsta.math.Vector3f;

public class Asteroid extends Mob {
    public static final int NUM_MESH_VARIANTS = 4;

    int size;
    int mesh_variant;

    public Asteroid(long x, long y, long z, World world, int size) {
        super(Entities.ASTEROID, x, y, z, world);
        this.size = size;
        this.mesh_variant = world.getRandom().nextInt(NUM_MESH_VARIANTS);
    }

    public Asteroid(long x, long y, long z, World world) {
        this(x, y, z, world, world.getRandom().nextInt(4));
    }

    public int getSize() {
        return size;
    }

    public int getSizeScale() {
        return 1 << size;
    }

    public int getMeshVariant() {
        return mesh_variant;
    }

    @Override
    public int getCollisionRadius() {
        return getSizeScale() * super.getCollisionRadius();
    }

    private int randRange(Random random, int range) {
        return random.nextInt(2 * range) - range;
    }

    public void setRandomVelocity() {
        Random random = world.getRandom();
        int velocityCap = Position.RESOLUTION * 1;
        getPosition().accelerate(randRange(random, velocityCap), randRange(random, velocityCap), randRange(random, velocityCap));
        getPosition().setAngle(random.nextInt((int)Position.ANGLE_RESOLUTION), 
            random.nextInt((int)Position.ANGLE_RESOLUTION), random.nextInt((int)Position.ANGLE_RESOLUTION));
        int rotCap = (int)(Position.ANGLE_RESOLUTION) / 64;
        getPosition().angularAccelerate(randRange(random, rotCap), randRange(random, rotCap), randRange(random, rotCap));
    }

    public void destroy() {
        if (size > 0) {
            Vector3f split = Vector3f.randomNonzero(new Vector3f(), new Random());
            split.scale(Position.RESOLUTION / 2f);
            for (int i = -1; i < 2; i += 2) {
                Asteroid piece = new Asteroid(getPosition().getX(), getPosition().getY(), getPosition().getZ(), world, size - 1);
                piece.getPosition().setVelocity(getPosition().getVelocityX(), getPosition().getVelocityY(), getPosition().getVelocityZ());
                piece.getPosition().accelerate((int)(i * split.x), (int)(i * split.y), (int)(i * split.z));
                // TODO: Set angular velocity
                world.spawn(piece);
            }
        }
        world.kill(this);
    }
}
