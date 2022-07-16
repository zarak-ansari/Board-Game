package structures.basic;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;
import utils.UnitAttributes;

/**
 * This is a representation of a Unit on the game board. A unit has a unique id
 * (this is used by the front-end. Each unit has a current UnitAnimationType,
 * e.g. move, or attack. The position is the physical position on the board.
 * UnitAnimationSet contains the underlying information about the animation
 * frames, while ImageCorrection has information for centering the unit on the
 * tile.
 * 
 * Also contains information relating to the state of the unit (health, attacks perTurn etc.).
 * Additionally, contains the logic for the Unit moving/attacking or move+attacking
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java  objects from a file

	/*
	 * ensures Jackson serialisation does not go into infinite recursion with
	 * Player->Unit->Player->...
	 */
	@JsonBackReference
	private Player player;

	private int id;
	private UnitAnimationType animation;
	private Position position;
	private UnitAnimationSet animations;
	private ImageCorrection correction;

	// information relating to current state of unit
	private int unitHealth;
	private int unitAttack;
	private int attacksRemaining;
	private int movesRemaining;
	
	// information relating to permanent state of unit
	private int attacksPerTurn;
	private int startingHealth = 0;
	private Tile tile;
	private boolean isAvatar;
	private ArrayList<String> abilities;

	public Unit() {}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
	}

	
	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;

		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(),
				currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
		this.tile = currentTile;
	}

	
	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
	}

	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	
	public UnitAnimationType getAnimation() {
		return animation;
	}

	
	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	
	public ImageCorrection getCorrection() {
		return correction;
	}

	
	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	
	public Position getPosition() {
		return position;
	}

	
	public void setPosition(Position position) {
		this.position = position;

	}

	
	public UnitAnimationSet getAnimations() {
		return animations;
	}

	
	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	
	/**
	 * This command sets the position of the Unit to a specified tile.
	 * 
	 * @param tile
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
	}

	
	public Player getPlayer() {
		return player;
	}

	
	public void setPlayer(Player player) {
		this.player = player;
	}

	
	public int getUnitHealth() {
		return unitHealth;
	}

	
	public void setUnitHealth(ActorRef out, int unitHealth) {
		// update unit's current health
		this.unitHealth = unitHealth;

		// if its the first time that the unit's health is being set, also set starting health
		if (this.startingHealth == 0) {
			this.startingHealth = unitHealth;
		}

		// if unit is an avatar then set player's health to the same value
		if (this.isAvatar) {
			this.player.setHealth(unitHealth);
		}

		// if unit has died
		if (this.unitHealth <= 0) {
			BasicCommands.playUnitAnimation(out, this, UnitAnimationType.death);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			BasicCommands.deleteUnit(out, this);
			tile.getBoard().removeUnit(this, tile.getTilex(), tile.getTiley());
			
			// when Windshrike dies, its player draws a card
			if 	(this.abilities.contains("drawCardOnDeath")) {
				player.drawCard();
			}
		}
		
		// update health on the UI
		BasicCommands.setUnitHealth(out, this, unitHealth);
	}

	
	public int getUnitAttack() {
		return unitAttack;
	}

	
	public void setUnitAttack(ActorRef out, int unitAttack) {
		this.unitAttack = unitAttack;

		// update attack on UI
		BasicCommands.setUnitAttack(out, this, unitAttack);
	}

	
	public int getAttacksRemaining() {
		return attacksRemaining;
	}

	
	public void setAttacksRemaining(int attacksRemaining) {
		this.attacksRemaining = attacksRemaining;
	}

	
	public void setAttacksPerTurn(int attacksPerTurn) {
		this.attacksPerTurn = attacksPerTurn;
		this.attacksRemaining = attacksPerTurn;
		// a unit can only ever move once per turn, even if it is able to attack twice
		this.movesRemaining = 1;
	}

	
	public int getMovesRemaining() {
		return movesRemaining;
	}
	
	
	public void setMovesRemaining(int movesRemaining) {
		this.movesRemaining = movesRemaining;
	}

	
	public int getAttacksPerTurn() {
		return attacksPerTurn;
	}

	
	public int getStartingHealth() {
		return this.startingHealth;
	}

	
	public void setTile(Tile t) {
		this.tile = t;
	}
	

	/**
	 * Interprets a click when this unit is selected as move, attack, or move + attack
	 * and performs that action`
	 * 
	 * @param out - a reference to the class responsible for updating the UI
	 * @param targetTile - the tile which has been clicked on
	 * @param gameState - the gameState of the current game
	 */
	public void performAction(ActorRef out, Tile targetTile, GameState gameState) {

		Tile startingTile = this.tile;

		if (targetTile.hasUnit()) {

			Unit targetUnit = targetTile.getUnit();

			// if tile is highlighted then it is a valid tile to attack or move + attack
			if (targetTile.getHighlighted() == 2) {

				// tile is adjacent, or unit is ranged, so perform an attack
				if (targetTile.isAdjacent(startingTile) || this.abilities.contains("ranged")) {
					if (attacksRemaining > 0) {
						attack(out, targetTile, targetUnit, gameState);
						attacksRemaining--;
						movesRemaining--;
					}
				} 
				// tile is non-adjacent so perform a move and attack
				else {
					if ((attacksRemaining > 0 && attacksPerTurn == 2) || (attacksRemaining > 0 && movesRemaining > 0)) {
						moveAndAttack(out, targetTile, targetUnit, gameState);
						movesRemaining--;
						attacksRemaining--;
					}
				}
			}
		}

		// tile does not have a unit
		else {
			// perform move if valid
			if (targetTile.getHighlighted() == 1 && movesRemaining > 0) {
				move(out, targetTile, gameState);
				movesRemaining--;
			}
		}
	}


	/**
	 * Perform a move action
	 * 
	 * @param out - a reference to the class responsible for updating the UI
	 * @param tile - the tile that this instance of Unit should move to
	 * @param gameState - the gameState of the current game
	 */
	public void move(ActorRef out, Tile tile, GameState gameState) {

		// establish mode in which to call move animation
		boolean verticalFirst = moveHelper(gameState.getBoard(), tile);
		
		// display appropriate move animation
		if (verticalFirst) {
			BasicCommands.moveUnitToTile(out, this, tile, verticalFirst);
		} else {
			BasicCommands.moveUnitToTile(out, this, tile);
		}

		
		// calculate approximate distance unit must travel...
				int distance = Math.abs(this.tile.getTilex() - tile.getTilex())
						+ Math.abs(this.tile.getTiley() - tile.getTiley());
		
		// remove unit from previous tile...
		this.tile.removeUnit();

		// ...and set its new position
		tile.setUnit(this);
		
		

		// ... and wait the amount of time that allows unit to reach target
		try {Thread.sleep(750 * distance);} catch (InterruptedException e) {e.printStackTrace();}
		
		// additional delay
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
	}

	
	/**
	 * Perform an attack action
	 * 
	 * @param out - a reference to the class responsible for updating the UI
	 * @param tile - the tile of the target unit that this instance of Unit should attack
	 * @param target - the target Unit ebing attacked
	 * @param gameState - the gameState of the current game
	 */
	public void attack(ActorRef out, Tile tile, Unit target, GameState gameState) {

		// display standard attack animation
		BasicCommands.playUnitAnimation(out, this, UnitAnimationType.attack);
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		
		
		if (this.abilities.contains("ranged")) {
			
			// display projectile animation if appropriate
			EffectAnimation projectile = BasicObjectBuilders.loadEffect(StaticConfFiles.f1_projectiles);
			BasicCommands.playProjectileAnimation(out, projectile, 0, this.tile, target.tile);

			// calculate approximate distance projectile must travel...
			int distance = Math.abs(this.tile.getTilex() - target.tile.getTilex())
					+ Math.abs(this.tile.getTiley() - target.tile.getTiley());

			// ... and wait the amount of time that allows projectile to reach target
			try {Thread.sleep(100 * distance);} catch (InterruptedException e) {e.printStackTrace();}
		}
	
		// reduce unit health
		int newTargetHealth = target.getUnitHealth() - this.unitAttack;
		target.setUnitHealth(out, newTargetHealth);

		// trigger counter attack
		if (newTargetHealth > 0) {
			// ranged units should only be counter attacked if they have attacked an adjacent tile
			if (!this.abilities.contains("ranged") || this.tile.isAdjacent(target.tile)) {
				target.counterAttack(out, this.tile, this, gameState);
			}
			BasicCommands.playUnitAnimation(out, target, UnitAnimationType.idle);
		}
		
		if (unitHealth > 0) {
			BasicCommands.playUnitAnimation(out, this, UnitAnimationType.idle);
		}
	}
	

	/**
	 * Perform an move and attack action
	 * 
	 * @param out - a reference to the class responsible for updating the UI
	 * @param tile - the tile of the target unit that this instance of Unit should move + attack
	 * @param target - the target Unit ebing attacked
	 * @param gameState - the gameState of the current game
	 */
	public void moveAndAttack(ActorRef out, Tile tile, Unit target, GameState gameState) {

		// move to the closest tile capable of attacking target unit)
		Board board = tile.getBoard();
		Tile moveToTile = moveAndAttackHelper(board, tile);
		move(out, moveToTile, gameState);
		
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}

		// attack target
		attack(out, tile, target, gameState);
	}

	
	/**
	 * Perform a counter attack
	 * 
	 * @param out - a reference to the class responsible for updating the UI
	 * @param tile - the tile of the target unit that this instance of Unit should counterattack
	 * @param target - the target Unit being attacked
	 * @param gameState - the gameState of the current game
	 */
	private void counterAttack(ActorRef out, Tile tile, Unit unit, GameState gameState) {
		// display attack animation
		BasicCommands.playUnitAnimation(out, this, UnitAnimationType.attack);
		try {Thread.sleep(1000);} catch (InterruptedException e) {e.printStackTrace();}
		
		// set counterattack target's health to new value
		int newUnitHealth = unit.getUnitHealth() - this.getUnitAttack();
		unit.setUnitHealth(out, newUnitHealth);
	}

	
	/**
	 * Helper function to decide which tile to move to when performing a move + attack action
	 * @param board
	 * @param attackTile
	 * 
	 * @return Tile - the closest Tile to this units current Tile which is capable of attacking the target unit
	 */
	public Tile moveAndAttackHelper(Board board, Tile attackTile) {
		Tile t = null;
		for (int steps = 1; steps <= 2; steps++) {
			for (int i = 0; i < board.getWidth(); i++) {
				for (int j = 0; j < board.getHeight(); j++) {
					t = board.getTile(i, j);
					if (t.isAdjacent(attackTile) && board.canBeReached(player, this, this.tile, t, 2)) {
						if (this.tile.stepsTo(t) == steps) {
							return t;
						}
					}
				}
			}
		}
		return t;
	}
	
	
	/**
	 * Helper function to establish if move animation should go vertically or horizontally first.
	 * Unit first avoids moving through an enemy unit, then if there is still a choice will avoid moving through a friendly unit if possible.
	 * @param board
	 * @param destinationTile
	 * 
	 * @return boolean - indicates whether the animation for a unit moving should move vertically first (true) or horizontally (false)
	 */
	public boolean moveHelper(Board board, Tile destinationTile) {
		
		Tile sourceTile = this.tile;
		
		int destinationX = destinationTile.getTilex();
		int destinationY = destinationTile.getTiley();
		
		int sourceX = sourceTile.getTilex();
		int sourceY = sourceTile.getTiley();
		
		// booleans which need to be populated to allow a decision to be made
		boolean horizontalEnemyInWay = false;
		boolean horizontalFriendlyInWay = false;
		boolean verticalEnemyInWay = false;
		boolean verticalFriendlyInWay = false;
		
		// populate booleans
		if (sourceX < destinationX) {
			for (int x = sourceX + 1; x <= destinationX; x++) {
				if (board.getTile(x, sourceY).hasUnit()) {
					if (board.getTile(x, sourceY).getUnit().getPlayer().equals(this.player)) {
						horizontalFriendlyInWay = true;
					} else {
						horizontalEnemyInWay = true;
					}
				}
			}
		} else if (sourceX > destinationX) {
			for (int x = sourceX-1; x >= destinationX; x--) {
				if (board.getTile(x, sourceY).hasUnit()) {
					if (board.getTile(x, sourceY).getUnit().getPlayer().equals(this.player)) {
						horizontalFriendlyInWay = true;
					} else {
						horizontalEnemyInWay = true;
					}
				}
			}
		}
		
		if (sourceY < destinationY) {
			for (int y = sourceY + 1; y <= destinationY; y++) {
				if (board.getTile(sourceX, y).hasUnit()) {
					if (board.getTile(sourceX, y).getUnit().getPlayer().equals(this.player)) {
						verticalFriendlyInWay = true;
					} else {
						verticalEnemyInWay = true;
					}
				}
			}
		} else if (sourceY > destinationY) {
			for (int y = sourceY - 1; y >= destinationY; y--) {
				if (board.getTile(sourceX, y).hasUnit()) {
					if (board.getTile(sourceX, y).getUnit().getPlayer().equals(this.player)) {
						verticalFriendlyInWay = true;
					} else {
						verticalEnemyInWay = true;
					}
				}
			}
		}
		
		// make decision about which way to move first
		if (horizontalEnemyInWay) {
			return true;
		}
		if (verticalEnemyInWay) {
			return false;
		}
		if (!verticalEnemyInWay && !horizontalEnemyInWay) {
			if (horizontalFriendlyInWay) {
				return true;
			}
			if (verticalFriendlyInWay) {
				return false;
			}
		}
		return false;
	}

	
	public ArrayList<String> getAbilities() {
		return this.abilities;
	}

	
	public void setAbilities(String confFile) {
		this.abilities = UnitAttributes.getAbilities(confFile);
	}

	
	public boolean isAvatar() {
		return isAvatar;
	}

	
	public void setIsAvatar(boolean isAvatar) {
		this.isAvatar = isAvatar;
	}

	
	/**
	 * Creates a Unit which is an avatar
	 * 
	 * @param configFile - the config file to use to build the unit
	 * @param startTile - starting position of avatar
	 * @param id
	 * 
	 * @return Unit - the newly created avatar
	 */
	public static Unit createAvatar(String configFile, Tile startTile, int id) {
		Unit avatar = BasicObjectBuilders.loadUnit(configFile, id, Unit.class);
		avatar.setPositionByTile(startTile);
		avatar.setAttacksPerTurn(1);
		avatar.setIsAvatar(true);
		return avatar;
	}

}
