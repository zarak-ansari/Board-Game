import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;
import org.junit.Test;
import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import commands.HighlightTell;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Player;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class UnitActionBoardHighlightingTests {

	static GameState gameState;
	static Board board;
	static Player AIPlayer;
	static Player humanPlayer;

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
		board = gameState.getBoard();
		AIPlayer = gameState.getAIPlayer();
		humanPlayer = gameState.getHumanPlayer();
		
		// put test units on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(AIPlayer);
		board.addUnit(testUnit, 1, 3);
		
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit2.setPositionByTile(board.getTile(1, 1)); 
		testUnit2.setPlayer(humanPlayer);
		board.addUnit(testUnit2, 1, 1);
		
		Unit testUnit3 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit3.setPositionByTile(board.getTile(1, 4)); 
		testUnit3.setPlayer(AIPlayer);
		board.addUnit(testUnit3, 1, 4);
		
		Unit testUnit4 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit4.setPositionByTile(board.getTile(3, 1)); 
		testUnit4.setPlayer(AIPlayer);
		board.addUnit(testUnit4, 3, 1);
		
		Unit testUnit5 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit5.setPositionByTile(board.getTile(3, 3)); 
		testUnit5.setPlayer(humanPlayer);
		board.addUnit(testUnit5, 3, 3);
		
		Unit testUnit6 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit6.setPositionByTile(board.getTile(4, 3)); 
		testUnit6.setPlayer(humanPlayer);
		board.addUnit(testUnit6, 4, 3);
		
		Unit testUnit7 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit7.setPositionByTile(board.getTile(4, 1)); 
		testUnit7.setPlayer(AIPlayer);
		board.addUnit(testUnit7, 4, 1);
		
		Unit testUnit8 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit8.setPositionByTile(board.getTile(5, 1)); 
		testUnit8.setPlayer(AIPlayer);
		board.addUnit(testUnit8, 5, 1);
		
		Unit testUnit9 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit9.setPositionByTile(board.getTile(7, 3)); 
		testUnit9.setPlayer(humanPlayer);
		board.addUnit(testUnit9, 7, 3);
		
		Unit testUnit10 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit10.setPositionByTile(board.getTile(7, 4)); 
		testUnit10.setPlayer(humanPlayer);
		board.addUnit(testUnit10, 7, 4);
		
		Unit testUnit11 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit11.setPositionByTile(board.getTile(8, 0)); 
		testUnit11.setPlayer(humanPlayer);
		board.addUnit(testUnit11, 8, 0);
		
		Unit testUnit12 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_blaze_hound, structures.GameState.getNextID(), Unit.class);
		testUnit12.setPositionByTile(board.getTile(6, 1)); 
		testUnit12.setPlayer(humanPlayer);
		board.addUnit(testUnit11, 6, 1);
	}
	
	
	///////////////////////////////////////////////// Tests for action highlighting for avatars /////////////////////////////////////////////////
	
	/*
	 * Tests attack and move + attack highlighting for human avatar
	 */
	@Test 
	public void attackHighlightHumanAvatar() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(humanPlayer, board.getTile(1, 2));
		board.highlightMoveAndAttacks(humanPlayer, board.getTile(1, 2));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(1, 3));
		expected.add(new TestTile(3, 1));
		expected.add(new TestTile(4, 1)); 
		expected.add(new TestTile(1, 4));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for human avatar
	 */
	@Test 
	public void moveHighlightHumanAvatar() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(humanPlayer, board.getTile(1, 2));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(0, 1));
		expected.add(new TestTile(0, 2));
		expected.add(new TestTile(0, 3)); 
		expected.add(new TestTile(1, 0));
		expected.add(new TestTile(2, 1)); 
		expected.add(new TestTile(2, 2));
		expected.add(new TestTile(3, 2)); 
		expected.add(new TestTile(2, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	
	/*
	 * Tests attack and move + attack highlighting for AI avatar
	 */
	@Test
	public void attackHighlightAIAvatar() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(AIPlayer, board.getTile(7, 2));
		board.highlightMoveAndAttacks(AIPlayer, board.getTile(7, 2));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(6, 1));
		expected.add(new TestTile(8, 0));
		expected.add(new TestTile(7, 3)); 
		expected.add(new TestTile(7, 4));
		expected.add(new TestTile(4, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for ai avatar
	 */
	@Test 
	public void moveHighlightAIAvatar() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(AIPlayer, board.getTile(7, 2));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(7, 0));
		expected.add(new TestTile(7, 1));
		expected.add(new TestTile(8, 1)); 
		expected.add(new TestTile(6, 2));
		expected.add(new TestTile(5, 2)); 
		expected.add(new TestTile(6, 3));
		expected.add(new TestTile(8, 2)); 
		expected.add(new TestTile(8, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	///////////////////////////////////////////////// Tests for action highlighting for human units /////////////////////////////////////////////////
	
	/*
	 * Tests attack and move + attack highlighting for human unit Test1/2
	 * (includes test that unit unable to move through enemy to perform move and attack)
	 */
	@Test
	public void attackHighlightHumanUnit() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(humanPlayer, board.getTile(1, 1));
		board.highlightMoveAndAttacks(humanPlayer, board.getTile(1, 1));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(1, 3));
		expected.add(new TestTile(3, 1));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests attack and move + attack highlighting for human unit - Test2/2
	 * (includes test that unit unable to move through enemy to perform move and attack)
	 */
	@Test
	public void attackHighlightHumanUnit2() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(humanPlayer, board.getTile(6, 1));
		board.highlightMoveAndAttacks(humanPlayer, board.getTile(6, 1));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(5, 1));
		expected.add(new TestTile(4, 1));
		expected.add(new TestTile(7, 2));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for human unit 1/2
	 * (includes test that unit unable to move through enemy)
	 */
	@Test
	public void MoveHighlightHumanUnit() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(humanPlayer, board.getTile(7, 3));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(6, 4));
		expected.add(new TestTile(6, 3));
		expected.add(new TestTile(5, 3));
		expected.add(new TestTile(6, 2));
		expected.add(new TestTile(8, 2));
		expected.add(new TestTile(8, 3));
		expected.add(new TestTile(8, 4));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for human unit 2/2
	 * (includes test that unit able to move through friendly)
	 */
	@Test
	public void MoveHighlightHumanUnit2() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(humanPlayer, board.getTile(3, 3));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(2, 4));
		expected.add(new TestTile(2, 3));
		expected.add(new TestTile(2, 2));
		expected.add(new TestTile(3, 2));
		expected.add(new TestTile(4, 2));
		expected.add(new TestTile(5, 3));
		expected.add(new TestTile(4, 4));
		expected.add(new TestTile(3, 4));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	///////////////////////////////////////////////// Tests for action highlighting for AI units /////////////////////////////////////////////////
	
	/*
	 * Tests attack and move + attack highlighting for ai unit - Test1/2
	 */
	@Test
	public void attackHighlightAIUnit1() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(AIPlayer, board.getTile(3, 1));
		board.highlightMoveAndAttacks(AIPlayer, board.getTile(3, 1));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(1, 1));
		expected.add(new TestTile(1, 2));
		expected.add(new TestTile(3, 3));
		expected.add(new TestTile(4, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	
	/*
	 * Tests attack and move + attack highlighting for ai unit - Test2/2
	 * (includes test that unit unable to move through enemy to perform move and attack)
	 */
	@Test
	public void attackHighlightAIUnit2() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightAttacks(AIPlayer, board.getTile(1, 3));
		board.highlightMoveAndAttacks(AIPlayer, board.getTile(1, 3));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(1, 2));
		expected.add(new TestTile(1, 1));
		expected.add(new TestTile(3, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for ai unit 1/2
	 * (includes test that unit unable to move through enemy)
	 */
	@Test
	public void MoveHighlightAIUnit1() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(AIPlayer, board.getTile(5, 1));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(4, 0));
		expected.add(new TestTile(5, 0));
		expected.add(new TestTile(6, 0));
		expected.add(new TestTile(4, 2));
		expected.add(new TestTile(5, 2));
		expected.add(new TestTile(6, 2));
		expected.add(new TestTile(5, 3));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	/*
	 * Tests move highlighting for ai unit 2/2
	 * (includes test that unit is able to move through friendly)
	 */
	@Test
	public void MoveHighlightAIUnit2() {
		
		// make sure trapped messages have been cleared 
		HighlightTell altTell = new HighlightTell();
		BasicCommands.altTell = altTell;
		altTell.resetMessages();
		
		// highlight attacks for humanAvatar
		board.highlightMoves(AIPlayer, board.getTile(4, 1));
		
		// get iterable of trapped messages
		Iterable<ObjectNode> messages = altTell.readMessages();
		
		// set what should be highlighted
		Set<TestTile> expected = new HashSet<TestTile>();
		expected.add(new TestTile(4, 0));
		expected.add(new TestTile(5, 0));
		expected.add(new TestTile(3, 0));
		expected.add(new TestTile(2, 1));
		expected.add(new TestTile(3, 2));
		expected.add(new TestTile(4, 2));
		expected.add(new TestTile(5, 2));
		
		// work out what has been highlighted
		Set<TestTile> actual = new HashSet<TestTile>();
		for (ObjectNode message : messages) {

			// extract relevant fields from message
			int x = Integer.parseInt(message.findValuesAsText("tilex").get(0));
			int y = Integer.parseInt(message.findValuesAsText("tiley").get(0));
			
			TestTile t = new TestTile(x, y);
			actual.add(t);
		}
		
		// check that correct tiles have been highlighted
		assertTrue("Incorrect highlighting", expected.equals(actual));
	
		// make sure trapped messages have been cleared 
		altTell.resetMessages();
	}
	
	
	
	
}
