package sekelsta.game.entity;

import java.util.Random;
import sekelsta.engine.Position;
import sekelsta.game.World;
import sekelsta.math.Vector3f;

public class Asteroid extends Mob {
    int size;

    public Asteroid(long x, long y, long z, World world, int s) {
        super(Entities.ASTEROID, x, y, z, world);
        size = s;
    }

    private int randRange(Random random, int range) {
        return random.nextInt(2 * range) - range;
    }

    public Asteroid(long x, long y, long z, World world) {
        this(x, y, z, world, 1);
    }

    public Asteroid(long x, long y, long z, World world, Random random) {
        this(x, y, z, world, random.nextInt(4));
    }

    public int getSize() {
        return size;
    }

    public int getSizeScale() {
        return 1 << size;
    }

    @Override
    public int getCollisionRadius() {
        return getSizeScale() * super.getCollisionRadius();
    }

    public void setRandomVelocity(Random random) {
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
