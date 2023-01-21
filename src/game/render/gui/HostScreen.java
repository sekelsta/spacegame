package sekelsta.game.render.gui;

import sekelsta.engine.Log;
import sekelsta.engine.network.RuntimeBindException;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class HostScreen extends Screen {
    private TextElement title;
    private TextElement portLabel;
    private TextInput portInput;
    private TextButton done;
    private TextButton cancel;

    public HostScreen(Overlay overlay, Game game) {
        this.title = new TextElement(Fonts.getTitleFont(), "Host Multiplayer");
        BitmapFont font = Fonts.getButtonFont();
        this.portLabel = new TextElement(font, "Enter port number:");
        this.portInput = new TextInput(font, String.valueOf(Game.DEFAULT_PORT), "Port");
        this.done = new TextButton(font, "Done", () -> tryHostMultiplayer(overlay, game, portInput.getEnteredText()));
        this.cancel = new TextButton(font, "Cancel", () -> game.escape());
        selectable = new SelectableElementList();
        selectable.add(portInput);
        selectable.add(done);
        selectable.add(cancel);
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

        try {
            game.allowConnections(port);
        }
        catch (RuntimeBindException e) {
            Log.error("Error: " + e.getMessage());
            return;
        }
        if (!game.isInGame()) {
            game.enterWorld();
        }
        overlay.popScreenIfEquals(this);
    }

    public void blit(double screenWidth, double screenHeight) {
        GuiElement selected = selectable.getSelected();
        int w = (int)screenWidth;
        int height = (int)(title.getHeight() * 1.25) 
                + (int)(portInput.getHeight() * 2) 
                + (int)(done.getHeight() * 1.25) 
                + (int)(cancel.getHeight() * 1.25);
        int yPos = ((int)screenHeight - height) / 2;
        title.position((w - title.getWidth()) / 2, yPos);
        title.blit(false);
        yPos += title.getHeight() + title.getHeight() / 4;
        portLabel.position((w - portLabel.getWidth()) / 2, yPos);
        portLabel.blit(false);
        yPos += (int)(1.25 * portLabel.getHeight());
        portInput.position((w - portInput.getWidth()) / 2, yPos);
        portInput.blit(true);
        yPos += 2 * portInput.getHeight();
        done.position((w - done.getWidth()) / 2, yPos);
        done.blit(done == selected);
        yPos += done.getHeight() + done.getHeight() / 4;
        cancel.position((w - cancel.getWidth()) / 2, yPos);
        cancel.blit(cancel == selected);
    }
}
