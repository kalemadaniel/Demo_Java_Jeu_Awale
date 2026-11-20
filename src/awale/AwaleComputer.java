package awale;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ThreadLocalRandom;

final class AwaleComputer {
    enum Level {
        EASY("Facile", 0), MEDIUM("Moyen", 2), HARD("Difficile", 4);

        private final String label;
        private final int depth;

        Level(String label, int depth) {
            this.label = label;
            this.depth = depth;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final int WIN = 10_000;
    private static final int BOUND = WIN + 100;

    private AwaleComputer() {
    }

    static int choose(AwaleGame game, Level level) {
        int first = game.turn() * AwaleGame.SIDE;
        List<Integer> legal = new ArrayList<>();
        for (int pit = first; pit < first + AwaleGame.SIDE; pit++) {
            if (game.canPlay(pit)) {
                legal.add(pit);
            }
        }
        if (legal.isEmpty()) {
            throw new IllegalArgumentException("Aucun coup disponible.");
        }
        if (level == Level.EASY) {
            return legal.get(ThreadLocalRandom.current().nextInt(legal.size()));
        }

        int bestPit = legal.get(0);
        int bestScore = -BOUND;
        for (int pit : legal) {
            int score = -search(game.play(pit), level.depth - 1, -BOUND, -bestScore);
            if (score > bestScore) {
                bestScore = score;
                bestPit = pit;
            }
        }
        return bestPit;
    }

    private static int search(AwaleGame game, int depth, int alpha, int beta) {
        if (Thread.currentThread().isInterrupted()) {
            throw new CancellationException();
        }
        int player = game.turn();
        int opponent = 1 - player;
        if (game.finished()) {
            if (game.winner() == AwaleGame.DRAW) {
                return 0;
            }
            return game.winner() == player ? WIN + depth : -WIN - depth;
        }
        if (depth == 0) {
            return 4 * (game.total(player) - game.total(opponent))
                    + 3 * (game.mobility(player) - game.mobility(opponent));
        }

        int best = -BOUND;
        int first = player * AwaleGame.SIDE;
        for (int pit = first; pit < first + AwaleGame.SIDE; pit++) {
            if (game.canPlay(pit)) {
                int score = -search(game.play(pit), depth - 1, -beta, -alpha);
                best = Math.max(best, score);
                alpha = Math.max(alpha, score);
                if (alpha >= beta) {
                    break;
                }
            }
        }
        return best;
    }
}
