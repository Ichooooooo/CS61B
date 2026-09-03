package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    // Draw a hex
    private static void addHexagon(int x, int y, int s, TETile[][] world, TETile tile) {
        for (int row = 0; row < 2 * s; row++) {
            int offset = Math.min(row, 2 * s - 1 - row);
            int counts = s + 2 * offset;
            int curX = x - offset;
            int curY = y + row;
            drawLine(curX, curY, counts, world, tile);
        }
    }

    // draw a line
    private static void drawLine(int x, int y, int counts, TETile[][] world, TETile tile) {
        for (int i = x; i < x + counts; i++) {
            world[i][y] = tile;
        }
    }

    // create a random tile
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(9);
        return switch (tileNum) {
            case 0 -> Tileset.FLOWER;
            case 1 -> Tileset.AVATAR;
            case 2 -> Tileset.GRASS;
            case 3 -> Tileset.MOUNTAIN;
            case 4 -> Tileset.SAND;
            case 5 -> Tileset.TREE;
            case 6 -> Tileset.WALL;
            case 7 -> Tileset.FLOOR;
            case 8 -> Tileset.WATER;
            default -> Tileset.NOTHING;
        };
    }

    private static void addHexColumn(int x, int y, int s, int num, TETile[][] world) {
        for (int i = 0; i < num; i++) {
            addHexagon(x, y + i * 2 * s, s, world, randomTile());
        }
    }

    public static void main(String[] args) {
        // initialize the world
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        // draw 19 hexagons
        addHexColumn(4, 9, 3, 3, world);
        addHexColumn(9, 6, 3, 4, world);
        addHexColumn(14, 3, 3, 5, world);
        addHexColumn(19, 6, 3, 4, world);
        addHexColumn(24, 9, 3, 3, world);

        ter.renderFrame(world);
    }
}
