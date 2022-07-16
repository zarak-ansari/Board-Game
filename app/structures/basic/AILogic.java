package structures.basic;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;

import akka.actor.ActorRef;
import commands.CheckMessageIsNotNullOnTell;
import events.CardClicked;
import events.TileClicked;
import play.libs.Json;
import structures.GameState;

/**
 *  This class is primarily tasked with providing the logic and functionality for an AI Player. The primary method here is playTurn(), which is called whenever the end turn button is clicked
 *  This class only contains one instance variable, altTell. This class only contains methods which build AI logic based on other classes.
 *  There are several helper methods in order to reduce repeated code in playTurn. These are found below this method.
 * 
 */


public class AILogic {
	static CheckMessageIsNotNullOnTell altTell = new CheckMessageIsNotNullOnTell();
	
	/**
	 * playTurn is the class method primarily tasked with providing the AI logic. This method handles playing cards, summoning units, as well as the AI's move and attack capabilities.
	 * @param gameState - Holds the current gameState
	 */
	public static void playTurn(GameState gameState) {
		
		// create event processors
		ObjectNode ClickMessage;
		events.EndTurnClicked endTurnProcessor = new events.EndTurnClicked();
		
		// declare convenience variables (add as necessary)
		ActorRef out = gameState.getOut();		
		Player AIPlayer = gameState.getAIPlayer();
		Player HumanPlayer = gameState.getHumanPlayer();
		Board board = gameState.getBoard();
		ArrayList<Unit> units = board.getUnits();		
		
		ArrayList<Unit> AIunits = new ArrayList<Unit>();
		ArrayList<Unit> HumanUnits = new ArrayList<Unit>();	
		
		// divide the units on the board between the AI and the Human in order to determine which are friendly and which are adversaries.
		for(Unit u: units) {
			if(u.getPlayer().equals(AIPlayer)){
				AIunits.add(u);
			}else {
				HumanUnits.add(u);
			}
		}
		
		// ensures that the AI player's avatar is always included as an AI unit.
		if(!AIunits.contains(AIPlayer.getAvatar())) {
			AIunits.add(AIPlayer.getAvatar());
		}
	
		// play cards
		AILogicPlayCards.playCards(gameState);
		try {Thread.sleep(800);} catch (InterruptedException e) {e.printStackTrace();}
		

		// Perform moves and/or attack for each unit
		for(Unit unit: AIunits) {
			performAIMoveAndAttack(unit, board, HumanPlayer, HumanUnits, out, gameState);
		}
		
		// This is looped separately as having this in the above loop caused bugs with AI Units not receiving a turn. This loop allows units with attackTwice ability to attack again.
		for(Unit unit: AIunits) {
			if(unit.getAbilities().contains("attackTwice")) {
				performAIMoveAndAttack(unit, board, HumanPlayer, HumanUnits, out, gameState);
			}
		}
		
		
		// Return control to human player
		ClickMessage = Json.newObject();
		ClickMessage.put("messageType", "endTurnClicked");
		ClickMessage.put("AI", "AI");
		endTurnProcessor.processEvent(out, gameState, ClickMessage);

		
	}
	
	
	/**
	 * Helper method that will determine the most optimal move and attack actions for each AI unit, as called from playTurn()
	 * @param unit - The Unit performing the move/attack
	 * @param board - Current Game Board
	 * @param HumanPlayer - The Human (Adversary) player
	 * @param HumanUnits - Any adversary units contained on the board
	 * @param out - ActorRef Object
	 * @param gameState - Current GameState
	 */
	private static void performAIMoveAndAttack(Unit unit, Board board, Player HumanPlayer, ArrayList<Unit> HumanUnits, ActorRef out, GameState gameState) {
		
		// create helper variables for the AI Logic
		Unit enemyAvatar = HumanPlayer.getAvatar();
		Tile enemyAvatarLocation = board.getTile(enemyAvatar.getPosition().getTilex(), enemyAvatar.getPosition().getTiley());
		int xPos = unit.getPosition().getTilex();
		int yPos = unit.getPosition().getTiley();
		
		// simulate a click on the unit.
		clickOnTile(out, gameState, board.getTile(xPos, yPos));

		// populate potential attacks and moves
		ArrayList<Tile> moveLocations = board.getHighlightedTiles(1);
		ArrayList<Tile> enemyLocations = board.getHighlightedTiles(2);
		
		// delay to improve feel of UI
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
		
		// variable to store the action we decide to take
		Tile firstActionTile = null;
		Tile secondActionTile = null;
		
		if(enemyLocations.size() == 0) {
			// No adjacent units to the AI Units
			
			// logic for non-avatar units
			if(!unit.isAvatar()) {
				// if unit is a ranged unit, attack any provoking units. If none, attack the enemy avatar. No logic for ranged units as they'll never have no adjacent units.
				if(unit.getAbilities().contains("provoke")) {						
					
					// Attempt to move towards the nearest unit
					Unit toBeProvoked = getClosestUnit(unit, board, enemyLocations, enemyAvatar);
					firstActionTile = AILogicPlayCards.getClosestTileToUnit(moveLocations, toBeProvoked);			
					
				}else if(unit.getAbilities().contains("flying")){
					
					// if there is a flying enemy on the board, move towards it
					boolean rangedFound = false;
					for(Unit enemyUnit: HumanUnits) {
						if(enemyUnit.getAbilities().contains("ranged")) {
							firstActionTile =  AILogicPlayCards.getClosestTileToUnit(moveLocations, enemyUnit);
							Tile rangedUnitTile = board.getTile(enemyUnit.getPosition().getTilex(), enemyUnit.getPosition().getTiley());
							secondActionTile = rangedUnitTile;
							rangedFound = true;
							break;
						}
					}
					// If no ranged units, move towards the enemy avatar.
					if(!rangedFound) {
						firstActionTile = AILogicPlayCards.getClosestTileToUnit(moveLocations, enemyAvatar);
						secondActionTile = enemyAvatarLocation;
					}
				}else {
					// If a regular unit, attempt to move closer to the enemy's avatar.
					firstActionTile = AILogicPlayCards.getClosestTileToUnit(moveLocations, enemyAvatar);
				}
			}else {
				// Logic for the avatar
				
				// Move away from the enemy's avatar if there's no surrounding units. Allows the Avatar to get more space. 
				ArrayList<Tile> enemyAvatarLocationList = new ArrayList<Tile>();
				enemyAvatarLocationList.add(enemyAvatarLocation);
				firstActionTile = getFurthestTile(unit, enemyAvatarLocationList, board, moveLocations);		
			}
		
		// this section deals with the situation where the AI unit has one adjacent adversary unit.
		}else if (enemyLocations.size()==1){
			// Details of the one unit nearby
			Unit adjacentUnit = enemyLocations.get(0).getUnit();
			Tile adjacentUnitTile = board.getTile(adjacentUnit.getPosition().getTilex(), adjacentUnit.getPosition().getTiley());
			
			// Ranged Logic: Move as far away from adjacent unit (firstAction), then attack it (secondAction)
			if(unit.getAbilities().contains("ranged")) {
				Tile rangedTile = board.getTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
				// If a ranged unit can only see one unit, this will be the avatar.					
				firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
				System.out.println(firstActionTile.getTilex() + " " + firstActionTile.getTiley());									
				
				secondActionTile = enemyAvatarLocation;
				System.out.println(secondActionTile.getTilex() + " " + secondActionTile.getTiley());

				
			}else if(unit.getAbilities().contains("flying")) {
				// Just move flying units to closer to the avatar. If the adjacent unit is the avatar, attack this. If ranged or provoke unit, attack this.
				if(adjacentUnit.getAbilities().contains("ranged")) {
					firstActionTile = adjacentUnitTile;
				}else if(adjacentUnit.isAvatar() || adjacentUnit.getAbilities().contains("provoke")) {	
					firstActionTile = adjacentUnitTile;			
				}else{
					firstActionTile = AILogicPlayCards.getClosestTileToUnit(moveLocations, enemyAvatar);
					secondActionTile = enemyAvatarLocation;
				}
			}else if(unit.getAbilities().contains("provoke")) {
				// Provoke units should attack adjacent units. Their usefulness comes from their ability to take hits and stay near units. So moving away wouldn't be too helpful.
				firstActionTile = adjacentUnitTile;
			}else if(unit.isAvatar()) {
				
				// Avatars should prioritise moving away from units. However, if it can easily kill this unit, then it should.
				if((adjacentUnit.getUnitHealth() <= unit.getUnitAttack()) || adjacentUnit.getUnitAttack()*2 < unit.getUnitHealth()) {
					// Make the Avatar avoid provoking units, and all other units when it's health is below half. 
					if(adjacentUnit.getAbilities().contains("provoke")) {
						firstActionTile = adjacentUnitTile;
					}else if(unit.getUnitHealth() < 10){
						firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
					}else {
						firstActionTile = adjacentUnitTile;
					}
				}else{
					firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
				}
			}else {
				// Logic for normal units
				
				// We want to clear any provoke or ranged units as a priority. 
				if(adjacentUnit.getAbilities().contains("provoke") || adjacentUnit.getAbilities().contains("ranged")) {
					firstActionTile = adjacentUnitTile;
					
				// else, attack if the situation calls for it.
				}else if(adjacentUnit.getUnitHealth() <= unit.getUnitAttack()) {
					firstActionTile = adjacentUnitTile;
				}else if(adjacentUnit.getUnitAttack()*2 <= unit.getUnitHealth() && (unit.getUnitHealth()*2 >= unit.getStartingHealth())) {
					firstActionTile = adjacentUnitTile;
				}else if(adjacentUnit.getUnitAttack() >= unit.getUnitHealth() && unit.getUnitAttack() < adjacentUnit.getUnitHealth()) {
					firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
				}else {
					// Attack by default. Above, we move away if the situation calls. But if we move away all the time, this is frustrating to play against.
					firstActionTile = adjacentUnitTile; 
				}
			}
			
		}else {
			// More than one unit surrounding this. 	
				
			// We set these in order to determine the board state. We initialize with null values, and then check if these values are null later.
			Unit rangedUnit = null;
			Unit provokeUnit = null;
			boolean containsAvatar = false;
			Unit maxAttackUnit = null;
			Unit oneHitFromDeath = null;
			int unitsMaximumAttack = 0;
			
			// first, we loop through all the current enemies to get an idea of board state, and attempt to determine both 
			for(Tile t: enemyLocations) {
				Unit concernedUnit = t.getUnit();
				// This will search and set maxAttackUnit to the surrounding unit with the highest attack statistic. 
				if(concernedUnit.getUnitAttack() > unitsMaximumAttack) {
					maxAttackUnit = concernedUnit;
					unitsMaximumAttack = maxAttackUnit.getUnitAttack();
				}
				// This will determine if there are any units that are one hit away from death, and if so, set oneHitFromDeath to be equal to this unit.
				if(concernedUnit.getUnitHealth() <= unit.getUnitAttack()) {
					oneHitFromDeath = concernedUnit;
				}
				
				// Lastly, we determine if there are any ranged, provoke or avatar units, and if so, set the corresponding variable equal to that unit.
				if(concernedUnit.getAbilities().contains("ranged")) {
					rangedUnit = concernedUnit;
				}else if(concernedUnit.getAbilities().contains("provoke")) {
					provokeUnit = concernedUnit;
				}else if(concernedUnit.isAvatar()) {
					containsAvatar = true;
				}
			}
			
			// we want the AI to be an aggressive, challenging opponent. Therefore, in the instance we're surrounded, we'll prioritise attacking, except if the unit is our avatar.
			if(unit.isAvatar()) {
				firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
			}else if(unit.getAbilities().contains("flying")){
				// prioritise moving flying units towards the enemy avatar. This gives us the ability to summon other units close to the adversary avatar, and directly attack.
				if(containsAvatar) {
					firstActionTile = enemyAvatarLocation;
				}else {
					firstActionTile = AILogicPlayCards.getClosestTileToUnit(moveLocations, enemyAvatar);
					secondActionTile = enemyAvatarLocation;
				}
			}else if(rangedUnit != null) { // ranged units are top priority given the damage they can do to avatars throughout a game.			
				firstActionTile = board.getTile(rangedUnit.getPosition().getTilex(), rangedUnit.getPosition().getTiley());
			}else if(containsAvatar) {
				firstActionTile = enemyAvatarLocation;
			}else if(provokeUnit != null) { // next, we want to clear any provoke units that may be provoking our units
				firstActionTile = board.getTile(provokeUnit.getPosition().getTilex(), provokeUnit.getPosition().getTiley());
			}else if(oneHitFromDeath != null && !unit.getAbilities().contains("ranged")) {
				firstActionTile = board.getTile(oneHitFromDeath.getPosition().getTilex(), oneHitFromDeath.getPosition().getTiley());
			}else {
				firstActionTile = board.getTile(maxAttackUnit.getPosition().getTilex(), maxAttackUnit.getPosition().getTiley());
			}
			if(unit.getAbilities().contains("ranged")) {
				secondActionTile = firstActionTile;
				firstActionTile = getFurthestTile(unit, enemyLocations, board, moveLocations);
			}
		}
		
		// perform the first action (move or attack) if there is one
		if(firstActionTile != null) {
			clickOnTile(out, gameState, firstActionTile);
		}
		
		// perform the second action (if any)
		if (secondActionTile != null) {
			// get (potentially updated) unit position
			xPos = unit.getPosition().getTilex();
			yPos = unit.getPosition().getTiley();
			
			// re-select the unit
			clickOnTile(out, gameState, board.getTile(xPos, yPos));
			
			// perform secondAction
			clickOnTile(out, gameState, secondActionTile);
		}																												
	}
	
	
	/**
	 * Helper Method - Used to retrieve the AI player's card that currently has the highest mana cost
	 * @param cards - All the cards currently in the AI Player's hand
	 * @return
	 */
	private static Card getCardWithHighestMana(ArrayList<Card> cards) {
		Card resultCard = cards.get(0);
		for(Card card : cards) {
			if(card.getManacost() > resultCard.getManacost()) {
				resultCard = card;
			}
		}
		return resultCard;
	}
	
	
	/**
	 * This is a helper method for having the AI player simulate clicks on cards. This means that AI's moves are displayed to the human user.
	 * @param out - ActorRef object
	 * @param gameState - Current Game State
	 * @param handPosition - Hand Position of the card in the AI Player's hand
	 */
	private static void clickOnCard(ActorRef out, GameState gameState, int handPosition) {
		CardClicked processor = new CardClicked();
		ObjectNode clickCardMessage = Json.newObject();
		clickCardMessage.put("messagetype", "cardclicked");
		clickCardMessage.put("position", handPosition);
		clickCardMessage.put("AI", "AI");
		processor.processEvent(out, gameState, clickCardMessage);
	}
	
	
	/**
	 * This is a helper method for having the AI player simulate clicks on tiles. This means that AI's moves are displayed to the human user.
	 * @param out - ActorRef Object
	 * @param gameState - Current GameState
	 * @param t - Tile to be clicked on
	 */
	private static void clickOnTile(ActorRef out, GameState gameState, Tile t) {
		TileClicked processor = new TileClicked();
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messagetype", "tileclicked");
		tileClickMessage.put("tilex", t.getTilex());
		tileClickMessage.put("tiley", t.getTiley());
		tileClickMessage.put("AI", "AI");
		processor.processEvent(out, gameState, tileClickMessage);
	}
	
	
	/**
	 * Helper method to return the closest unit to the current AI Unit.
	 * @param AIUnit - AI Unit concerned
	 * @param b - The current Board
	 * @param enemyLocations - An ArrayList of Tile objects containing enemy locations
	 * @param enemyAvatar - The Unit object for the Human Player's avatar.
	 * @return - returns the Unit closest to the AI unit.
	 */
	private static Unit getClosestUnit(Unit AIUnit, Board b, ArrayList<Tile> enemyLocations, Unit enemyAvatar) {
		Tile AIUnitLocation = b.getTile(AIUnit.getPosition().getTilex(), AIUnit.getPosition().getTiley());
		int AITilex = AIUnitLocation.getTilex();
		int AITiley = AIUnitLocation.getTiley();
		Unit closestUnit = enemyAvatar;
		
		
		for(Tile t: enemyLocations) {
			int difference = (AITilex - closestUnit.getPosition().getTilex()) + (AITiley - closestUnit.getPosition().getTiley());
			if(difference < 0) {difference = (difference*difference)/difference;}
			int xPo = t.getTilex();
			int yPo = t.getTiley();
			
			int newDiffrence = (AITilex - xPo) + (AITiley - yPo);
			if(newDiffrence < 0) {newDiffrence = (newDiffrence*newDiffrence)/newDiffrence;}
			
			if(newDiffrence < difference) {
				closestUnit = b.getTile(xPo, yPo).getUnit();
			}
			
		}
		return closestUnit;
	}

	
	/**
	 * Helper method to retrieve the furthest tile from human player units. This allows AI units to retreat from Human units.
	 * @param unit - AI Unit that is concerned
	 * @param enemyLocations - An ArrayList of Tiles containing enemies
	 * @param board - Current Board object
	 * @param highlightedTiles - An ArrayList of Tiles currently highlighted for valid moves.
	 * @return
	 */
	private static Tile getFurthestTile(Unit unit, ArrayList<Tile> enemyLocations, Board board, ArrayList<Tile> highlightedTiles) {
		// Helper variables to store frequently used data.
		Tile uLocation = board.getTile(unit.getPosition().getTilex(), unit.getPosition().getTiley());
		int uLocationX = uLocation.getTilex();
		int uLocationY = uLocation.getTiley();
		ArrayList<Tile> adjacentUnitLocations = new ArrayList<Tile>();
		
		// Initializing furtherstTile
		Tile furthestTile;
		if(highlightedTiles.isEmpty()) {
			furthestTile = uLocation;
		}else {
			furthestTile = highlightedTiles.get(0);
		}
		
		
		int counter = 0; // Counter is used just to make sure no infinite loops occur. 
		
		while(counter == 0 || furthestTile.hasUnit()) { // We loop until we find a tile without a unit, or we have looped too many times.
			int north = 0;
			int south = 0;
			int east = 0;
			int west = 0;
			counter++;
			
			if(counter > 6) {
				break; // Breaks if we spend too much time searching
			}
			
			// In this code, we loop through each enemy location, and plot this using the north, east, south and west variables. This allows us to determine the best place to move to in order to avoid the most units.
			for(Tile e: enemyLocations) {
				int xPo = uLocation.getTilex() - e.getTilex();
				int yPo = uLocation.getTiley() - e.getTiley();
				
				if(xPo < 0 && yPo < 0) {
					//Unit is to the North-West, so we want to move South-East		
					north+=1;
					west+=1;	
				}else if (xPo == 0 && yPo > 0) {
					//Unit is to the North, want to move south
					south +=2;
				}else if (xPo < 0 && yPo > 0) {
					//Unit is to the North-East
					north+=1;
					east+=1;
				}else if(xPo < 0 && yPo == 0) {
					//Unit is to the East
					east +=2;
				}else if(xPo <0 && yPo < 0) {
					//Unit is to the South-East
					south+=1;
					east+=1;
				}else if(xPo == 0 && yPo < 0) {
					//Unit is to the South
					south +=2;
				}else if(xPo > 0 && yPo < 0) {
					//Unit is to the South-West
					south +=1;
					west+=1;
				}else if(xPo > 0 && yPo == 0) {
					//Unit is to the west
					west+=2;
				}
			}
			
			// The below code determines the best new tile based on the results from the above. 
			if(north > south) {
				if(east > west) {
					//Units are to North-East - Move South-West
					int newYco = uLocationY+1; if(newYco > 4) {newYco=4;}
					int newXco = uLocationX-1; if(newXco < 0) {newXco=0;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {west+=1;} //Try move south instead
				}else if (east == west){
					// Units are to the North - Move South
					int newYco = uLocationY+2; if(newYco > 4) {newYco=4;}
					furthestTile = board.getTile(uLocationX, newYco);
					if(furthestTile.hasUnit()) {south+=1;} //Try move east instead
				}else {
					// Units are to the North-West - Move South-East
					int newYco = uLocationY+1; if(newYco > 4) {newYco=4;}
					int newXco = uLocationX+1; if(newXco > 8) {newXco=8;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {east+=1;}
				}
			}else if(north == south) {
				if(east > west) {
					//Units are to the East - Move West
					int newYco = uLocationY; if(newYco < 0) {newYco=0;}
					int newXco = uLocationX-2; if(newXco < 0) {newXco=0;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {west+=1;}
				}else if (east == west){
					//Do nothing - A stopping case in case all other tiles are occupied, or the units surround us and moving is not optimal. 
					return uLocation;
				}else {
					// Units are to the West - Move East
					int newYco = uLocationY; if(newYco < 0) {newYco=0;}
					int newXco = uLocationX+2; if(newXco > 8) {newXco=8;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {east+=1;}
				}
			}else {
				if(east > west) {
					// Units are to the South-East - Move South-West
					int newYco = uLocationY-1; if(newYco < 0) {newYco=0;}
					int newXco = uLocationX-1; if(newXco < 0) {newXco=0;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {west+=1;}
				}else if (east == west){
					// Units are to the South - Move North
					int newYco = uLocationY-2; if(newYco <0 ) {newYco=0;}
					int newXco = uLocationX; if(newXco < 0) {newXco=0;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {north+=1;}
				}else {
					// Units are to the South-West - Move South-East
					int newYco = uLocationY-1; if(newYco < 0) {newYco=0;}
					int newXco = uLocationX+1; if(newXco > 8) {newXco=8;}
					furthestTile = board.getTile(newXco, newYco);
					if(furthestTile.hasUnit()) {east+=1;}
				}
			}
		}
		return furthestTile;
		
}
	
}

