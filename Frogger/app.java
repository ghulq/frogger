import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class app {
    static class GamePanel extends JPanel implements KeyListener {
        private Image frogImage;
        private int frogX;
        private int frogY;
        private final int WINDOW_WIDTH = 810;
        private final int WINDOW_HEIGHT = 900;
        private final int GRID_COLUMNS = 9; // Number of columns
        private final int GRID_SIZE = WINDOW_WIDTH / GRID_COLUMNS; // Size of each cell
        private final int MOVE_DISTANCE = GRID_SIZE;
        private Clip musicClip;
        private boolean isMusicPlaying = false;

        public GamePanel() {
            frogImage = new ImageIcon("img/frog.png").getImage();
            setFocusable(true);
            addKeyListener(this);
            // Position frog at bottom center of grid
            frogX = (GRID_COLUMNS / 2) * GRID_SIZE;
            frogY = WINDOW_HEIGHT - GRID_SIZE;
            // Set preferred size to ensure proper grid layout
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            // Start background music
            playBackgroundMusic();
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

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw grid
            g.setColor(Color.LIGHT_GRAY);
            // Draw vertical lines
            for (int x = 0; x <= WINDOW_WIDTH; x += GRID_SIZE) {
                g.drawLine(x, 0, x, WINDOW_HEIGHT);
            }
            // Draw horizontal lines
            for (int y = 0; y <= WINDOW_HEIGHT; y += GRID_SIZE) {
                g.drawLine(0, y, WINDOW_WIDTH, y);
            }
            // Draw frog
            g.drawImage(frogImage, frogX, frogY, GRID_SIZE, GRID_SIZE, this);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            
            // Music toggle control
            if (key == KeyEvent.VK_M) {
                toggleMusic();
                return;
            }

            int oldX = frogX;
            int oldY = frogY;
            switch (key) {
                case KeyEvent.VK_W:
                case KeyEvent.VK_UP:
                    frogY -= MOVE_DISTANCE;
                    break;
                case KeyEvent.VK_S:
                case KeyEvent.VK_DOWN:
                    frogY += MOVE_DISTANCE;
                    break;
                case KeyEvent.VK_A:
                case KeyEvent.VK_LEFT:
                    frogX -= MOVE_DISTANCE;
                    break;
                case KeyEvent.VK_D:
                case KeyEvent.VK_RIGHT:
                    frogX += MOVE_DISTANCE;
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
        public void keyReleased(KeyEvent e) {}
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
