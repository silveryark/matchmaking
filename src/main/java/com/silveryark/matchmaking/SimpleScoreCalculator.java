package com.silveryark.matchmaking;

import org.springframework.stereotype.Service;

@Service
public class SimpleScoreCalculator implements ScoreCalculator {

    @Override
    public int matchScore(Point player1, Point player2) {
        return (player1.getWidth() + player2.getWidth()) - Math.abs(player1.getCenter() - player2.getCenter());
    }
}
