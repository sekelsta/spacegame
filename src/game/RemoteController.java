package sekelsta.game;

import java.util.*;

import sekelsta.engine.entity.IController;
import sekelsta.engine.entity.Entity;

public class RemoteController implements IController {

    private final Entity entity;
    private LinkedList<Entity> pastUpdates = new LinkedList<>();
    private long lastUpdateTick;
    protected int maxUpdatesStored = 4;

    public RemoteController(Entity entity) {
        this.entity = entity;
    }

    public void handleUpdateMessage(long tick, Entity entity) {
        if (tick > lastUpdateTick) {
            for (long i = lastUpdateTick; i < tick - 1; ++i) {
                pastUpdates.add(null);
            }
            pastUpdates.add(entity);
            lastUpdateTick = tick;
            while (pastUpdates.size() > maxUpdatesStored || pastUpdates.getFirst() == null) {
                pastUpdates.removeFirst();
            }
        }
        else if (pastUpdates.size() > lastUpdateTick - tick) {
            int index = (int)(pastUpdates.size() - 1 - lastUpdateTick + tick);
            pastUpdates.set(index, entity);
        }
    }

    @Override
    public void preUpdate() {
        if (pastUpdates.size() == 0) {
            return;
        }

        // Minus 1, because our entity hasn't ticked yet
        long currentTick = entity.getWorld().getCurrentTick() - 1;
        if (currentTick == lastUpdateTick) {
            // Handle updates that came in just one tick late
            entity.updateFrom(pastUpdates.getLast());
        }
        else if (currentTick > lastUpdateTick) {
            entity.updateFromLate(pastUpdates.getLast(), (int)(currentTick - lastUpdateTick));
        }
    }

    @Override
    public void postUpdate() {
        long currentTick = entity.getWorld().getCurrentTick();
        int index = pastUpdates.size() - 1 + (int)(currentTick - lastUpdateTick);
        if (index >= 0 && index < pastUpdates.size() && pastUpdates.get(index) != null) {
            // Handle updates that came in too early or right on time
            entity.updateFrom(pastUpdates.get(index));
        }
    }
}
