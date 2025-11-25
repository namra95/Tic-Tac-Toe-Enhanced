package ttt.core.ai;

import org.junit.jupiter.api.Test;
import ttt.core.Board;
import ttt.core.Mark;

import static org.junit.jupiter.api.Assertions.*;

class MinimaxTest {

    @Test
    void aiTakesImmediateWin() {
        // X to move, X can win by playing index 2
        Board b = Board.initial(Mark.X)
                .apply(0) // X
                .apply(3) // O
                .apply(1); // X
        Minimax ai = Minimax.hard(Mark.X);
        int move = ai.chooseMove(b);
        assertEquals(2, move, "AI should take winning move at 2");
    }

    @Test
    void aiBlocksImmediateLoss() {
        // O threatens to win at index 2 next turn, so X must block
        Board b = Board.initial(Mark.X)
                .apply(3) // X
                .apply(0) // O
                .apply(4) // X
                .apply(1); // O  -> O O _ across 0,1,2
        Minimax ai = Minimax.hard(Mark.X);
        int move = ai.chooseMove(b);
        assertEquals(2, move, "AI should block O's win at 2");
    }

    @Test
    void aiPrefersCenterOnEmptyBoard() {
        Board b = Board.initial(Mark.X);
        Minimax ai = Minimax.hard(Mark.X);
        int move = ai.chooseMove(b);
        assertEquals(4, move, "Center is strongest opening in 3x3");
    }

    @Test
    void mediumStillChoosesLegalMoves() {
        Board b = Board.initial(Mark.X).apply(4); // X took center already
        Minimax ai = Minimax.medium(Mark.O);      // O to play
        int move = ai.chooseMove(b);
        assertTrue(b.isLegal(move), "Medium bot should return a legal move");
    }
}
