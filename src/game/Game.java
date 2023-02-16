package sekelsta.game;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import javax.sound.sampled.*;

import sekelsta.engine.*;
import sekelsta.engine.SoftwareVersion;
import sekelsta.engine.entity.IController;
import sekelsta.engine.entity.Entity;
import sekelsta.engine.network.*;
import sekelsta.engine.render.Camera;
import sekelsta.engine.render.Window;
import sekelsta.game.entity.Entities;
import sekelsta.game.entity.Spaceship;
import sekelsta.game.network.*;
import sekelsta.game.render.Renderer;
import sekelsta.game.render.gui.*;

public class Game implements ILoopable, INetworked {
    public static final int DEFAULT_PORT = 7654;
    public static final SoftwareVersion VERSION = new SoftwareVersion(0, 0, 0);
    public static final String GAME_ID = "MySpaceGame";

    private boolean running = true;

    private InitialConfig initialConfig;
    private UserSettings settings;
    private World world;
    private Window window;
    private Renderer renderer;
    private Input input;
    private Camera camera;
    private NetworkManager networkManager;
    private Overlay overlay;
    private Clip music;

    public Game(boolean graphical) {
        if (graphical) {
            this.initialConfig = new InitialConfig(DataFolders.getUserMachineFolder("initconfig.toml"));
            this.window = new Window(initialConfig, GAME_ID);
            Fonts.load();
            this.renderer = new Renderer();
            this.window.setResizeListener(renderer);
            this.input = new Input(this);
            this.window.setInput(input);
            this.overlay = new Overlay(this);
            this.input.setOverlay(this.overlay);
            this.input.updateConnectedGamepads();

            try {
                music = AudioSystem.getClip();
                // Use BufferedInputStream so that mark/reset is supported
                InputStream bufferedAudio = new BufferedInputStream(
                    Game.class.getResourceAsStream("/assets/audio/planetrise.wav")
                );
                AudioInputStream input = AudioSystem.getAudioInputStream(bufferedAudio);
                music.open(input);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.settings = new UserSettings(DataFolders.getUserFolder("settings.toml"), this);

            music.start();
        }
        this.world = null;
        Entities.init();
    }

    public void enterWorld() {
        this.world = new World(this, true);
        if (isGraphical()) {
            world.spawnLocalPlayer(input);
            input.setPlayer(world.getLocalPlayer());
        }
        initGraphical();
    }

    public void cancelConnecting() {
        if (networkManager != null) {
            networkManager.close();
            networkManager = null;
        }
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
        world.localPlayer = pawn;
        pawn.setController(input);
        input.setPlayer(world.getLocalPlayer());
        if (camera == null) {
            initGraphical();
        }
        else {
            camera.setFocus(world.getLocalPlayer());
        }

        if (world.isDead(pawn)) {
            onLocalPlayerShipDestroyed();
        }
    }

    private void initGraphical() {
        if (isGraphical()) {
            this.camera = new Camera(world.getLocalPlayer());
            this.input.setCamera(camera);
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

    public UserSettings getSettings() {
        return settings;
    }

    public boolean isNetworked() {
        return networkManager != null;
    }

    @Override
    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void allowConnections(int port) {
        NetworkManager.context = new MessageContext();
        assert(networkManager == null);
        networkManager = new NetworkManager(port);
        networkManager.registerMessageType(ClientJoinGame::new);
        networkManager.registerMessageType(ServerSetWorldTick::new);
        networkManager.registerMessageType(ServerSpawnEntity::new);
        networkManager.registerMessageType(ServerGivePawn::new);
        networkManager.registerMessageType(ServerRemoveEntity::new);
        networkManager.registerMessageType(EntityUpdate::new);
        networkManager.registerMessageType(ServerExplodeShip::new);
        networkManager.registerMessageType(ClientRespawn::new);
        networkManager.registerMessageType(ClientSpawnEntity::new);
        networkManager.start();

        if (world != null && world.isPaused()) {
            world.togglePaused();
        }
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
                NetworkManager.context.tick = world.getCurrentTick();
                if (world.getLocalPlayer() != null && !world.authoritative) {
                    networkManager.queueBroadcast(new EntityUpdate(world.getLocalPlayer()));
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
        Fonts.clean();
        // Only close things once, even if called multiple times
        if (window != null) {
            window.close(initialConfig);
            window = null;
        }
        if (networkManager != null) {
            networkManager.close();
            networkManager = null;
        }
        if (initialConfig != null) {
            initialConfig.save();
            initialConfig = null;
        }
        if (settings != null) {
            settings.save();
            settings = null;
        }
    }

    @Override
    public void connectionRejected(String reason) {
        networkManager.close();
        networkManager = null;
        if (isInGame()) {
            exitWorld();
        }
        assert(isGraphical());
        overlay.pushScreen(new ConnectionRejectedScreen(this, reason));
        Log.info("Connection rejected by server due to: " + reason);
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
        for (Entity mob : world.getMobs()) {
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
            for (Entity mob : world.getMobs()) {
                IController c = mob.getController();
                if (c instanceof RemotePlayer && connectionID == ((RemotePlayer)c).connectionID) {
                    world.remove(mob);
                }
            }
        }
        else {
            exitWorld();
            assert(isGraphical());
            overlay.pushScreen(new ConnectionLostScreen(this));
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

    public void onLocalPlayerShipDestroyed() {
        Entity focus = new Entity(world.localPlayer.getX(), world.localPlayer.getY(), world.localPlayer.getZ());
        camera.setFocus(focus);
        overlay.pushScreen(new ShipDestroyedScreen(this, overlay));
    }

    public void respawn() {
        if (world.authoritative) {
            world.respawn(null);
            camera.setFocus(world.localPlayer);
        }
        else {
            ClientRespawn message = new ClientRespawn();
            networkManager.queueBroadcast(message);
        }
    }

    public void setVolume(float volume) {
        FloatControl gainControl = (FloatControl)music.getControl(FloatControl.Type.MASTER_GAIN);
        double decibels = Math.log(volume) / Math.log(2) * 20.0;
        if (decibels < gainControl.getMinimum()) {
            decibels = -1/0f;
        }
        decibels = Math.min(decibels, gainControl.getMaximum());
        gainControl.setValue((float)decibels);
    }

    public void toggleFullscreen() {
        window.toggleFullscreen();
    }
}
