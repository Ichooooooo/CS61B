package byow.Core.Input;

import edu.princeton.cs.introcs.StdDraw;

public class KeyboardInputSource implements InputSource {

    @Override
    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                return c;
            }
        }
    }

    @Override
    public boolean possibleNextInput() {
        return true;
    }
}
