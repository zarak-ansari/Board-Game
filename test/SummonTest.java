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
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * This is an example of a JUnit test. In this case, we want to be able to test the logic
 * of our system without needing to actually start the web server. We do this by overriding
 * the altTell method in BasicCommands, which means whenever a command would normally be sent
 * to the front-end it is instead discarded. We can manually simulate messages coming from the
 * front-end by calling the processEvent method on the appropriate event processor.
 * @author Richard
 *
 */
public class SummonTest {

	
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
	
	
	@Test
	public void summonUnit() {
		
		
		// create dummy message for clicking on comodo charger card after game initialisation there is a comodo charger in position 1 on UI (position 0 in Player.cards)
		ObjectNode cardClickMessage = Json.newObject();
		cardClickMessage.put("messageType", "cardClicked");
		cardClickMessage.put("position", 1);
		
		// simulate clicking on this card - this will cause valid summon locations (those surrounding (1,2)) to be highlighted
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, cardClickMessage);
		
		
		// create dummy message for trying to play this on tile (1,3) - i.e. valid
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messageType", "tileClicked");
		tileClickMessage.put("tilex", 1);
		tileClickMessage.put("tiley", 3);
		
		// simulate click on tile (1,3)
		events.TileClicked processor2 = new events.TileClicked();
		processor2.processEvent(null, gameState, tileClickMessage);
		
		
		// check unit place on board in position (1,3)
		assertTrue("Summoned unit not present on board", gameState.getBoard().getTile(1, 3).hasUnit());
	}
	@Test
	public void summonAzureHerald() {
		//Create an azureHerald card and set this as the clicked card in gamestate
		Card azureHerald = BasicObjectBuilders.loadCard(StaticConfFiles.c_azure_herald, 5, Card.class);
		//gameState.setCardPosition(4);
		Player currentPlayer = gameState.getHumanPlayer();
		Board board = gameState.getBoard();
		int position = currentPlayer.getPosition();
		currentPlayer.setCard(position, azureHerald);
		currentPlayer.setMana(7);
		
		//Set Avatar to have sub 20 health
		gameState.getHumanPlayer().getAvatar().setUnitHealth(null, 10);
		int initialAvatarHealth = 10;
		Unit avatar = gameState.getHumanPlayer().getAvatar();
		assertTrue("Avatar health problem", gameState.getHumanPlayer().getAvatar().getUnitHealth() == 10); //Tests if this is the part of the test that failed.
		
		ObjectNode cardClickMessage = Json.newObject();
		cardClickMessage.put("messageType", "cardClicked");
		cardClickMessage.put("position", position+1);
		
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, cardClickMessage);
		
		// create dummy message for trying to play this on tile (2, 2) - i.e. valid
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messageType", "tileClicked");
		tileClickMessage.put("tilex", 2);
		tileClickMessage.put("tiley", 2);
		
		// simulate click on tile (2,2)
		events.TileClicked processor2 = new events.TileClicked();
		processor2.processEvent(null, gameState, tileClickMessage);
		
		//Test that a unit was summoned, this was an azure herald and the avatar's health was subsequently increased.
		assertTrue("Unit was not summoned to the board", gameState.getBoard().getTile(2, 2).hasUnit());
		assertTrue("Azure Herald not summoned", gameState.getBoard().getTile(2,2).getUnit().getId() == 5 || gameState.getBoard().getTile(2,2).getUnit().getId() == 15);
		assertTrue("Avatar's health was not incremented", avatar.getUnitHealth() == (initialAvatarHealth+3));
		//currentPlayer.setPosition(position+1);
		
	}
	
	@Test
	public void summonBlazeHound() {	
		Card blazeHound = BasicObjectBuilders.loadCard(StaticConfFiles.c_blaze_hound, 23, Card.class);
		//gameState.setCardPosition(4);
		Player currentPlayer = gameState.getHumanPlayer();
		currentPlayer.setPosition(currentPlayer.getPosition()+1);
		int position = currentPlayer.getPosition();
		currentPlayer.setCard(position, blazeHound);
		currentPlayer.setMana(7);
		
		int player1CardNumber = gameState.getHumanPlayer().getPosition();
		int player2CardNumber = gameState.getAIPlayer().getPosition();
		
		ObjectNode cardClickMessage = Json.newObject();
		cardClickMessage.put("messageType", "cardClicked");
		cardClickMessage.put("position", position+1);
		
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, cardClickMessage);
		
		// create dummy message for trying to play this on tile (1, 1) - i.e. valid
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messageType", "tileClicked");
		tileClickMessage.put("tilex", 1);
		tileClickMessage.put("tiley", 1);
		
		// simulate click on tile (1, 1)
		events.TileClicked processor2 = new events.TileClicked();
		processor2.processEvent(null, gameState, tileClickMessage);
		
		assertTrue("Unit was not summoned to the board", gameState.getBoard().getTile(1, 1).hasUnit());
		
		Unit unit = gameState.getBoard().getTile(1, 1).getUnit();
		
		//Test that both players received a new card. As they will have used one with the Blaze Hound, and gained another, they should have the same as position as they did originally.
		assertTrue("Player 1 did not receive another card",(player1CardNumber)==gameState.getHumanPlayer().getPosition());
		assertTrue("Player 2 did not receive another card",(player2CardNumber+1)==gameState.getAIPlayer().getPosition());
		
	}
	
	@Test
	public void summonIronCliffGuardian() {
		Card ironCliff = BasicObjectBuilders.loadCard(StaticConfFiles.c_ironcliff_guardian, 6, Card.class);
		//gameState.setCardPosition(4);
		Player currentPlayer = gameState.getHumanPlayer();
		int position = currentPlayer.getPosition();
		currentPlayer.setCard(position, ironCliff);
		currentPlayer.setMana(7);
		
		ObjectNode cardClickMessage = Json.newObject();
		cardClickMessage.put("messageType", "cardClicked");
		cardClickMessage.put("position", position+1);
		
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, cardClickMessage);
		
		// create dummy message for trying to play this on tile (1, 1) - i.e. valid
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messageType", "tileClicked");
		tileClickMessage.put("tilex", 7);
		tileClickMessage.put("tiley", 1);
		
		// simulate click on tile (1, 1)
		events.TileClicked processor2 = new events.TileClicked();
		processor2.processEvent(null, gameState, tileClickMessage);
		
		assertTrue("Unit was not summoned to the board", gameState.getBoard().getTile(7, 1).hasUnit());
		//currentPlayer.setPosition(position+1);
	}
}