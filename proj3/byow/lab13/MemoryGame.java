package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful Render'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful Render'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) {
            int pos = RandomUtils.uniform(this.rand, 0, CHARACTERS.length);
            s.append(CHARACTERS[pos]);
        }
        return s.toString();
    }

    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);

        if (!gameOver) {
            Font smallFont = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(smallFont);

            StdDraw.text(3.5, height - 1, "Round: " + round);

            if (playerTurn) {
                StdDraw.text(width / 2.0, height - 1, "Type!");
            } else {
                StdDraw.text(width / 2.0, height - 1, "Watch!");
            }

            int pos = RandomUtils.uniform(rand, 0, ENCOURAGEMENT.length);
            StdDraw.textRight(width - 1, height - 1, ENCOURAGEMENT[pos]);

            StdDraw.line(0, height - 2, width, height - 2);
        }

        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);

        StdDraw.text(width / 2.0, height / 2.0, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (char c : letters.toCharArray()) {
            drawFrame(String.valueOf(c));
            StdDraw.pause(1000);
            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        StringBuilder input = new StringBuilder();
        while (input.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                input.append(c);
                drawFrame(input.toString());
            }
        }
        return input.toString();
    }

    public void startGame() {
        round = 1;
        gameOver = false;
        while (!gameOver) {
            playerTurn = false;
            drawFrame("Round: " + round);
            StdDraw.pause(1000);
            String randomString = generateRandomString(round);
            flashSequence(randomString);
            playerTurn = true;
            drawFrame("");
            String inputString = solicitNCharsInput(round);
            StdDraw.pause(500);
            if (!randomString.equals(inputString)) {
                gameOver = true;
            } else {
                round++;
            }
        }
        drawFrame("Game Over! You made it to round: " + round);
    }

}
