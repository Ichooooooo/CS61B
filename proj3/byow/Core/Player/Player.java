package byow.Core.Player;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

import static byow.Core.Render.WorldRender.HEIGHT;
import static byow.Core.Render.WorldRender.WIDTH;

/**
 * 玩家属性
 * 1. 横坐标
 * 2. 纵坐标
 * 3. 上次站的板块的样式
 * 玩家行为
 * 1. W A S D移动, 并改变世界(包括渲染当前站的位置和还原上一步的板块)
 * 2. 取得当前横纵坐标
 */

public class Player {
    private int x;
    private int y;

    // 记录用户上一个行走的板块的样式
    private TETile lastPositionType;

    // 初始化用户, 随机化玩家初始位置 , 横坐标随机[1, WIDTH], 纵坐标随机[1, HEIGHT], 并将人物画在世界上
    public Player(TETile[][] world, Random random) {
        int maxWidth = WIDTH - 1, minWidth = 1;
        int maxHeight = HEIGHT - 1, minHeight = 1;
        int originX = random.nextInt(maxWidth - minWidth + 1) + minWidth;
        int originY = random.nextInt(maxHeight - minHeight + 1) + minHeight;

        while (!isPlaceWalkable(world, originX, originY)) {
            originX = random.nextInt(maxWidth - minWidth + 1) + minWidth;
            originY = random.nextInt(maxHeight - minHeight + 1) + minHeight;
        }

        x = originX;
        y = originY;

        // 改变游戏界面
        lastPositionType = world[x][y];
        world[x][y] = Tileset.AVATAR;
    }

    // 取得玩家现在横坐标位置
    public int x() {
        return x;
    }

    // 取得玩家现在纵坐标位置
    public int y() {
        return y;
    }

    /**
     * 判断可以走的位置
     * @param world 当前世界
     * @param width 判断的位置横坐标
     * @param height 判断的位置纵坐标
     * @return 位置是否可走
     * 是否可走判断:
     * 1. 越界不可以走(最外层算墙壁,不合理)
     * 2. UNLOCKED_DOOR 可走
     * 3. FLOOR 可走
     */
    private boolean isPlaceWalkable(TETile[][] world, int width, int height) {
        if (width < 1 || width >= WIDTH - 1 || height < 1 || height >= HEIGHT - 1) {
            return false;
        }

        return world[width][height].equals(Tileset.UNLOCKED_DOOR)
            || world[width][height].equals(Tileset.FLOOR);
    }

    /**
     * 渲染人物移动后的世界
     * 1. 还原用户当前站的位置的板块
     * 2. 更新 lastPositionType
     * 3. 渲染用户下一步站的位置
     * @param world 游戏世界
     * @param x 当前横坐标
     * @param y 当前纵坐标
     * @param nextX 下一步的横坐标
     * @param nextY 下一步的纵坐标
     */
    private void moveTo(TETile[][] world, int x, int y, int nextX, int nextY) {
        // 还原人物原来站的板块
        world[x][y] = lastPositionType;

        // 更新lastPositionType
        lastPositionType = world[nextX][nextY];

        // 更新用户现在站的板块
        world[nextX][nextY] = Tileset.AVATAR;
    }

    /**
     * 移动玩家:
     * 1. 判断能否移动
     * 2. 渲染世界
     * 3. 更新坐标
     * @param world 游戏世界
     */
    public void moveUp(TETile[][] world) {
        if (!isPlaceWalkable(world, x, y + 1)) {
            return;
        }
        moveTo(world, x, y, x, y + 1);
        y++;
    }

    public void moveDown(TETile[][] world) {
        if (!isPlaceWalkable(world, x, y - 1)) {
            return;
        }
        moveTo(world, x, y, x, y - 1);
        y--;
    }

    public void moveLeft(TETile[][] world) {
        if (!isPlaceWalkable(world, x - 1, y)) {
            return;
        }
        moveTo(world, x, y, x - 1, y);
        x--;
    }

    public void moveRight(TETile[][] world) {
        if (!isPlaceWalkable(world, x + 1, y)) {
            return;
        }
        moveTo(world, x, y, x + 1, y);
        x++;
    }
}
