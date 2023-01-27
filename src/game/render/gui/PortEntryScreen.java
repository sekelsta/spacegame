package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class PortEntryScreen extends Screen {
    protected Overlay overlay;
    protected Game game;

    protected TextElement title;
    protected TextElement portLabel;
    protected TextInput portInput;
    protected TextButton done;
    protected TextButton cancel;
    protected String error;
    protected BitmapFont errorFont;

    protected PortEntryScreen(Overlay overlay, Game game) {
        this.overlay = overlay;
        this.game = game;

        errorFont = Fonts.getButtonFont();
        this.portLabel = new TextElement(Fonts.getButtonFont(), "Enter port number:");
        this.portInput = new TextInput(Fonts.getButtonFont(), String.valueOf(Game.DEFAULT_PORT), "Port");
        this.cancel = new TextButton(Fonts.getButtonFont(), "Cancel", () -> game.escape());
    }

    protected int tryParsePort(String strPort) {
        if (strPort.equals("")) {
            error = "Enter a port number";
            return -1;
        }

        int port = 0;
        try {
            port = Integer.valueOf(strPort);
        }
        catch (NumberFormatException e) {
            error = "Could not parse port number";
            return -1;
        }

        if (port == 0) {
            error = "Enter a non-zero port number";
            return -1;
        }

        return port;
    }
}
