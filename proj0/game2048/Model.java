package game2048;

import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author icovo
 */
public class Model extends Observable {
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

//    public class

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */

    public boolean tileNullJudge (int col, int row) {
        Tile t = board.tile (col, row);
        if (t == null) return true;
        else return false;
    }

    // 判断两tile能否合并
    public boolean tileMerge (Tile tile1, Tile tile2) {
        if (tile1.value() == tile2.value()) {
            return true;
        } else {
            return false;
        }
    }

    // 每次合并都需要计算价值
    public void addScore (Tile t1, Tile t2) {
        score = score + t1.value() + t2.value();
    }

    public int max (int x, int y) {
        return x > y ? x : y;
    }


    // 找每一行看能否找到相邻的看能否合并, 我们每次找完一行都会移动
    public int handleRow (int col, int row, int highLim) {
        for (int newr = row + 1; newr <= highLim; newr ++) {
            if (tileNullJudge(col, newr)) continue;
            else {
                Tile pas = board.tile (col, row);
                Tile aft = board.tile (col, newr);
                if (tileMerge(pas, aft)) {
                    addScore(pas, aft);
                    board.move (col, newr, pas);
                    highLim = max (newr - 1, 0);
                } else {
                    // 边界问题?
                    board.move (col, newr - 1, pas);
                    highLim = max (newr - 1, 0);
                }

                return highLim;
            }
        }

        Tile t = board.tile (col, row);
        if (row != highLim) {
            board.move (col, highLim, t);
            return -1;
        } else {
            return highLim;
        }
    }

    // 处理列, 并且需要知道是否真的操作过. 注意需要定义一个上线代表合并查找的范围hignLim, 可以对比每次hignLim是否变化判断是否操作过
    public boolean handleCol (int col) {
        int hignLim = board.size() - 1;

        boolean changed = false;

        for (int row = board.size() - 2; row >= 0; row--) {
            if (tileNullJudge(col, row)) continue;

            int newhignLim = handleRow (col, row, hignLim);
            if (newhignLim == -1) {
                changed = true;
            } else if (newhignLim != hignLim) {
                changed = true;
                hignLim = newhignLim;
            }
        }

        return changed;
    }

//    public boolean moveToEmpty (int col, int row) {
//        Tile t = board.tile(col, row);
//        for (int findr = board.size() - 1; findr >= row + 1; findr --) {
//            if (tileNullJudge(col, findr)) {
//                board.move(col, findr, t);
//                return true;
//            }
//        }
//
//        return false;
//    }

//    public boolean CheckFinal (int col) {
//        boolean changed = false;
//        for (int nowr = board.size() - 1; nowr >= 0; nowr --) {
//            if (tileNullJudge(col, nowr)) continue;
//
//            if (moveToEmpty(col, nowr)) {
//                changed = true;
//            }
//        }
//
//        return changed;
//    }

    public boolean tilt(Side side) {
        boolean changed;
        changed = false;

        // TODO: Modify this.board (and perhaps this.score) to account
        // for the tilt to the Side SIDE. If the board changed, set the
        // changed local variable to true.

        board.setViewingPerspective(side);
        for (int col = 0; col < board.size(); col ++) {
            if (handleCol(col)) {
                changed = true;
            }
        }

        board.setViewingPerspective(Side.NORTH);

//        for (int col = 0; col < board.size(); col ++) {
//            if (CheckFinal(col)) {
//                changed = true;
//            }
//        }

        checkGameOver();
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i ++) {
            for (int j = 0; j < b.size(); j ++) {
                Tile t = b.tile(i, j);
                if (t == null) return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any tile is equal to the maximum valid value.
     * Maximum valid value is given by MAX_PIECE. Note that
     * given a Tile object t, we get its value with t.value().
     */
    public static boolean maxTileExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i ++) {
            for (int j = 0; j < b.size(); j ++) {
                Tile t = b.tile (i, j);
                if (t == null) {
                    continue;
                }
                if (t.value() == MAX_PIECE) return true;
            }
        }
        return false;
    }

    /**
     * Returns true if there are any valid moves on the board.
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.
     * 2. There are two adjacent tiles with the same value.
     */
    public static boolean atLeastOneMoveExists(Board b) {
        // TODO: Fill in this function.
        for (int i = 0; i < b.size(); i ++) {
            for (int j = 0; j < b.size(); j ++) {
                Tile t = b.tile (i, j);
                if (t == null) return true;
            }
        }

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};
        for (int i = 0; i < b.size(); i ++) {
            for (int j = 0; j < b.size(); j ++) {
                for (int k1 = 0; k1 < 4; k1 ++) {
                    int nx = i + dx[k1];
                    int ny = j + dy[k1];

                    if (nx >= 0 && nx < b.size() && ny >= 0 && ny < b.size()) {
                        Tile near = b.tile (nx, ny);
                        Tile now = b.tile (i, j);

                        if (near.value() == now.value()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
