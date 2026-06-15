package com.codeduel.service;

import org.springframework.stereotype.Service;

/**
 * Standard Elo rating system used in chess and competitive games.
 *
 * Formula:
 *   Expected score: E = 1 / (1 + 10^((opponentRating - playerRating) / 400))
 *   New rating:     R' = R + K * (actual - expected)
 *
 * K-factor controls how much ratings shift per game.
 * We use a dynamic K: new players (< 10 matches) get K=40; established players get K=20.
 */
@Service
public class EloService {

    private static final int K_NEW    = 40;   // fewer than 10 matches
    private static final int K_NORMAL = 20;
    private static final int NEW_PLAYER_THRESHOLD = 10;

    /**
     * @param winnerRating  current Elo of the winner
     * @param loserRating   current Elo of the loser
     * @param winnerMatches total matches played by winner before this match
     * @param loserMatches  total matches played by loser before this match
     * @return int[2] — { winnerDelta, loserDelta }
     */
    public int[] calculateDeltas(int winnerRating, int loserRating,
                                  int winnerMatches, int loserMatches) {
        double expectedWinner = expected(winnerRating, loserRating);
        double expectedLoser  = expected(loserRating, winnerRating);

        int kWinner = kFactor(winnerMatches);
        int kLoser  = kFactor(loserMatches);

        int winnerDelta = (int) Math.round(kWinner * (1.0 - expectedWinner));
        int loserDelta  = (int) Math.round(kLoser  * (0.0 - expectedLoser));

        return new int[]{ winnerDelta, loserDelta };
    }

    private double expected(int playerRating, int opponentRating) {
        return 1.0 / (1.0 + Math.pow(10, (opponentRating - playerRating) / 400.0));
    }

    private int kFactor(int totalMatches) {
        return totalMatches < NEW_PLAYER_THRESHOLD ? K_NEW : K_NORMAL;
    }
}
