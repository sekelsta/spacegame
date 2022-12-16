package sekelsta.game.render;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import sekelsta.engine.Log;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class JoinScreen extends Screen {
    private TextButton done;
    private TextButton cancel;
    private Overlay overlay;
    private Game game;

    public JoinScreen(Overlay overlay, Game game) {
        this.overlay = overlay;
        this.game = game;
        BitmapFont font = overlay.getButtonFont();
        this.done = new TextButton(font, "Done", () -> tryJoinServer());
        this.cancel = new TextButton(font, "Cancel", () -> game.escape());
        addSelectableItem(done);
        addSelectableItem(cancel);
    }

    private void tryJoinServer() {
        String strAddress = "127.0.0.1";
        String strPort = String.valueOf(Game.DEFAULT_PORT);

        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName(strAddress);
        }
        catch (UnknownHostException e) {
            Log.error("Could not parse IP address:\n        " + e);
            return;
        }

        int port = 0;
        try {
            port = Integer.valueOf(strPort);
        }
        catch (NumberFormatException e) {
            Log.error("Could not parse port number:\n        " + e);
            return;
        }

        game.joinServer(new InetSocketAddress(netAddress, port));
        overlay.pushScreen(new ConnectingScreen(overlay));
    }
}
