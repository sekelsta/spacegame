package sekelsta.engine;

import org.lwjgl.glfw.GLFW;
import sekelsta.engine.render.Window;

public class InputManager {
    // See
    // https://www.glfw.org/docs/latest/input_guide.html
    // for GLFW input info

    public Window window;

    protected double prevCursorX;
    protected double prevCursorY;

    public void processKey(int key, int scancode, int action, int mods) {
    }

    public void inputCharacter(char character) {
    }

    public void moveCursor(double xPos, double yPos) {
        prevCursorX = xPos;
        prevCursorY = yPos;
    }

    public void processMouseClick(int button, int action, int mods) {
    }

    public void processScroll(double xOffset, double yOffset) {
    }

    public void joystickConnectionChanged(int joystickID, int event) {
    }

    public void processGamepadButton(int button, int action) {
    }
}
