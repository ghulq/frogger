package com.frogger.game;

public class Frog {
    private int x;
    private int y;

    public Frog(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
    }

    public void resetPosition(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public boolean checkCollision(Obstacle obstacle) {
        // Implement collision detection logic here
        return false; // Placeholder return value
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}