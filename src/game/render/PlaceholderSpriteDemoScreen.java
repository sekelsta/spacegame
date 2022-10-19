package sekelsta.game.render;

import sekelsta.engine.render.SpriteBatch;
import sekelsta.engine.render.Texture;

public class PlaceholderSpriteDemoScreen extends Screen {

    // DEBUG
    private SpriteBatch spriteBatch = new SpriteBatch(); // TODO: Release resources
    private final Texture connectingPlaceholder = new Texture("Connecting.png");
    // END DEBUG

    @Override
    public void blit(double screenWidth, double screenHeight) {
        super.blit(screenWidth, screenHeight);
        spriteBatch.setTexture(connectingPlaceholder);
        spriteBatch.blit((int)((screenWidth - 512) / 2), (int)((screenHeight - 256) / 2), 512, 256, 0, 0);
        spriteBatch.render();
    }
}
