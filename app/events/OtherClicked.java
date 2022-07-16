package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Board;

/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case somewhere that is not on a card tile or the end-turn button, this
 * causes all relevant gamestate to be reset and board highlighting to be 
 * returned to default.
 * 
 * { messageType = â€œotherClickedâ€� }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class OtherClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		if (gameState.isGameOver()) {
			return;
		} 
		else if (gameState.isUiClickable() || message.hasNonNull("AI")) {

			Board b = gameState.getBoard();

			// reset board highlighting
			b.highlightAll(0);

			// reset other gameState
			BasicCommands.drawCard(out, gameState.getClickedCard(), gameState.getCardPosition(), 0);
			gameState.setCardSelected(false);
			gameState.setClickedCard(null);
			gameState.setUnitSelected(false);
			gameState.setClickedUnit(null);
		}
		

	}

}
