package ttt.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PlayRequest(
        @Min(0) @Max(8) int index
) {}

