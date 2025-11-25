package ttt.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RulesTest {

    @Test
    void xRowWinDetected() {
        // X plays 0,1,2
        Board b = Board.initial(Mark.X)
                .apply(0) // X
                .apply(3) // O
                .apply(1) // X
                .apply(4) // O
                .apply(2); // X wins row 0-1-2
        assertEquals(GameResult.X_WIN, Rules.result(b));
        assertTrue(Rules.isTerminal(b));
        assertEquals(Mark.X, Rules.winner(b));
    }

    @Test
    void drawDetected() {
        // A standard 3x3 draw sequence
        // X:0 O:1 X:2 O:4 X:3 O:5 X:7 O:6 X:8
        Board b = Board.initial(Mark.X)
                .apply(0).apply(1)
                .apply(2).apply(4)
                .apply(3).apply(5)
                .apply(7).apply(6)
                .apply(8);
        assertEquals(GameResult.DRAW, Rules.result(b));
        assertTrue(Rules.isTerminal(b));
        assertEquals(Mark.EMPTY, Rules.winner(b));
    }
}

