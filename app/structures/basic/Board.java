package structures.basic;

import java.util.ArrayList;
import akka.actor.ActorRef;
import commands.BasicCommands;
import utils.BasicObjectBuilders;

/**
 * Representation of the gameboard as a two dimensional array of Tile objects. Contains a list of units currently present on the board,
 * as well as methods to add units to or remove units from the gameboard. Contains the logic for establishing the valid tiles a unit
 * can move to or attack, as well as a functions tiles to indicate these actions to the user.
 */

public class Board {
	
	private Tile[][] tiles;
	ArrayList<Unit> units;
	private int width;
	private int height;
	ActorRef out;

	public Board(ActorRef out, int width, int height) {
		this.tiles = new Tile[width][height];
		this.out = out;
		this.width = width;
		this.height = height;

		// create gameboard of specified width and height
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				this.tiles[i][j] = BasicObjectBuilders.loadTile(i, j);
				this.tiles[i][j].setBoard(this);
				BasicCommands.drawTile(out, this.tiles[i][j], 0);
			}
		}
		this.units = new ArrayList<Unit>();
	}

	
	public int getWidth() {
		return width;
	}

	
	public int getHeight() {
		return height;
	}

	
	public Tile getTile(int xPos, int yPos) {
		return this.tiles[xPos][yPos];
	}
	
	
	public ArrayList<Unit> getUnits() {
		return this.units;
	}
	

	/**
	 * Add a new unit to the Board
	 * 
	 * @param unit - the unit to be added to the board
	 * @param tilex - x coordinate (0 indexed) of desired unit position
	 * @param tiley - y coordinate (0 indexed) of desired unit position
	 */
	public void addUnit(Unit unit, int tilex, int tiley) {
		this.units.add(unit);
		tiles[tilex][tiley].setUnit(unit);
	}

	/**
	 * Remove a new unit from the Board
	 *
	 * @param unit - the unit to be removed from the board
	 * @param tilex - x coordinate (0 indexed) of unit
	 * @param tiley - y coordinate (0 indexed) of unit
	 */
	public void removeUnit(Unit unit, int tilex, int tiley) {
		this.units.remove(unit);
		tiles[tilex][tiley].removeUnit();
		BasicCommands.deleteUnit(out, unit);
	}


	/**
	 * Highlight a given tile in a given mode
	 * 
	 * @param x - horizontal position of tile (zero-indexed)
	 * @param y - vertical position of tile (zero-indexed)
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightTile(int x, int y, int mode) {
		BasicCommands.drawTile(out, this.tiles[x][y], mode);
		tiles[x][y].setHighlighted(mode);

		// delay to avoid causing buffer overflow by sending too many requests to the front end at once
		try {Thread.sleep(10);} catch (InterruptedException e) {}
	}


	/**
	 * Highlight all tiles on the board e.g. to unhighlight whole board call with
	 * mode = 0
	 * 
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightAll(int mode) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				highlightTile(i, j, mode);
			}
		}
	}

	
	/**
	 * Highlight player's units on the board
	 * 
	 * @param player - the player whose units should be highlighted
	 * @param includeAvatar - toggle whether player's avatar is highlighted
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightUnits(Player player, boolean includeAvatar, int mode) {
		for (Unit unit : units) {
			if (unit.getPlayer().equals(player)) {

				Position position = unit.getPosition();
				int x = position.getTilex();
				int y = position.getTiley();

				// highlight tile of unit, ensuring that avatar is not highlighted if called with includeAvatar=false
				if (unit.equals(player.getAvatar()) && !includeAvatar) {
					continue;
				}
				highlightTile(x, y, mode);
			}
		}
	}
	
	
	/**
	 * Highlight the tiles that flying units can move to (i.e. any unoccupied tile)
	 * 
	 * @param player - the player who the flying unit belongs to
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightFlyingTiles(Player player, int mode) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (!tiles[i][j].hasUnit()) {
					highlightTile(i, j, mode);
				}
			}
		}
	}

	
	/*
	 * Highlights the units that can be attacked by ranged units (i.e. every unit
	 * on the board that is not a friendly unit)
	 * 
	 * @param player - the player who the ranged unit belongs to
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightRangedUnits(Player player, int mode) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (tiles[i][j].hasUnit()) {
					Unit unit = tiles[i][j].getUnit();
					if (unit.getPlayer() != player) {
						highlightTile(i, j, mode);
					}
				}
			}
		}
	}

	
	/**
	 * Highlights the avatar belonging to player
	 * 
	 * @param player - the player whose avatar is to be highlighted
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightAvatar(Player player, int mode) {
		for (Unit unit : units) {
			if (unit.equals(player.getAvatar())) {
				Position position = unit.getPosition();
				int x = position.getTilex();
				int y = position.getTiley();
				highlightTile(x, y, mode);
			}
		}

	}

	
	/**
	 * Highlight all tiles not containing a unit
	 * 
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightEmpty(int mode) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (!tiles[i][j].hasUnit()) {
					highlightTile(i, j, mode);
				}
			}
		}
	}

	
	/**
	 * Highlight tiles which normal units can be summoned to for a player
	 * 
	 * @param player - the player who is summoning the unit
	 * @param mode - mode to highlight (0 - normal; 1 - white; 2 - red)
	 */
	public void highlightSummonLocations(Player player, int mode) {
		for (Unit unit : units) {
			if (unit.getPlayer().equals(player)) {

				// get position of unit
				Position position = unit.getPosition();
				int horizontalPos = position.getTilex();
				int verticalPos = position.getTiley();

				// highlight adjacent tiles if empty
				for (int x = horizontalPos - 1; x <= horizontalPos + 1; x++) {
					for (int y = verticalPos - 1; y <= verticalPos + 1; y++) {
						if (!(x == horizontalPos && y == verticalPos)
								&& (x >= 0 && y >= 0 && x < width && y < height)) {
							if (!tiles[x][y].hasUnit()) {
								highlightTile(x, y, mode);
							}
						}
					}
				}
			}
		}
	}

	
	/**
	 * Highlight tiles that a unit can attack without moving
	 * 
	 * @param player - the player whose unit's tile we have selected
	 * @param tile   - the tile of the unit we are highlighting the valid attacks for
	 */
	public void highlightAttacks(Player player, Tile tile) {

		Unit unit = tile.getUnit();

		int horizontalPos = tile.getTilex();
		int verticalPos = tile.getTiley();

		// highlight adjacent tiles if they contain an enemy unit
		for (int x = horizontalPos - 1; x <= horizontalPos + 1; x++) {
			for (int y = verticalPos - 1; y <= verticalPos + 1; y++) {
				if (!(x == horizontalPos && y == verticalPos) && (x >= 0 && y >= 0 && x < width && y < height)) {
					if (tiles[x][y].hasUnit() && !tiles[x][y].getUnit().getPlayer().equals(player)) {
						highlightTile(x, y, 2);
					}
				}
			}
		}
	}

	
	/**
	 * Highlight tiles that the unit can attack after moving
	 * 
	 * @param player - the player whose unit's tile we have selected
	 * @param tile   - the tile of the unit we are highlighting the valid attacks for
	 */
	public void highlightMoveAndAttacks(Player player, Tile tile) {

		Unit unit = tile.getUnit();

		int horizontalPos = tile.getTilex();
		int verticalPos = tile.getTiley();

		// highlight tiles which can be attacked by performing a 'move & attack'
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if (!(x == horizontalPos && y == verticalPos)) {
					Tile t = getTile(x, y);
					// if we can reach a tile in two steps or fewer from the current position
					if (canBeReached(player, unit, tile, t, 2)) {
						
						// then highlight any adjacent tile with an enemy unit
						for (int i = x - 1; i <= x + 1; i++) {
							for (int j = y - 1; j <= y + 1; j++) {
								if (i >= 0 && j >= 0 && i < width && j < height) {

									// only highlight tiles with enemy units
									if (tiles[i][j].hasUnit() && !tiles[i][j].getUnit().getPlayer().equals(player)) {
										highlightTile(i, j, 2);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	
	/**
	 * Helper method to ascertain if a unit on a given origin tile can reach a particular destination 
	 * tile in a certain number of horizontal/vertical steps. Returns true if the unit on origin tile can reach destination tile in a
	 * certain number of horizontal/vertical steps
	 * 
	 * @param player - the player of the unit on the initial origin tile
	 * @param unit - the unit on the initial origin tile
	 * @param origin - tile unit we are starting from
	 * @param destination - tile we want to end up on
	 * @param moves - parameter controlling number of steps a unit can make
	 * 
	 * @return boolean - true is the unit on origin can reach destination in 'moves' or fewer steps
	 */
	public boolean canBeReached(Player player, Unit unit, Tile origin, Tile destination, int moves) {

		// maximum steps exceeded
		if (moves < 0) {
			return false;
		}

		// get origin and dest positions
		int originX = origin.getTilex();
		int originY = origin.getTiley();

		int destX = destination.getTilex();
		int destY = destination.getTiley();

		// if origin == dest and we have made at least one step then it means we can move to dest from origin
		if (moves != 2) {
			if (originX == destX && originY == destY) {
				if (!origin.hasUnit()) {
					return true;
				}
			}
		}

		// step up / down / left / right, decrement moves, and make recursive call
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				// don't include diagonal steps
				if (Math.abs(j) != Math.abs(i)) {
					// make sure step would not go off edge of board
					if (originX + i >= 0 && originY + j >= 0 && originX + i < width && originY + j < height) {

						// tile we have stepped to
						Tile newOrigin = getTile(originX + i, originY + j);

						// if the tile we have stepped to has a unit then we can only step through it if it belongs to player
						if (newOrigin.hasUnit()) {
							if (newOrigin.getUnit().getPlayer().equals(player) && !newOrigin.getUnit().equals(unit)) {
								if (canBeReached(player, unit, newOrigin, destination, moves - 1)) {
									return true;
								}
							}
						}
						// tile doesn't have a unit so we can step through it
						else {
							if (canBeReached(player, unit, newOrigin, destination, moves - 1)) {
								return true;
							}
						}
					}
				}
			}
		}
		// exhausted possible routes to reach destination without finding a valid route
		return false;
	}

	
	/**
	 * Highlights the valid moves for a normal unit
	 * 
	 * @param player - the player a unit belongs to
	 * @param tile - the tile the unit is on
	 */
	public void highlightMoves(Player player, Tile tile) {
		Unit unit = tile.getUnit();

		int horizontalPos = tile.getTilex();
		int verticalPos = tile.getTiley();
		// for tiles within maximum moving range (two steps)
		for (int x = horizontalPos - 2; x <= horizontalPos + 2; x++) {
			for (int y = verticalPos - 2; y <= verticalPos + 2; y++) {
				// if unit is not already on tile, and tile within board limits
				if (!(x == horizontalPos && y == verticalPos) && (x >= 0 && y >= 0 && x < width && y < height)) {
					Tile t = getTile(x, y);
					// highlight the tile if tile it can be reached legally
					if (canBeReached(player, unit, tile, t, 2)) {
						highlightTile(x, y, 1);
					}
				}
			}
		}
	}
	
	
	/**
	 * Function to determine the tiles of units which are provoking the unit on a given tile
	 * 
	 * @param tile - the tile the unit is on
	 * @param currentPlayer - the player who the unit belongs to
	 * 
	 * @return ArrayList<Tile> - 
	 */
	 
	public ArrayList<Tile> getProvokingUnitTiles(Tile tile, Player player){
		ArrayList<Tile> result = new ArrayList<>();
		
		int Xpos = tile.getTilex();
		int Ypos = tile.getTiley();
				
		int[] offset = {-1,0,1};
		
		for(int i : offset) {
			for(int j : offset) {
				// ignore tile unit occupies
				if(!(i==0 && j==0)) {
					
					int targetX = Xpos+i; int targetY = Ypos+j;
					
					// check if target X and Y are within board
					if(targetX>=0 && targetY>=0 && targetX < this.width && targetY < this.height) { 
						Tile targetTile = tiles[targetX][targetY];
						// check if target tile has a unit (to avoid null pointer exceptions in next stage)
						if(targetTile.hasUnit()) {
							Unit adjacentUnit = targetTile.getUnit();
							// check if adjacent unit has provoke ability and belongs to enemy
							if(adjacentUnit.getAbilities().contains("provoke") && adjacentUnit.getPlayer()!=player) {
								result.add(targetTile);
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	
	/**
	 * Highlight each tile in an arraylist of tiles in mode 2
	 * 
	 * @param tiles - arraylist of tiles to be highlighted
	 */
	public void highLightProvokingUnits(ArrayList<Tile> tiles) {
		for(Tile t: tiles) {
			highlightTile(t.getTilex(),t.getTiley(),2);
		}
	}

	public ArrayList<Tile> getHighlightedTiles(int requiredMode) {
		ArrayList<Tile> result = new ArrayList<>();
		
		for(Tile[] row: tiles) {
			for(Tile t: row) {
				if(t.getHighlighted()==requiredMode) {
					result.add(t);
				}
			}
		}
		return result;
	}
}
