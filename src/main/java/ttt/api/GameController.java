package ttt.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ttt.api.dto.*;
import ttt.service.GameService;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService svc;

    public GameController(GameService svc) { this.svc = svc; }

    @PostMapping
    public ResponseEntity<GameStateDTO> create(@Valid @RequestBody NewGameRequest req) {
        return ResponseEntity.ok(svc.createGame(req.mode(), req.aiPlays()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameStateDTO> get(@PathVariable String id) {
        return ResponseEntity.ok(svc.getState(id));
    }

    @PostMapping("/{id}/play")
    public ResponseEntity<GameStateDTO> play(@PathVariable String id, @Valid @RequestBody PlayRequest req) {
        return ResponseEntity.ok(svc.playHumanMove(id, req.index()));
    }

    @PostMapping("/{id}/ai-move")
    public ResponseEntity<GameStateDTO> aiMove(@PathVariable String id) {
        return ResponseEntity.ok(svc.playAiMove(id));
    }

    @GetMapping("/{id}/hint")
    public ResponseEntity<HintResponse> hint(@PathVariable String id) {
        int idx = svc.hint(id);
        return ResponseEntity.ok(new HintResponse(idx));
    }
}

