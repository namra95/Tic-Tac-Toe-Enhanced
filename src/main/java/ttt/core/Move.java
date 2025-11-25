package ttt.core;

public record Move(int index) {
    public Move {
        if (index < 0 || index > 8) {
            throw new IllegalArgumentException("Move index must be b/w 0-8");
        }
    }
}
