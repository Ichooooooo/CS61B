package byow.Core.WorldGenerator;

import byow.Core.Engine;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.util.Random;

import static byow.Core.Engine.HEIGHT;
import static byow.Core.Engine.WIDTH;

public class Test {

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        Engine engine = new Engine();

        TETile[][] world = engine.interactWithInputString(args[0]);
        ter.renderFrame(world);
    }
}
