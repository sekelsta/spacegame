package sekelsta.game.entity;

import java.nio.ByteBuffer;
import java.util.Random;

import sekelsta.engine.entity.*;
import sekelsta.engine.network.ByteVector;
import sekelsta.math.Vector3f;

public class Asteroid extends Movable {
    public static final int NUM_MESH_VARIANTS = 4;

    protected int size;
    protected int mesh_variant;

    public Asteroid(double x, double y, double z, Random random, int size) {
        super(x, y, z);
        this.size = size;
        this.mesh_variant = random.nextInt(NUM_MESH_VARIANTS);
    }

    public Asteroid(double x, double y, double z, Random random) {
        this(x, y, z, random, random.nextInt(4));
    }

    public Asteroid(ByteBuffer buffer) {
        super(buffer);
        size = buffer.getInt();
        mesh_variant = buffer.getInt();
    }

    @Override
    public void encode(ByteVector buffer) {
        super.encode(buffer);
        buffer.putInt(size);
        buffer.putInt(mesh_variant);
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
    public double getCollisionRadius() {
        return getSizeScale();
    }

    @Override
    public EntityType getType() {
        return Entities.ASTEROID;
    }

    private float randRange(Random random, float range) {
        return random.nextFloat() * 2 * range - range;
    }

    public void setRandomVelocity() {
        Random random = world.getRandom();
        float velocityCap = (float)Movable.ONE_METER;
        accelerate(randRange(random, velocityCap), randRange(random, velocityCap), randRange(random, velocityCap));
        setAngle(random.nextFloat() * Movable.ANGLE_RESOLUTION, 
            random.nextFloat() * Movable.ANGLE_RESOLUTION, random.nextFloat() * Movable.ANGLE_RESOLUTION);
        float rotCap = Movable.ANGLE_RESOLUTION / 64f;
        angularAccelerate(randRange(random, rotCap), randRange(random, rotCap), randRange(random, rotCap));
    }

    public void destroy() {
        if (size > 0) {
            Vector3f split = Vector3f.randomNonzero(new Vector3f(), world.getRandom());
            for (int i = -1; i < 2; i += 2) {
                Asteroid piece = new Asteroid(getX(), getY(), getZ(), world.getRandom(), size - 1);
                piece.setVelocity(getVelocityX(), getVelocityY(), getVelocityZ());
                piece.accelerate((int)(i * split.x), (int)(i * split.y), (int)(i * split.z));
                // TODO #28: Set angular velocity
                world.spawn(piece);
            }
        }
        world.remove(this);
    }
}
