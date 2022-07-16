package structures.basic;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.node.ObjectNode;
import akka.actor.ActorRef;
import events.CardClicked;
import events.TileClicked;
import play.libs.Json;
import structures.GameState;
import structures.basic.Unit;

/**
 * This class contains logic for playing cards which is triggered at the start of each AI turn.
 * Spell cards are prioritised, then if there is enough mana left, unit cards can also be played.
 */
public class AILogicPlayCards {
	
	/**
	 * Main method for playing all cards in the AI player's hand.
	 * @param gameState - Main GameState object
	 */
	public static void playCards(GameState gameState) {
		
		Player AIPlayer = gameState.getAIPlayer();		
		ArrayList<Card> AICards = AIPlayer.getCards();
		ActorRef out = gameState.getOut();
		
		ArrayList<Card> playableCards = checkForMana(AICards, AIPlayer.getMana());
		

		// play one card per loop iteration until no playable cards remain
		// cards are removed from playable cards if player doesn't have enough mana for them or they are played
		int counter = 0; // Counter to avoid an infinite while loop
		while(playableCards.size() > 0 && counter<AICards.size()) {	
			// Attempt to play spell card. Returning true if played
			boolean spellPlayed = playSpellCard(out, gameState, playableCards);
			
			
			// If a spell is not played, summon a unit (if there are any cards left)
			if(!spellPlayed && playableCards.size()>0) {
				playSummonCard(out, gameState, playableCards);				
			}
			
			// Update playable cards after spell/ summon has been played to check for mana
			playableCards = checkForMana(playableCards, AIPlayer.getMana());
			counter++;
		}
	}
	
	/**
	 * Method for playing spell cards used in the main playcard method
	 * Plays Staff of Y'kir' as soon as possible	 
	 * Plays Entropic Decay if ranged unit is on the board or a unit with health above 4
	 * 
	 * @param out - ActorRef
	 * @param gameState - GameState object
	 * @param playableCards - An arraylist of cards 
	 */		
	private static boolean playSpellCard(ActorRef out, GameState gameState, ArrayList<Card> playableCards) {
		// if staff of y'kir is available, prioritize playing that
		// otherwise, play entropic decay
		ArrayList<Card> spellCards = filterSpellCards(playableCards);		
		Board board = gameState.getBoard();
		
		// logic for playing staff of y'kir : increasing avatar attack by 2
		for(Card card : spellCards) {
			if(!gameState.getAIPlayer().getCards().contains(card)) continue; // avoiding any errors if card doesn't exist in hand anymore
			
			if(card.getCardname().equals("Staff of Y'Kir'")) {
				clickOnCard(out, gameState, card);
				ArrayList<Tile> possibleTiles = board.getHighlightedTiles(2);
				if(possibleTiles.size()==1) {
					clickOnTile(out, gameState, possibleTiles.get(0));
					playableCards.remove(card);
					return true;
				} else {
					System.err.println("Error: Staff of Y'Kir highlighted "+possibleTiles.size()+" tiles.");
					playableCards.remove(card);
				}
			}
		}
		
		
		// logic for playing entropic decay: killing a non-avatar enemy unit
		String priorityAbility = "ranged"; // AI will try play card on units with this ability first
		int threshold = 5; // Otherwise, entropic decay will be played on units with health above (not equal to) this threshold
		
		for(Card card : spellCards) {
			if(!gameState.getAIPlayer().getCards().contains(card)) continue; // avoiding any errors if card doesn't exist in hand anymore

			if(card.getCardname().equals("Entropic Decay")) {
				
				clickOnCard(out, gameState, card);
				ArrayList<Tile> possibleTiles = board.getHighlightedTiles(2);
				
				// If no enemies have been highlighted, exit method returning false
				if(possibleTiles.size()==0) return false;
				
				// Otherwise look for tiles with priority ability
				Tile targetTile = getTileWithAbility(possibleTiles, priorityAbility);
				
				// If no enemies with priority ability, look for units above threshold health
				if(targetTile==null) {
					Tile highestEnemyHealthTile = getTileWithHighestEnemyHealth(possibleTiles);
					if(highestEnemyHealthTile.getUnit().getUnitHealth() > threshold) {
						targetTile = highestEnemyHealthTile;
					}
				}
				
				// If targetTile is not null, i.e. a suitable target has been found, play card on that unit
				if(targetTile!=null) {
					clickOnTile(out, gameState, targetTile);	
					return true;
				} else {
					return false;
				}
			}
		}
		
		// no cards played, exit method returning false 
		return false;
	}

	
	/**
	 * Method for playing summon cards used in the main playcard method
	 * Picks unit card with highest mana and summons it on the board
	 * If unit is ranged or flying, summons it as far away from enemy units as possible
	 * Otherwise, summons the unit on the closest possible tile to enemy avatar
	 * 
	 * @param out - ActorRef
	 * @param gameState - GameState object
	 * @param playableCards - An arraylist of cards 
	 */			
	private static void playSummonCard(ActorRef out, GameState gameState, ArrayList<Card> playableCards) {
		
		Card toBePlayed = getUnitCardWithHighestMana(playableCards);
		clickOnCard(out, gameState, toBePlayed);
		
		if(!gameState.getAIPlayer().getCards().contains(toBePlayed)) return; // avoiding any errors if card doesn't exist in hand anymore
		
		ArrayList<Tile> possibleTiles = gameState.getBoard().getHighlightedTiles(2);
		Tile targetTile = null; 
		
		// choosing targetTile
		if(possibleTiles.size()>0) {
			if(toBePlayed.getCardname().equals("WindShrike") || toBePlayed.getCardname().equals("Pyromancer")) {
				// Get furthest tile from average enemy position for ranged/flying units
				int[] avgXYPositionOfEnemyUnits = averageLocationOfUnits(getHumanUnits(gameState));
				Tile averageTileOfEnemyUnits = gameState.getBoard().getTile(avgXYPositionOfEnemyUnits[0], avgXYPositionOfEnemyUnits[1]);
				targetTile = getFurthestTile(possibleTiles, averageTileOfEnemyUnits);
				
			} else {
				// Get closest tile to avatar for all other units
				targetTile = getClosestTileToUnit(possibleTiles, gameState.getHumanPlayer().getAvatar());
			}
		}
		
		
		// if a targetTile was chosen, play the card, otherwise show an error on the console
		if(targetTile!=null) {
			clickOnTile(out, gameState, targetTile);
		} else {
			System.err.println("Error: couldn't pick target tile for " + toBePlayed.getCardname());
		}
		playableCards.remove(toBePlayed);
	}

	/**
	 * Helper method for checking which cards have enough mana.
	 * 
	 * @param availableCards - An arraylist of cards
	 */	
	private static ArrayList<Card> checkForMana(ArrayList<Card> availableCards, int playerMana){
		ArrayList<Card> result = new ArrayList<>();
		
		for(Card c: availableCards) {
			if(playerMana>=c.getManacost()) {
				result.add(c);
			}
		}
		
		return result;
	}

	/**
	 * Helper method for only returning spellcards out of a list of cards.
	 * 
	 * @param availableCards - An arraylist of cards
	 */	
	private static ArrayList<Card> filterSpellCards(ArrayList<Card> availableCards){
		ArrayList<Card> result = new ArrayList<>();
		for(Card c: availableCards) {
			if(!c.isUnitCard()) {
				result.add(c);
			}
		}
		return result;
	}
	
	/**
	 * Helper method for returning the card with highest mana out of a list of cards.
	 * Used for summoning units.
	 * 
	 * @param cards - An arraylist of cards
	 */		
	private static Card getUnitCardWithHighestMana(ArrayList<Card> cards) {
		Card resultCard = null;
		for(Card card : cards) {
			if(card.isUnitCard() && (resultCard==null || card.getManacost() > resultCard.getManacost())) {
				resultCard = card;
			}
		}
		return resultCard;
	}

	
	/**
	 * Helper method for playing entropic decay. 
	 * Returns enemy unit with the passed in ability.
	 * If multiple such units exist, return the one with highest health
	 * If no such units exists, returns null
	 * 
	 * @param playableTiles - An arraylist of tiles (pass in highlighted tiles)
	 * @param ability - String for ability
	 */			
	private static Tile getTileWithAbility(ArrayList<Tile> playableTiles, String ability) {
		
		ArrayList<Tile> tilesWithAbility = new ArrayList<>();
		
		for(Tile t: playableTiles) {
			if(t.hasUnit()) {
				if(t.getUnit().getAbilities().contains(ability)) {
					tilesWithAbility.add(t);
				}
			}
		}
		
		if(tilesWithAbility.size()==0) {
			return null;
		} else if(tilesWithAbility.size()==1) {
			return tilesWithAbility.get(0);
		} else {
			return getTileWithHighestEnemyHealth(tilesWithAbility);
		}
	}
	
	/**
	 * Helper method for getting the the unit with highest health in an arraylist of tiles
	 * Currently only used for entropic decay
	 * 
	 * @param playableTiles - An arraylist of tiles (pass in highlighted tiles)
	 */			
	private static Tile getTileWithHighestEnemyHealth(ArrayList<Tile> playableTiles) {
		
		Tile result = null;
		Integer maxHealth = null;
		
		for(Tile t: playableTiles) {
			int health = t.getUnit().getUnitHealth();
			if(result==null ||  health > maxHealth) {
				result = t;
				maxHealth = health; 
			}
		}
		return result;
	}

	/**
	 * Helper method for simulating clicks on cards on AI's behalf
	 * 
	 * @param out - ActorRef 
	 * @param gameState - Main GameState object
	 * @param Card c - Card object to be clicked on
	 */	
	private static void clickOnCard(ActorRef out, GameState gameState, Card c) {
		
		int handPosition = gameState.getAIPlayer().getCards().indexOf(c) + 1;
		CardClicked processor = new CardClicked();
		ObjectNode clickCardMessage = Json.newObject();
		clickCardMessage.put("messagetype", "cardclicked");
		clickCardMessage.put("position", handPosition);
		clickCardMessage.put("AI", "AI");
		processor.processEvent(out, gameState, clickCardMessage);
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}

	/**
	 * Helper method for simulating clicks on a tile on AI's behalf
	 * 
	 * @param out - ActorRef 
	 * @param gameState - Main GameState object
	 * @param Tile t - Tile object on the board to be clicked on
	 */	
	private static void clickOnTile(ActorRef out, GameState gameState, Tile t) {
		TileClicked processor = new TileClicked();
		ObjectNode tileClickMessage = Json.newObject();
		tileClickMessage.put("messagetype", "tileclicked");
		tileClickMessage.put("tilex", t.getTilex());
		tileClickMessage.put("tiley", t.getTiley());
		tileClickMessage.put("AI", "AI");
		processor.processEvent(out, gameState, tileClickMessage);
		try {Thread.sleep(500);} catch (InterruptedException e) {e.printStackTrace();}
	}

	/**
	 * Helper method for getting closest tile to a particular unit
	 * Currently used to summon cards closest to AI avatar
	 * Can be used if another unit is specifically being targeted
	 * 
	 * @param availableTiles - ArrayList of available Tile objects 
	 * @param target - Unit 
	 */	
	public static Tile getClosestTileToUnit(ArrayList<Tile> availableTiles, Unit target) {
		int targetX = target.getPosition().getTilex();
		int targetY = target.getPosition().getTiley();
		
		Tile result= null;
		Integer minDistance = null;
		
		for(Tile t: availableTiles) {
			int diffX = Math.abs(t.getTilex()-targetX);
			int diffY = Math.abs(t.getTiley()-targetY);
			int distance = diffX + diffY;
			
			if(minDistance==null || distance<minDistance) {
				minDistance = distance;
				result = t;
			}
		}
		return result;				
	}
	
	/**
	 * Helper method for getting furthest tile from a particular tile
	 * Currently used to summon cards flying and ranged cards
	 * which are summoned as far away from average enemy units as possible
	 * 
	 * @param availableTiles - ArrayList of available Tile objects 
	 * @param target - Unit 
	 */		
	private static Tile getFurthestTile(ArrayList<Tile> availableTiles, Tile target) {
		Tile result=null;
		int maxDistance = 0;
		int targetX = target.getTilex();
		int targetY = target.getTiley();

		
		for(Tile t: availableTiles) {
			int diffX = Math.abs(t.getTilex()-targetX);
			int diffY = Math.abs(t.getTiley()-targetY);
			int distance = diffX + diffY;
			
			if(result==null || distance > maxDistance) {
				result = t;
				maxDistance = distance;
			}
		}
		return result;
	}

	/**
	 * Helper method for getting average location of an ArrayList of units
	 * Returns an array of two ints:
	 * 	[0] Average X position of enemy units
	 * 	[1] Average Y position of enemy units
	 * 
	 * @param units - ArrayList of units
	 */
	private static int[] averageLocationOfUnits(ArrayList<Unit> units) {
		
		int sumX = 0;
		int sumY = 0;

		for(Unit u: units) {
			sumX+= u.getPosition().getTilex();
			sumY+= u.getPosition().getTiley();
		}
		
		int averageX = Math.round(sumX/units.size());
		int averageY = Math.round(sumY/units.size());
		
		int[] result = {averageX, averageY};
		return result;
	}

	/**
	 * Helper method for getting all human units 
	 * Returns an arraylist of unit objects
	 * 
	 * @param gameState - Main gameState object
	 */
	private static ArrayList<Unit> getHumanUnits(GameState gameState){
		ArrayList<Unit> result = new ArrayList<>();
		
		ArrayList<Unit> unitsOnBoard = gameState.getBoard().getUnits();
		Player humanPlayer = gameState.getHumanPlayer();
		
		for(Unit u: unitsOnBoard) {
			if(u.getPlayer()==humanPlayer) {
				result.add(u);
			}
		}
		return result;
	}

}
