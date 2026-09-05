package byow.Core.Render;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;

import static byow.Core.Render.WorldRender.HEIGHT;
import static byow.Core.Render.WorldRender.WIDTH;

public class MenuRender {

    public static void render() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));

        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.75,
                        "CS61B: THE GAME");

        StdDraw.setFont(new Font("Monaco", Font.PLAIN, 20));
        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.50,
                "New Game (N)");

        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.42,
                "Load Game (L)");

        StdDraw.text(WIDTH / 2.0, HEIGHT * 0.34,
                "Quit (Q)");

        StdDraw.show();
    }

}
