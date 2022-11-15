package sekelsta.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;

public class Gamepad {
    public final int joystickID;
    private GLFWGamepadState state;
    private GLFWGamepadState prev;
    private InputManager callback;

    public Gamepad(int joystickID, InputManager callback) {
        assert(callback != null);
        this.joystickID = joystickID;
        this.callback = callback;
        prev = GLFWGamepadState.create();
        state = GLFWGamepadState.create();
    }

    public void update() {
        GLFWGamepadState swap = prev;
        prev = state;
        state = swap;
        boolean success = GLFW.glfwGetGamepadState(joystickID, state);
        if (!success) {
            Log.warn("Error getting gamepad state");
        }
        for (int i = 0; i < GLFW.GLFW_GAMEPAD_BUTTON_LAST; ++i) {
            if (state.buttons(i) != prev.buttons(i)) {
                callback.processGamepadButton(i, state.buttons(i));
            }
        }
    }

    public float axis(int axis) {
        return state.axes(axis);
    }

    public float prevAxis(int axis) {
        return prev.axes(axis);
    }
}
