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
    private TextElement title;
    private TextElement addressLabel;
    private TextElement portLabel;
    private TextInput addressInput;
    private TextInput portInput;
    private TextButton done;
    private TextButton cancel;
    private Overlay overlay;
    private Game game;

    public JoinScreen(Overlay overlay, Game game) {
        this.overlay = overlay;
        this.game = game;
        this.title = new TextElement(Fonts.getTitleFont(), "Join Server");
        BitmapFont font = Fonts.getButtonFont();
        this.addressLabel = new TextElement(font, "Enter server IP address:");
        this.portLabel = new TextElement(font, "Enter port number:");
        this.addressInput = new TextInput(font, "", "IP address");
        this.portInput = new TextInput(font, String.valueOf(Game.DEFAULT_PORT), "Port");
        this.done = new TextButton(font, "Done", () -> tryJoinServer());
        this.cancel = new TextButton(font, "Cancel", () -> game.escape());
        selectable = new SelectableElementList();
        selectable.add(addressInput);
        selectable.add(portInput);
        selectable.add(done);
        selectable.add(cancel);
    }

    private void tryJoinServer() {
        String strAddress = addressInput.getEnteredText();
        String strPort = portInput.getEnteredText();

        if (strAddress.equals("")) {
            Log.error("IP address is empty");
            selectable.setTextFocus(addressInput);
            return;
        }

        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName(strAddress);
        }
        catch (UnknownHostException e) {
            Log.error("Could not parse IP address:\n        " + e);
            selectable.setTextFocus(addressInput);
            return;
        }

        int port = 0;
        try {
            port = Integer.valueOf(strPort);
        }
        catch (NumberFormatException e) {
            Log.error("Could not parse port number:\n        " + e);
            selectable.setTextFocus(portInput);
            return;
        }

        game.joinServer(new InetSocketAddress(netAddress, port));
        overlay.pushScreen(new ConnectingScreen(overlay, game, strAddress + ":" + strPort));
    }

    public void positionPointer(double xPos, double yPos) {
        selectable.clearSelection();
        selectable.selectByPointer(xPos, yPos);
    }

    @Override
    public boolean trigger() {
        if (selectable.isLastTextInputFocused()) {
            tryJoinServer();
            return true;
        }

        selectable.tab();
        return true;
    }

    @Override
    public boolean click(double xPos, double yPos) {
        boolean used = selectable.focusTextByPointer(xPos, yPos);
        if (used) {
            return true;
        }
        return super.trigger();
    }

    public void blit(double screenWidth, double screenHeight) {
        GuiElement selected = selectable.getSelected();
        GuiElement textFocus = selectable.getTextFocus();
        int w = (int)screenWidth;
        int height = (int)(title.getHeight() * 1.25)
                + (int)(1.25 * addressLabel.getHeight())
                + (int)(1.25 * addressInput.getHeight())
                + (int)(1.25 * portLabel.getHeight())
                + portInput.getHeight() * 2
                + (int)(done.getHeight() * 1.25)
                + (int)(cancel.getHeight() * 1.25);
        int yPos = ((int)screenHeight - height) / 2;
        title.position((w - title.getWidth()) / 2, yPos);
        title.blit(false);
        yPos += title.getHeight() + title.getHeight() / 4;
        addressLabel.position((w - addressLabel.getWidth()) / 2, yPos);
        addressLabel.blit(false);
        yPos += (int)(1.25 * addressLabel.getHeight());
        addressInput.position((w - addressInput.getWidth()) / 2, yPos);
        addressInput.blit(addressInput == textFocus);
        yPos += (int)(1.25 * addressInput.getHeight());
        portLabel.position((w - portLabel.getWidth()) / 2, yPos);
        portLabel.blit(false);
        yPos += (int)(1.25 * portLabel.getHeight());
        portInput.position((w - portInput.getWidth()) / 2, yPos);
        portInput.blit(portInput == textFocus);
        yPos += 2 * portInput.getHeight();
        done.position((w - done.getWidth()) / 2, yPos);
        done.blit(done == selected);
        yPos += done.getHeight() + done.getHeight() / 4;
        cancel.position((w - cancel.getWidth()) / 2, yPos);
        cancel.blit(cancel == selected);
    }
}
