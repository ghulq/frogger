import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class app {
    static class GamePanel extends JPanel implements KeyListener {
        private Image frogImage;
        private boolean keyIsDown;
        private int frogX;
        private int frogY;
        private final int WINDOW_WIDTH = 810;
        private final int WINDOW_HEIGHT = 900;
        private final int GRID_COLUMNS = 9; // Number of columns
        private final int GRID_SIZE = WINDOW_WIDTH / GRID_COLUMNS; // Size of each cell
        private final int MOVE_DISTANCE = GRID_SIZE;
        private Clip musicClip;
        private boolean isMusicPlaying = false;

        // Add these new fields at the top of the GamePanel class
        private Image truckImage;
        private int truckX;
        private int truckY;
        private final int TRUCK_SPEED = 2;
        private boolean gameOver = false;
        private Timer timer;

        // Add to existing fields in GamePanel class
        private Image logImage;
        private Rectangle logBounds;
        private final int LOG_WIDTH = 3 * GRID_SIZE; // 3 columns wide
        private int logX;
        private int logY;
        private int logSpeed = 2;
        private boolean logMovingRight = Math.random() < 0.5; // Random direction

        // Add to existing fields in GamePanel
        private Rectangle waterArea;
        private final Color WATER_COLOR = new Color(0, 0, 255, 128); // Semi-transparent blue
        private final int WATER_HEIGHT = 2 * GRID_SIZE; // 2 rows high

        public GamePanel() {
            // Load images
            frogImage = new ImageIcon("img/frog.png").getImage();
            truckImage = new ImageIcon("img/icecreamtruck.png").getImage();
            logImage = new ImageIcon("img/log.png").getImage();
            
            // Set panel properties
            setFocusable(true);
            addKeyListener(this);
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            
            // Generate water area first
            generateWaterArea();
            
            // Initialize positions
            frogX = (GRID_COLUMNS / 2) * GRID_SIZE;
            frogY = WINDOW_HEIGHT - GRID_SIZE;
            resetTruck();
            resetLog();
            
            // Start background music and game
            playBackgroundMusic();
            startGame();
        }

        private void playBackgroundMusic() {
            try {
                // Change file extension from .mp3 to .wav
                File musicFile = new File("sounds/FroggersLullaby.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioStream);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                isMusicPlaying = true;
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                System.err.println("Error playing music: " + e.getMessage());
                e.printStackTrace(); // Add stack trace for better debugging
            }
        }

        private void toggleMusic() {
            if (musicClip != null) {
                if (isMusicPlaying) {
                    musicClip.stop();
                } else {
                    musicClip.start();
                }
                isMusicPlaying = !isMusicPlaying;
            }
        }

        private void resetTruck() {
            truckX = -GRID_SIZE; // Start outside left edge
            // Random row between 1 and GRID_COLUMNS-2 (avoid top and bottom rows)
            truckY = ((int)(Math.random() * (GRID_COLUMNS-2) + 1)) * GRID_SIZE;
        }

        private void resetLog() {
            logY = waterArea.y; // Place log at water area height
            if (logMovingRight) {
                logX = -LOG_WIDTH;
            } else {
                logX = WINDOW_WIDTH;
                logSpeed = -logSpeed; // Reverse speed for left movement
            }
            logBounds = new Rectangle(logX, logY, LOG_WIDTH, GRID_SIZE);
        }

        private void startGame() {
            timer = new Timer(16, (e) -> {
                if (!gameOver) {
                    moveTruck();
                    moveLog(); // Add log movement
                    checkCollision();
                    repaint();
                }
            });
            timer.start();
        }

        private void moveTruck() {
            truckX += TRUCK_SPEED;
            if (truckX > WINDOW_WIDTH) {
                resetTruck();
            }
            
            // Move log
            logX += logSpeed;
            logBounds.setLocation(logX, logY);
            
            // Reset log when it moves off screen
            if ((logMovingRight && logX > WINDOW_WIDTH) || 
                (!logMovingRight && logX < -LOG_WIDTH)) {
                logMovingRight = Math.random() < 0.5; // New random direction
                resetLog();
            }
            
            // Move frog with log if standing on it
            if (isOnLog()) {
                frogX += logSpeed;
            }
        }

        private void moveLog() {
            // Move log left or right
            if (logMovingRight) {
                logX += logSpeed;
                if (logX > WINDOW_WIDTH) {
                    // Reset to start from left again
                    logX = -LOG_WIDTH;
                    // Randomize vertical position
                    logY = ((int)(Math.random() * (GRID_COLUMNS-2) + 1)) * GRID_SIZE;
                }
            } else {
                logX -= logSpeed;
                if (logX < -LOG_WIDTH) {
                    // Reset to start from right again
                    logX = WINDOW_WIDTH;
                    // Randomize vertical position
                    logY = ((int)(Math.random() * (GRID_COLUMNS-2) + 1)) * GRID_SIZE;
                }
            }
        }

        private void checkCollision() {
            Rectangle frogBounds = new Rectangle(frogX, frogY, GRID_SIZE, GRID_SIZE);
            
            // Check truck collision
            Rectangle truckBounds = new Rectangle(truckX, truckY, GRID_SIZE, GRID_SIZE);
            if (frogBounds.intersects(truckBounds)) {
                gameOver("Game Over! Hit by truck!");
                return;
            }
            
            // Check water collision, but only if not on log
            if (frogBounds.intersects(waterArea) && !isOnLog()) {
                gameOver("Game Over! Frog drowned!");
            }
            
            // Check log collision
            Rectangle logBounds = new Rectangle(logX, logY, LOG_WIDTH, GRID_SIZE);
            if (frogBounds.intersects(logBounds)) {
                // Frog is on the log, move with the log
                frogX += (logMovingRight ? logSpeed : -logSpeed);
            }
        }

        // Add new helper method for game over
        private void gameOver(String message) {
            gameOver = true;
            timer.stop();
            if (musicClip != null) {
                musicClip.stop();
            }
            JOptionPane.showMessageDialog(this, message, "Frogger", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Draw water area
            g.setColor(WATER_COLOR);
            g.fillRect(waterArea.x, waterArea.y, waterArea.width, waterArea.height);
            
            // Draw log
            g.drawImage(logImage, logX, logY, LOG_WIDTH, GRID_SIZE, this);
            
            // Draw truck
            if (!gameOver) {
                g.drawImage(truckImage, truckX, truckY, GRID_SIZE, GRID_SIZE, this);
            }
            // Draw frog
            g.drawImage(frogImage, frogX, frogY, GRID_SIZE, GRID_SIZE, this);
        }

        // Modify keyPressed to prevent movement when game is over
        @Override
        public void keyPressed(KeyEvent e) {
            if (gameOver) return;
            int key = e.getKeyCode();
            
            // Music toggle control
            if (key == KeyEvent.VK_M) {
                toggleMusic();
                return;
            }

            int oldX = frogX;
            int oldY = frogY;
            if (keyIsDown) {
                return; // Prevent multiple movements if key is held down
            }
            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    frogY -= MOVE_DISTANCE;
                    keyIsDown = true;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    frogY += MOVE_DISTANCE;
                    keyIsDown = true;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    frogX -= MOVE_DISTANCE;
                    keyIsDown = true;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    frogX += MOVE_DISTANCE;
                    keyIsDown = true;
                    break;
            }
            if (frogX < 0) frogX = 0;
            if (frogX > getWidth() - GRID_SIZE) frogX = getWidth() - GRID_SIZE;
            if (frogY < 0) frogY = 0;
            if (frogY > getHeight() - GRID_SIZE) frogY = getHeight() - GRID_SIZE;
            if (oldX != frogX || oldY != frogY) {
                repaint();
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {}
        
        @Override
        public void keyReleased(KeyEvent e) {
            keyIsDown = false; // Reset key state on release
        }

        private void generateWaterArea() {
            // Calculate available rows (excluding bottom 3 rows and top row)
            int availableRows = GRID_COLUMNS - 4;
            // Random starting row (avoid bottom 3 rows)
            int startRow = (int)(Math.random() * availableRows);
            // Create water area
            waterArea = new Rectangle(
                0,                  // x position (left edge)
                startRow * GRID_SIZE, // y position
                WINDOW_WIDTH,       // full width
                WATER_HEIGHT        // 2 rows height
            );
        }

        private boolean isOnLog() {
            Rectangle frogBounds = new Rectangle(frogX, frogY, GRID_SIZE, GRID_SIZE);
            return frogBounds.intersects(logBounds);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Frogger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);
        
        frame.pack(); // Use pack instead of setSize to respect preferred size
        frame.setLocationRelativeTo(null); // Center on screen
        
        Image icon = Toolkit.getDefaultToolkit().getImage("img/icon.png");
        frame.setIconImage(icon);
        frame.setVisible(true);
    }
}
