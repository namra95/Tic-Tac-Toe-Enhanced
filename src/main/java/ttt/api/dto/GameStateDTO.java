package ttt.api.dto;

public record GameStateDTO(
        String gameId,
        String board,     // e.g. "XO..O.X.."
        String toMove,    // "X" or "O"
        String status,    // "IN_PROGRESS","X_WIN","O_WIN","DRAW"
        String winner,    // "X","O","" (empty if none)
        String mode,      // "PVP"|"PVE"
        String aiPlays    // "X"|"O"|"" (empty if PVP)
) {}

