package structures.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A basic representation of a tile on the game board. Tiles have both a pixel position
 * and a grid position. Tiles also have a width and height in pixels and a series of urls
 * that point to the different renderable textures that a tile might have.
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class Tile {

	@JsonIgnore
	private static ObjectMapper mapper = new ObjectMapper(); // Jackson Java Object Serializer, is used to read java objects from a file
	
	private List<String> tileTextures;
	private int xpos;
	private int ypos;
	private int width;
	private int height;
	private int tilex;
	private int tiley;
	private boolean hasUnit = false;
	private Unit unit;
	private int highlighted = 0;
	private Board board;
	
	public Tile() {}
	
	public Tile(String tileTexture, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		tileTextures = new ArrayList<String>(1);
		tileTextures.add(tileTexture);
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}
	
	public Tile(List<String> tileTextures, int xpos, int ypos, int width, int height, int tilex, int tiley) {
		super();
		this.tileTextures = tileTextures;
		this.xpos = xpos;
		this.ypos = ypos;
		this.width = width;
		this.height = height;
		this.tilex = tilex;
		this.tiley = tiley;
	}
	
	
	public List<String> getTileTextures() {
		return tileTextures;
	}
	
	
	public void setTileTextures(List<String> tileTextures) {
		this.tileTextures = tileTextures;
	}
	
	
	public int getXpos() {
		return xpos;
	}
	
	
	public void setXpos(int xpos) {
		this.xpos = xpos;
	}
	
	
	public int getYpos() {
		return ypos;
	}
	
	
	public void setYpos(int ypos) {
		this.ypos = ypos;
	}
	
	
	public int getWidth() {
		return width;
	}
	
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	
	public int getHeight() {
		return height;
	}
	
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	
	public int getTilex() {
		return tilex;
	}
	
	
	public void setTilex(int tilex) {
		this.tilex = tilex;
	}
	
	
	public int getTiley() {
		return tiley;
	}
	
	
	public void setTiley(int tiley) {
		this.tiley = tiley;
	}
	
	
	public void setUnit(Unit unit) {
		this.unit = unit;
		hasUnit = true;
		unit.setTile(this);
		unit.setPositionByTile(this);
	}
	
	
	public boolean hasUnit() {
		return hasUnit;
	}
	
	
	public void removeUnit() {
		this.unit = null;
		hasUnit=false;
	}
	
	
	public Unit getUnit() {
		return unit;
	}
	
	
	public void setHighlighted(int mode) {
		this.highlighted = mode;
	}

	
	public int getHighlighted() {
		return highlighted;
	}
	
	
	public void setBoard(Board board) {
		this.board = board;
	
		
	}
	public Board getBoard() {
		return board;
		
	}
	
	
	/**
	 * Loads a tile from a configuration file
	 * parameters.
	 * @param configFile
	 * @return
	 */
	public static Tile constructTile(String configFile) {
		
		try {
			Tile tile = mapper.readValue(new File(configFile), Tile.class);
			return tile;
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
		
	}

	
	/**
	 * Helper method to check if one tile is adjacent to another
	 * @param startingTile
	 * @return true if this instance of Tile is adjacent to startingTile
	 */
	public boolean isAdjacent(Tile startingTile) {
		
		int x = tilex;
		int y = tiley;
		
		int targetX = startingTile.getTilex();
		int targetY = startingTile.getTiley();
		
		for (int i = x - 1; i <= x + 1 ; i++) {
			for (int j = y - 1; j <= y + 1 ; j++) {
				if (i >= 0 && j >= 0 && i < width && j < height) {
					if (!(i == x && j == y)) {
						if (i == targetX && j == targetY) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	
	/**
	 * Helper method to count the steps between tiles
	 * @param destinationTile
	 * @return the number of steps between this Tile and destinationTile
	 */
	public int stepsTo(Tile destinationTile) {
		int x = tilex;
		int y = tiley;
		
		int targetX = destinationTile.getTilex();
		int targetY = destinationTile.getTiley();
		
		int steps = 0;
		
		int xSteps = Math.abs(x - targetX);
		int ySteps = Math.abs(y - targetY);
		
		steps = xSteps + ySteps;
		
		return steps;
	}
}
