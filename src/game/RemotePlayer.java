package sekelsta.game;

import sekelsta.engine.entity.Entity;

public class RemotePlayer extends RemoteController {
    public final long connectionID;

    public RemotePlayer(Entity entity, long connectionID) {
        super(entity);
        this.connectionID = connectionID;
        this.maxUpdatesStored = 8;
    }
}
