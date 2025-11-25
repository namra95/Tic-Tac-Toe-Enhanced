package ttt.core.ai;

import ttt.core.Board;

/**
 * A game-playing agent that chooses a move (0..8) for the current board state.
 */
public interface Bot {
    /**
     * Decide which move to play (index 0..8).
     * Throws IllegalStateException if no legal moves exist.
     */
    int chooseMove(Board board);
}
