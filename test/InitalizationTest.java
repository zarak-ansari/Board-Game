
import static org.junit.Assert.assertTrue;

import org.junit.*;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

import commands.BasicCommands;
import commands.CheckMessageIsNotNullOnTell;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Player;

/**
 * This is an example of a JUnit test. In this case, we want to be able to test the logic
 * of our system without needing to actually start the web server. We do this by overriding
 * the altTell method in BasicCommands, which means whenever a command would normally be sent
 * to the front-end it is instead discarded. We can manually simulate messages coming from the
 * front-end by calling the processEvent method on the appropriate event processor.
 * @author Richard
 *
 */
public class InitalizationTest {

	
	static GameState gameState;
	static CheckMessageIsNotNullOnTell altTell = new CheckMessageIsNotNullOnTell();
	
	
	 ///////////////////////////////////////////////// Initialise game ahead of running initialisation tests /////////////////////////////////////////////////

	
	@BeforeClass	
	public static void setup() {
		// First override the alt tell variable so we can issue commands without actually running front-end
		 // create an alternative tell
		BasicCommands.altTell = altTell; // specify that the alternative tell should be used

		// As we are not starting the front-end, we have no GameActor, so lets manually create the components we want to test
		gameState = new GameState(); // create state storage
		Initalize initalizeProcessor = new Initalize(); // create an initalize event processor

		// lets simulate recieveing an initalize message
		ObjectNode eventMessage = Json.newObject(); // create a dummy message
		initalizeProcessor.processEvent(null, gameState, eventMessage); // send it to the initalize event processor
	}
	
	 ///////////////////////////////////////////////// Tests /////////////////////////////////////////////////
	
	// Check game initialised
	@Test
	public void initialised() {
		assertTrue("Game not initialised", gameState.gameInitialised);
	}
	
	// Check board is created and has correct width and height
	public void boardCreated() {
		assertTrue("Board not created", !gameState.getBoard().equals(null));
		assertTrue("Board does not have correct width", gameState.getBoard().getWidth() == 9);
		assertTrue("Board does not have correct height", gameState.getBoard().getHeight() == 5);
	}
	
	
	// Player starting health
	@Test
	public void startingHealth() {
		assertTrue("Health not initialised correctly", gameState.getHumanPlayer().getHealth() == 20);
		assertTrue("Health not initialised correctly", gameState.getAIPlayer().getHealth() == 20);
	}
	
	// Player starting mana
	@Test
	public void startingMana() {
		assertTrue("Mana not initialised correctly", gameState.getHumanPlayer().getMana() == 2);
		assertTrue("Mana not initialised correctly", gameState.getAIPlayer().getMana() == 2);;
	}
	
	
	// Initial card draws
	@Test
	public void initialCardDraw() {
		assertTrue("Starting cards not drawn", gameState.getHumanPlayer().getCard(0) != null);
		assertTrue("Starting cards not drawn", gameState.getHumanPlayer().getCard(1) != null);
		assertTrue("Starting cards not drawn", gameState.getHumanPlayer().getCard(2) != null);
		
		assertTrue("Starting cards not drawn", gameState.getAIPlayer().getCard(0) != null);
		assertTrue("Starting cards not drawn", gameState.getAIPlayer().getCard(1) != null);
		assertTrue("Starting cards not drawn", gameState.getAIPlayer().getCard(2) != null);
	}
	
	// Avatars loaded to board
	@Test
	public void avatarsLoaded() {
		Board board = gameState.getBoard();
		Player humanPlayer = gameState.getHumanPlayer();
		Player AIPlayer = gameState.getAIPlayer();
		
		assertTrue("Player avatar not loaded", board.getTile(1, 2).hasUnit());
		assertTrue("Player avatar not loaded", !board.getTile(1, 2).getUnit().equals(null));
		assertTrue("Player avatar not loaded", board.getTile(1, 2).getUnit().equals(humanPlayer.getAvatar()));
		
		assertTrue("AI avatar not loaded", board.getTile(7, 2).hasUnit());
		assertTrue("AI avatar not loaded", !board.getTile(7, 2).getUnit().equals(null));
		assertTrue("AI avatar not loaded", board.getTile(7, 2).getUnit().equals(AIPlayer.getAvatar()));
		
		assertTrue("Player avatar health not initialised correctly", humanPlayer.getAvatar().getUnitHealth() == 20);
		assertTrue("Player avatar attack not initialised correctly", humanPlayer.getAvatar().getUnitAttack() == 2);
		
		assertTrue("AI avatar health not initialised correctly", humanPlayer.getAvatar().getUnitHealth() == 20);
		assertTrue("AI avatar attack not initialised correctly", humanPlayer.getAvatar().getUnitAttack() == 2);
	}
	

	
}
