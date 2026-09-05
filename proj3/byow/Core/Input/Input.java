package byow.Core.Input;

/**
 * Input 类负责创建不同类型的输入源。
 *
 * Engine 通过该类获取 InputSource，
 * 从而统一处理键盘输入和字符串输入。
 *
 * @author Everlasting
 */
public class Input {

    public static InputSource keyboardInput() {
        return new KeyboardInputSource();
    }

    public static InputSource stringInput(String input) {
        return new StringInputSource(input);
    }
}
