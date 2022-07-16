package structures;

import commands.BasicCommands;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Unit;
import akka.actor.ActorRef;

/**
 * This class can be used to hold information about the on-going game. Its
 * created with the GameActor.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class GameState {

	public boolean gameInitialised = false;  
	private Player humanPlayer;
	private Player AIPlayer;	
	private Board board;
	private static int nextId = 0;
	private ActorRef out;
	private int cardPosition;
	private boolean UnitMoving = false;
	
	// state related to turn control
	private Player currentPlayer;
	private boolean UIactive = true;
	
	// state used to help interpret card and tile clicks
	private boolean cardSelected = false;
	private boolean unitSelected = false;
	private Card clickedCard;
	private Unit clickedUnit;
	private boolean gameOver = false;
	
	
	public GameState() {
		gameInitialised = true;
	}

	
	public void setBoard(Board board) {
		this.board = board;
	}

	
	public Board getBoard() {
		return board;
	}

	
	public static int getNextID() {
		int result = nextId;
		nextId++;
		return result;
	}

	

	public void setHumanPlayer(Player player) {
		this.humanPlayer = player;
		player.setGameState(this);
	}

	
	public Player getHumanPlayer() {
		return humanPlayer;
	}

	
	public void setAIPlayer(Player player) {
		this.AIPlayer = player;
		player.setGameState(this);
	}

	
	public Player getAIPlayer() {
		return AIPlayer;
	}

	
	public void setCardSelected(boolean b) {
		this.cardSelected = b;
	}

	
	public boolean isCardSelected() {
		return cardSelected;
	}

	
	public void setClickedCard(Card c) {
		this.clickedCard = c;
	}

	
	public Card getClickedCard() {
		return clickedCard;
	}


	public int getCardPosition() {
		return cardPosition;
	}


	public void setCardPosition(int cardPosition) {
		this.cardPosition = cardPosition;
	}

	
	public Boolean isUnitSelected() {
		return unitSelected;
	}

	
	public void setUnitSelected(boolean b) {
		unitSelected = b;
	}

	
	public void setClickedUnit(Unit unit) {
		this.clickedUnit = unit;
	}

	
	public Unit getClickedUnit() {
		return clickedUnit;
	}

	
	public ActorRef getOut() {
		return out;
	}

	
	public void setOut(ActorRef out) {
		this.out = out;
	}
	
	
	public void endGame(ActorRef out, Player losingPlayer) {
		// winning player displayed
		if (losingPlayer.equals(AIPlayer)) {
			BasicCommands.addPlayer1Notification(out, "Game Over! You won the game!", 10);
		} else {
			BasicCommands.addPlayer1Notification(out, "Game Over! You lost the game!", 10);
		}
		// deactivate UI
		gameOver = true;
	}

	
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	
	public void setCurrentPlayer(Player currentPlayer) {
		this.currentPlayer = currentPlayer;
	}

	
	public void endTurn() {
		// reset turn gamestate
		setUnitSelected(false);
		setClickedUnit(null);
		setCardSelected(false);
		setClickedCard(null);
		getBoard().highlightAll(0);
		
		// transfer control to other player
		currentPlayer.endTurn();
		currentPlayer = currentPlayer.equals(humanPlayer) ? AIPlayer : humanPlayer;
		currentPlayer.startTurn();
	}

	
	public Player getOtherPlayer() {
		return currentPlayer.equals(humanPlayer) ? AIPlayer : humanPlayer;
	}
	
	
	public void deactivateUI() {
		this.UIactive = false;
	}
	
	
	public void activateUI() {
		this.UIactive = true;
	}
	
	
	public boolean isUiClickable() {
		return UIactive;
	}

	
	public boolean isUnitMoving() {
		return UnitMoving;
	}

	
	public void setUnitMoving(boolean unitMoving) {
		UnitMoving = unitMoving;
	}

	
	public boolean isGameOver() {
		return gameOver;
	}
}
