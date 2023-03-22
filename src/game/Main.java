package sekelsta.game;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import sekelsta.engine.DataFolders;
import sekelsta.engine.Gameloop;
import sekelsta.engine.Log;

public class Main {
    private static int DEFAULT_FRAME_CAP = 120;

    public static void main(String[] args) {
        DataFolders.init(Game.GAME_ID);
        Log.info("Starting " + Game.GAME_ID + " " + Game.VERSION + " with args: " + String.join(" ", args));
        int port = Game.DEFAULT_PORT;
        if (args.length == 0) {
            run();
        }
        else if (args[0].equals("singleplayer")) {
            runSingleplayer();
        }
        else if (args[0].equals("integrated")) {
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
            runIntegrated(port);
        }
        else if (args[0].equals("server")) {
            if (args.length > 1) {
                port = Integer.parseInt(args[1]);
            }
            runServer(port);
        }
        else if (args[0].equals("client")) {
            if (args.length < 2) {
                System.out.println("IP address of host required");
                System.exit(1);
            }
            String address = args[1];
            if (args.length > 2) {
                port = Integer.parseInt(args[2]);
            }
            runClient(address, port);
        }
        else {
            System.out.println("Unknown arguments, use server [port] or client [ipaddress] [port]\n" 
                             + "Port is optional and defaults to " + Game.DEFAULT_PORT);
            System.exit(1);
        }
    }

    public static void run() {
        Game game = new Game(true);
        new Gameloop(game, DEFAULT_FRAME_CAP).run();
    }

    public static void runSingleplayer() {
        Game game = new Game(true);
        Log.info("Skipping menu screen");
        game.enterWorld();
        new Gameloop(game, DEFAULT_FRAME_CAP).run();
    }

    public static void runIntegrated(int port) {
        Log.info("Integrated client-server listening on port " + port);
        Game game = new Game(true);
        game.enterWorld();
        game.allowConnections(port);
        new Gameloop(game, DEFAULT_FRAME_CAP).run();
    }

    public static void runClient(String address, int port) {
        Log.info("Connecting client to " + address + " port " + port);

        InetAddress netAddress = null;
        try {
            netAddress = InetAddress.getByName(address);
        }
        catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        Game game = new Game(true);
        game.joinServer(new InetSocketAddress(netAddress, port));
        new Gameloop(game, DEFAULT_FRAME_CAP).run();
    }

    public static void runServer(int port) {
        Log.info("Starting server for port " + port);
        Game game = new Game(false);
        game.enterWorld();
        game.allowConnections(port);
        new Gameloop(game, 1).run();
    }
}
