package ttt.core;

public final class Rules {
    private static final int[][] LINES = { //winning lines
            {0,1,2},{3,4,5},{6,7,8},   // rows
            {0,3,6},{1,4,7},{2,5,8},   // cols
            {0,4,8},{2,4,6}            // diags
    };

    private Rules() {} //prevent creating rule obj

    public static GameResult result(Board b) {
        Mark winner = winner(b);
        if (winner == Mark.X) {
            return GameResult.X_WIN;
        }
        if (winner == Mark.O) {
            return GameResult.O_WIN;
        }
        boolean anyEmpty = false;
        for (int i = 0; i < 9; i++) {
            if (b.cell(i) == Mark.EMPTY) {
                anyEmpty = true;
                break;
            }
        }
        return anyEmpty ? GameResult.IN_PROGRESS : GameResult.DRAW;
    }

    //check if game over
    public static boolean isTerminal(Board b) {
        return result(b) != GameResult.IN_PROGRESS;
    }

    public static Mark winner(Board b) {
        for (int[] line : LINES) { //for each set
            Mark a = b.cell(line[0]); //check first cell
            if (a == Mark.EMPTY) { //if first cell is empty, skip
                continue;
            }
            if (a == b.cell(line[1]) && a == b.cell(line[2])) { //compare each cell in a line
                return a; //return that mark as winner
            }
        }
        return Mark.EMPTY; //no one won yet
    }

}
