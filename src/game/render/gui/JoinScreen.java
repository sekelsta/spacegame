package sekelsta.game.render.gui;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.*;

import sekelsta.engine.Log;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class JoinScreen extends PortEntryScreen {
    private TextElement addressLabel;
    private TextInput addressInput;

    public JoinScreen(Overlay overlay, Game game) {
        super(overlay, game);
        this.title = new TextElement(Fonts.getTitleFont(), "Join Server");
        BitmapFont font = Fonts.getButtonFont();
        this.addressLabel = new TextElement(font, "Enter server IP address:");
        this.addressInput = new TextInput(font, game.getSettings().lastJoinedIP, "IP address");
        this.done = new TextButton(font, "Done", () -> tryJoinServer());
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
            error = "Enter an IP address";
            selectable.setTextFocus(addressInput);
            return;
        }

        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName(strAddress);
        }
        catch (UnknownHostException e) {
            error = "Error parsing IP address";
            selectable.setTextFocus(addressInput);
            return;
        }

        int port = tryParsePort(strPort);
        if (port == -1) {
            selectable.setTextFocus(portInput);
            return;
        }

        game.joinServer(new InetSocketAddress(netAddress, port));
        overlay.pushScreen(new ConnectingScreen(overlay, game, strAddress + ":" + strPort));

        game.getSettings().lastJoinedIP = strAddress;
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
        return super.click(xPos, yPos);
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
                + cancel.getHeight();
        if (error != null) {
            height += (int)(errorFont.getHeight() * 1.25);
        }
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
