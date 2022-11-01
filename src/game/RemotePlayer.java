package sekelsta.game;

import sekelsta.game.entity.Controller;

public class RemotePlayer implements Controller {
    public final long connectionID;

    public RemotePlayer(long connectionID) {
        this.connectionID = connectionID;
    }

    @Override
    public void update() {
        // Do nothing
    }
}
