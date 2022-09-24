package sekelsta.game;

import org.lwjgl.glfw.GLFW;
import sekelsta.engine.InputManager;
import sekelsta.engine.render.Window;
import sekelsta.game.entity.Controller;
import sekelsta.game.entity.Spaceship;

public class Input extends InputManager implements Controller {
    Camera camera;
    Spaceship player;

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setPlayer(Spaceship player) {
        this.player = player;
    }

    @Override
    public void processKey(int key, int scancode, int action, boolean focused) {
        if (action == GLFW.GLFW_PRESS) {
            if (key == GLFW.GLFW_KEY_F11) {
                window.toggleFullscreen();
            }
            if (key == GLFW.GLFW_KEY_X) {
                player.fire();
            }
        }
    }

    @Override
    public void inputCharacter(char character) {
        //System.out.println(character);
    }

    @Override
    public void moveCursor(double xPos, double yPos) {
        if (window.getMouseButton(GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS
            // TEMP DEBUG
            || window.getMouseButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS
            ) {
            double diffX = xPos - prevCursorX;
            double diffY = yPos - prevCursorY;
            camera.addYaw((float)-diffX / 100f);
            camera.addPitch((float)diffY / 100f);
        }
        super.moveCursor(xPos, yPos);        
    }

    @Override
    public void processMouseClick(int button, int action, int mods) {
        if (action == GLFW.GLFW_PRESS) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                //camera.addYaw(0.1f);
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                //camera.addYaw(-0.1f);
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                //camera.addPitch(0.1f);
            }
        }
    }

    // With a standard mouse scroll wheel, yOffset will be 1 or -1
    // (up is positive, down is negative)
    @Override
    public void processScroll(double xOffset, double yOffset) {
        if (camera != null) {
            camera.scroll(-1 * yOffset);
        }
    }

    @Override
    public void joystickConnectionChanged(int joystickID, int event) {
        System.out.println("joystick connect/disconnect");
    }

    public void update() {
        if (window.isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            player.thrust();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_V)) {
            player.reverse();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_W)) {
            player.pitchUp();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_S)) {
            player.pitchDown();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_A)) {
            player.yawLeft();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_D)) {
            player.yawRight();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_Q)) {
            player.rollCounterclockwise();
        }
        if (window.isKeyDown(GLFW.GLFW_KEY_E)) {
            player.rollClockwise();
        }
    }
}
