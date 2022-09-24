package sekelsta.game.entity;

import sekelsta.engine.entity.EntityType;
import sekelsta.game.render.entity.*;

public class Entities {
    public static final EntityType SPACESHIP = EntityType.create("spaceship", SpaceshipRenderer::new);
    public static final EntityType PROJECTILE = EntityType.create("projectile", ProjectileRenderer::new);
    public static final EntityType ASTEROID = EntityType.create("asteroid", AsteroidRenderer::new);
}
