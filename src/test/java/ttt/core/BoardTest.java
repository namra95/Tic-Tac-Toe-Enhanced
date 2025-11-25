package ttt.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    @Test
    void initialBoardHasNineLegalMoves() {
        Board b = Board.initial(Mark.X);
        List<Integer> moves = b.legalMoves();
        assertEquals(9, moves.size());
        assertTrue(moves.containsAll(List.of(0,1,2,3,4,5,6,7,8)));
        assertEquals(Mark.X, b.toMove());
    }

    @Test
    void applyFlipsTurnAndReducesLegalMoves() {
        Board b = Board.initial(Mark.X);
        Board b2 = b.apply(4);
        assertEquals(Mark.O, b2.toMove());
        assertFalse(b2.isLegal(4));
        assertEquals(8, b2.legalMoves().size());
    }

    @Test
    void illegalMoveThrows() {
        Board b = Board.initial(Mark.X).apply(0);
        assertThrows(IllegalArgumentException.class, () -> b.apply(0));
    }
}
