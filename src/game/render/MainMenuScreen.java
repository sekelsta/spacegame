package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class MainMenuScreen extends Screen {
    public MainMenuScreen(Overlay overlay, Game game) {
        BitmapFont font = overlay.getButtonFont();
        buttons.add(new TextButton(font, "Single player", () -> game.enterWorld()));
        buttons.add(new TextButton(font, "Exit", () -> game.stop()));     
    }
}
