package ttt.core;

public record GameState(Board board, GameResult result) {
    public static GameState of(Board b) { return new GameState(b, Rules.result(b)); }

    public boolean isTerminal() { return result != GameResult.IN_PROGRESS; }
}
