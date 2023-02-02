package sekelsta.game.render.gui;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.Texture;
import sekelsta.engine.render.gui.*;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;

public class OptionsScreen extends Screen {
    private SpriteBatch spritebatch = new SpriteBatch();
    private Texture texture = new Texture("ui.png");
    private Slider slider;

    public OptionsScreen(Overlay overlay, Game game) {
        BitmapFont font = Fonts.getButtonFont();
        items.add(new TextElement(font, "Audio volume:"));
        slider = new Slider(
            game.getSettings().getVolume(),
            () -> game.getSettings().setVolume(slider.getValue())
        );
        addSelectableItem(slider);
        addSelectableItem(new TextButton(font, "Credits", () -> overlay.pushScreen(new CreditsScreen(game))));
        addSelectableItem(new TextButton(font, "Done", () -> game.escape()));
    }

    @Override
    public boolean pausesGame() {
        return true;
    }

    @Override
    public void blit(double screenWidth, double screenHeight) {
        spritebatch.setTexture(texture);

        int height = 0;
        for (int i = 0; i < items.size(); ++i) {
            int h = items.get(i).getHeight();
            height += h;
            if (i + 1 != items.size()) {
                height += h / 4;
            }
        }
        int yPos = ((int)screenHeight - height) / 2;
        GuiElement selected = selectable.getSelected();
        for (GuiElement item : items) {
            item.position(((int)screenWidth - item.getWidth()) / 2, yPos);
            yPos += (int)(1.25 * item.getHeight());
            item.blit(spritebatch, item == selected);
        }

        spritebatch.render();
    }
}
