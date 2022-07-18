package sekelsta.game;

import sekelsta.engine.Gameloop;

public class Main {
    public static void main(String[] args) {
        Game game = new Game();
        new Gameloop(game, 120).run();
    }
}
