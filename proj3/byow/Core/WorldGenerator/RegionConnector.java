package byow.Core.WorldGenerator;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

import static byow.Core.Engine.HEIGHT;
import static byow.Core.Engine.WIDTH;
import static byow.Core.WorldGenerator.WorldGenerator.getRegionSize;
import static java.util.Collections.shuffle;

/**
 * 用于连接地图中彼此独立的房间和迷宫区域，
 * 最终使所有区域形成一个完整的连通块。
 *
 * <p>首先寻找能够连接两个或多个不同区域的墙格作为候选连接点，
 * 随后随机遍历这些连接点，并使用并查集维护区域之间的连通关系。
 * 必要的连接点一定会被打开，同时以较小概率打开额外连接点，
 * 从而在地图中产生少量环路。</p>
 *
 * <p>通过 {@link #connect(TETile[][], int[][], Random)}
 * 方法连接地图。</p>
 */
public class RegionConnector {

    // 冗余连接点被额外打开的概率为 1 / EXTRA_CONNECTOR_CHANCE
    private static final int EXTRA_CONNECTOR_CHANCE = 20;

    // 所有可能用于连接不同区域的候选连接点
    private final List<Connector> connectorPositions = new ArrayList<>();

    // 上、下、左、右四个相邻方向
    public static final int[][] DIRS = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
    };

    /**
     * 连接地图中所有彼此独立的房间和迷宫区域。
     *
     * <p>候选连接点会被随机打乱，并通过并查集判断其是否能够连接
     * 两个尚未连通的区域。能够合并不同连通块的连接点一定会被打开；
     * 已经不再需要的连接点则有较小概率被额外打开，以生成少量环路。</p>
     *
     * <p>主要连接点打开后，会屏蔽附近的其他候选连接点，
     * 避免多个主要入口彼此紧邻。</p>
     *
     * @param world      将要进行连接的地图
     * @param regions    每个格子所属的区域编号，0 表示不属于任何区域
     * @param random     随机数生成器
     */
    public void connect(TETile[][] world, int[][] regions, Random random) {
        connectorPositions.clear();
        // 统计可能会变成门的格子
        updatePos(world, regions);

        shuffle(connectorPositions, random);

        int regionSize = getRegionSize(regions);
        DSU dsu = new DSU(regionSize + 1);

        boolean[][] blocked = new boolean[WIDTH][HEIGHT];

        // 遍历可能会变成门的格子，通过并查集来判断是否应该变成门
        for (Connector connector : connectorPositions) {
            int x = connector.x;
            int y = connector.y;

            if (blocked[x][y]) {
                continue;
            }

            TreeSet<Integer> set = connector.regions;
            int first = set.first();
            boolean connected = false;
            for (int region : set) {
                if (dsu.merge(first, region)) {
                    connected = true;
                }
            }

            if (connected) {
                world[x][y] = Tileset.UNLOCKED_DOOR;
                blockNearby(blocked, x, y);
            } else if (random.nextInt(EXTRA_CONNECTOR_CHANCE) == 0) {
                world[x][y] = Tileset.UNLOCKED_DOOR;
            }
        }
    }

    /**
     * 查找地图中所有可能连接不同区域的候选连接点。
     *
     * <p>对于每一个墙格，检查其上下左右四个相邻格子所属的区域。
     * 如果该墙格相邻至少两个不同的区域，则将其记录为候选连接点。</p>
     *
     * @param world   当前地图
     * @param regions 每个格子所属的区域编号
     */
    private void updatePos(TETile[][] world, int[][] regions) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                if (!world[i][j].equals(Tileset.WALL)) {
                    continue;
                }
                TreeSet<Integer> regionCounts = new TreeSet<>();

                for (int[] dir : DIRS) {
                    int x = i + dir[0];
                    int y = j + dir[1];
                    if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
                        continue;
                    }
                    int region = regions[x][y];
                    if (region != 0) {
                        regionCounts.add(region);
                    }
                }

                if (regionCounts.size() >= 2) {
                    this.connectorPositions.add(new Connector(i, j, regionCounts));
                }
            }
        }
    }

    /**
     * 屏蔽指定连接点周围距离小于 2 的位置，
     * 避免两个主要连接点彼此紧邻。
     *
     * @param blocked 记录不能再作为主要连接点的位置
     * @param x 已打开连接点的横坐标
     * @param y 已打开连接点的纵坐标
     */
    private void blockNearby(boolean[][] blocked, int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || nx >= WIDTH || ny < 0 || ny >= HEIGHT) {
                    continue;
                }
                blocked[nx][ny] = true;
            }
        }
    }

    /**
     * 表示一个候选连接点。
     *
     * <p>记录连接点在地图中的坐标，以及该位置能够连接到的所有区域编号。</p>
     */
    private static class Connector {
        private final int x;
        private final int y;
        private final TreeSet<Integer> regions;

        Connector(int x, int y, TreeSet<Integer> regions) {
            this.x = x;
            this.y = y;
            this.regions = regions;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }

        public Set<Integer> regions() {
            return this.regions;
        }
    }

    /**
     * 并查集，用于维护各个区域之间的连通关系。
     *
     * <p>使用路径压缩和按集合大小合并，以快速判断两个区域
     * 是否已经属于同一个连通块。</p>
     */
    private static class DSU {
        private final int[] parent;
        private final int[] size;

        /**
         * 创建包含 {@code n} 个独立集合的并查集。
         *
         * @param n 元素数量
         */
        DSU(int n) {
            parent = new int[n];
            size = new int[n];

            for (int i = 0; i < n; i++) {
                parent[i] = i;
                size[i] = 1;
            }
        }

        /**
         * 查找元素所在集合的根节点，并进行路径压缩。
         *
         * @param x 要查询的元素
         * @return x 所属集合的根节点
         */
        int find(int x) {
            if (parent[x] != x) {
                parent[x] = find(parent[x]);
            }
            return parent[x];
        }

        /**
         * 合并两个元素所在的集合。
         *
         * @param a 第一个元素
         * @param b 第二个元素
         * @return 如果两个集合原本不同并成功合并则返回 true，
         *         如果它们已经属于同一个集合则返回 false
         */
        boolean merge(int a, int b) {
            a = find(a);
            b = find(b);
            if (a == b) {
                return false;
            }
            if (size[a] < size[b]) {
                int temp = a;
                a = b;
                b = temp;
            }

            parent[b] = a;
            size[a] += size[b];

            return true;
        }
    }
}
