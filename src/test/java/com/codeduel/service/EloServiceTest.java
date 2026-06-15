package com.codeduel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive Elo rating tests.
 *
 * Elo formula:
 *   E  = 1 / (1 + 10^((opponent - player) / 400))
 *   R' = R + K * (actual - expected)
 *
 * K=40 for < 10 matches, K=20 otherwise.
 */
class EloServiceTest {

    private EloService eloService;

    @BeforeEach
    void setUp() {
        eloService = new EloService();
    }

    // ---- symmetry -------------------------------------------------------

    @Test
    void equalRatings_winnerGainsAndLoserLosesByEquivalentAmount() {
        int[] deltas = eloService.calculateDeltas(1200, 1200, 20, 20);
        assertThat(deltas[0]).isPositive();
        assertThat(deltas[1]).isNegative();
        assertThat(deltas[0]).isEqualTo(-deltas[1]);
    }

    // ---- favourites and underdogs ---------------------------------------

    @Test
    void strongerPlayerWins_gainsLittleElo() {
        int[] deltas = eloService.calculateDeltas(1600, 1200, 20, 20);
        assertThat(deltas[0]).isGreaterThan(0);
        assertThat(deltas[0]).isLessThan(10);
    }

    @Test
    void underdog_winsMoreElo() {
        int[] deltas = eloService.calculateDeltas(1000, 1400, 20, 20);
        assertThat(deltas[0]).isGreaterThan(15);
    }

    @Test
    void strongerPlayerLoses_loosesMuchMoreElo() {
        int[] deltas = eloService.calculateDeltas(1000, 1600, 20, 20);
        assertThat(deltas[1]).isLessThan(-15);
    }

    // ---- K-factor -------------------------------------------------------

    @Test
    void newPlayer_usesHigherKFactor() {
        int[] newPlayer = eloService.calculateDeltas(1200, 1200, 5, 30);
        int[] estPlayer = eloService.calculateDeltas(1200, 1200, 30, 5);
        assertThat(newPlayer[0]).isGreaterThan(estPlayer[0]);
    }

    @Test
    void bothNewPlayers_deltaIs20() {
        int[] both = eloService.calculateDeltas(1200, 1200, 5, 5);
        assertThat(both[0]).isEqualTo(20);
        assertThat(both[1]).isEqualTo(-20);
    }

    @Test
    void bothEstablishedPlayers_deltaIs10() {
        int[] both = eloService.calculateDeltas(1200, 1200, 30, 30);
        assertThat(both[0]).isEqualTo(10);
        assertThat(both[1]).isEqualTo(-10);
    }

    // ---- boundary -------------------------------------------------------

    @Test
    void returnsArrayOfSize2() {
        int[] deltas = eloService.calculateDeltas(1500, 1500, 15, 15);
        assertThat(deltas).hasSize(2);
    }

    @Test
    void winnerDeltaAlwaysPositive() {
        int[] deltas = eloService.calculateDeltas(800, 2800, 50, 50);
        assertThat(deltas[0]).isPositive();
    }

    @Test
    void loserDeltaAlwaysNegative() {
        int[] deltas = eloService.calculateDeltas(2800, 800, 50, 50);
        assertThat(deltas[1]).isNegative();
    }
}
