import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Player;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;

public class EndGameTest {
	static GameState gameState;
	static Board board;
	static Player AIPlayer;
	static Player humanPlayer;
	
	
	
	public static void setup() {
		gameState = new GameState();
		Initalize initalizeProcessor = new Initalize();
		ObjectNode eventMessage = Json.newObject();
		initalizeProcessor.processEvent(null, gameState, eventMessage);
		board = gameState.getBoard();
	}
	

	@Test
	public void checkGameEnds() {

		setup();
		AIPlayer = gameState.getAIPlayer();
		AIPlayer.setHealth(-1);
		assertTrue(" AI Health <1 did not end game", gameState.isGameOver());
		
		setup();
		humanPlayer = gameState.getHumanPlayer();
		humanPlayer.setHealth(0);
		assertTrue("Player Health <1 did not end game", gameState.isGameOver());
		
		
		setup();
		AIPlayer = gameState.getAIPlayer();
		int size = AIPlayer.getDeck().getDeckSize();
		for (int i = 0; i <= size; i++) {
			AIPlayer.drawCard();
		}
		assertTrue(" AI deck <1 did not end game", gameState.isGameOver());
	}
}

