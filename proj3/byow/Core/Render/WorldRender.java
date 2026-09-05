package byow.Core.Render;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class WorldRender {

    /** 游戏世界的宽度和高度 */
    public static final int WIDTH = 101;
    public static final int HEIGHT = 61;

    private final TERenderer ter = new TERenderer();

    public void initialize() {
        ter.initialize(WIDTH, HEIGHT);
    }

    public void render(TETile[][] world) {
        ter.renderFrame(world);
    }
}
