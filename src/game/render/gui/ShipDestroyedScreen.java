package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;

public class ShipDestroyedScreen extends Screen {   
    private TextElement title;

    public ShipDestroyedScreen(Game game) {
        title = new TextElement(Fonts.getTitleFont(), "Ship destroyed");
        items.add(title);
        addSelectableItem(new TextButton(Fonts.getButtonFont(), "Respawn", () -> game.respawn()));
    }
}
