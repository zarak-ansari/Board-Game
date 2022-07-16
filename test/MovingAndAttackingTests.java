import static org.junit.Assert.assertTrue;

import org.junit.*;

import com.fasterxml.jackson.databind.node.ObjectNode;
import commands.BasicCommands;
import commands.HighlightTell;
import events.Initalize;
import play.libs.Json;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Position;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

public class MovingAndAttackingTests {

	static GameState gameState;
	static Board board;
	static Player AIPlayer;
	static Player humanPlayer;

	/*
	 * Pre-test initialisation of game
	 */	
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
	}
	
	
	
	/*
	 * Test moving of normal unit (can only move once)
	 */
	@Test
	public void basicMove() {
		setup();
		events.TileClicked processor = new events.TileClicked();
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();

		// try to make invalid move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 8);
		ClickMessage.put("tiley", 4);
		processor.processEvent(null, gameState, ClickMessage);

		// check unit has not moved
		assertTrue("Invalid move actuated", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 2);
		assertTrue("Invalid move actuated", board.getTile(1, 2).getUnit().equals(unit));
		assertTrue("Invalid move actuated", board.getTile(1, 2).hasUnit());

		assertTrue("Invalid move actuated", !board.getTile(8, 4).hasUnit());
		assertTrue("Invalid move actuated", board.getTile(8, 4).getUnit() == null);
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		// try to make valid move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 0);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has moved
		assertTrue("Unit did not move correctly", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 0);
		assertTrue("Unit did not move correctly", board.getTile(1, 0).getUnit().equals(unit));
		assertTrue("Unit did not move correctly", board.getTile(1, 0).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(1, 2).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(1, 2).getUnit() == null);
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 0);
		processor.processEvent(null, gameState, ClickMessage);
		
		// try to move again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 1);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has not moved
		assertTrue("Normal unit should not be able to move twice", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 0);
		assertTrue("Normal unit should not be able to move twice", board.getTile(1, 0).getUnit().equals(unit));
		assertTrue("Normal unit should not be able to move twice", board.getTile(1, 0).hasUnit());
		
		assertTrue("Normal unit should not be able to move twice", !board.getTile(1, 1).hasUnit());
		assertTrue("Normal unit should not be able to move twice", board.getTile(1, 1).getUnit() == null);
	}
	
	
	/*
	 * Test moving of azurite lion (can only move twice)
	 */
	@Test
	public void azuriteLionMove() {
		setup();
		events.TileClicked processor = new events.TileClicked();
		
		// put azurite lion on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 2;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		// simulate click on tile to move to
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has moved
		assertTrue("Unit did not move correctly", unit.getPosition().getTilex() == 3 && unit.getPosition().getTiley() == 3);
		assertTrue("Unit did not move correctly", board.getTile(3, 3).getUnit().equals(unit));
		assertTrue("Unit did not move correctly", board.getTile(3, 3).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(1, 3).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(1, 3).getUnit() == null);
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// try to move again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has moved a second time
		assertTrue("Azure herald should not be able to move twice", unit.getPosition().getTilex() == 3 && unit.getPosition().getTiley() == 3);
		assertTrue("Azure herald should not be able to move twice", board.getTile(3, 3).getUnit().equals(unit));
		assertTrue("Azure herald should not be able to move twice", board.getTile(3, 3).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(3, 2).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(3, 2).getUnit() == null);
		
		
		
	}
	
	
	/*
	 * Test that a normal unit cannot attack then move
	 */
	@Test
	public void normalUnitAttemptMoveAfterAttack() {
		setup();
		events.TileClicked processor = new events.TileClicked();
		
		// put adjacent enemy on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 1;
		testUnit.setAttacksPerTurn(attacksPerTurn);
 
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		int targetHealthBefore = testUnit.getUnitHealth();

		// attack adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit.getUnitHealth();

		// check unit has not moved
		assertTrue("Should not move to attack adjacent", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 2);
		assertTrue("Should not move to attack adjacent", board.getTile(1, 2).getUnit().equals(unit));
		assertTrue("Should not move to attack adjacent", board.getTile(1, 2).hasUnit());
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		// try to make an otherwise valid move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 0);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has not moved
		assertTrue("Normal unit should not be able to move after an attack", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 2);
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(1, 2).getUnit().equals(unit));
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(1, 2).hasUnit());
		
		assertTrue("Normal unit should not be able to move after an attack", !board.getTile(1, 0).hasUnit());
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(1, 0).getUnit() == null);
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthBefore = testUnit.getUnitHealth();
		
		// attempt to attack adjacent unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthAfter = testUnit.getUnitHealth();
		// check target health not reduced
		assertTrue("Normal unit should be able to attack twice", targetHealthBefore - targetHealthAfter == 0);
		
	}
	
	
	/*
	 * Test move and attack functionality of a normal unit
	 */
	@Test
	public void normalUnitMoveAndAttack() {
		setup();
		events.TileClicked processor = new events.TileClicked();
		
		// put adjacent enemy on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(4, 1)); 
		testUnit.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit, 4, 1);
		BasicCommands.drawUnit(null, testUnit, board.getTile(4, 1));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 1;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		int targetHealthBefore = testUnit.getUnitHealth();

		// attack non-adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 4);
		ClickMessage.put("tiley", 1);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit.getUnitHealth();

		// check unit has moved somewhere
		assertTrue("Should move to attack non-adjacent", board.getTile(1, 2).getUnit() == null);
		assertTrue("Should move to attack non-adjacent", !board.getTile(1, 2).hasUnit());
		
		// check that it has moved to a tile adjacent to unit
		Tile target = board.getTile(4, 1);
		
		Position p = unit.getPosition();
		int x = p.getTilex();
		int y = p.getTiley();
		Tile movedTo = board.getTile(x, y);
		assertTrue("To move and attack unit should move to an adjacent tile to target", movedTo.isAdjacent(target));
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		
	}
	
	/*
	 * Azurite lion attack twice and attempt move
	 */
	@Test
	public void azuriteLionAttackTwice() {
		setup();
		events.TileClicked processor = new events.TileClicked();

		// put azurite lion on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth()+10);	// artificially increase unit health so it doesn't die after being counter attacked
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 2;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// put enemy on board
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card2 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit2.setPositionByTile(board.getTile(2, 3)); 
		testUnit2.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit2, 2, 3);
		BasicCommands.drawUnit(null, testUnit2, board.getTile(2, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit2.setUnitHealth(null, 10);	// artificially increase enemies health so can attack it twice
		testUnit2.setUnitAttack(null, card2.getBigCard().getAttack());
		attacksPerTurn = 1;
		testUnit2.setAttacksPerTurn(attacksPerTurn);
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		int targetHealthBefore = testUnit2.getUnitHealth();

		// attack adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit2.getUnitHealth();

		// check unit has not moved
		assertTrue("Should not move to attack adjacent", board.getTile(1, 3).hasUnit());
		assertTrue("Should not move to attack adjacent", board.getTile(1, 3).getUnit().equals(unit));
		
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthBefore = testUnit2.getUnitHealth();

		// attack adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthAfter = testUnit2.getUnitHealth();

		// check unit has not moved
		assertTrue("Should not move to attack adjacent", board.getTile(1, 3).getUnit().equals(unit));
		assertTrue("Should not move to attack adjacent", board.getTile(1, 3).hasUnit());
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		
		// select unit again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// attempt to move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 0);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has not moved
		assertTrue("Normal unit should not be able to move after an attack", unit.getPosition().getTilex() == 1 && unit.getPosition().getTiley() == 3);
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(1, 3).getUnit().equals(unit));
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(1, 3).hasUnit());
		
		assertTrue("Normal unit should not be able to move after an attack", !board.getTile(0, 3).hasUnit());
		assertTrue("Normal unit should not be able to move after an attack", board.getTile(0, 3).getUnit() == null);
		
	}
	
	/*
	 * Azurite lion move and attack twice
	 */
	@Test
	public void azuriteLionMoveAndAttackTwice() {
		setup();
		events.TileClicked processor = new events.TileClicked();

		// put azurite lion on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 2;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// put two enemies on board
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card2 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit2.setPositionByTile(board.getTile(3, 2)); 
		testUnit2.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit2, 3, 2);
		BasicCommands.drawUnit(null, testUnit2, board.getTile(3, 2));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit2.setUnitHealth(null, card2.getBigCard().getHealth());
		testUnit2.setUnitAttack(null, card2.getBigCard().getAttack());
		attacksPerTurn = 1;
		testUnit2.setAttacksPerTurn(attacksPerTurn);
		
		Unit testUnit3 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card3 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit3.setPositionByTile(board.getTile(2, 0)); 
		testUnit3.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit3, 2, 0);
		BasicCommands.drawUnit(null, testUnit3, board.getTile(2, 0));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit3.setUnitHealth(null, card3.getBigCard().getHealth());
		testUnit3.setUnitAttack(null, card3.getBigCard().getAttack());
		attacksPerTurn = 1;
		testUnit3.setAttacksPerTurn(attacksPerTurn);
		
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		
		int targetHealthBefore = testUnit2.getUnitHealth();

		// attack non-adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 2);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit2.getUnitHealth();

		// check unit has moved somewhere
		assertTrue("Should move to attack non-adjacent", board.getTile(1, 3).getUnit() == null);
		assertTrue("Should move to attack non-adjacent", !board.getTile(1, 3).hasUnit());
		
		// check that it has moved to a tile adjacent to unit
		Tile target = board.getTile(3, 2);
		
		Position p = unit.getPosition();
		int x = p.getTilex();
		int y = p.getTiley();
		Tile movedTo = board.getTile(x, y);
		assertTrue("To move and attack unit should move to an adjacent tile to target", movedTo.isAdjacent(target));
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		
		// simulate click on unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", x);
		ClickMessage.put("tiley", y);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthBefore = testUnit3.getUnitHealth();
		
		// attack non-adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 0);
		processor.processEvent(null, gameState, ClickMessage);

		targetHealthAfter = testUnit3.getUnitHealth();

		// check unit has moved somewhere
		assertTrue("Should move to attack non-adjacent", board.getTile(x, y).getUnit() == null);
		assertTrue("Should move to attack non-adjacent", !board.getTile(x, y).hasUnit());

		// check that it has moved to a tile adjacent to unit
		target = board.getTile(2, 0);

		p = unit.getPosition();
		x = p.getTilex();
		y = p.getTiley();
		movedTo = board.getTile(x, y);
		assertTrue("To move and attack unit should move to an adjacent tile to target", movedTo.isAdjacent(target));

		// check target health reduced
		assertTrue("Attack should reduce target's health",
				targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
	}
	
	
	/*
	 * Azurite lion move, attack, attack
	 */
	@Test
	public void azuriteLionMoveAttackAttack() {
		setup();
		events.TileClicked processor = new events.TileClicked();

		// put azurite lion on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 2;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// put enemy on board
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card2 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit2.setPositionByTile(board.getTile(3, 3)); 
		testUnit2.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit2, 3, 3);
		BasicCommands.drawUnit(null, testUnit2, board.getTile(3, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit2.setUnitHealth(null, 10);
		testUnit2.setUnitAttack(null, card2.getBigCard().getAttack());
		attacksPerTurn = 1;
		testUnit2.setAttacksPerTurn(attacksPerTurn);
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		// move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has moved
		assertTrue("Unit did not move correctly", unit.getPosition().getTilex() == 2 && unit.getPosition().getTiley() == 3);
		assertTrue("Unit did not move correctly", board.getTile(2, 3).getUnit().equals(unit));
		assertTrue("Unit did not move correctly", board.getTile(2, 3).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(1, 3).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(1, 3).getUnit() == null);
		
		int targetHealthBefore = testUnit2.getUnitHealth();
		
		// select unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// attack
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit2.getUnitHealth();
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		targetHealthBefore = testUnit2.getUnitHealth();
		
		// select unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// attack again
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		targetHealthAfter = testUnit2.getUnitHealth();
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
				
		
	}
	
	
	
	/*
	 * Azurite lion move, attack, move
	 */
	@Test
	public void azuriteLionMoveAttackMoveAttack() {
		setup();
		events.TileClicked processor = new events.TileClicked();

		// put azurite lion on board
		Unit testUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit.setPositionByTile(board.getTile(1, 3)); 
		testUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnit, 1, 3);
		BasicCommands.drawUnit(null, testUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnit.setUnitAttack(null, card1.getBigCard().getAttack());
		int attacksPerTurn = 2;
		testUnit.setAttacksPerTurn(attacksPerTurn);
		
		// put enemy on board
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card2 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit2.setPositionByTile(board.getTile(3, 3)); 
		testUnit2.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit2, 3, 3);
		BasicCommands.drawUnit(null, testUnit2, board.getTile(3, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit2.setUnitHealth(null, 10);
		testUnit2.setUnitAttack(null, card2.getBigCard().getAttack());
		attacksPerTurn = 1;
		testUnit2.setAttacksPerTurn(attacksPerTurn);
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		// move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has moved
		assertTrue("Unit did not move correctly", unit.getPosition().getTilex() == 2 && unit.getPosition().getTiley() == 3);
		assertTrue("Unit did not move correctly", board.getTile(2, 3).getUnit().equals(unit));
		assertTrue("Unit did not move correctly", board.getTile(2, 3).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(1, 3).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(1, 3).getUnit() == null);
		
		int targetHealthBefore = testUnit2.getUnitHealth();
		
		// select unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// attack
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		int targetHealthAfter = testUnit2.getUnitHealth();
		
		// check target health reduced
		assertTrue("Attack should reduce target's health", targetHealthBefore - targetHealthAfter == unit.getUnitAttack());
		
		// select unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		// move
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 2);
		ClickMessage.put("tiley", 4);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check unit has not moved
		assertTrue("Azurite lion should not be able to move, attack, then move", unit.getPosition().getTilex() == 2 && unit.getPosition().getTiley() == 3);
		assertTrue("Azurite lion should not be able to move, attack, then move", board.getTile(2, 3).getUnit().equals(unit));
		assertTrue("Azurite lion should not be able to move, attack, then move", board.getTile(2, 3).hasUnit());
		
		assertTrue("Azurite lion should not be able to move, attack, then move", !board.getTile(2, 4).hasUnit());
		assertTrue("Azurite lion should not be able to move, attack, then move", board.getTile(2, 4).getUnit() == null);
	}
	
	@Test
	public void rangedUnitAttack() {
		setup();
		events.TileClicked processor = new events.TileClicked();
		
		gameState.getHumanPlayer().setMana(5);

		// place Fire Spitter on the board
		Unit fsUnit = BasicObjectBuilders.loadUnit(StaticConfFiles.u_fire_spitter, structures.GameState.getNextID(), Unit.class);
		Card fsCard = BasicObjectBuilders.loadCard(StaticConfFiles.c_fire_spitter, 2, Card.class);
		fsUnit.setPositionByTile(board.getTile(1, 3)); 
		fsUnit.setPlayer(gameState.getHumanPlayer());
		board.addUnit(fsUnit, 1, 3);
		BasicCommands.drawUnit(null, fsUnit, board.getTile(1, 3));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		fsUnit.setUnitHealth(null, fsCard.getBigCard().getHealth());
		fsUnit.setUnitAttack(null, fsCard.getBigCard().getAttack());
		fsUnit.setAttacksPerTurn(1);
		
		assertTrue("FireSpitter was not summoned to the board", gameState.getBoard().getTile(1, 3).hasUnit());
		
		// put enemy on board
		Unit testUnit2 = BasicObjectBuilders.loadUnit(StaticConfFiles.u_azurite_lion, structures.GameState.getNextID(), Unit.class);
		Card card2 = BasicObjectBuilders.loadCard(StaticConfFiles.c_azurite_lion, 17, Card.class);
		testUnit2.setPositionByTile(board.getTile(8, 0)); 
		testUnit2.setPlayer(gameState.getAIPlayer());
		board.addUnit(testUnit2, 8, 0);
		BasicCommands.drawUnit(null, testUnit2, board.getTile(8, 0));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnit2.setUnitHealth(null, 10);
		testUnit2.setUnitAttack(null, card2.getBigCard().getAttack());
		int attacksPerTurn = 1;
		testUnit2.setAttacksPerTurn(attacksPerTurn);
		
		assertTrue("Enemy Unit not summoned to the board", gameState.getBoard().getTile(8, 0).hasUnit());
		
		int targetHealthInitial = testUnit2.getUnitHealth();
		int fireSpitterAttack = fsUnit.getUnitAttack();
		
		// simulate click on unit
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 1);
		ClickMessage.put("tiley", 3);
		processor.processEvent(null, gameState, ClickMessage);
		
		Unit unit = gameState.getClickedUnit();
		
		//int targetHealthBefore = testUnit.getUnitHealth();
		
		// attack non-adjacent unit
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 8);
		ClickMessage.put("tiley", 0);
		processor.processEvent(null, gameState, ClickMessage);

		int targetHealthAfter = testUnit2.getUnitHealth();
		
		assertTrue("Fire Spitter did not attack the unit across the board", (targetHealthAfter+fireSpitterAttack)==targetHealthInitial);
		
		
	}
	//Windshrike tests

public class Windshrike {
	
	@Test
	public void summonWindshrike() {
		//put Windshrike into player's hand
		Card windShrike = BasicObjectBuilders.loadCard(StaticConfFiles.c_windshrike, 24, Card.class);
		Player currentPlayer = gameState.getHumanPlayer();
		Board board = gameState.getBoard();
		int position = currentPlayer.getPosition()+1;
		currentPlayer.setCard(position, windShrike);
		currentPlayer.setMana(7);			
		ObjectNode cardClickMessage = Json.newObject();
		cardClickMessage.put("messageType", "cardClicked");
		cardClickMessage.put("position", 2);
		// transfer message
		events.CardClicked processor = new events.CardClicked();
		processor.processEvent(null, gameState, cardClickMessage);
		//place on tile (0, 4)		
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messageType", "tileClicked");
		tileClickMessage.put("tilex", 0);
		tileClickMessage.put("tiley", 4);
		//now click Windshrike unit 
		events.TileClicked processor2 = new events.TileClicked();
		processor2.processEvent(null, gameState, tileClickMessage);
		Unit unit = gameState.getClickedUnit();
		
		assertTrue("Windshrike was not summoned to the board", gameState.getBoard().getTile(0, 4).hasUnit());
			}
	@Test
	public void WindshrikeMoveAnywhere() {
		setup();
		events.TileClicked processor = new events.TileClicked();

		// set Windshrike on a tile
		Unit testUnitWshrike = BasicObjectBuilders.loadUnit(StaticConfFiles.u_windshrike, structures.GameState.getNextID(), Unit.class);
		Card card1 = BasicObjectBuilders.loadCard(StaticConfFiles.c_windshrike, 24, Card.class);
		testUnitWshrike.setPositionByTile(board.getTile(0,4)); 
		testUnitWshrike.setPlayer(gameState.getHumanPlayer());
		board.addUnit(testUnitWshrike, 0, 4);
		BasicCommands.drawUnit(null, testUnitWshrike, board.getTile(0, 4));
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		testUnitWshrike.setUnitHealth(null, card1.getBigCard().getHealth());
		testUnitWshrike.setUnitAttack(null, card1.getBigCard().getAttack());
			
		// make-click to begin to move Windshrike
		ObjectNode ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 0);
		ClickMessage.put("tiley", 4);
		processor.processEvent(null, gameState, ClickMessage);
		Unit unit = gameState.getClickedUnit();
		
		// test whether Windshrike can move to a random faraway tile (3,1) on the Board
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "tileClicked");
		ClickMessage.put("tilex", 3);
		ClickMessage.put("tiley", 1);
		processor.processEvent(null, gameState, ClickMessage);
		
		// check Windshrike has moved to far-afield location on Board
		assertTrue("Unit did not move correctly", unit.getPosition().getTilex() == 3 && unit.getPosition().getTiley() == 1);
		assertTrue("Unit did not move correctly", board.getTile(3, 1).getUnit().equals(unit));
		assertTrue("Unit did not move correctly", board.getTile(3, 1).hasUnit());
		
		assertTrue("Starting tile still contains information about unit", !board.getTile(0, 4).hasUnit());
		assertTrue("Starting tile still contains information about unit", board.getTile(0, 4).getUnit() == null);
		
	}
	
}
}