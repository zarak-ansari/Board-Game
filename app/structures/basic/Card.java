package structures.basic;

import java.util.ArrayList;
import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import utils.BasicObjectBuilders;
import utils.CardNameToUnit;
import utils.CardNameToEffectAnimation;
import utils.StaticConfFiles;

/**
 * This is the base representation of a Card which is rendered in the player's hand.
 * A card has an id, a name (cardname) and a manacost. A card then has a large and mini
 * version. The mini version is what is rendered at the bottom of the screen. The big
 * version is what is rendered when the player clicks on a card in their hand.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Card {
	
	private int id;
	
	private String cardname;
	private int manacost;
	
	private MiniCard miniCard;
	private BigCard bigCard;
	
	public Card() {};
	
	
	public Card(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard) {
		super();
		this.id = id;
		this.cardname = cardname;
		this.manacost = manacost;
		this.miniCard = miniCard;
		this.bigCard = bigCard;
	}
	
	
	public int getId() {
		return id;
	}
	
	
	public void setId(int id) {
		this.id = id;
	}
	
	
	public String getCardname() {
		return cardname;
	}
	
	
	public void setCardname(String cardname) {
		this.cardname = cardname;
	}
	
	
	public int getManacost() {
		return manacost;
	}
	
	
	public void setManacost(int manacost) {
		this.manacost = manacost;
	}
	
	
	public MiniCard getMiniCard() {
		return miniCard;
	}
	
	
	public void setMiniCard(MiniCard miniCard) {
		this.miniCard = miniCard;
	}
	
	
	public BigCard getBigCard() {
		return bigCard;
	}
	
	
	public void setBigCard(BigCard bigCard) {
		this.bigCard = bigCard;
	}
	
	
	public boolean isUnitCard() {
		if(this.bigCard.getAttack()==-1) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public String toString() {
		return cardname;
	}
	
	/**
	 * play a card if player has sufficient mana, remove card from hand and decrement mana
	 * 
	 * @param out - reference to object responsible for updating UI
	 * @param gameState - current gamestate
	 * @param card - the card to play
	 * @param currentPlayer - the player who is playing the card
	 * @param tile - the tile to play the card on
	 */
	public static void playCard(ActorRef out, GameState gameState, Card card, Player currentPlayer, Tile tile) {
		
		int validHighlight = 2; // card.isUnitCard() ? 1 : 2;
		
		// if valid tile is not selected
		if(tile.getHighlighted() == 0) {
			BasicCommands.addPlayer1Notification(out, "Invalid Tile", 2);
			BasicCommands.drawCard(out, card, gameState.getCardPosition(), 0);
			gameState.setCardSelected(false);
			gameState.setClickedCard(null);			
		} 
		
		// display a notification if not enough mana to play card
		else if (currentPlayer.getMana() < card.getManacost()) {
			BasicCommands.addPlayer1Notification(out, "Not Enough Mana", 2);
			BasicCommands.drawCard(out, card, gameState.getCardPosition(), 0);
			gameState.setCardSelected(false);
			gameState.setClickedCard(null);
		} else {
			// attempt to play the card

			if (card.isUnitCard()) {
				// summon unit
				if (!tile.hasUnit()) {
					
					currentPlayer.setMana(currentPlayer.getMana()-card.getManacost());
		 			currentPlayer.cardUsed(gameState.getCardPosition());
		 			
		 			playCardAnimation(out, card, tile);
					
					summonUnit(out, gameState, card, currentPlayer, tile);
				} else {
					System.err.println("Error: Attempted to summon unit on an already occupied tile.");
				}
			} 
			
			else { 
				// cast spell
				boolean spellCast = castSpell(out, card, tile);
				
				if(spellCast) {
					currentPlayer.setMana(currentPlayer.getMana()-card.getManacost());
		 			currentPlayer.cardUsed(gameState.getCardPosition());
		 			
		 			playCardAnimation(out, card, tile);
					spellCastEffects(out, gameState, currentPlayer);
				}
			}
		}
		
	}
	
	
	/**
	 * Summon a unit to the board
	 * 
	 * @param out - reference to object responsible for updating UI
	 * @param gameState - current gamestate
	 * @param card - the card of the unit being summoned
	 * @param currentPlayer - the player who is playing the card
	 * @param tile - the tile to play the card on
	 */
	public static void summonUnit(ActorRef out, GameState gameState, Card card, Player currentPlayer, Tile tile) {
		Board board = gameState.getBoard();
		
		// summon the unit
		Unit unit = BasicObjectBuilders.loadUnit(CardNameToUnit.map.get(card.getCardname()), card.getId(), Unit.class);
		unit.setPositionByTile(tile); 
		unit.setPlayer(currentPlayer);
		BasicCommands.drawUnit(out, unit, tile);
		try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
		unit.setUnitAttack(out, card.getBigCard().getAttack());
		unit.setUnitHealth(out, card.getBigCard().getHealth());
		// 2 attacks per turn for serpenti/azurite lion, otherwise 1
		int attacksPerTurn = (unit.getAbilities().contains("attackTwice")) ? 2 : 1;	
		unit.setAttacksPerTurn(attacksPerTurn);
	
		// make sure unit can't move or attack on the turn they were summoned
		unit.setAttacksRemaining(0);
		unit.setMovesRemaining(0);
			
		// place unit on board
		board.addUnit(unit, tile.getTilex(), tile.getTiley());
			
		// perform On-Summon Effects
		if(unit.getAbilities().contains("healAvatarOnSummon")) {
			// Azure Herald - +3 Health to Avatar
			int newHealth = currentPlayer.getAvatar().getUnitHealth() + 3;
			if(newHealth > 20) {newHealth = 20;}
			currentPlayer.getAvatar().setUnitHealth(out, newHealth);
			try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}					
		}
		
		if(unit.getAbilities().contains("drawCardOnSummon")) {
			//Blaze Hound - Both Players draw a card
			gameState.getHumanPlayer().drawCard();
			gameState.getAIPlayer().drawCard();
		}
	}
	
	
	/**
	 * Cast a spell on the board
	 * 
	 * @param out - reference to object responsible for updating UI
	 * @param gameState - current gamestate
	 * @param card - the card of the spell being summoned
	 * @param tile - the tile to play the card on
	 */
	public static boolean castSpell(ActorRef out, Card card, Tile tile) {
		Unit target = tile.getUnit();
		String cardName = card.getCardname();
		
		if(cardName.equals("Truestrike")) {
			target.setUnitHealth(out, target.getUnitHealth()-2);
			return true;
		} else if(cardName.equals("Entropic Decay")) {
			target.setUnitHealth(out, 0);
			return true;
		} else if(cardName.equals("Staff of Y'Kir'")) {
			target.setUnitAttack(out, target.getUnitAttack()+2);
			return true;
		} else if(cardName.equals("Sundrop Elixir")) {
			// increase health by 5 unless this would exceed starting health of unit
			int desiredHealth = Math.min(target.getStartingHealth(), target.getUnitHealth()+5); 
			target.setUnitHealth(out, desiredHealth);
			return true;
		} else {
			System.err.println(cardName + " is not defined as a spell.");
			return false;
		}
	}
	
	
	/**
	 * Trigger spell cast effects
	 * 
	 * @param out - reference to object responsible for updating UI
	 * @param gameState - current gamestate
	 * @param card - the card of the spell being player
	 * @param currentPlayer - the player who is playing the card
	 * @param tile - the tile to play the card on
	 */
	public static void spellCastEffects(ActorRef out, GameState gameState, Player currentPlayer) {
		
		// loop over the units currently on the board
		ArrayList<Unit> units = gameState.getBoard().getUnits();
		for(Unit u: units) {
			
			// if unit belongs to enemy and it has spellCastEffect ability then increase it's health and attack by one
			if(u.getAbilities().contains("spellCastEffect") && u.getPlayer()!=currentPlayer) {
				u.setUnitAttack(out, u.getUnitAttack()+1);
				u.setUnitHealth(out, u.getUnitHealth()+1);
			}
		}
	}
	
	
	/**
	 * Plays the animation for a given card
	 * 
	 * @param out - reference to object responsible for updating UI
	 * @param card - the card being played
	 * @param tile - the tile the card is being played on
	 */
	public static void playCardAnimation(ActorRef out, Card card, Tile tile) {
		
		String cardName = card.getCardname();
		
		// load the relevant animation
		String effectFile;
		if(card.isUnitCard()) {
			// get generic summon animation
			effectFile = StaticConfFiles.f1_summon;			
		} else {
			// gets specific effect file for the spell from the map
			effectFile = CardNameToEffectAnimation.map.get(cardName);
		}
		
		// display animation
		EffectAnimation ef = BasicObjectBuilders.loadEffect(effectFile); //building effect animation
		BasicCommands.playEffectAnimation(out, ef, tile); //playing animation
	}
	
	
	
}
