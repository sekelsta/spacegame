package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.gui.TextElement;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectingScreen extends Screen {
    public ConnectingScreen(Overlay overlay, Game game, String address) {
        items.add(new TextElement(overlay.getTitleFont(), "Connecting..."));
        BitmapFont font = overlay.getButtonFont();
        items.add(new TextElement(font, address));
        this.addSelectableItem(new TextButton(font, "Cancel", () -> {
            game.cancelConnecting();
            overlay.popScreen();
        }));
    }
}
