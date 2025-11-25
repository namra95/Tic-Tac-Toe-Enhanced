package ttt.core;
import ttt.core.ai.Minimax;

import java.util.Scanner;

public class Demo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // X = human, O = AI
        GameState game = GameFactory.newGameXStarts();
        Board board = game.board();
        Minimax ai = Minimax.hard(Mark.O); // AI plays O (hard mode)

        while (!Rules.isTerminal(board)) {
            // show board
            System.out.println(pretty(board));

            // human turn
            System.out.print("Your move (0-8): ");
            int mv = sc.nextInt();
            if (!board.isLegal(mv)) {
                System.out.println("Illegal move, try again.");
                continue;
            }
            board = board.apply(mv);
            if (Rules.isTerminal(board)) break;

            // AI turn
            int aiMove = ai.chooseMove(board);
            System.out.println("AI plays: " + aiMove);
            board = board.apply(aiMove);
        }

        System.out.println(pretty(board));
        System.out.println("Game over: " + Rules.result(board));
    }

    private static String pretty(Board b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            Mark m = b.cell(i);
            char c = switch (m) {
                case X -> 'X';
                case O -> 'O';
                case EMPTY -> '.';
            };
            sb.append(c);
            if (i % 3 == 2) sb.append('\n'); else sb.append(" ");
        }
        sb.append("Turn: ").append(b.toString().contains("turn=") ? "" : "").append(b.toString());
        return sb.toString();
    }
}

