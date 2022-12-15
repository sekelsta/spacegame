package sekelsta.game.render;

import sekelsta.engine.render.text.BitmapFont;

public class ConnectingScreen extends Screen {
    public BitmapFont font;

    public ConnectingScreen(Overlay overlay) {
        this.font = overlay.getButtonFont();
    }

    @Override
    public void blit(double screenWidth, double screenHeight) {
        String text = "Connecting...";
        int x = (int)((screenWidth - font.getWidth(text)) / 2);
        int y = (int)((screenHeight - font.getHeight()) / 2);
        font.blit(text, x, y);
    }
}
