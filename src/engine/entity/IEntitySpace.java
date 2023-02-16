package sekelsta.engine.entity;

import java.util.Random;

import sekelsta.engine.Particle;

public interface IEntitySpace {
    <T extends Entity> T spawn(T entity);
    Entity remove(Entity mob);
    Random getRandom();
    Particle addParticle(Particle particle);
    long getCurrentTick();
}
