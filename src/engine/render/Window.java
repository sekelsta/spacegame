package sekelsta.engine.render;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.Version;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowPosCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import sekelsta.engine.InitialConfig;
import sekelsta.engine.InputManager;
import sekelsta.engine.Log;

// A wrapper for OpenGL's GLFW window
public class Window {
    private long window;
    private int width;
    private int height;
    private boolean focused;
    private boolean fullscreen;
    private boolean wasMaximized;
    private static Thread mainThread;

    private static final int MIN_WIDTH = 20;
    private static final int MIN_HEIGHT = 20;

    // Cached values from before entering fullscreen
    // The GLFW function to update these values takes arrays (or IntBuffers)
    private int[] windowPosX = new int[1];
    private int[] windowPosY = new int[1];
    private int[] windowWidth = new int[1];
    private int[] windowHeight = new int[1];

    static {
        Log.info("Using LWJGL " + Version.getVersion());
        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        mainThread = Thread.currentThread();
        Log.info("Initialized GLFW " + GLFW.glfwGetVersionString());

        GLFW.glfwSetErrorCallback((error, description) -> handleError(error, description));

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public Window(InitialConfig config, String title) {
        this.fullscreen = config.fullscreen;
        this.wasMaximized = config.maximized;

        // Get screen size and monitor work area
        GLFWVidMode mode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        int[] workX = new int[1];
        int[] workY = new int[1];
        int[] workWidth = new int[1];
        int[] workHeight = new int[1];
        GLFW.glfwGetMonitorWorkarea(GLFW.glfwGetPrimaryMonitor(), workX, workY, workWidth, workHeight);

        int defaultWidth = workWidth[0] * 2 / 3;
        int defaultHeight = workHeight[0] * 2 / 3;

        this.width = config.windowWidth == null? defaultWidth : (int)config.windowWidth.longValue();
        this.height = config.windowHeight == null? defaultHeight : (int)config.windowHeight.longValue();
        this.width = Math.min(this.width, workWidth[0]);
        this.height = Math.min(this.height, workHeight[0]);
        this.width = Math.max(this.width, MIN_WIDTH);
        this.height = Math.max(this.height, MIN_HEIGHT);
        windowWidth[0] = this.width;
        windowHeight[0] = this.height;

        // Set OpenGL version
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);

        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);

        long monitor = MemoryUtil.NULL;
        if (fullscreen) {
            monitor =  GLFW.glfwGetPrimaryMonitor();
            width = mode.width();
            height = mode.height();
        }
        window = GLFW.glfwCreateWindow(width, height, title, monitor, MemoryUtil.NULL);
        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create GLFW window.");
        }

        GLFW.glfwSetWindowSizeLimits(window, MIN_WIDTH, MIN_HEIGHT, GLFW.GLFW_DONT_CARE, GLFW.GLFW_DONT_CARE);
        if (wasMaximized) {
            GLFW.glfwMaximizeWindow(window);
        }
        else {
            GLFW.glfwRestoreWindow(window);
            if (!fullscreen) {
                GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
            }
        }

        // Get window decorations size
        int[] frameLeft = new int[1];
        int[] frameTop = new int[1];
        int[] frameRight = new int[1];
        int[] frameBottom = new int[1];
        GLFW.glfwGetWindowFrameSize(window, frameLeft, frameTop, frameRight, frameBottom);

        // Position the window
        int defaultPosX = (mode.width() - windowWidth[0]) / 2;
        int defaultPosY = (mode.height() - windowHeight[0]) / 2;
        windowPosX[0] = config.windowPosX == null? defaultPosX : (int)config.windowPosX.longValue();
        windowPosY[0] = config.windowPosY == null? defaultPosY : (int)config.windowPosY.longValue();
        // Limit position
        windowPosX[0] = Math.min(windowPosX[0], workX[0] + workWidth[0] - this.width - frameRight[0]);
        windowPosY[0] = Math.min(windowPosY[0], workY[0] + workHeight[0] - this.height - frameBottom[0]);
        windowPosX[0] = Math.max(windowPosX[0], workX[0] + frameLeft[0]);
        windowPosY[0] = Math.max(windowPosY[0], workY[0] + frameTop[0]);

        if (!wasMaximized && !fullscreen) {
            GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]);
        }

        // Listen for focus loss/gain events
        GLFW.glfwSetWindowFocusCallback(window, 
            (window, focused) -> this.focused = focused
        );

        // Listen for position change events
        GLFW.glfwSetWindowPosCallback(window, new GLFWWindowPosCallback() {
                @Override
                public void invoke(long window, int xPos, int yPos) {
                    if (!fullscreen) {
                        windowPosX[0] = xPos;
                        windowPosY[0] = yPos;
                    }
                }
            }
        );

        GLFW.glfwMakeContextCurrent(window);

        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        // Allow LWJGL to interoperate with GLFW's OpenGL context
        GL.createCapabilities();

        // Set window icon
        GLFWImage.Buffer icons = GLFWImage.malloc(3);
        icons.put(0, loadIcon("/assets/icon/icon16.png"));
        icons.put(1, loadIcon("/assets/icon/icon32.png"));
        icons.put(2, loadIcon("/assets/icon/icon48.png"));
        GLFW.glfwSetWindowIcon(window, icons);
    }

    private GLFWImage loadIcon(String name) {
        ImageRGBA image = ImageUtils.loadPNG(name);
        GLFWImage icon = GLFWImage.malloc();
        icon.set(image.getWidth(), image.getHeight(), image.pixels);
        return icon;
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    public void updateInput() {
        // Needed for the window to respond to events, e.g. user clicks the 'X'
        GLFW.glfwPollEvents();
    }

    public void close(InitialConfig config) {
        if (Thread.currentThread() != mainThread) {
            throw new RuntimeException("Window was closed from the wrong thread");
        }

        // Save size and position for next session
        config.windowWidth = Long.valueOf(this.windowWidth[0]);
        config.windowHeight = Long.valueOf(this.windowHeight[0]);
        config.windowPosX = Long.valueOf(this.windowPosX[0]);
        config.windowPosY = Long.valueOf(this.windowPosY[0]);
        config.fullscreen = this.fullscreen;
        if (fullscreen) {
            config.maximized = wasMaximized;
        }
        else {
            config.maximized = isMaximized();
        }

        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null).free();
    }

    private static void handleError(int error, long description) {
        throw new RuntimeException(GLFWErrorCallback.getDescription(description));
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
                    if (!fullscreen && !isMaximized()) {
                        GLFW.glfwGetWindowSize(window, windowWidth, windowHeight);
                        // Apparently resizing can change pos without calling that callback, so update here too
                        GLFW.glfwGetWindowPos(window, windowPosX, windowPosY);
                    }
                }
            }
        );

    }

    public void setInput(InputManager input) {
        input.window = this;

        GLFW.glfwSetKeyCallback(window, 
            (window, key, scancode, action, mods) -> input.processKey(key, scancode, action, mods)
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

    public boolean isMouseButtonPressed(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    private boolean isFocused() {
        return this.focused;
    }

    private boolean isMaximized() {
        return GLFW.glfwGetWindowAttrib(window, GLFW.GLFW_MAXIMIZED) == GLFW.GLFW_TRUE;
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;
        if (fullscreen) {
            wasMaximized = isMaximized();
            long monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(monitor);
            GLFW.glfwSetWindowMonitor(window, monitor, 0, 0, videoMode.width(), videoMode.height(), GLFW.GLFW_DONT_CARE);
        }
        else {
            // First, set the window to not be fullscreen
            GLFW.glfwSetWindowMonitor(window, MemoryUtil.NULL, windowPosX[0], windowPosY[0], windowWidth[0], windowHeight[0], GLFW.GLFW_DONT_CARE);
            // Get monitor work area
            int[] workX = new int[1];
            int[] workY = new int[1];
            int[] workWidth = new int[1];
            int[] workHeight = new int[1];
            GLFW.glfwGetMonitorWorkarea(GLFW.glfwGetPrimaryMonitor(), workX, workY, workWidth, workHeight);
            // Get window decorations size
            int[] frameLeft = new int[1];
            int[] frameTop = new int[1];
            int[] frameRight = new int[1];
            int[] frameBottom = new int[1];
            GLFW.glfwGetWindowFrameSize(window, frameLeft, frameTop, frameRight, frameBottom);
            // Limit position, width, and height
            windowPosX[0] = Math.max(windowPosX[0], workX[0] + frameLeft[0]);
            windowPosY[0] = Math.max(windowPosY[0], workY[0] + frameTop[0]);
            windowWidth[0]  = Math.min(windowWidth[0], workWidth[0] - frameLeft[0] - frameRight[0]);
            windowHeight[0] = Math.min(windowHeight[0], workHeight[0] - frameTop[0] - frameBottom[0]);
            GLFW.glfwSetWindowPos(window, windowPosX[0], windowPosY[0]);
            GLFW.glfwSetWindowSize(window, windowWidth[0], windowHeight[0]);
            if (wasMaximized) {
                GLFW.glfwMaximizeWindow(window);
            }
        }
    }
}
