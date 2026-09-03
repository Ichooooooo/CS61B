package byow.Core.WorldGenerator;

/**
 * 表示地图中的一个矩形房间。
 *
 * <p>房间由左下角坐标 {@code (x, y)}、宽度和高度确定。
 * 创建后房间的位置和大小不会发生变化。</p>
 */
public final class Room {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }
}
