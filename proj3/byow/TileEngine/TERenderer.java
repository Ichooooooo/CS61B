package byow.TileEngine;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;

/**
 * 用于渲染 tile（方块）的工具类。
 *
 * 你不需要修改这个文件。
 * 当然你也可以修改，但要小心。
 *
 * 我们强烈建议，在你把其他部分都实现正确之前，不要随便修改这个渲染器。
 *
 * 除非你想实现一些比较高级的功能，例如：
 * - 让屏幕能够滚动
 * - 让视角跟随玩家角色
 * - 或者其他类似的特殊效果
 */
public class TERenderer {
    private static final int TILE_SIZE = 16;
    private int width;
    private int height;
    private int xOffset;
    private int yOffset;

    /**
     * 和另一个 initialize 方法功能相同。
     *
     * 唯一的区别是：
     * xOff 和 yOff 参数可以改变 renderFrame 方法开始绘制的位置。
     *
     * 例如：
     *
     * 如果你设置：
     * w = 60
     * h = 30
     * xOff = 3
     * yOff = 4
     *
     * 然后调用 renderFrame，并传入一个 TETile[50][25] 数组，
     *
     * 那么渲染器会留下：
     * - 左边 3 个 tile 的空白
     * - 右边 7 个 tile 的空白
     * - 底部 4 个 tile 的空白
     * - 顶部 1 个 tile 的空白
     *
     * @param w 窗口宽度，以 tile 数量为单位
     * @param h 窗口高度，以 tile 数量为单位
     */
    public void initialize(int w, int h, int xOff, int yOff) {
        this.width = w;
        this.height = h;
        this.xOffset = xOff;
        this.yOffset = yOff;

        // 设置实际窗口大小：
        // 每一个 tile 占 16 × 16 像素
        StdDraw.setCanvasSize(width * TILE_SIZE, height * TILE_SIZE);

        // 设置绘制字符时使用的字体
        Font font = new Font("Monaco", Font.BOLD, TILE_SIZE - 2);
        StdDraw.setFont(font);

        // 设置坐标系：
        // x 范围为 0 ~ width
        // y 范围为 0 ~ height
        StdDraw.setXscale(0, width);
        StdDraw.setYscale(0, height);

        // 将背景清空为黑色
        StdDraw.clear(new Color(0, 0, 0));

        // 开启双缓冲，避免绘制过程中出现闪烁
        StdDraw.enableDoubleBuffering();

        // 显示当前绘制内容
        StdDraw.show();
    }

    /**
     * 初始化 StdDraw 的参数，并打开 StdDraw 窗口。
     *
     * w 和 h 表示整个窗口的宽和高，单位都是 tile。
     *
     * 如果你传给 renderFrame 的 TETile[][] 数组比窗口尺寸小，
     * 那么窗口的右边和上边会留下额外的空白区域。
     *
     * 例如：
     *
     * 如果：
     * w = 60
     * h = 30
     *
     * 那么会创建一个：
     * 60 个 tile 宽 × 30 个 tile 高
     * 的窗口。
     *
     * 如果之后调用 renderFrame，并传入：
     *
     * TETile[50][25]
     *
     * 那么：
     * - 右侧会留下 10 个 tile 的空白
     * - 顶部会留下 5 个 tile 的空白
     *
     * 如果你希望在左边或者底部留下空间，
     * 那么应该使用另一个带 xOffset、yOffset 参数的 initialize 方法。
     *
     * @param w 窗口宽度，以 tile 数量为单位
     * @param h 窗口高度，以 tile 数量为单位
     */
    public void initialize(int w, int h) {
        initialize(w, h, 0, 0);
    }

    /**
     * 接收一个二维 TETile 数组，并把整个二维数组绘制到屏幕上。
     *
     * 绘制从：
     *
     * xOffset, yOffset
     *
     * 开始。
     *
     * 假设数组是一个 N × M 的数组，
     * 那么屏幕上各个位置对应的数组元素关系如下。
     *
     * 坐标单位都是 tile：
     *
     *              位置       xOffset |xOffset+1|xOffset+2| .... |xOffset+world.length
     *
     * yOffset+world[0].length [0][M-1] | [1][M-1] | [2][M-1] | .... | [N-1][M-1]
     *                    ...   ......  |  ......  |  ......  | .... | ......
     *              yOffset+2   [0][2]  |  [1][2]  |  [2][2]  | .... | [N-1][2]
     *              yOffset+1   [0][1]  |  [1][1]  |  [2][1]  | .... | [N-1][1]
     *                yOffset   [0][0]  |  [1][0]  |  [2][0]  | .... | [N-1][0]
     *
     * 通过改变：
     *
     * xOffset
     * yOffset
     * 以及初始化时的屏幕大小
     *
     * 你可以在屏幕的不同位置预留空白区域，
     * 用于显示其他信息，例如 GUI。
     *
     * 这个方法假定：
     *
     * xScale 和 yScale 已经设置好了，
     *
     * 并且：
     *
     * x 坐标最大值 = 屏幕宽度（tile 数）
     * y 坐标最大值 = 屏幕高度（tile 数）
     *
     * @param world 要进行渲染的二维 TETile[][] 数组
     */
    public void renderFrame(TETile[][] world) {

        // 世界在 x 方向上的 tile 数量，即“有多少列”
        int numXTiles = world.length;

        // 世界在 y 方向上的 tile 数量，即“每列有多少个 tile”
        int numYTiles = world[0].length;

        // 每次重新渲染之前，先把整个屏幕清空为黑色
        StdDraw.clear(new Color(0, 0, 0));

        // 遍历二维世界中的所有 tile
        for (int x = 0; x < numXTiles; x += 1) {
            for (int y = 0; y < numYTiles; y += 1) {

                // world 中不允许出现 null
                // 如果某个位置没有 tile，就直接报错
                if (world[x][y] == null) {
                    throw new IllegalArgumentException(
                            "位置 x=" + x + ", y=" + y + " 的 Tile 是 null。"
                    );
                }

                // 绘制当前 tile。
                // 注意实际绘制位置需要加上 offset。
                world[x][y].draw(x + xOffset, y + yOffset);
            }
        }

        // 把这一帧真正显示到屏幕上
        StdDraw.show();
    }
}