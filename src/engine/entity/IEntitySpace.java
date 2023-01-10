package sekelsta.engine.entity;

import java.util.Random;

public interface IEntitySpace {
    <T extends Entity> T spawn(T entity);
    Entity remove(Entity mob);
    Random getRandom();
}
