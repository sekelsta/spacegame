package sekelsta.game;

import sekelsta.engine.entity.Movable;

public class RemotePlayer extends RemoteController {
    public final long connectionID;

    public RemotePlayer(Movable entity, long connectionID) {
        super(entity);
        this.connectionID = connectionID;
    }
}
