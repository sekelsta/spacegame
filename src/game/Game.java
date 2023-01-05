package sekelsta.game;

import java.net.InetSocketAddress;

import sekelsta.engine.DataFolders;
import sekelsta.engine.ILoopable;
import sekelsta.engine.Log;
import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.entity.IController;
import sekelsta.engine.entity.Movable;
import sekelsta.engine.network.Connection;
import sekelsta.engine.network.INetworked;
import sekelsta.engine.network.NetworkManager;
import sekelsta.engine.render.Camera;
import sekelsta.engine.render.Window;
import sekelsta.game.entity.Entities;
import sekelsta.game.entity.Spaceship;
import sekelsta.game.network.*;
import sekelsta.game.render.*;

public class Game implements ILoopable, INetworked {
    public static final int DEFAULT_PORT = 7654;
    public static final SoftwareVersion VERSION = new SoftwareVersion(0, 0, 0);
    public static final String GAME_ID = "MySpaceGame";

    private boolean running = true;

    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;
    private NetworkManager networkManager;
    private Overlay overlay;

    public Game(boolean graphical) {
        if (graphical) {
            this.window = new Window(DataFolders.getUserMachineFolder("initconfig.toml"), GAME_ID);
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input(this);
            this.window.setInput(input);
            this.overlay = new Overlay(this);
            this.input.setOverlay(this.overlay);
            this.input.updateConnectedGamepads();
        }
        this.world = null;
        Entities.init();
    }

    public void enterWorld() {
        this.world = new World(this, true);
        if (isGraphical()) {
            this.world.spawnLocalPlayer(input);
        }
        initGraphical();
    }

    public void exitWorld() {
        this.camera = null;
        this.world = null;
        if (networkManager != null) {
            networkManager.close();
            networkManager = null;
        }
        this.input.setCamera(null);
        this.input.setPlayer(null);
        overlay.pushScreen(new MainMenuScreen(overlay, this));
    }

    public void takePawn(Spaceship pawn) {
        assert(world.localPlayer == null);
        world.localPlayer = pawn;
        pawn.setController(input);
        initGraphical();
    }

    private void initGraphical() {
        if (isGraphical()) {
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
            this.input.setPlayer(this.world.getLocalPlayer());
            while (overlay.hasScreen()) {
                overlay.popScreen();
            }
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

    public boolean isNetworked() {
        return networkManager != null;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void allowConnections(int port) {
        NetworkManager.context = new GameContext(-1);
        assert(networkManager == null);
        networkManager = new NetworkManager(port);
        networkManager.registerMessageType(ClientJoinGame::new);
        networkManager.registerMessageType(ServerSetWorldTick::new);
        networkManager.registerMessageType(ServerSpawnEntity::new);
        networkManager.registerMessageType(ServerGivePawn::new);
        networkManager.registerMessageType(ServerRemoveEntity::new);
        networkManager.registerMessageType(MobUpdate::new);
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
        if (input != null) {
            input.update();
        }
        if (world != null) {
            world.update();
        }
        if (networkManager != null) {
            if (world != null) {
                ((GameContext)NetworkManager.context).tick = world.getCurrentTick();
                if (world.getLocalPlayer() != null && !world.authoritative) {
                    networkManager.queueBroadcast(new MobUpdate(world.getLocalPlayer()));
                }
            }
            networkManager.update(this);
        }
    }

    @Override
    public void render(float interpolation) {
        if (window == null) {
            return;
        }
        window.updateInput();
        renderer.render(interpolation, camera, world, overlay);
        window.swapBuffers();
    }

    public void stop() {
        running = false;
    }

    @Override
    public void close() {
        running = false;
        // Only close things once, even if called multiple times
        if (overlay != null) {
            overlay.close();
            overlay = null;
        }
        if (window != null) {
            window.close();
            window = null;
        }
        if (networkManager != null) {
            networkManager.close();
            networkManager = null;
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
        networkManager.queueMessage(client, new ServerSetWorldTick());
        // Send info about all existing entities
        for (Movable mob : world.getMobs()) {
            ServerSpawnEntity message = new ServerSpawnEntity(mob);
            networkManager.queueMessage(client, message);
        }
    }

    @Override
    public void connectionTimedOut(long connectionID) {
        Log.info("Connection " + connectionID + " timed out");
        disconnect(connectionID);
    }

    @Override
    public void handleDisconnect(long connectionID) {
        Log.info("Connection " + connectionID + " disconnected");
        disconnect(connectionID);
    }

    private void disconnect(long connectionID) {
        if (world.authoritative) {
            // Note: If the pawn hasn't spawned yet, or is in the list awaiting spawning, this won't remove it
            for (Movable mob : world.getMobs()) {
                IController c = mob.getController();
                if (c instanceof RemotePlayer && connectionID == ((RemotePlayer)c).connectionID) {
                    world.remove(mob);
                }
            }
        }
        else {
            exitWorld();
            assert(isGraphical());
            overlay.pushScreen(new ConnectionLostScreen(overlay, this));
        }
    }

    public World getWorld() {
        return world;
    }

    // in-game as opposed to in the main menu
    public boolean isInGame() {
        return world != null;
    }

    public void escape() {
        overlay.escape(this);
        if (world != null && world.isPaused() != overlay.isPaused()) {
            world.togglePaused();
        }
    }
}
