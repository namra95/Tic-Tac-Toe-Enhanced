import { useState } from "react";
import "./App.css";

const API_BASE = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

function App() {
  const [game, setGame] = useState(null);     // current game state from backend
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastHint, setLastHint] = useState(null);

  const hasGame = !!game;

  async function startGame(mode = "PVE", aiPlays = "O") {
    setLoading(true);
    setError("");
    setLastHint(null);
    try {
      const res = await fetch(`${API_BASE}/games`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ mode, aiPlays }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.error || `Create failed: ${res.status}`);
      }
      const data = await res.json();
      setGame(data);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  // Play a human move, then (if PVE) trigger AI move
  async function handleCellClick(index) {
    if (!game || loading) return;

    // can't play if game over
    if (game.status !== "IN_PROGRESS") return;

    // can't play in non-empty cell
    if (game.board[index] !== ".") return;

    // in PVE, don't let human play on AI's turn
    if (game.mode === "PVE" && game.toMove === game.aiPlays) return;

    setLoading(true);
    setError("");
    setLastHint(null);

    try {
      // 1. send human move
      const playRes = await fetch(`${API_BASE}/games/${game.gameId}/play`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ index }),
      });
      if (!playRes.ok) {
        const body = await playRes.json().catch(() => ({}));
        throw new Error(body.error || `Play failed: ${playRes.status}`);
      }
      let updated = await playRes.json();
      setGame(updated);

      // 2. if still in progress and it's AI's turn in PVE, ask AI to move
      if (
        updated.mode === "PVE" &&
        updated.status === "IN_PROGRESS" &&
        updated.aiPlays === updated.toMove
      ) {
        const aiRes = await fetch(
          `${API_BASE}/games/${updated.gameId}/ai-move`,
          { method: "POST" }
        );
        if (!aiRes.ok) {
          const body = await aiRes.json().catch(() => ({}));
          throw new Error(body.error || `AI move failed: ${aiRes.status}`);
        }
        updated = await aiRes.json();
        setGame(updated);
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }

  async function requestHint() {
    if (!game || game.status !== "IN_PROGRESS") return;

    setError("");
    setLastHint(null);
    try {
      const res = await fetch(`${API_BASE}/games/${game.gameId}/hint`);
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.error || `Hint failed: ${res.status}`);
      }
      const data = await res.json();
      setLastHint(data.index);
    } catch (e) {
      setError(e.message);
    }
  }

  function renderStatus() {
    if (!game) return "No game started yet.";
    if (game.status === "IN_PROGRESS") {
      if (game.mode === "PVE" && game.toMove === game.aiPlays) {
        return `AI (${game.aiPlays}) is thinking...`;
      }
      return `Turn: ${game.toMove}`;
    }
    if (game.status === "DRAW") return "It's a draw!";
    if (game.winner) return `Winner: ${game.winner}`;
    return `Status: ${game.status}`;
  }

  function cellClass(i) {
    let base = "cell";
    if (!game) return base;
    if (game.board[i] !== ".") base += " cell--filled";
    if (lastHint === i) base += " cell--hint";
    return base;
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Tic-Tac-Toe (Spring Boot + React)</h1>
        <p className="subtitle">
          Player vs Player or Player vs AI (Minimax on backend)
        </p>
      </header>

      <main className="main">
        <section className="controls">
          <h2>New Game</h2>
          <div className="button-row">
            <button
              disabled={loading}
              onClick={() => startGame("PVP", null)}
            >
              👥 Player vs Player
            </button>
            <button
              disabled={loading}
              onClick={() => startGame("PVE", "O")}
            >
              🤖 Player (X) vs AI (O)
            </button>
            <button
              disabled={loading}
              onClick={() => startGame("PVE", "X")}
            >
              🤖 AI (X) vs Player (O)
            </button>
          </div>

          {hasGame && (
            <div className="info-row">
              <span>
                Mode: {game.mode}{" "}
                {game.mode === "PVE" && `(AI plays ${game.aiPlays})`}
              </span>
              <span>Game ID: {game.gameId}</span>
            </div>
          )}

          {error && <div className="error">⚠ {error}</div>}
        </section>

        <section className="board-section">
          <h2>Board</h2>
          <div className="board">
            {Array.from({ length: 9 }).map((_, idx) => (
              <button
                key={idx}
                className={cellClass(idx)}
                onClick={() => handleCellClick(idx)}
                disabled={
                  !game ||
                  loading ||
                  game.status !== "IN_PROGRESS" ||
                  game.board[idx] !== "." ||
                  (game.mode === "PVE" &&
                    game.toMove === game.aiPlays) // AI's turn
                }
              >
                {game ? (game.board[idx] === "." ? "" : game.board[idx]) : ""}
              </button>
            ))}
          </div>

          <div className="status-row">
            <span>{renderStatus()}</span>
            <div className="status-buttons">
              <button
                onClick={requestHint}
                disabled={
                  !game || loading || game.status !== "IN_PROGRESS"
                }
              >
                💡 Hint
              </button>
              <button
                onClick={() => {
                  setGame(null);
                  setError("");
                  setLastHint(null);
                }}
                disabled={loading}
              >
                Clear Board
              </button>
            </div>
          </div>

          {lastHint != null && game && (
            <div className="hint-text">
              Suggested move for <strong>{game.toMove}</strong>: cell{" "}
              <code>{lastHint}</code>
            </div>
          )}
        </section>
      </main>

      <footer className="footer">
        <span>Backend: Spring Boot · Engine: Minimax Tic-Tac-Toe</span>
      </footer>
    </div>
  );
}

export default App;
