package ttt.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record NewGameRequest(
        @NotBlank @Pattern(regexp = "PVP|PVE") String mode,
        // When PVE, who should the AI play as? "X" or "O". Optional for PVP.
        @Pattern(regexp = "X|O") String aiPlays
) {}

