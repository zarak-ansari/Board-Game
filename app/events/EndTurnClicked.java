package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;

/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case the end-turn button, triggering logic in gameState to transfer control to
 * the next player.
 * 
 * { messageType = â€œendTurnClickedâ€� }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class EndTurnClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		if (gameState.isGameOver()) {
			return;
		} 
		// do not allow user to click 'End Turn' when it is the AI's turn
		else if (gameState.isUiClickable() || message.hasNonNull("AI")) {
			gameState.endTurn();
		}
	}

}
