package sekelsta.engine.render;

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.InputManager;

// A wrapper for OpenGL's GLFW window
public class Window {
    private long window;
    private int width;
    private int height;
    private boolean focused;

    static {
        System.out.println("Using LWJGL " + Version.getVersion());
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public Window(int widthIn, int heightIn, String title) {
        this.width = widthIn;
        this.height = heightIn;
        // Set OpenGL version
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(widthIn, heightIn, title, MemoryUtil.NULL, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window.");
        }
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            // Get window size
            GLFW.glfwGetWindowSize(window, pWidth, pHeight);
            // Get screen size
            GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            // Center the window on the screen
            GLFW.glfwSetWindowPos(window, (mode.width() - pWidth.get(0)) / 2, (mode.height() - pHeight.get(0)) / 2);
        }

        // Listen for focus loss/gain events
        GLFW.glfwSetWindowFocusCallback(window, 
            (window, focused) -> this.focused = focused
        );

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
        GL.createCapabilities();
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    public void updateInput() {
        // Needed for the window to respond to events, e.g. user clicks the 'X'
        GLFW.glfwPollEvents();
    }

    public void close() {
		// Free the window callbacks and destroy the window
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

	    // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    public void setResizeListener(IFramebufferSizeListener listener) {
        listener.windowResized(width, height);
        // Handle resizing the window
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
                @Override
                public void invoke(long window, int widthIn, int heightIn) {
                    width = widthIn;
                    height = heightIn;
                    GL11.glViewport(0, 0, width, height);
                    listener.windowResized(width, height);
                }
            }
        );

    }

    public void setInput(InputManager input) {
        input.window = this;

        GLFW.glfwSetKeyCallback(window, 
            (window, key, scancode, action, mods) -> input.processKey(key, scancode, action, isFocused())
        );

        GLFW.glfwSetCharCallback(window,
            (window, codepoint) -> input.inputCharacter((char)codepoint)
        );

        GLFW.glfwSetCursorPosCallback(window,
            (window, xPos, yPos) -> input.moveCursor(xPos, yPos)
        );

        GLFW.glfwSetMouseButtonCallback(window,
            (window, button, action, mods) -> input.processMouseClick(button, action, mods)
        );

        GLFW.glfwSetScrollCallback(window,
            (window, xOffset, yOffset) -> input.processScroll(xOffset, yOffset)
        );

        GLFW.glfwSetJoystickCallback(
            (joystickID, event) -> input.joystickConnectionChanged(joystickID, event)
        );
    }

    public int getMouseButton(int button) {
        return GLFW.glfwGetMouseButton(window, button);
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private boolean isFocused() {
        return this.focused;
    }
}
