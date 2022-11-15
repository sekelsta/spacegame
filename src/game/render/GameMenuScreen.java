package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class GameMenuScreen extends Screen {
    public GameMenuScreen(Overlay overlay, Game game) {
        BitmapFont font = overlay.getButtonFont();
        buttons.add(new TextButton(font, "Resume", () -> game.escape()));
        buttons.add(new TextButton(font, "Quit", () -> game.exitWorld())); 
    }
}
