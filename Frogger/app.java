import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URI;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.io.*;

public class app {
    private static JFrame frame;
    private static GamePanel currentGame;
    private static String username = ""; // Keep only username field
    // Add static constants for window dimensions
    private static final int WINDOW_WIDTH = 810;
    private static final int WINDOW_HEIGHT = 900;

    private static class StartMenu extends JPanel {
        public StartMenu() {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JLabel title = new JLabel("FROGGER");
            title.setFont(new Font("Arial", Font.BOLD, 48));
            gbc.gridy = 0;
            add(title, gbc);
            
            // Add login button
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> {
                LoginWindow loginWindow = new LoginWindow(frame);
                loginWindow.setVisible(true);
            });
            gbc.gridy = 1;
            add(loginButton, gbc);
            
            JButton startButton = new JButton("Start Game");
            startButton.addActionListener(e -> startGame());
            gbc.gridy = 2;
            add(startButton, gbc);
            
            JButton exitButton = new JButton("Exit");
            exitButton.addActionListener(e -> System.exit(0));
            gbc.gridy = 3;
            add(exitButton, gbc);
        }
    }

    private static class DeathScreen extends JPanel {
        public DeathScreen(String message) {
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            JLabel gameOverLabel = new JLabel("GAME OVER");
            gameOverLabel.setFont(new Font("Arial", Font.BOLD, 48));
            gbc.gridy = 0;
            add(gameOverLabel, gbc);
            
            JLabel messageLabel = new JLabel(message);
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 24));
            gbc.gridy = 1;
            add(messageLabel, gbc);
            
            JButton restartButton = new JButton("Restart");
            restartButton.addActionListener(e -> restartGame());
            gbc.gridy = 2;
            add(restartButton, gbc);
            
            JButton menuButton = new JButton("Back to Menu");
            menuButton.addActionListener(e -> showStartMenu());
            gbc.gridy = 3;
            add(menuButton, gbc);
            
            JButton exitButton = new JButton("Exit");
            exitButton.addActionListener(e -> System.exit(0));
            gbc.gridy = 4;
            add(exitButton, gbc);
        }
    }

    static class GamePanel extends JPanel implements KeyListener {
        // Add field to store final score
        private int finalScore;

        // Add this with other constants at the top of GamePanel
        private final int GRID_COLUMNS = 9;
        private final int GRID_SIZE = WINDOW_WIDTH / GRID_COLUMNS;
        private final int MOVE_DISTANCE = GRID_SIZE; // Add this line

        // Add these fields at the top with other fields
        private Image truckImage;
        private Timer timer;
        private boolean gameOver = false;
        private int truckSpeed = 6; // Initial speed

        private Image frogImage;
        private boolean keyIsDown;
        private int frogX;
        private int frogY;
        private Clip musicClip;
        private boolean isMusicPlaying = false;

        // Add these new fields at the top of the GamePanel class
        private ArrayList<Truck> trucks = new ArrayList<>();
        private final int NUM_TRUCKS = 5; // Number of trucks to spawn

        // Replace single log variables with an ArrayList
        private ArrayList<Log> logs = new ArrayList<>();

        // Add to existing fields in GamePanel class
        private Rectangle waterArea;
        private final Color WATER_COLOR = new Color(0, 0, 255, 128); // Semi-transparent blue
        private final int WATER_HEIGHT = 2 * GRID_SIZE; // 2 rows high
        private Image logImage;
        private final int LOG_WIDTH = GRID_SIZE * 3; // 3 grid cells wide

        // Add score field at the top with other fields
        private int score = 0;
        private JLabel scoreLabel;

        // Modify constructor to add score display
        public GamePanel() {
            // Load images
            frogImage = new ImageIcon("img/frog.png").getImage();
            truckImage = new ImageIcon("img/icecreamtruck.png").getImage();
            logImage = new ImageIcon("img/log.png").getImage(); // Add log image loading
            
            // Set panel properties
            setFocusable(true);
            addKeyListener(this);
            setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
            
            // Generate water area first
            generateWaterArea();
            
            // Initialize positions
            frogX = (GRID_COLUMNS / 2) * GRID_SIZE;
            frogY = WINDOW_HEIGHT - GRID_SIZE;
            resetTrucks();
            resetLogs();
            
            // Add score label
            scoreLabel = new JLabel("Score: 0");
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 24));
            scoreLabel.setForeground(Color.WHITE);
            add(scoreLabel);
            
            // Start background music and game
            playBackgroundMusic();
            startGame();
        }

        private void playBackgroundMusic() {
            try {
                // Change file extension from .mp3 to .wav
                File musicFile = new File("sounds/FroggysMelody.wav");
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

        // Replace resetTruck() with resetTrucks()
        private void resetTrucks() {
            trucks.clear();
            for (int i = 0; i < NUM_TRUCKS; i++) {
                trucks.add(new Truck());
            }
        }

        private void resetLogs() {
            logs.clear();
            // Create logs for each row in water area
            int numRows = WATER_HEIGHT / GRID_SIZE;
            for (int row = 0; row < numRows; row++) {
                int logY = waterArea.y + (row * GRID_SIZE);
                logs.add(new Log(logY));
            }
        }

        private void startGame() {
            timer = new Timer(16, (e) -> {
                if (!gameOver) {
                    moveTrucks();
                    moveLogs(); // Add log movement
                    checkCollision();
                    repaint();
                }
            });
            timer.start();
        }

        private void moveTrucks() {
            for (Truck truck : trucks) {
                truck.move();
            }
        }

        private void moveLogs() {
            for (Log log : logs) {
                log.move();
            }
        }

        // Add method to update score
        private void updateScore() {
            if (frogY == 0) {  // Frog reached top
                score++;
                scoreLabel.setText("Score: " + score);
                
                // Reset frog position
                frogX = (GRID_COLUMNS / 2) * GRID_SIZE;
                frogY = WINDOW_HEIGHT - GRID_SIZE;
                
                // Generate new water area and reset obstacles
                generateWaterArea();
                resetLogs();
                resetTrucks();  // Add this line to reset trucks
                
                // Optionally increase difficulty
                if (score % 5 == 0) {
                    increaseDifficulty();
                }
            }
        }

        // Add method to increase difficulty
        private void increaseDifficulty() {
            // Add more trucks
            trucks.add(new Truck());
            // Increase truck speed
            truckSpeed++;
            // Update existing trucks' speed
            resetTrucks();
            // Add more logs or increase their speed
            logs.add(new Log(waterArea.y + (logs.size() % 2) * GRID_SIZE));
        }

        private void checkCollision() {
            Rectangle frogBounds = new Rectangle(frogX, frogY, GRID_SIZE, GRID_SIZE);
            
            // Check all truck collisions
            for (Truck truck : trucks) {
                if (frogBounds.intersects(truck.getBounds())) {
                    gameOver("Game Over! Hit by truck!");
                    return;
                }
            }
            
            // Check water collision, but only if not on any log
            if (frogBounds.intersects(waterArea) && !isOnAnyLog()) {
                gameOver("Game Over! Frog drowned!");
            }
            
            // Check log collisions and move frog with log
            for (Log log : logs) {
                if (frogBounds.intersects(log.getBounds())) {
                    frogX += log.getSpeed();
                    break;
                }
            }

            // Add score update check
            updateScore();
        }

        private boolean isOnAnyLog() {
            Rectangle frogBounds = new Rectangle(frogX, frogY, GRID_SIZE, GRID_SIZE);
            for (Log log : logs) {
                if (frogBounds.intersects(log.getBounds())) {
                    return true;
                }
            }
            return false;
        }

        // Add new helper method for game over
        private void gameOver(String message) {
            gameOver = true;
            timer.stop();
            if (musicClip != null) {
                musicClip.stop();
            }

            // Save final score before resetting
            finalScore = score;

            // Submit score if user is logged in
            if (!username.isEmpty()) {
                submitGameScore(finalScore);
            }

            frame.getContentPane().removeAll();
            frame.getContentPane().add(new DeathScreen(message));
            frame.revalidate();
            frame.repaint();
        }

        private static void submitGameScore(int score) {
            try {
                // Only proceed if user is logged in
                if (username.isEmpty()) {
                    return;
                }

                String jsonPayload = String.format("{\"username\":\"%s\",\"score\":%d}", 
                    username, score);
                    
                URL url = new URL("https://7020200a-8134-4bf7-8f70-b1b4af1e0cb6-00-30nki5009yq2i.janeway.replit.dev/api/v0/user/addgame");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Failed to submit game score. Response code: " + responseCode);
                    
                    // Read error message if available
                    try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        System.err.println("Error details: " + response.toString());
                    }
                }
                
                conn.disconnect();
            } catch (Exception e) {
                System.err.println("Error submitting game score: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // Add green background
            setBackground(new Color(34, 139, 34)); // Forest Green
            
            // Draw water area
            g.setColor(WATER_COLOR);
            g.fillRect(waterArea.x, waterArea.y, waterArea.width, waterArea.height);
            
            // Draw all logs
            for (Log log : logs) {
                g.drawImage(logImage, log.getX(), log.getY(), LOG_WIDTH, GRID_SIZE, this);
            }
            
            // Draw all trucks
            if (!gameOver) {
                for (Truck truck : trucks) {
                    g.drawImage(truckImage, truck.getX(), truck.getY(), GRID_SIZE, GRID_SIZE, this);
                }
            }
            
            // Draw frog
            g.drawImage(frogImage, frogX, frogY, GRID_SIZE, GRID_SIZE, this);
            
            // Update score position to top-left corner
            scoreLabel.setLocation(10, 10);
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

        // Add this class inside GamePanel but before the constructor
        private class Log {
            private int x;
            private int y;
            private boolean movingRight;
            private Rectangle bounds;
            private int speed;

            public Log(int y) {
                this.y = y;
                this.movingRight = Math.random() < 0.5;
                this.speed = 2;
                if (!movingRight) {
                    this.speed = -this.speed;
                }
                resetPosition();
            }

            private void resetPosition() {
                if (movingRight) {
                    x = -GamePanel.this.LOG_WIDTH;
                } else {
                    x = WINDOW_WIDTH;
                }
                bounds = new Rectangle(x, y, GamePanel.this.LOG_WIDTH, GRID_SIZE);
            }

            public void move() {
                x += speed;
                bounds.setLocation(x, y);
                if ((speed > 0 && x > WINDOW_WIDTH) || 
                    (speed < 0 && x < -GamePanel.this.LOG_WIDTH)) {
                    resetPosition();
                }
            }

            public Rectangle getBounds() {
                return bounds;
            }

            public int getX() {
                return x;
            }

            public int getY() {
                return y;
            }

            public boolean isMovingRight() {
                return movingRight;
            }

            public int getSpeed() {
                return speed;
            }
        }

        // Inner class for Truck
        private class Truck {
            private int x;
            private int y;
            private boolean movingRight;
            private final int speed;
            
            public Truck() {
                this.movingRight = Math.random() < 0.5;
                // Generate random speed between truckSpeed-2 and truckSpeed+2
                int randomSpeed = truckSpeed + (int)(Math.random() * 5) - 2;
                this.speed = movingRight ? randomSpeed : -randomSpeed;
                resetPosition();
            }
            
            private void resetPosition() {
                // Get valid row that's not in water area
                do {
                    y = ((int)(Math.random() * (GRID_COLUMNS-2) + 1)) * GRID_SIZE;
                } while (y >= waterArea.y && y < waterArea.y + waterArea.height);
                
                // Set x position based on direction
                x = movingRight ? -GRID_SIZE : WINDOW_WIDTH;
            }
            
            public void move() {
                x += speed;
                if ((movingRight && x > WINDOW_WIDTH) || 
                    (!movingRight && x < -GRID_SIZE)) {
                    resetPosition();
                }
            }
            
            public Rectangle getBounds() {
                return new Rectangle(x, y, GRID_SIZE, GRID_SIZE);
            }
            
            public int getX() { return x; }
            public int getY() { return y; }
        }
    }

    private static void showStartMenu() {
        frame.getContentPane().removeAll();
        frame.getContentPane().add(new StartMenu());
        frame.revalidate();
        frame.repaint();
    }

    private static void startGame() {
        frame.getContentPane().removeAll();
        currentGame = new GamePanel();
        frame.getContentPane().add(currentGame);
        frame.revalidate();
        frame.repaint();
        currentGame.requestFocusInWindow();
    }

    private static void restartGame() {
        currentGame = new GamePanel();
        frame.getContentPane().removeAll();
        frame.getContentPane().add(currentGame);
        frame.revalidate();
        frame.repaint();
        currentGame.requestFocusInWindow();
    }

    private static class LoginWindow extends JDialog {
        private String username;
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginWindow(JFrame parent) {
            super(parent, "Login", true);
            setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Username field
            JLabel userLabel = new JLabel("Username:");
            usernameField = new JTextField(15);
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(userLabel, gbc);
            gbc.gridx = 1;
            add(usernameField, gbc);
            
            // Password field
            JLabel passLabel = new JLabel("Password:");
            passwordField = new JPasswordField(15);
            gbc.gridx = 0;
            gbc.gridy = 1;
            add(passLabel, gbc);
            gbc.gridx = 1;
            add(passwordField, gbc);
            
            // Login button
            JButton loginButton = new JButton("Login");
            loginButton.addActionListener(e -> attemptLogin());
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(loginButton, gbc);
            
            pack();
            setLocationRelativeTo(parent);
        }

        private void attemptLogin() {
            try {
                URL url = new URL("https://7020200a-8134-4bf7-8f70-b1b4af1e0cb6-00-30nki5009yq2i.janeway.replit.dev/api/v0/user/login");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);

                String jsonPayload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", 
                    usernameField.getText(), 
                    new String(passwordField.getPassword()));

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 303) {
                    username = usernameField.getText();
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Login failed. Please try again.");
                }
                
                conn.disconnect();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        frame = new JFrame("Frogger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        frame.getContentPane().add(new StartMenu());
        
        frame.pack();
        frame.setLocationRelativeTo(null);
        
        Image icon = Toolkit.getDefaultToolkit().getImage("img/icon.png");
        frame.setIconImage(icon);
        frame.setVisible(true);
    }
}