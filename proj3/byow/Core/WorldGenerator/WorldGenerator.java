package byow.Core.WorldGenerator;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static byow.Core.Render.WorldRender.HEIGHT;
import static byow.Core.Render.WorldRender.WIDTH;


/**
 * 负责生成完整的游戏世界。
 *
 * <p>地图生成依次经过房间生成、迷宫生成、区域连接、
 * 死胡同移除以及多余墙壁清理这几个阶段。
 * 对于相同的随机种子，生成的地图应当保持一致。</p>
 *
 * <p>通过 {@link #generate(long)} 方法生成地图。</p>
 */
public class WorldGenerator {

    public static TETile[][] generate(long seed) {

        Random random = new Random(seed);
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        int[][] regions = new int[WIDTH][HEIGHT];
        List<Room> rooms = new ArrayList<>();

        initWorld(world);

        // 生成房间
        RoomGenerator roomGenerator = new RoomGenerator();
        roomGenerator.generate(world, regions, rooms, random);

        // 生成迷宫
        MazeGenerator mazeGenerator = new MazeGenerator(world, random, regions, getRegionSize(regions));
        mazeGenerator.generateMaze();

        // 生成树
        RegionConnector regionConnector = new RegionConnector();
        regionConnector.connect(world, regions, random);

        // 移走死胡同
        DeadEndRemover deadEndRemover = new DeadEndRemover(world, rooms);
        deadEndRemover.removeDeadEnd();

        // 去除多余墙壁
        removeWall(world);

        return world;
    }

    /**
     * 初始化地图，将所有格子设置为墙壁。
     *
     * @param world 要初始化的地图
     */
    private static void initWorld(TETile[][] world) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                world[i][j] = Tileset.WALL;
            }
        }
    }

    /**
     * 删除地图中不再包围有效区域的多余墙壁。
     *
     * <p>遍历所有格子，通过 {@link #needDelete(TETile[][], int, int)}
     * 判断墙壁是否仍然靠近地板或连接点。
     * 不再需要的墙壁会被替换为 {@link Tileset#NOTHING}。</p>
     *
     * @param world 当前地图
     */
    private static void removeWall(TETile[][] world) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (needDelete(world, i, j)) {
                    world[i][j] = Tileset.NOTHING;
                }
            }
        }
    }

    /**
     * 判断指定位置的墙壁是否可以删除。
     *
     * <p>只有当前位置为墙壁，并且其周围 3 × 3 范围内
     * 不存在地板或连接点时，该墙壁才被认为是多余的。</p>
     *
     * @param world 当前地图
     * @param x     要检查位置的横坐标
     * @param y     要检查位置的纵坐标
     * @return 如果该墙壁可以删除则返回 {@code true}，
     *         否则返回 {@code false}
     */
    private static boolean needDelete(TETile[][] world, int x, int y) {
        if (!world[x][y].equals(Tileset.WALL)) {
            return false;
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) {
                    continue;
                }
                if (world[nx][ny].equals(Tileset.FLOOR)
                        || world[nx][ny].equals(Tileset.UNLOCKED_DOOR)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 获取当前地图中最大的区域编号。
     *
     * <p>区域编号从 1 开始，0 表示该格子尚未属于任何区域，
     * 因此最大的区域编号可以作为当前已经生成的区域数量上界。</p>
     *
     * @param regions 每个格子所属的区域编号
     * @return 当前最大的区域编号
     */
    public static int getRegionSize(int[][] regions) {
        int regionSize = 0;
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                regionSize = Math.max(regionSize, regions[i][j]);
            }
        }
        return regionSize;
    }
}
