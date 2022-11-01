package sekelsta.engine.entity;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import sekelsta.engine.render.entity.EntityRenderer;

public class EntityType {
    public static class EntityTypeNotRegisteredException extends RuntimeException {
        public EntityTypeNotRegisteredException() {}
        public EntityTypeNotRegisteredException(int id, int numRegistered) {
            super("EntityType " + id + " not registered (" + numRegistered + " types registered)");
        }
    };

    private static ArrayList<EntityType> types = new ArrayList<>();

    private final int id;

    private final Function<ByteBuffer, Entity> entityDecoder;
    // To allow for lazy loading, and unloading, of meshes
    private final Supplier<EntityRenderer> rendererSupplier;
    private EntityRenderer renderer = null;
    
    private EntityType(int id, Function<ByteBuffer, Entity> entityDecoder, Supplier<EntityRenderer> rendererSupplier) {
        this.id = id;
        this.entityDecoder = entityDecoder;
        this.rendererSupplier = rendererSupplier;
    }

    public static EntityType create(Function<ByteBuffer, Entity> entityDecoder, 
            Supplier<EntityRenderer> rendererSupplier) {
        EntityType type = new EntityType(types.size(), entityDecoder, rendererSupplier);
        types.add(type);
        return type;
    }

    public EntityRenderer getRenderer() {
        if (renderer == null) {
            renderer = rendererSupplier.get();
        }
        return renderer;
    }

    public int getID() {
        return id;
    }

    public static EntityType getByID(int id) {
        try {
            return types.get(id);
        }
        catch (IndexOutOfBoundsException e) {
            throw new EntityTypeNotRegisteredException(id, types.size());
        }
    }

    public Entity decode(ByteBuffer buffer) {
        return entityDecoder.apply(buffer);
    }
}
