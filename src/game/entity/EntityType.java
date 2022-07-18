package sekelsta.game.entity;

import java.util.HashMap;
import java.util.function.Supplier;

import sekelsta.game.entity.Entity;
import sekelsta.game.render.entity.EntityRenderer;

public class EntityType {
    private static final HashMap<String, EntityType> registry = new HashMap<>();

    // To be used as a network-unique and consistent identifier
    public final String name;

    // To allow for lazy loading, and unloading, of meshes
    private final Supplier<EntityRenderer> rendererSupplier;
    private EntityRenderer renderer = null;
    
    private EntityType(String name, Supplier<EntityRenderer> rendererSupplier) {
        this.name = name;
        this.rendererSupplier = rendererSupplier;
    }

    public static EntityType create(String name, Supplier<EntityRenderer> rendererSupplier) {
        EntityType type = new EntityType(name, rendererSupplier);
        registry.put(name, type);
        return type;
    }

    public EntityRenderer getRenderer() {
        if (renderer == null) {
            renderer = rendererSupplier.get();
        }
        return renderer;
    }
}
