package sekelsta.game.render;

import java.awt.Font;
import java.io.IOException;
import java.util.*;

import org.lwjgl.opengl.GL11;

import sekelsta.engine.render.*;
import sekelsta.engine.render.gui.TextButton;
import sekelsta.engine.render.text.BitmapFont;
import sekelsta.game.Game;
import sekelsta.math.Vector2f;

// For rendering 2D text and images in front of the world
public class Overlay {
    private static final double scale = 1.0;
    private final BitmapFont font = new BitmapFont(new Font(Font.SANS_SERIF, Font.PLAIN, 48), true);
    private final SpriteBatch spriteBatch = new SpriteBatch();

    private Deque<Screen> screenStack = new ArrayDeque<>();
    private double xPointer, yPointer;

    public Overlay(Game game) {
        screenStack.push(new MainMenuScreen(this, game));
    }

    public void pushScreen(Screen screen) {
        // TODO: Tell previous screen it lost focus
        screenStack.push(screen);
        screen.positionPointer(xPointer, yPointer);
    }

    public void popScreen() {
        screenStack.pop();
    }

    public boolean hasScreen() {
        return screenStack.size() > 0;
    }

    public BitmapFont getButtonFont() {
        return font;
    }

    public static double getScale() {
        return scale;
    }

    public boolean isPaused() {
        for (Screen screen : screenStack) {
            if (screen.pausesGame()) {
                return true;
            }
        }
        return false;
    }

    public void positionPointer(double xPos, double yPos) {
        xPointer = xPos * scale;
        yPointer = yPos * scale;
        if (hasScreen()) {
            screenStack.peek().positionPointer(xPointer, yPointer);
        }
    }

    public void escape(Game game) {
        if (screenStack.peek() instanceof MainMenuScreen) {
            return;
        }

        if (hasScreen()) {
            popScreen();
        }
        else {
            pushScreen(new GameMenuScreen(this, game));
        }
    }

    // TODO: Don't repeat so much code
    public boolean trigger() {
        if (hasScreen()) {
            return screenStack.peek().trigger();
        }
        return false;
    }

    public boolean up() {
        if (hasScreen()) {
            return screenStack.peek().up();
        }
        return false;
    }

    public boolean down() {
        if (hasScreen()) {
            return screenStack.peek().down();
        }
        return false;
    }

    public boolean top() {
        if (hasScreen()) {
            return screenStack.peek().top();
        }
        return false;
    }

    public boolean bottom() {
        if (hasScreen()) {
            return screenStack.peek().bottom();
        }
        return false;
    }

    public void render(Vector2f uiDimensions) {
        if (hasScreen()) {
            screenStack.peek().blit(uiDimensions.x, uiDimensions.y);
        }

        font.render();
    }

    public void close() {
        font.clean();
        spriteBatch.clean();
    }
}
