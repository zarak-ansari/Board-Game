package events;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Board;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;


/**
 * Indicates that the user has clicked an object on the game canvas, in this
 * case a tile. The event returns the x (horizontal) and y (vertical) indices of
 * the tile that was clicked. Tile indices start at 1.
 * 
 * Based on gamestate, the click is interpretted either as a request to highlight the 
 * tiles the unit on the clicked tile can attack or move to, or as a request to move to
 * or attack the clicked tile if a unit is already selected.
 * 
 * { messageType = “tileClicked” tilex = <x index of the tile> tiley = <y index
 * of the tile> }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class TileClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		if (gameState.isGameOver()) {
			return;
		} 
		else if (gameState.isUiClickable() || message.hasNonNull("AI")) {
		
			// get information from gameState
			Board b = gameState.getBoard();
			Player currentPlayer = gameState.getCurrentPlayer();

			// extract information from message
			int tilex = message.get("tilex").asInt();
			int tiley = message.get("tiley").asInt();
			Tile tile = b.getTile(tilex, tiley);
	
			// check flags in gameState to help decide what action to take
			Boolean cardSelected = gameState.isCardSelected();
	
			// a card is currently selected therefore click is interpreted as spell cast or unit summon
			if (cardSelected) {
				Card card = gameState.getClickedCard();
				Card.playCard(out, gameState, card, currentPlayer, tile);
				b.highlightAll(0);
			}
			// no card currently selected so either move/attack highlighting or triggering move/attack
			else {
				Boolean unitSelected = gameState.isUnitSelected();
			
				// no unit selected in gameState so click interpreted as request for highlighting
				if (!unitSelected) {
					if (tile.hasUnit()) {
						Unit unit = tile.getUnit();
						// highlight possible actions if a player has clicked on their own unit, respecting its number of moves/attacks remaining this turn
						if (unit.getPlayer().equals(currentPlayer)) {

							ArrayList<Tile> provokingTiles = b.getProvokingUnitTiles(tile, currentPlayer);

							if (unit.getAttacksRemaining() > 0) {
							
								//if there is a provoking unit only highlight those tiles
								if(provokingTiles.size()>0){
									b.highLightProvokingUnits(provokingTiles);
								}else if(unit.getAbilities().contains("ranged")) {
									b.highlightRangedUnits(currentPlayer, 2);
								}else {
									b.highlightAttacks(currentPlayer, tile);
									if ((unit.getAttacksRemaining() > 0 && unit.getAttacksPerTurn() == 2) || (unit.getAttacksRemaining() > 0 && unit.getMovesRemaining() > 0)) {
										b.highlightMoveAndAttacks(currentPlayer, tile);
									}
								}
							}
							if (unit.getMovesRemaining() > 0 && provokingTiles.size()==0) {
								b.highlightMoves(currentPlayer, tile);
								if (unit.getAbilities().contains("flying")) {
									b.highlightFlyingTiles(currentPlayer, 1);
								}
							}
						}
						// set in gameState the unit which has been selected
						gameState.setUnitSelected(true);
						gameState.setClickedUnit(unit);
					}
				}
				// a unit is already selected so click should trigger an actual move, attack, or move + attack event
				else {
					Unit unit = gameState.getClickedUnit();
					unit.performAction(out, tile, gameState);
			
					// dehighlight board and reset relevant gamestate information
					b.highlightAll(0);
					gameState.setUnitSelected(false);
					gameState.setClickedUnit(null);
				}
			}

			// after any actions complete reset gamestate so that no card selected and no unit selected
			gameState.setCardSelected(false);
			gameState.setClickedCard(null);
			}
		}
}
