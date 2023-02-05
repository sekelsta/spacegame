package sekelsta.game.render.gui;

import java.io.IOException;
import java.util.*;

import org.lwjgl.opengl.GL11;

import sekelsta.engine.render.*;
import sekelsta.engine.render.gui.TextButton;
import sekelsta.game.Game;
import shadowfox.math.Vector2f;

// For rendering 2D text and images in front of the world
public class Overlay {
    private Deque<Screen> screenStack = new ArrayDeque<>();
    private double xPointer, yPointer;
    private Game game;

    public Overlay(Game game) {
        this.game = game;
        screenStack.push(new MainMenuScreen(this, game));
    }

    public void pushScreen(Screen screen) {
        screenStack.push(screen);
        screen.positionPointer(xPointer, yPointer);
    }

    public void popScreen() {
        screenStack.pop();
        if (hasScreen()) {
            screenStack.peek().refresh();
        }
    }

    public void popScreenIfEquals(Screen screen) {
        if (hasScreen() && screenStack.peek().equals(screen)) {
            popScreen();
        }
    }

    public boolean hasScreen() {
        return screenStack.size() > 0;
    }

    public float getScale() {
        return game.getSettings().uiScale;
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
        xPointer = xPos * getScale();
        yPointer = yPos * getScale();
        if (hasScreen()) {
            screenStack.peek().positionPointer(xPointer, yPointer);
        }
    }

    public void escape(Game game) {
        if (screenStack.peek() instanceof MainMenuScreen) {
            return;
        }

        if (hasScreen()) {
            screenStack.peek().onEscape();
            popScreen();
        }
        else {
            pushScreen(new GameMenuScreen(this, game));
        }
    }

    public boolean trigger() {
        if (hasScreen()) {
            return screenStack.peek().trigger();
        }
        return false;
    }

    public boolean click() {
        if (hasScreen()) {
            return screenStack.peek().click(xPointer, yPointer);
        }
        return false;
    }

    public void holdLeftMouseButton() {
        if (hasScreen()) {
            screenStack.peek().holdLeftMouseButton(xPointer, yPointer);
        }
    }

    public void up() {
        if (hasScreen()) {
            screenStack.peek().up();
        }
    }

    public void down() {
        if (hasScreen()) {
            screenStack.peek().down();
        }
    }

    public void left() {
        if (hasScreen()) {
            screenStack.peek().left();
        }
    }

    public void right() {
        if (hasScreen()) {
            screenStack.peek().right();
        }
    }

    public void top() {
        if (hasScreen()) {
            screenStack.peek().top();
        }
    }

    public void bottom() {
        if (hasScreen()) {
            screenStack.peek().bottom();
        }
    }

    public void backspace() {
        if (hasScreen()) {
            screenStack.peek().backspace();
        }
    }

    public void inputCharacter(char character) {
        if (hasScreen()) {
            screenStack.peek().inputCharacter(character);
        }
    }

    public void tab() {
        if (hasScreen()) {
            screenStack.peek().tab();
        }
    }

    public void render(Vector2f uiDimensions) {
        if (hasScreen()) {
            screenStack.peek().blit(uiDimensions.x, uiDimensions.y);
        }
        Fonts.render();
    }
}
