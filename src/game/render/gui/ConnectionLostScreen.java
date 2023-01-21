package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectionLostScreen extends Screen {
    public ConnectionLostScreen(Game game) {        
        items.add(new TextElement(Fonts.getTitleFont(), "Connection lost"));
        addSelectableItem(new TextButton(Fonts.getButtonFont(), "Okay", () -> game.escape()));
    }
}
