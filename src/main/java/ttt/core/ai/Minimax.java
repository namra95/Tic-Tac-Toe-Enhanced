package ttt.core.ai;

import ttt.core.*;

import java.util.*;

/**
 * Minimax AI with optional alpha-beta pruning and depth-limited search.
 * Score positions from the perspective of aiMark (X or O).
 */
public final class Minimax implements Bot {

    // Big terminal outcomes
    private static final int WIN_SCORE   =  1000;
    private static final int LOSS_SCORE  = -1000;
    private static final int DRAW_SCORE  =  0;

    // Heuristic weights for "not finished yet"
    private static final int TWO_IN_ROW_OPEN = 10; // open threat or blockable threat
    private static final int CENTER_WEIGHT   = 3;
    private static final int CORNER_WEIGHT   = 2;

    // Winning lines (same logic Rules uses)
    private static final int[][] LINES = {
            {0,1,2},{3,4,5},{6,7,8},
            {0,3,6},{1,4,7},{2,5,8},
            {0,4,8},{2,4,6}
    };
    private static final int[] CORNERS = {0,2,6,8};
    private static final int CENTER = 4;

    private final Mark aiMark;
    private final boolean useAlphaBeta;
    private final int maxDepth;
    private final Map<StateKey, Integer> memo = new HashMap<>();

    /**
     * @param aiMark which side the bot is playing as (X or O)
     * @param useAlphaBeta true to enable pruning
     * @param maxDepth how far to search. 9 ~ perfect for 3x3.
     */
    public Minimax(Mark aiMark, boolean useAlphaBeta, int maxDepth) {
        if (aiMark == Mark.EMPTY) throw new IllegalArgumentException("aiMark must be X or O");
        if (maxDepth < 1) throw new IllegalArgumentException("maxDepth >= 1 required");
        this.aiMark = aiMark;
        this.useAlphaBeta = useAlphaBeta;
        this.maxDepth = maxDepth;
    }

    // Convenience factories for difficulty levels.
    public static Minimax easy(Mark aiMark)   { return new Minimax(aiMark, true, 2); } // shallow -> can blunder
    public static Minimax medium(Mark aiMark) { return new Minimax(aiMark, true, 4); }
    public static Minimax hard(Mark aiMark)   { return new Minimax(aiMark, true, 9); } // basically perfect

    @Override
    public int chooseMove(Board board) {
        List<Integer> legal = board.legalMoves();
        if (legal.isEmpty()) throw new IllegalStateException("No legal moves.");

        // Small practical opening heuristic: if we're deep search and center is open, just take center.
        if (maxDepth >= 9 && board.isLegal(CENTER)) {
            return CENTER;
        }

        int bestMove = legal.get(0);
        int bestScore = Integer.MIN_VALUE;

        int alpha = Integer.MIN_VALUE;
        int beta  = Integer.MAX_VALUE;

        for (int mv : legal) {
            Board next = board.apply(mv);
            int score = minValue(next, 1, alpha, beta);
            if (score > bestScore) {
                bestScore = score;
                bestMove = mv;
            }
            if (useAlphaBeta) {
                alpha = Math.max(alpha, bestScore);
            }
        }
        return bestMove;
    }

    // MIN layer: opponent tries to reduce our score
    private int minValue(Board b, int depth, int alpha, int beta) {
        GameResult res = Rules.result(b);
        if (res != GameResult.IN_PROGRESS) return terminalScore(res);
        if (depth >= maxDepth) return heuristic(b);

        StateKey key = new StateKey(b, depth, false);
        Integer cached = memo.get(key);
        if (cached != null) return cached;

        int val = Integer.MAX_VALUE;
        for (int mv : b.legalMoves()) {
            int score = maxValue(b.apply(mv), depth + 1, alpha, beta);
            val = Math.min(val, score);

            if (useAlphaBeta) {
                if (val <= alpha) break;       // prune
                beta = Math.min(beta, val);
            }
        }
        memo.put(key, val);
        return val;
    }

    // MAX layer: AI tries to improve its score
    private int maxValue(Board b, int depth, int alpha, int beta) {
        GameResult res = Rules.result(b);
        if (res != GameResult.IN_PROGRESS) return terminalScore(res);
        if (depth >= maxDepth) return heuristic(b);

        StateKey key = new StateKey(b, depth, true);
        Integer cached = memo.get(key);
        if (cached != null) return cached;

        int val = Integer.MIN_VALUE;
        for (int mv : b.legalMoves()) {
            int score = minValue(b.apply(mv), depth + 1, alpha, beta);
            val = Math.max(val, score);

            if (useAlphaBeta) {
                if (val >= beta) break;        // prune
                alpha = Math.max(alpha, val);
            }
        }
        memo.put(key, val);
        return val;
    }

    /**
     * Assign scores to terminal states from aiMark's perspective.
     */
    private int terminalScore(GameResult r) {
        return switch (r) {
            case X_WIN -> (aiMark == Mark.X) ? WIN_SCORE : LOSS_SCORE;
            case O_WIN -> (aiMark == Mark.O) ? WIN_SCORE : LOSS_SCORE;
            case DRAW  -> DRAW_SCORE;
            case IN_PROGRESS -> throw new IllegalArgumentException("Not terminal");
        };
    }

    /**
     * Heuristic score for non-terminal boards (depth limit reached).
     * Positive score is good for aiMark, negative is bad.
     */
    private int heuristic(Board b) {
        int score = 0;

        // 1. Line-based threats:
        //    + if ai has 2 in a line and the 3rd is empty (winning threat)
        //    - if opponent has that
        for (int[] line : LINES) {
            int aiCount = 0;
            int oppCount = 0;
            int emptyCount = 0;

            for (int idx : line) {
                Mark m = b.cell(idx);
                if (m == aiMark) aiCount++;
                else if (m == Mark.EMPTY) emptyCount++;
                else oppCount++;
            }

            if (aiCount == 2 && emptyCount == 1) score += TWO_IN_ROW_OPEN;
            if (oppCount == 2 && emptyCount == 1) score -= TWO_IN_ROW_OPEN;
        }

        // 2. Center control
        if (b.cell(CENTER) == aiMark) {
            score += CENTER_WEIGHT;
        } else if (b.cell(CENTER) == aiMark.opponent()) {
            score -= CENTER_WEIGHT;
        }

        // 3. Corner control
        for (int c : CORNERS) {
            if (b.cell(c) == aiMark) {
                score += CORNER_WEIGHT;
            } else if (b.cell(c) == aiMark.opponent()) {
                score -= CORNER_WEIGHT;
            }
        }

        return score;
    }

    /**
     * Compact hash key for memoization.
     * We encode:
     * - which marks are in each of the 9 cells
     * - which depth we're at (just parity, not full depth)
     * - whether we're in a MAX node or MIN node
     *
     * This keeps repeated board states from being re-evaluated.
     */
    private static final class StateKey {
        private final long packed;

        StateKey(Board b, int depth, boolean maxNode) {
            long p = 0L;
            for (int i = 0; i < 9; i++) {
                long v = switch (b.cell(i)) {
                    case EMPTY -> 0L;
                    case X -> 1L;
                    case O -> 2L;
                };
                p |= (v << (i * 2)); // 2 bits per cell
            }

            // Add a couple bits of extra context to reduce collisions
            // bit 20: maxNode flag
            if (maxNode) {
                p |= (1L << 20);
            }
            // bit 21: depth parity
            if ((depth & 1) == 1) {
                p |= (1L << 21);
            }

            this.packed = p;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof StateKey k) && k.packed == this.packed;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(packed);
        }
    }
}
