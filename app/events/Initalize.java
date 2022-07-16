package events;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Deck;
import structures.basic.Player;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;


/**
 * Indicates that both the core game loop in the browser is starting, meaning
 * that it is ready to recieve commands from the back-end.
 * 
 * Also responsible for creating the gameboard, both players and their avatars,
 * as well as loading each player's deck object and then drawing three cards for each player.
 * Also sets various starting gameState. 
 * 
 * { 
 *   messageType = “initalize”
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Initalize implements EventProcessor{

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		
		// create board
		Board board = new Board(out, 9, 5);
		gameState.setBoard(board);
		
		// create players and avatars
		Player player1 = new Player(out, StaticConfFiles.humanAvatar, board.getTile(1,2), true);
		Player player2 = new Player(out, StaticConfFiles.aiAvatar, board.getTile(7,2), false);
		board.addUnit(player1.getAvatar(), 1, 2);
		board.addUnit(player2.getAvatar(), 7, 2);
		player1.getAvatar().setPlayer(player1);
		player2.getAvatar().setPlayer(player2);
		
		// build decks in correct order
		ArrayList<Card> cardsPlayer1 = (ArrayList<Card>) OrderedCardLoader.getPlayer1Cards();
		ArrayList<Card> cardsPlayer2 = (ArrayList<Card>) OrderedCardLoader.getPlayer2Cards();
		new Deck(player1, cardsPlayer1);
		new Deck(player2, cardsPlayer2);
		
		// set important game state
		gameState.setHumanPlayer(player1);
		gameState.setAIPlayer(player2);
		gameState.setCurrentPlayer(player1);
		gameState.setOut(out);
		
		// draw three initial cards for each player
		for (int i = 0; i<3; i++) {
			player1.drawCard();
			player2.drawCard();
		}
		
		// begin first turn
		player1.startTurn();
	}
}