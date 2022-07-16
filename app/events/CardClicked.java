package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Player;

/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case a card. The event returns the position in the player's hand the card
 * resides within.
 * 
 * { messageType = “cardClicked” position = <hand index position [1-6]> }
 * 
 * Additional logic interprets the card click in terms of current gamestate and
 * if appropriate, highlights the tiles upon which the clicked card can be played.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class CardClicked implements EventProcessor {
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		if (gameState.isGameOver()) {
			return;
		} 
		else if (gameState.isUiClickable() || message.hasNonNull("AI")) {		
		
			// establish identity of clicked card
			int handPosition = message.get("position").asInt();
			Player player = gameState.getCurrentPlayer();
			Player enemy = gameState.getOtherPlayer();
			Card clickedCard = player.getCard(handPosition - 1);
			String cardName = clickedCard.getCardname();
			Board board = gameState.getBoard();
			
			// if a different card was previously selected 
			if(gameState.isCardSelected() && gameState.getClickedCard()!=clickedCard) {
				// de-highlight previously selected card
				BasicCommands.drawCard(out, gameState.getClickedCard(), gameState.getCardPosition(), 0);
				// de-highlight previously highlighted tiles
				board.highlightAll(0);
			}
			
			// handle case where a unit has been clicked, but no action taken immediately prior to card being clicked
			if (gameState.isUnitSelected()) {
				gameState.setUnitSelected(false);
				gameState.setClickedUnit(null);
				board.highlightAll(0);
			}
			
			// update gameState so that future events can be interpreted
			gameState.setCardSelected(true);
			gameState.setClickedCard(clickedCard);
			gameState.setCardPosition(handPosition);
			
			// highlight the selected card on the UI
			BasicCommands.drawCard(out, clickedCard, handPosition, 1);
		
			// highlight where spell cards can be played and unit cards summoned		
			if (cardName.equals("Truestrike")) {
				// truestrike
				board.highlightUnits(enemy, true, 2);
			} else if (cardName.equals("Sundrop Elixir")) {
				// sundrop elixir
				board.highlightUnits(player, true, 2);
			} else if (cardName.equals("Staff of Y'Kir'")) {
				// staff of y'kir
				board.highlightAvatar(player, 2);
			} else if (cardName.equals("Entropic Decay")) {
				// entropic decay
				board.highlightUnits(enemy, false, 2);
			} else if (cardName.equals("Ironcliff Guardian") || cardName.equals("Planar Scout") ) {
				// ironcliff guardian or planar scout - can be summoned anywhere
				board.highlightEmpty(2);
			} else {
				// other units - can only be summoned to tile adjacent to a friendly unit
				board.highlightSummonLocations(player, 2);
			}
		}
	}

}
