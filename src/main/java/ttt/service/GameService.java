package ttt.service;

import org.springframework.stereotype.Service;
import ttt.api.dto.GameStateDTO;
import ttt.core.*;
import ttt.core.ai.Minimax;
import ttt.domain.GameSession;
import ttt.domain.Mode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {
    private final Map<UUID, GameSession> store = new ConcurrentHashMap<>();

    public GameStateDTO createGame(String modeStr, String aiPlaysStr) {
        Mode mode = Mode.valueOf(modeStr);
        Mark starts = Mark.X; // X starts standard
        Board board = Board.initial(starts);

        Mark aiPlays = null;
        if (mode == Mode.PVE) {
            if (aiPlaysStr == null) throw new BadRequestException("aiPlays must be 'X' or 'O' for PVE");
            aiPlays = Mark.valueOf(aiPlaysStr);
        }

        UUID id = UUID.randomUUID();
        var session = new GameSession(id, board, mode, aiPlays);
        store.put(id, session);
        return toDTO(session);
    }

    public GameStateDTO getState(String id) {
        var s = get(id);
        return toDTO(s);
    }

    public GameStateDTO playHumanMove(String id, int index) {
        var s = get(id);
        if (Rules.isTerminal(s.board())) throw new BadRequestException("Game is already terminal.");
        if (!s.board().isLegal(index)) throw new BadRequestException("Illegal move: " + index);

        s.setBoard(s.board().apply(index));
        return toDTO(s);
    }

    public GameStateDTO playAiMove(String id) {
        var s = get(id);
        if (s.mode() != Mode.PVE) throw new BadRequestException("AI move only allowed in PVE mode.");
        if (Rules.isTerminal(s.board())) throw new BadRequestException("Game is already terminal.");

        Mark aiSide = s.aiPlays();
        if (aiSide == null) throw new BadRequestException("AI side not set.");

        // Only move if it's AI's turn
        if (s.board().toMove() != aiSide) throw new BadRequestException("It's not AI's turn.");

        Minimax ai = Minimax.hard(aiSide); // or medium/easy depending on query param later
        int mv = ai.chooseMove(s.board());
        s.setBoard(s.board().apply(mv));
        return toDTO(s);
    }

    public int hint(String id) {
        var s = get(id);
        if (Rules.isTerminal(s.board())) throw new BadRequestException("Game is terminal; no hint.");
        // Hint from the current player's perspective:
        Mark side = s.board().toMove();
        Minimax ai = Minimax.hard(side);
        return ai.chooseMove(s.board());
    }

    private GameSession get(String id) {
        try {
            UUID uuid = UUID.fromString(id);
            var s = store.get(uuid);
            if (s == null) throw new NotFoundException("Game not found: " + id);
            return s;
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid UUID: " + id);
        }
    }

    private static GameStateDTO toDTO(GameSession s) {
        Board b = s.board();
        GameResult r = Rules.result(b);
        String winner = switch (r) {
            case X_WIN -> "X";
            case O_WIN -> "O";
            default -> "";
        };
        return new GameStateDTO(
                s.id().toString(),
                encodeBoard(b),
                b.toMove().name(),
                r.name(),
                winner,
                s.mode().name(),
                s.aiPlays() == null ? "" : s.aiPlays().name()
        );
    }

    /** Encode board to string like "XO..O.X.." */
    private static String encodeBoard(Board b) {
        StringBuilder sb = new StringBuilder(9);
        for (int i = 0; i < 9; i++) {
            sb.append(switch (b.cell(i)) {
                case X -> 'X';
                case O -> 'O';
                case EMPTY -> '.';
            });
        }
        return sb.toString();
    }
}
