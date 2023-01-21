package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.gui.TextElement;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectingScreen extends Screen {
    private Game game;

    public ConnectingScreen(Overlay overlay, Game game, String address) {
        this.game = game;
        items.add(new TextElement(Fonts.getTitleFont(), "Connecting..."));
        BitmapFont font = Fonts.getButtonFont();
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
