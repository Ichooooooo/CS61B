package byow.Core.WorldGenerator;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.List;

import static byow.Core.Render.WorldRender.HEIGHT;
import static byow.Core.Render.WorldRender.WIDTH;

/**
 * 移走所有死胡同
 * 1. 实现细节
 *      a. 如果满足 "非房子区域" "当前板块是FLOOR" 则参与判断
 *      b. 通过判断能通行的邻居的个数, 只有一个邻居判定为死胡同
 *      c. 能通行的邻居包括 : FLOOR, LOCKED_DOOR, UNLOCKED_DOOR
 *      d. 死胡同变成 WALL
 *  2. 依赖/改变 外部变量
 *      a. 传入 rooms 判断房子区域在哪里
 *      b. 传入 world 移走死胡同
 */

public class DeadEndRemover {
    private TETile[][] world;
    private List<Room> rooms;
    private boolean[][] roomTarget;

    DeadEndRemover(TETile[][] world, List<Room> rooms) {
        this.world = world;
        this.rooms = rooms;
        roomTarget = new boolean[WIDTH][HEIGHT];
    }

    // 标记所有属于房子的节点
    private void targetRoom() {
        for (Room room : rooms) {
            int x = room.x();
            int y = room.y();
            int width = room.width();
            int height = room.height();

            for (int i = x; i < x + width; i++) {
                for (int j = y; j < y + height; j++) {
                    roomTarget[i][j] = true;
                }
            }
        }
    }

    // 判断这个点属不属于能走的邻居
    private boolean isWalkable(int width, int height) {
        return world[width][height].equals(Tileset.FLOOR)
        || world[width][height].equals(Tileset.UNLOCKED_DOOR)
        || world[width][height].equals(Tileset.LOCKED_DOOR);
    }

    // 通过通道附近是否有三面墙判断是不是叶子节点
    private boolean isDeadEnd(int width, int height) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        // 判断这个点是不是FLOOR地板, 不是不参与判断
        if (!world[width][height].equals(Tileset.FLOOR)) {
            return false;
        }

        int walkableNum = 0;
        for (int i = 0; i < 4; i ++) {
            int x = width + dx[i];
            int y = height + dy[i];
            if (x < 0 || y < 0 || x > WIDTH - 1 || y > HEIGHT - 1) {
                continue;
            }
            if (isWalkable(x, y)) {
                walkableNum++;
            }
        }

        return (walkableNum <= 1);
    }

    // 将死胡同变成WALL
    private void removeTile(int width, int height) {
        world[width][height] = Tileset.WALL;
    }

    // 移走所有死胡同
    public void removeDeadEnd() {
        targetRoom();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 1; i < WIDTH - 1; i++) {
                for (int j = 1; j < HEIGHT - 1; j++) {
                    // 如果当前是房间区域, 不参与判断
                    if (roomTarget[i][j]) {
                        continue;
                    }
                    if (isDeadEnd(i, j)) {
                        removeTile(i, j);
                        changed = true;
                    }
                }
            }
        }
    }
}
