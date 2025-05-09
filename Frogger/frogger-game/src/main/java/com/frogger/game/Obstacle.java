public class Obstacle {
    private int positionX;
    private int positionY;
    private int speed;
    private boolean movingRight;

    public Obstacle(int startX, int startY, int speed) {
        this.positionX = startX;
        this.positionY = startY;
        this.speed = speed;
        this.movingRight = true;
    }

    public void move() {
        if (movingRight) {
            positionX += speed;
        } else {
            positionX -= speed;
        }
    }

    public void reverseDirection() {
        movingRight = !movingRight;
    }

    public void resetPosition(int newX, int newY) {
        this.positionX = newX;
        this.positionY = newY;
    }

    public int getPositionX() {
        return positionX;
    }

    public int getPositionY() {
        return positionY;
    }
}