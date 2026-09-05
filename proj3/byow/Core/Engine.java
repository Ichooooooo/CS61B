package byow.Core;

import byow.Core.Input.Input;
import byow.Core.Input.InputSource;
import byow.Core.Player.Player;
import byow.Core.Render.MenuRender;
import byow.Core.Render.SeedInputRender;
import byow.Core.Render.WorldRender;
import byow.Core.WorldGenerator.WorldGenerator;
import byow.TileEngine.TETile;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * Engine 类负责根据当前的游戏状态执行对应逻辑，是游戏的核心控制器。
 *
 * @author Everlasting icovo
 * */
public class Engine {

    /** 当前游戏世界。 */
    private TETile[][] world = null;

    /** 当前游戏玩家。 */
    private Player player;

    /** 用于暂存用户输入的新世界随机种子。 */
    private final StringBuilder seedString = new StringBuilder();

    /** 用于渲染世界的渲染器 */
    private WorldRender worldrender = new WorldRender();

    /** 随机种子生成的随机数生成器。 */
    private Random random;

    /** 当前游戏所处的状态。 */
    private GameState state = GameState.MENU;

    /** 当前游戏历史存档 */
    private final StringBuilder historyString = new StringBuilder();

    /**
     * 键盘交互模式
     * 持续读取用户输入并处理对应操作。
     * */
    public void interactWithKeyboard() {
        // 初始化绘图环境
        worldrender.initialize();

        // 渲染菜单
        MenuRender.render();

        // 新建输入源
        InputSource inputSource = Input.keyboardInput();

        // 处理输入
        interact(inputSource, true);

        // 键盘交互结束后关闭窗口
        EventQueue.invokeLater(() -> {
            for (Frame frame : Frame.getFrames()) {
                frame.dispose();
            }
        });
    }

    /**
     * 指令交互模式（并用于自动评分，不要大幅度修改）
     *
     * @param input 要输入给程序的一串字符
     * @return 处理完所有输入后最终的世界状态
     */
    public TETile[][] interactWithInputString(String input) {
        // 新建输入源
        InputSource inputSource = Input.stringInput(input);

        // 处理输入
        interact(inputSource, false);
        return world;
    }

    /**
     * 统一处理来自不同输入源的输入，并根据不同的 GameState 对输入进行不同的处理。
     *
     * @param inputSource 当前使用的输入源
     * */
    private void interact(InputSource inputSource, boolean renderEnabled) {

        // 若存在下一个输入，则一直处理
        while (inputSource.possibleNextInput()) {

            // 从输入源取出输入字符并大写
            char c = Character.toUpperCase(inputSource.getNextKey());

            // 存储历史命令
            historyString.append(c);

            // 根据不同的游戏状态分别处理输入
            switch (state) {
                case MENU -> handleMenuInput(c);
                case SEED -> handleSeedInput(c);
                case PLAYING -> handlePlayingInput(c);
            }

            // 如果是退出状态，就退出循环
            if (state == GameState.QUIT) {
                break;
            }

            // 根据不同的游戏状态处理渲染
            if (renderEnabled) {
                render();
            }
        }

    }

    private void render() {
        switch (state) {
            case MENU -> MenuRender.render();
            case SEED -> SeedInputRender.render(seedString.toString());
            case PLAYING -> worldrender.render(world);
        }
    }


    /**
     * 处理主菜单状态下的输入。
     *
     * N：开始新游戏并进入种子输入状态。
     * L：加载已有游戏。
     * Q：退出游戏。
     *
     * @param c 用户输入的字符
     */
    private void handleMenuInput(char c) {
        switch (c) {
            case 'N':
                state = GameState.SEED;
                break;
            case 'L':
                // 删除L
                historyString.deleteCharAt(historyString.length() - 1);

                // 加载存档，生成世界
                loadGame();

                break;
            case 'Q':
                quit();
                break;
        }
    }

    /**
     * 处理随机种子的输入
     *
     * 在读取到 S 之前，将输入的字符依次加入 seedString
     * 在读取到 S 之后，将根据种子生成世界，并进入 PLAYING 状态
     *
     * @param c 用户输入的字符
     */
    private void handleSeedInput(char c) {
        if (c == 'S') { // 种子输入完了

            // 获取种子
            long seed = Long.parseLong(seedString.toString());

            // 初始化随机数生成器
            random = new Random(seed);

            // 初始化世界
            world = WorldGenerator.generate(seed);

            // 初始化人物
            player = new Player(world, random);

            // 更新游戏状态
            state = GameState.PLAYING;

        } else { // 种子没有输入完

            // 更新当前种子
            seedString.append(c);

        }
    }


    /**
     * 处理游戏进行状态下的输入。
     * 后续将在这里处理玩家移动、保存游戏等操作。
     *
     * @param c 用户输入的字符
     */
    private void handlePlayingInput(char c) {

        switch(c) {
            case 'W' :
                player.moveUp(world);
                break;
            case 'A' :
                player.moveLeft(world);
                break;
            case 'S' :
                player.moveDown(world);
                break;
            case 'D' :
                player.moveRight(world);
                break;
            case 'Q' :
                // :Q 如果是该命令必须立即保存并退出
                if (historyString.charAt(historyString.length() - 2) == ':') {

                    // 从存档字符中删除 :Q 这个命令
                    historyString.deleteCharAt(historyString.length() - 1);
                    historyString.deleteCharAt(historyString.length() - 1);

                    // 保存文档
                    saveGame();

                    // 退出
                    quit();
                }
                break;

        }

    }

    /**
     * 加载存档世界
     * 1.如果存档不存在
     *      - 系统应当直接退出并关闭 UI，不能产生错误
     * 2.存档存在
     *      - 通过interactWithInputString运行历史命令
     *      - 通过interactWithInputString的返回值初始化world
     */
    private void loadGame() {
        // 读取路径
        Path path = Path.of("byow/Core/Save/savefile.txt");

        // 加载世界但不存在之前的存档，直接退出并关闭 UI
        if (!Files.exists(path)) {
            quit();
            return;
        }

        try {
            // 读取存档文件的内容
            String content = Files.readString(path);

            // 通过 historyString 重现游戏过程，并初始化世界
            world = interactWithInputString(content);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 存储当前存档，在发现没有Save文件夹的时候自动创建
     */
    private void saveGame() {
        Path path = Path.of("byow/Core/Save/savefile.txt");

        try {
            // 如果发现没有Save文件夹就创建
            Files.createDirectories(path.getParent());

            // 将存档存入文件
            Files.writeString(path, historyString.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 需要游戏结束的时候将游戏状态调整为 QUIT
     */
    private void quit() {
        state = GameState.QUIT;
    }
}