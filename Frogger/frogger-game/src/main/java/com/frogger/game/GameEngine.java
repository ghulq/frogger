package com.frogger.game;

public class GameEngine {
    private int score;
    private int lives;
    private boolean gameOver;

    public GameEngine() {
        this.score = 0;
        this.lives = 3;
        this.gameOver = false;
    }

    public void updateScore(int points) {
        score += points;
    }

    public int getScore() {
        return score;
    }

    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
        checkGameOver();
    }

    public int getLives() {
        return lives;
    }

    public void checkGameOver() {
        if (lives <= 0) {
            gameOver = true;
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void resetGame() {
        score = 0;
        lives = 3;
        gameOver = false;
    }
}