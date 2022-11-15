package sekelsta.game.render;

import sekelsta.engine.render.gui.TextButton;

public class Screen {
    protected TextButtonList buttons = new TextButtonList();

    public boolean pausesGame() {
        return false;
    }

    public void positionPointer(double xPos, double yPos) {
        buttons.positionPointer(xPos, yPos);
    }

    public boolean trigger() {
        return buttons.trigger();
    }

    public boolean up() {
        return buttons.up();
    }

    public boolean down() {
        return buttons.down();
    }

    public boolean top() {
        return buttons.top();
    }

    public boolean bottom() {
        return buttons.bottom();
    }

    public void blit(double screenWidth, double screenHeight) {
        int xPos = ((int)screenWidth - buttons.getWidth()) / 2;
        int yPos = ((int)screenHeight - buttons.getHeight()) / 2;
        buttons.position(xPos, yPos);
        buttons.blit();
    }
}
