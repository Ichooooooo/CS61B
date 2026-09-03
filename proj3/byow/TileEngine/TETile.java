package byow.TileEngine;

import java.awt.Color;
import java.util.Arrays;
import java.util.Random;

import edu.princeton.cs.introcs.StdDraw;
import byow.Core.RandomUtils;

/**
 * TETile 对象用于表示世界中的一个图块（tile）。
 * 一个二维的 tile 数组可以组成一整张地图，
 * 并且可以通过 TERenderer 类将其绘制到屏幕上。
 *
 * 所有 TETile 对象都必须具有：
 * - 一个字符 character
 * - 文字颜色 textColor
 * - 背景颜色 backgroundColor
 *
 * 这些属性用于在屏幕上表示该图块。
 *
 * 你还可以选择提供一个图片文件路径。
 * 图片应当是合适的大小（16x16），并用它替代 Unicode 字符来显示图块。
 *
 * 如果提供的图片路径无法找到，那么 draw 方法会退回到使用
 * 字符 + 颜色的方式进行绘制。
 *
 * 因此你完全可以在自己的电脑上使用自定义图片作为 tile。
 *
 * 提供的 TETile 类是不可变的（immutable），
 * 也就是说，它的实例变量创建之后都无法修改。
 *
 * 如果你愿意，也可以把 TETile 修改成可变类（mutable）。
 */

public class TETile {

    // 不要修改 character 这个变量的名字，否则 autograder 会出问题。
    private final char character;

    private final Color textColor;
    private final Color backgroundColor;
    private final String description;
    private final String filepath;

    /**
     * TETile 的完整构造函数。
     *
     * @param character
     *        显示在屏幕上的字符
     *
     * @param textColor
     *        字符本身的颜色（前景色）
     *
     * @param backgroundColor
     *        字符后面的背景颜色
     *
     * @param description
     *        对这个 tile 的描述。
     *        当鼠标悬停在 tile 上时，会在 GUI 中显示这个描述。
     *
     * @param filepath
     *        用来表示这个 tile 的图片文件完整路径。
     *        图片大小必须正确（16x16）。
     */
    public TETile(char character,
                  Color textColor,
                  Color backgroundColor,
                  String description,
                  String filepath) {

        this.character = character;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.description = description;
        this.filepath = filepath;
    }

    /**
     * 不包含 filepath 的构造函数。
     *
     * 在这种情况下，filepath 会被设置成 null。
     *
     * 因此绘制 tile 时甚至不会尝试加载图片，
     * 而是直接使用提供的字符和颜色来绘制。
     *
     * @param character
     *        显示在屏幕上的字符
     *
     * @param textColor
     *        字符本身的颜色
     *
     * @param backgroundColor
     *        字符后面的背景颜色
     *
     * @param description
     *        tile 的描述。
     *        鼠标悬停在 tile 上时，会在 GUI 中显示。
     */
    public TETile(char character,
                  Color textColor,
                  Color backgroundColor,
                  String description) {

        this.character = character;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.description = description;
        this.filepath = null;
    }

    /**
     * 创建 TETile t 的一个副本，
     * 但是把它的文字颜色替换成给定的 textColor。
     *
     * @param t
     *        要复制的 tile
     *
     * @param textColor
     *        新 tile 的前景色 / 文字颜色
     */
    public TETile(TETile t, Color textColor) {
        this(t.character,
                textColor,
                t.backgroundColor,
                t.description,
                t.filepath);
    }

    /**
     * 在屏幕上的 (x, y) 位置绘制这个 tile。
     *
     * 如果提供了有效的 filepath，
     * 就使用该路径对应的图片来绘制。
     *
     * 否则就退回到使用：
     *
     * 字符 + 前景色 + 背景色
     *
     * 来表示这个 tile。
     *
     * 注意：
     * 提供的图片必须具有正确的大小（16x16）。
     * 程序不会自动缩放或者裁剪图片。
     *
     * @param x x 坐标
     * @param y y 坐标
     */
    public void draw(double x, double y) {

        if (filepath != null) {

            try {
                StdDraw.picture(
                        x + 0.5,
                        y + 0.5,
                        filepath
                );
                return;
            } catch (IllegalArgumentException e) {
                /*
                 * 如果找不到图片文件，
                 * StdDraw.picture 会抛出异常。
                 *
                 * 这里选择静默处理这个异常，
                 * 然后继续使用字符和颜色绘制 tile。
                 */
            }
        }

        // 设置背景颜色
        StdDraw.setPenColor(backgroundColor);

        // 绘制背景方块
        StdDraw.filledSquare(
                x + 0.5,
                y + 0.5,
                0.5
        );

        // 设置文字颜色
        StdDraw.setPenColor(textColor);

        // 在 tile 中央绘制字符
        StdDraw.text(
                x + 0.5,
                y + 0.5,
                Character.toString(character())
        );
    }

    /**
     * 返回这个 tile 的字符表示。
     *
     * 主要用于文本模式绘制。
     *
     * @return tile 对应的字符
     */
    public char character() {
        return character;
    }

    /**
     * 返回这个 tile 的描述。
     *
     * 这个描述可以用于：
     *
     * - 鼠标悬停时显示文字
     * - 测试两个 tile 是否表示同一种东西
     *
     * @return tile 的描述
     */
    public String description() {
        return description;
    }

    /**
     * 创建给定 tile 的一个副本，
     * 但会稍微随机改变它的文字颜色。
     *
     * 新颜色的：
     *
     * red   会在原 red 值上下 dr 范围内变化
     * green 会在原 green 值上下 dg 范围内变化
     * blue  会在原 blue 值上下 db 范围内变化
     *
     * @param t
     *        要复制的 tile
     *
     * @param dr
     *        红色分量允许变化的最大范围
     *
     * @param dg
     *        绿色分量允许变化的最大范围
     *
     * @param db
     *        蓝色分量允许变化的最大范围
     *
     * @param r
     *        使用的随机数生成器
     */
    public static TETile colorVariant(
            TETile t,
            int dr,
            int dg,
            int db,
            Random r) {

        Color oldColor = t.textColor;

        int newRed =
                newColorValue(oldColor.getRed(), dr, r);

        int newGreen =
                newColorValue(oldColor.getGreen(), dg, r);

        int newBlue =
                newColorValue(oldColor.getBlue(), db, r);

        Color c =
                new Color(
                        newRed,
                        newGreen,
                        newBlue
                );

        return new TETile(t, c);
    }

    /**
     * 根据原颜色分量 v，
     * 在 ±dv 范围内随机生成一个新的颜色分量。
     */
    private static int newColorValue(
            int v,
            int dv,
            Random r) {

        int rawNewValue =
                v + RandomUtils.uniform(
                        r,
                        -dv,
                        dv + 1
                );

        /*
         * 确保颜色值不会超出合法范围：
         *
         * 0 <= value <= 255
         */
        int newValue =
                Math.min(
                        255,
                        Math.max(0, rawNewValue)
                );

        return newValue;
    }

    /**
     * 将一个二维 TETile 数组转换成 String。
     *
     * 这个方法非常适合调试。
     *
     * 注意：
     *
     * 在 tile rendering engine 中，
     * y = 0 实际上对应世界的最底部。
     *
     * 因此这里打印地图的时候，
     * y 必须从最大的值往 0 打印。
     *
     * 所以看起来打印顺序好像是“反过来的”。
     *
     * 这样最终终端里显示出来的地图，
     * 才和图形界面看到的是一致的。
     *
     * @param world
     *        要打印的二维世界
     *
     * @return
     *        世界对应的字符串表示
     */
    public static String toString(TETile[][] world) {

        int width = world.length;
        int height = world[0].length;

        StringBuilder sb =
                new StringBuilder();

        /*
         * 从地图顶部开始往下打印。
         */
        for (int y = height - 1;
             y >= 0;
             y -= 1) {

            /*
             * 从左往右打印。
             */
            for (int x = 0;
                 x < width;
                 x += 1) {

                if (world[x][y] == null) {

                    throw new IllegalArgumentException(
                            "Tile at position x="
                                    + x
                                    + ", y="
                                    + y
                                    + " is null."
                    );
                }

                sb.append(
                        world[x][y].character()
                );
            }

            sb.append('\n');
        }

        return sb.toString();
    }

    /**
     * 创建给定二维 tile 数组的一个副本。
     *
     * @param tiles
     *        要复制的二维数组
     *
     * @return
     *        复制出来的新二维数组
     */
    public static TETile[][] copyOf(
            TETile[][] tiles) {

        if (tiles == null) {
            return null;
        }

        /*
         * 创建一个新的二维数组。
         *
         * 第一维长度和原来的 tiles 一样。
         */
        TETile[][] copy =
                new TETile[tiles.length][];

        int i = 0;

        /*
         * 遍历每一列。
         */
        for (TETile[] column : tiles) {

            /*
             * 复制这一列。
             */
            copy[i] =
                    Arrays.copyOf(
                            column,
                            column.length
                    );

            i += 1;
        }

        return copy;
    }
}