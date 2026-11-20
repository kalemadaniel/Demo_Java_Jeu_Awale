package awale;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Une position immuable de la variante à semis direct. */
final class AwaleGame {
    static final int COLUMNS = 8;
    static final int SIDE = 2 * COLUMNS;
    static final int PIT_COUNT = 2 * SIDE;
    static final int HUMAN = 0;
    static final int OPPONENT = 1;
    static final int PLAYING = -2;
    static final int DRAW = -1;

    private final int[] pits;
    private final int turn;
    private final int winner;
    private final int captured;

    AwaleGame() {
        this(opening(), HUMAN, PLAYING, 0);
    }

    private AwaleGame(int[] pits, int turn, int winner, int captured) {
        this.pits = pits;
        this.turn = turn;
        this.winner = winner;
        this.captured = captured;
    }

    int turn() { return turn; }
    int seeds(int pit) { return pits[pit]; }
    int winner() { return winner; }
    int captured() { return captured; }
    boolean finished() { return winner != PLAYING; }

    boolean canPlay(int pit) {
        return !finished() && pit >= 0 && pit < PIT_COUNT && pit / SIDE == turn && pits[pit] >= 2;
    }

    AwaleGame play(int pit) {
        if (!canPlay(pit)) {
            throw new IllegalArgumentException("Case non jouable.");
        }
        int[] board = pits.clone();
        int own = turn * SIDE;
        int other = (1 - turn) * SIDE;
        int source = pit - own;
        int hand = board[pit];
        int taken = 0;
        board[pit] = 0;
        Set<String> relayPositions = null;
        int laps = 0;
        while (true) {
            // Avant de mémoriser les semis longs, la plupart des coups se sont déjà terminés.
            if (++laps == 16) {
                relayPositions = new HashSet<>();
            }
            if (relayPositions != null && !relayPositions.add(source + "/" + hand + Arrays.toString(board))) {
                board[own + source] += hand;
                return new AwaleGame(board, 1 - turn, DRAW, taken);
            }

            int last = source;
            while (hand > 0) {
                last = (last + 1) % SIDE;
                board[own + last]++;
                hand--;
            }
            int landing = own + last;
            if (board[landing] == 1) {
                break;
            }

            if (last >= COLUMNS) {
                int oppositeInner = other + SIDE + COLUMNS - 1 - last;
                int oppositeOuter = other + last - COLUMNS;
                if (board[oppositeInner] > 0 && board[oppositeOuter] > 0) {
                    hand = board[oppositeInner] + board[oppositeOuter];
                    taken += hand;
                    board[oppositeInner] = 0;
                    board[oppositeOuter] = 0;
                    continue;
                }
            }
            source = last;
            hand = board[landing];
            board[landing] = 0;
        }

        int nextWinner = playableCount(board, 1 - turn) == 0 ? turn : PLAYING;
        return new AwaleGame(board, 1 - turn, nextWinner, taken);
    }

    AwaleGame draw() {
        return new AwaleGame(pits, turn, DRAW, captured);
    }

    String positionKey() {
        return turn + Arrays.toString(pits);
    }

    int total(int player) {
        int first = player * SIDE;
        int total = 0;
        for (int pit = first; pit < first + SIDE; pit++) {
            total += pits[pit];
        }
        return total;
    }

    int mobility(int player) {
        return playableCount(pits, player);
    }

    static int pitAt(int row, int column) {
        switch (row) {
            case 0: return SIDE + COLUMNS - 1 - column;
            case 1: return SIDE + COLUMNS + column;
            case 2: return SIDE - 1 - column;
            case 3: return column;
            default: throw new IllegalArgumentException("Rangée inconnue.");
        }
    }

    private static int playableCount(int[] board, int player) {
        int count = 0;
        int first = player * SIDE;
        for (int pit = first; pit < first + SIDE; pit++) {
            if (board[pit] >= 2) {
                count++;
            }
        }
        return count;
    }

    private static int[] opening() {
        int[] board = new int[PIT_COUNT];
        Arrays.fill(board, 0, COLUMNS, 4);
        Arrays.fill(board, SIDE, SIDE + COLUMNS, 4);
        return board;
    }
}
