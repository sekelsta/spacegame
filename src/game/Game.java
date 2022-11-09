package sekelsta.game;

import java.net.InetSocketAddress;

import sekelsta.engine.DataFolders;
import sekelsta.engine.ILoopable;
import sekelsta.engine.Log;
import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.entity.Movable;
import sekelsta.engine.network.Connection;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.NetworkManager;
import sekelsta.engine.render.Window;
import sekelsta.game.entity.Entities;
import sekelsta.game.entity.Spaceship;
import sekelsta.game.network.*;
import sekelsta.game.render.Renderer;

public class Game implements ILoopable, INetworked {
    public static final SoftwareVersion VERSION = new SoftwareVersion(0, 0, 0);
    public static final String GAME_ID = "MySpaceGame";

    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;
    private NetworkManager networkManager;

    public Game(boolean graphical) {
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), GAME_ID);
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input();
            this.window.setInput(input);
        }
        this.world = new World(this, true);
        Entities.init();
    }

    public void enterWorld() {
        if (isGraphical()) {
            this.world.spawnLocalPlayer(input);
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
            this.input.setPlayer(this.world.getLocalPlayer());
            this.input.setWorld(this.world);
        }
    }

    public void takePawn(Spaceship pawn) {
        assert(world.localPlayer == null);
        world.localPlayer = pawn;
        pawn.setController(input);
        if (isGraphical()) {
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
            this.input.setPlayer(this.world.getLocalPlayer());
            this.input.setWorld(this.world);
        }

    }

    private boolean isGraphical() {
        return window != null;
    }

    @Override
    public SoftwareVersion getVersion() {
        return VERSION;
    }

    @Override
    public String getGameID() {
        return GAME_ID;
    }

    @Override
    public boolean isRunning() {
        return running && (window == null || !window.shouldClose());
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void allowConnections(int port) {
        assert(networkManager == null);
        networkManager = new NetworkManager(port);
        networkManager.registerMessageType(ClientJoinGame::new);
        networkManager.registerMessageType(ServerSpawnEntity::new);
        networkManager.registerMessageType(ServerGivePawn::new);
        networkManager.registerMessageType(ServerRemoveEntity::new);
        networkManager.start();
    }

    public void joinServer(InetSocketAddress socketAddress) {
        allowConnections(0);
        this.world = new World(this, false);
        networkManager.joinServer(this, socketAddress);
    }

    @Override
    public void update() {
        if (window != null) {
            window.updateInput();
        }
        if (world != null) {
            world.update();
        }
        if (networkManager != null) {
            networkManager.update(this);
        }
    }

    @Override
    public void render(float interpolation) {
        if (window == null) {
            return;
        }
        window.updateInput();
        renderer.render(interpolation, camera, world);
        window.swapBuffers();
    }

    @Override
    public void close() {
        if (!running) {
            // Already closed
            return;
        }
        running = false;
        if (window != null) {
            window.close();
        }
        if (networkManager != null) {
            networkManager.close();
        }
    }

    @Override
    public void connectionRejected(String reason) {
        // TODO: Make sure this can only be called while we are trying to join a server, not while connected
        networkManager.close();
        networkManager = null;
        Log.info("Connection rejected by server due to " + reason);
    }

    @Override
    public void receivedHelloFromServer(SoftwareVersion version) {
        Log.info("Server running version " + version + " says \"hello\".");
        int skin = world.getRandom().nextInt(Spaceship.NUM_SKINS);
        ClientJoinGame joinGameMessage = new ClientJoinGame(skin);
        networkManager.queueBroadcast(joinGameMessage);
    }

    @Override
    public void clientConnectionAccepted(Connection client) {
        Log.info("Accepted connection " + client.getID());
        // Send info about all existing entities
        for (Movable mob : world.getMobs()) {
            ServerSpawnEntity message = new ServerSpawnEntity(mob);
            networkManager.queueMessage(client, message);
        }
    }

    @Override
    public void connectionTimedOut(long connectionID) {
        Log.debug("Connection timed out: " + connectionID);
    }

    @Override
    public void handleDisconnect(long connectionID) {
        Log.debug("connection " + connectionID + " disconnected");
    }

    public World getWorld() {
        return world;
    }
}
