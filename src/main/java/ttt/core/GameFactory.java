package ttt.core;

public final class GameFactory {
    private GameFactory() {}
    public static GameState newGameXStarts() { return GameState.of(Board.initial(Mark.X)); }
    public static GameState newGameOStarts() { return GameState.of(Board.initial(Mark.O)); }
}

