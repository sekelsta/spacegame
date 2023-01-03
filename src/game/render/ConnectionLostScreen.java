package sekelsta.game.render;

import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class ConnectionLostScreen extends Screen {
    public ConnectionLostScreen(Overlay overlay, Game game) {        
        items.add(new TextElement(overlay.getTitleFont(), "Connection lost"));
        addSelectableItem(new TextButton(overlay.getButtonFont(), "Okay", () -> game.escape()));
    }
}
