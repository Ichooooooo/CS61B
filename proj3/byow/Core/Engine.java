package byow.Core;

import byow.Core.WorldGenerator.WorldGenerator;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

public class Engine {

    // 创建一个地图渲染器
    TERenderer ter = new TERenderer();

    /*
     * 你可以自由修改地图的宽度和高度。
     */
    public static final int WIDTH = 101;
    public static final int HEIGHT = 61;

    /**
     * 用于开始并探索一个新的世界。
     *
     * 这个方法应该处理用户的所有键盘输入，
     * 包括主菜单中的输入。
     */
    public void interactWithKeyboard() {
    }

    /**
     * 这个方法用于自动评测（autograding）和测试你的代码。
     *
     * 输入参数 input 是一串字符，例如：
     *
     * "n123sswwdasdassadwas"
     * "n123sss:q"
     * "lwww"
     *
     * Engine 应该表现得和用户在 interactWithKeyboard()
     * 中一个一个敲入这些字符时完全一样。
     *
     *
     * 需要记住：
     *
     * 以 ":q" 结尾的字符串应该让游戏：
     *
     * 保存（save）并退出（quit）。
     *
     *
     * 例如：
     *
     * interactWithInputString("n123sss:q")
     *
     * 我们期望游戏先执行前 7 个命令：
     *
     * n123sss
     *
     * 然后执行：
     *
     * :q
     *
     * 保存当前游戏状态并退出。
     *
     *
     * 如果之后再执行：
     *
     * interactWithInputString("l")
     *
     * 那么游戏应该恢复到刚才保存时完全相同的状态。
     *
     *
     * 换句话说，下面这两次调用：
     *
     * interactWithInputString("n123sss:q")
     *
     * 然后：
     *
     * interactWithInputString("lww")
     *
     *
     * 最后得到的世界状态，应该和直接调用：
     *
     * interactWithInputString("n123sssww")
     *
     * 得到的世界状态完全一致。
     *
     *
     * @param input
     *        要输入给程序的一串字符
     *
     * @return
     *        一个二维 TETile[][] 数组，
     *        表示最终世界的状态
     */
    public TETile[][] interactWithInputString(String input) {

        /*
         * TODO：
         *
         * 完成这个方法。
         *
         * 让 Engine 根据参数 input 中传入的字符串运行游戏，
         *
         * 最后返回一个二维 tile 数组，
         *
         * 这个数组应该和用户通过 interactWithKeyboard()
         * 输入完全相同指令时，屏幕最终显示出来的世界一致。
         *
         *
         * 可以查看：
         *
         * proj3.byow.InputDemo
         *
         * 来看看如何设计一个比较干净的输入接口，
         * 从而让同一套游戏逻辑同时支持多种输入方式。
         */

        input = input.toUpperCase();
        TETile[][] finalWorldFrame = null;

        if (input.charAt(0) == 'N') {
            StringBuilder seedString = new StringBuilder();
            int index = 1;
            while (input.charAt(index) != 'S') {
                seedString.append(input.charAt(index));
                index++;
            }

            long seed = Long.parseLong(String.valueOf(seedString));
            finalWorldFrame = WorldGenerator.generate(seed);
        }

        return finalWorldFrame;
    }
}