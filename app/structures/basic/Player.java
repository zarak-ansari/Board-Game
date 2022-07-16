package structures.basic;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;

/**
 * A basic representation of of the Player. A player has health and mana.
 * 
 * A player also has an avatar and a set of cards currently contained in their 'hand'.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Player {

	private int health;
	private int mana;
	
	// ensures Jackson serialisation does not go into infinite recursion with Player->Unit->Player->...
	@JsonManagedReference
	private Unit avatar;
	
	private Deck deck;
	private Card[] cards = new Card[6];
	private int nextPosition = 0;
	private ActorRef out;
	private GameState gameState;
	private boolean human;
	private int turnNumber = 1;
	
	
	public Player() {
		super();
		this.health = 20;
		this.mana = 3;
	}

	
	public Player(int health, int mana) {
		super();
		this.health = health;
		this.mana = mana;
	}
	

	public Player(ActorRef out, String configFile, Tile startTile, boolean human) {
		super();
		this.out = out;
		this.human = human;
		this.health = 20;
		this.setMana(turnNumber + 1);
		
		// Make an avatar and place it on the board
		createAvatar(configFile, startTile);
	}
	
	/**
	 * Creates player's avatar using create avatar method in Unit
	 * shows it on the UI and sets initial attributes
	 */			
	public void createAvatar(String configFile, Tile startTile) {
		
		// ID for other units is based on Card ID. Setting id to -1 to human and -2 for AI avatars
		int id = human ? -1 : -2;
		Unit avatar = Unit.createAvatar(configFile, startTile,id);
		avatar.setPlayer(this);
		this.avatar = avatar;
		
		BasicCommands.drawUnit(out, getAvatar(), startTile);
		
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		avatar.setUnitHealth(out, health);
		avatar.setUnitAttack(out, 2);
	}

	
	public int getTurnNumber() {
		return turnNumber;
	}

	
	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	
	public int getHealth() {
		return health;
	}

	/**
	 * Sets player health and calls methods for on damage effect, 
	 * ending game and updating UI
	 */			
	public void setHealth(int newHealth) {
		// if player is being damaged
		if(this.health>newHealth) {onDamageEffects(gameState);}
		
		this.health = newHealth;
		
		if (this.health <1) {
			this.health = 0; // Ensure negative health isn't shown on screen
			gameState.endGame(out, this);
		}
		
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}

		// Update humanplayer or AI health on front end
		if(human) {
			BasicCommands.setPlayer1Health(out, this);
		} else {
			BasicCommands.setPlayer2Health(out, this);
		}
	}
	
	/**
	 * Method for completing additional actions when player avatar is damaged
	 * Silverguard knight's attack increases whenever it's player's avatar is attacked
	 */		
	public void onDamageEffects(GameState gameState) {
		ArrayList<Unit> units = gameState.getBoard().getUnits();
		for(Unit u : units) {
			// if unit belongs to current player and is a silverguard knight
			if(u.getPlayer()==this && u.getAbilities().contains("avatarDamageEffect")) {
				//add 2 attack each time avatar gets damaged
				u.setUnitAttack(out, u.getUnitAttack()+2);
			}
		}
	}

	
	public int getMana() {
		return mana;
	}

	
	public void setMana(int mana) {
		this.mana = mana;
		if(human) {
			BasicCommands.setPlayer1Mana(out, this);
		} else {
			BasicCommands.setPlayer2Mana(out, this);
		}
	}

	
	public Unit getAvatar() {
		return this.avatar;
	}

	
	public void setDeck(Deck deck) {
		this.deck = deck;
	}

	
	public Deck getDeck() {
		return deck;
	}

	/**
	 * Refreshes cards displayed on UI  
	 * Used at changes of turns or when a card is played/ drawn
	 */		
	public void refreshOnUI() {
		
		if(gameState.getCurrentPlayer()==this) {
			for (int i = 0; i<cards.length; i++) {
				BasicCommands.deleteCard(out, i+1);
				
				if(cards[i]!=null) {
					BasicCommands.drawCard(out, cards[i], i+1, 0);
					try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		}
	}
	
	/**
	 * Draws a card from player's deck and places in player's hand
	 * If player's hand is full, discards card
	 */	
	public void drawCard() {
		
		//if deck if empty then game ends
		if (deck.getDeckSize() == 0) { 
			gameState.endGame(out, this);
		}

		// if space in player's hand
		if (nextPosition < cards.length) {
			// draw next card from deck and put in player's hand
			Card nextCard = deck.drawCard();
			cards[nextPosition] = nextCard;
			nextPosition++;
			// display updated cards on UI
			refreshOnUI();
		} else {
			// player's hand is full so draw card from deck and discard it
			deck.drawCard();
		}
	}

	
	public Card getCard(int handPosition) {
		return cards[handPosition];
	}


	public int getPosition() {
		return nextPosition;
	}
	

	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}
	
	
	/**
	 * Remove a card from the player's hand once it has been removed
	 * 
	 * @param p - position of card to be removed
	 */
	public void cardUsed(int p) {

		for(int i = p-1; i< nextPosition-1; i++) {
			cards[i] = cards[i+1];
		}
		cards[nextPosition-1] = null;
		nextPosition--;

		refreshOnUI();	
	}
	
	
	/**
	 * Gets the position of a specific card
	 * 
	 * @param c - the card to find the position of
	 * @return position of card or -1 if card not present
	 */
	public int returnPositionOfCard(Card c) {
		for(int i = 0; i< nextPosition; i++) {
			if(cards[i].equals(c)) {
				return i;
			}
		}return -1;
	}

	/**
	* End's a player's turn, including drawing a card, reset mana, unit's attacks per turn and deactivating UI
	*/
	public void endTurn() {
		setMana(0);
		drawCard();
		turnNumber++;
		ArrayList<Unit> units = gameState.getBoard().getUnits();
		
		// reset move and attack counters for each unit belonging to player
		for (Unit unit : units) {
			if (unit.getPlayer().equals(this)) {
				unit.setAttacksPerTurn(unit.getAttacksPerTurn());
			}
		}
		gameState.deactivateUI();
	}
	
	/**
	* Starts the next turn, announcing change of turn, refreshing cards,
	* Also play's AI's turn or activates UI for human player
	*/
	public void startTurn() {
		setMana(turnNumber + 1);
		refreshOnUI();
		if (!human) {
			BasicCommands.addPlayer1Notification(out, "Computer's Turn", 2);
			AILogic.playTurn(gameState);
		} else {
			BasicCommands.addPlayer1Notification(out, "Player's Turn", 2);
			// UI should only be activated to accept user clicks if it is the user's turn
			gameState.activateUI();
		}
	}
	
	/**
	* Returns an arraylist of cards in player's hand
	*/
	public ArrayList<Card> getCards(){
		ArrayList<Card> result = new ArrayList<>();
		for(int i=0; i<cards.length; i++) {
			if(cards[i]!=null) {
				result.add(cards[i]);
			}
		}
		return result;
	}
	

	
	
	
	
	
	
	//Exclusively used for the tests - Allow us to add and remove cards without messing the nextPosition
	public void setPosition(int p) {
		nextPosition += p;
	}
	
	// for testing only, use drawCard() instead in game logic
	public void setCard(int handPosition, Card card) {
		cards[handPosition] = card;
	}

}