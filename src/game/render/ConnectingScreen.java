package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.gui.TextElement;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectingScreen extends Screen {
    private Game game;

    public ConnectingScreen(Overlay overlay, Game game, String address) {
        this.game = game;
        items.add(new TextElement(overlay.getTitleFont(), "Connecting..."));
        BitmapFont font = overlay.getButtonFont();
        items.add(new TextElement(font, address));
        this.addSelectableItem(new TextButton(font, "Cancel", () -> {
            game.cancelConnecting();
            overlay.popScreen();
        }));
    }

    @Override
    public void onEscape() {
        game.cancelConnecting();
    }
}
