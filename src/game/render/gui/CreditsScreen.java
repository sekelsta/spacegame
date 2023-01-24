package sekelsta.game.render.gui;

import java.util.Scanner;

import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.gui.TextElement;
import sekelsta.game.Game;

public class CreditsScreen extends Screen {
    public CreditsScreen(Game game) {
        try (Scanner scanner = new Scanner(CreditsScreen.class.getResourceAsStream("/assets/credits.txt"))) {
            scanner.useDelimiter("\n");
            while(scanner.hasNext()) {
                items.add(new TextElement(Fonts.getTextFont(), scanner.next()));
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        addSelectableItem(new TextButton(Fonts.getButtonFont(), "Back", () -> game.escape()));
    }

    @Override
    public boolean pausesGame() {
        return true;
    }
}
