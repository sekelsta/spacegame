package sekelsta.game.entity;

import sekelsta.engine.entity.EntityType;
import sekelsta.game.entity.*;
import sekelsta.game.render.entity.*;

public class Entities {
    public static final EntityType SPACESHIP = EntityType.create(Spaceship::new, SpaceshipRenderer::new);
    public static final EntityType PROJECTILE = EntityType.create(Projectile::new, ProjectileRenderer::new);
    public static final EntityType ASTEROID = EntityType.create(Asteroid::new, AsteroidRenderer::new);

    public static void init() {}
}
