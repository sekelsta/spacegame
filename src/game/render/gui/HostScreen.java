package sekelsta.game.render.gui;

import sekelsta.engine.Log;
import sekelsta.engine.network.RuntimeBindException;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class HostScreen extends PortEntryScreen {

    public HostScreen(Overlay overlay, Game game) {
        super(overlay, game);
        this.title = new TextElement(Fonts.getTitleFont(), "Host Multiplayer");
        BitmapFont font = Fonts.getButtonFont();
        this.done = new TextButton(font, "Done", () -> tryHostMultiplayer());
        selectable.add(portInput);
        selectable.add(done);
        selectable.add(cancel);
    }

    private void tryHostMultiplayer() {
        String strPort = portInput.getEnteredText();
        int port = tryParsePort(strPort);
        if (port == -1) {
            return;
        }

        try {
            game.allowConnections(port);
        }
        catch (RuntimeBindException e) {
            Log.error("Error: " + e.getMessage());
            error = "Error binding port";
            return;
        }
        if (!game.isInGame()) {
            game.enterWorld();
        }
        overlay.popScreenIfEquals(this);
    }

    @Override
    public boolean trigger() {
        if (super.trigger()) {
            return true;
        }

        tryHostMultiplayer();
        return true;
    }

    public void blit(double screenWidth, double screenHeight) {
        GuiElement selected = selectable.getSelected();
        int w = (int)screenWidth;
        int height = (int)(title.getHeight() * 1.25) 
                + (int)(portInput.getHeight() * 2) 
                + (int)(done.getHeight() * 1.25) 
                + cancel.getHeight();
        if (error != null) {
            height += (int)(errorFont.getHeight() * 1.25);
        }
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
        if (error != null) {
            int xPos = (w - errorFont.getWidth(error)) / 2;
            errorFont.blit(error, xPos, yPos, Fonts.ERROR_COLOR);
            yPos += (int)(errorFont.getHeight() * 1.25);
        }
        done.position((w - done.getWidth()) / 2, yPos);
        done.blit(done == selected);
        yPos += done.getHeight() + done.getHeight() / 4;
        cancel.position((w - cancel.getWidth()) / 2, yPos);
        cancel.blit(cancel == selected);
    }
}
