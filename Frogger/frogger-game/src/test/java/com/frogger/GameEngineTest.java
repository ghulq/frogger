import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.frogger.game.GameEngine;

public class GameEngineTest {
    private GameEngine gameEngine;

    @BeforeEach
    public void setUp() {
        gameEngine = new GameEngine();
    }

    @Test
    public void testInitialScore() {
        assertEquals(0, gameEngine.getScore());
    }

    @Test
    public void testUpdateScore() {
        gameEngine.updateScore(10);
        assertEquals(10, gameEngine.getScore());
    }

    @Test
    public void testGameOver() {
        gameEngine.setLives(0);
        assertTrue(gameEngine.checkGameOver());
    }

    @Test
    public void testResetGame() {
        gameEngine.updateScore(50);
        gameEngine.setLives(0);
        gameEngine.resetGame();
        assertEquals(0, gameEngine.getScore());
        assertEquals(3, gameEngine.getLives()); // Assuming 3 is the initial number of lives
    }
}