package sekelsta.game.render;

import sekelsta.engine.Log;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class HostScreen extends Screen {
    private TextButton done;
    private TextButton cancel;

    public HostScreen(Overlay overlay, Game game) {
        BitmapFont font = overlay.getButtonFont();
        this.done = new TextButton(font, "Done", () -> tryHostMultiplayer(overlay, game, String.valueOf(Game.DEFAULT_PORT)));
        this.cancel = new TextButton(font, "Cancel", () -> game.escape());
        buttons.add(done);
        buttons.add(cancel);
    }

    private void tryHostMultiplayer(Overlay overlay, Game game, String strPort) {
        int port = 0;
        try {
            port = Integer.valueOf(strPort);
        }
        catch (NumberFormatException e) {
            Log.error("Could not parse port number:\n        " + e);
            return;
        }
        if (!game.isInGame()) {
            game.enterWorld();
        }
        game.allowConnections(port);
        overlay.popScreenIfEquals(this);
    }
}
