package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class OptionsScreen extends Screen {
    public OptionsScreen(BitmapFont font, Game game) {
        addSelectableItem(new TextButton(font, "Placeholder", () -> System.out.println("much placeholder, such wow")));
        addSelectableItem(new TextButton(font, "Credits", () -> System.out.println("Not yet implemented: credits")));
        addSelectableItem(new TextButton(font, "Done", () -> game.escape()));
    }

    @Override
    public boolean pausesGame() {
        return true;
    }
}
