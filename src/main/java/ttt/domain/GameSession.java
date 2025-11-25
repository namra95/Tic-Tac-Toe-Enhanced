package ttt.domain;

import ttt.core.Board;
import ttt.core.Mark;

import java.time.Instant;
import java.util.UUID;

public class GameSession {
    private final UUID id;
    private Board board;
    private final Mode mode;
    private final Mark aiPlays; // null for PVP; otherwise X or O
    private final Instant createdAt;

    public GameSession(UUID id, Board board, Mode mode, Mark aiPlays) {
        this.id = id;
        this.board = board;
        this.mode = mode;
        this.aiPlays = aiPlays;
        this.createdAt = Instant.now();
    }

    public UUID id() { return id; }
    public Board board() { return board; }
    public void setBoard(Board b) { this.board = b; }
    public Mode mode() { return mode; }
    public Mark aiPlays() { return aiPlays; }
    public Instant createdAt() { return createdAt; }
}
