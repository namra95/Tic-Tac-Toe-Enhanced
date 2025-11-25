package ttt.core;

import com.sun.jdi.ArrayReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Board {
    private final Mark[] cells; // length 9
    private final Mark toMove; // who's turn x or o

    //constructor validates input and sets it in a safe internal state
    public Board(Mark[] cells, Mark toMove) {
        Objects.requireNonNull(cells, "cells");
        Objects.requireNonNull(toMove, "toMove");
        if (cells.length != 9) {
            throw new IllegalArgumentException("Board must have 9 cells");
        }
        this.cells = Arrays.copyOf(cells, 9);
        this.toMove = toMove;
    }

    //create fresh new board
    public static Board initial(Mark starts) {
        Mark[] cells = new Mark[9];
        Arrays.fill(cells, Mark.EMPTY);
        return new Board(cells, starts);
    }

    //getter for whose turn it is
    public Mark toMove() {
        return toMove;
    }

    //check what mark is in a specific cell
    public Mark cell(int idx) {
        if (idx < 0 || idx > 8) {
            throw new IllegalArgumentException("index b/w 0-8");
        }
        return cells[idx];
    }

    //check for valid move n return a list of valid cells
    public List<Integer> legalMoves() {
        List<Integer> moves = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            if (cells[i] == Mark.EMPTY) {
                moves.add(i);
            }
        }
        return moves;
    }


    //check if 1 move is valid
    public boolean isLegal(int idx) {
        return idx >= 0 && idx < 9 && cells[idx] == Mark.EMPTY;
    }

    //return new board after move is applied
    public Board apply(int idx) {
        if(!isLegal(idx)) {
            throw new IllegalArgumentException("Illegal move at " + idx);
        }
        if (Rules.isTerminal(this)) { //this is the current game obj
            throw new IllegalStateException("Game is already terminal"); //check if game has ended
        }
        Mark[] next = Arrays.copyOf(cells, 9);//clone board
        next[idx] = toMove; //place mark on idx
        return new Board(next, toMove.opponent());
    }

    public Board apply(Move m) {
        return apply(m.index());
    }

    //prevent modifying the internal board state(for DTO)
    public Mark[] cells() {
        return Arrays.copyOf(cells, 9);
    }

    //for logging/debugging
    @Override public String toString() {
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 9; i++) {
            sb.append(switch (cells[i]) {
                case X -> 'X';
                case O -> 'O';
                default -> '.';
            });
            if (i % 3 == 2 && i < 8) {
                sb.append('/');
            }
        }
        sb.append(" turn=").append(toMove);
        return sb.toString();
    }

    //check game state by comparing boards
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Board)) return false;
        Board b = (Board) o;
        return Arrays.equals(this.cells, b.cells);
    }

    //check for board equality for AI
    @Override public int hashCode() {
        return 31 * Arrays.hashCode(cells) + toMove.hashCode();
    }


}
