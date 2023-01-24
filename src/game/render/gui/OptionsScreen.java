package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class OptionsScreen extends Screen {
    public OptionsScreen(Overlay overlay, Game game) {
        BitmapFont font = Fonts.getButtonFont();
        addSelectableItem(new TextButton(font, "Placeholder", () -> System.out.println("much placeholder, such wow")));
        addSelectableItem(new TextButton(font, "Credits", () -> overlay.pushScreen(new CreditsScreen(game))));
        addSelectableItem(new TextButton(font, "Done", () -> game.escape()));
    }

    @Override
    public boolean pausesGame() {
        return true;
    }
}
