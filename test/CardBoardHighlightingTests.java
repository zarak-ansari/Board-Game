import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.*;
import org.junit.Test;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import commands.HighlightTell;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Player;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;


/*
 * This class contains tests for the highlighting that should occur on clicking spell/unit cards
 */

public class CardBoardHighlightingTests {

	static GameState gameState;

	/*
	 * Pre-test initialisation of game
	 */
	@BeforeClass	
	public static void setup() {

		// specify where to trap messages for front-end
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;

		// initialise
		gameState = new GameState();
		Initalize initalizeProcessor = new Initalize();
		ObjectNode eventMessage = Json.newObject(); 
		initalizeProcessor.processEvent(null, gameState, eventMessage);
	}
	
	
	
	/*
	 * Example test for checking if a given tile has been highlighted
	 */
	@Test
	public void exampleCheckHighlight() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// try to highlight a tile
		Board b = gameState.getBoard();
		b.highlightTile(5, 0, 2);
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// loop over them (only one in this case)
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));

			// check that tile (5, 0) has been highlighted
			assertTrue("Highlighted wrong tile", x == 5 && y == 0);
		}
		
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
		
	}
	
	/*
	 * Highlight All (used for resetting highlighting) test
	 */
	@Test
	public void highlightAll() {
		
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		
		// define tiles that should be highlighted (all units adjacent the player's existing unit (avatar at (1,2))
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		
		for (int i = 0; i < b.getWidth(); i++) {
			for (int j = 0; j < b.getHeight(); j++) {
				expectedTiles.add(new TestTile(i, j));
			}
		}


		b.highlightAll(0);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();
	
		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}

		// compare expected highlights with actual highlights
		boolean match = true;
		
		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}
		
		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}
		
		assertTrue("Highlight all did not work", match == true);
		
	}
	
	
	///////////////////////////////////////////////// Tests for board highlighting on clicking a unit card /////////////////////////////////////////////////
	
	/*
	 * Summon Anywhere (Ironcliff Guardian and Planar Scout)
	 */
	@Test
	public void summonAnywhereHighlight() {
		
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		
		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_planar_scout, 28, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);
		
		// define tiles that should be highlighted (all which don't contain a unit)
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>(); 
		for(int i = 0; i < 9; i++) {
			for(int j = 0; j < 5; j++) {
				if (!(i == 1 && j == 2) && !(i == 7 && j == 2)) {
					TestTile tile = new TestTile(i, j);
					expectedTiles.add(tile);
				}
			}
		}

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);
		
		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();
	
		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}
		
		// compare expected highlights with actual highlights
		boolean match = true;
		
		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}
		
		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}
		
		assertTrue("Summon anywhere unit summoning highlighting not correct", match == true);

		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
	}
	
	/*
	 * Normal Summon (all other units)
	 */
	@Test
	public void normalSummonHighlight() {
		
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		
		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 7, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);
		
		// define tiles that should be highlighted (all units adjacent the player's existing unit (avatar at (1,2))
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		expectedTiles.add(new TestTile(0, 1));
		expectedTiles.add(new TestTile(0, 2));
		expectedTiles.add(new TestTile(0, 3));
		expectedTiles.add(new TestTile(1, 1));
		expectedTiles.add(new TestTile(1, 3));
		expectedTiles.add(new TestTile(2, 1));
		expectedTiles.add(new TestTile(2, 2));
		expectedTiles.add(new TestTile(2, 3));

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);
		
		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();
	
		for (ObjectNode message : messages) {
			// extract relevant fields from message
			
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		
		}

		// compare expected highlights with actual highlights
		boolean match = true;
		
		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}
		
		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}
		
		assertTrue("Normal unit summoning highlighting not correct", match == true);
		
		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
		
	}
	
	///////////////////////////////////////////////// Tests for board highlighting on clicking a spell card /////////////////////////////////////////////////
	
	/*
	 *  TrueStrike - Highlight Enemy Units
	 */
	@Test
	public void truestrikeHighlight() {
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;

		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_truestrike, 14, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);

		// define tiles that should be highlighted 
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		expectedTiles.add(new TestTile(7, 2));

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);

		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();

		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}

		// compare expected highlights with actual highlights
		boolean match = true;

		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}

		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}

		assertTrue("Truestrike board highlighting not correct", match == true);
		
		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
	}
	
	/*
	 * Sundrop Elixir - Highlight Player Units
	 */
	@Test
	public void sundropElixirHighlight() {
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;

		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_sundrop_elixir, 18, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);

		// define tiles that should be highlighted
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		expectedTiles.add(new TestTile(1, 2));

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);

		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();

		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}

		// compare expected highlights with actual highlights
		boolean match = true;

		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}

		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}

		assertTrue("Sundrop Elixir board highlighting not correct", match == true);
		
		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
	}
	
	/*
	 * Staff of Y'kir - Highlight Player Avatar
	 */
	@Test
	public void staffOfYkirHighlight() {
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;

		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_staff_of_ykir, 32, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);

		// define tiles that should be highlighted 
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		expectedTiles.add(new TestTile(1, 2));

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);

		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();

		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}

		// compare expected highlights with actual highlights
		boolean match = true;

		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}

		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}

		assertTrue("Staff of Y'kir board highlighting not correct", match == true);
		
		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
	}
	
	/*
	 * Entropic Decay - Highlight Enemy Units (except avatar) 
	 */
	@Test
	public void entropicDecayHighlight() {
		// define where messages will be trapped
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;

		// put card in hand
		Card testCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_entropic_decay, 27, Card.class);
		Player player = gameState.getHumanPlayer();
		player.setCard(0, testCard);

		// define tiles that should be highlighted 
		Board b = gameState.getBoard();
		ArrayList<TestTile> expectedTiles = new ArrayList<TestTile>();

		// make clicked card message
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "cardClicked");
		ClickMessage.put("position", 1);

		// call CardClicked.processEvent()
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, ClickMessage);

		// get messages trapped by altTell
		Iterable<ObjectNode> messages = altTell.readMessages();
		ArrayList<TestTile> actualTiles = new ArrayList<TestTile>();

		for (ObjectNode message : messages) {
			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			TestTile tile = new TestTile(x, y);
			actualTiles.add(tile);
		}

		// compare expected highlights with actual highlights
		boolean match = true;

		for (TestTile tile : expectedTiles) {
			boolean tilePresent = false;
			for (TestTile otherTile : actualTiles) {
				if (tile.equals(otherTile)) {
					tilePresent = true;
				}
			}
			if (!tilePresent) {
				match = false;
			}
		}

		if (actualTiles.size() != expectedTiles.size()) {
			match = false;
		}

		assertTrue("Entropic decay board highlighting not correct", match == true);
		
		gameState.setCardSelected(false);
		gameState.setClickedCard(null);
	}
	
	
}
