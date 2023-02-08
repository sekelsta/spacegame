package sekelsta.game.render.gui;

import sekelsta.engine.render.gui.*;
import sekelsta.game.Game;

public class ShipDestroyedScreen extends Screen {   
    private TextElement title;

    public ShipDestroyedScreen(Game game, Overlay overlay) {
        title = new TextElement(Fonts.getTitleFont(), "Ship destroyed");
        items.add(title);
        addSelectableItem(new TextButton(Fonts.getButtonFont(), "Respawn", () -> respawn(game, overlay)));
    }

    private void respawn(Game game, Overlay overlay) {
        game.respawn();
        overlay.popScreenIfEquals(this);
    }
}
