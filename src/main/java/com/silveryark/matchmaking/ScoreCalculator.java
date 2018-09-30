package com.silveryark.matchmaking;

public interface ScoreCalculator {

    int matchScore(Point player1, Point player2);

    class Point {
        private int center;
        private int width;

        public Point(int center, int width) {
            this.center = center;
            this.width = width;
        }

        public int getCenter() {
            return center;
        }

        public int getWidth() {
            return width;
        }
    }
}
