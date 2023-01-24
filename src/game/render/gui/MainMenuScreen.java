package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class MainMenuScreen extends Screen {
    public MainMenuScreen(Overlay overlay, Game game) {
        BitmapFont font = Fonts.getButtonFont();
        addSelectableItem(new TextButton(font, "Single player", () -> game.enterWorld()));
        addSelectableItem(new TextButton(font, "Host and play", 
            () -> overlay.pushScreen(new HostScreen(overlay, game)))
        );
        addSelectableItem(new TextButton(font, "Join server", 
            () -> overlay.pushScreen(new JoinScreen(overlay, game)))
        );
        addSelectableItem(new TextButton(font, "Options", () -> overlay.pushScreen(new OptionsScreen(overlay, game))));
        addSelectableItem(new TextButton(font, "Exit", () -> game.stop()));     
    }
}
