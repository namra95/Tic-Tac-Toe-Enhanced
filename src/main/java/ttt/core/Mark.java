package ttt.core;

public enum Mark {
    X, O, EMPTY;

    public Mark opponent() {
        return switch (this) {
            case X -> O;
            case O -> X;
            default -> EMPTY;
        };
    }
}
