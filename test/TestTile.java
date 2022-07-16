
public class TestTile {
	private int x;
	public int getX() {
		return x;
	}


	public void setX(int x) {
		this.x = x;
	}


	public int getY() { 
		return y;
	}


	public void setY(int y) {
		this.y = y;
	}


	private int y;
	
	public TestTile(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	public String toString() {
		return x + " " + y;
	}
	
	public boolean equals(Object o) {
		if (o instanceof TestTile) {

			if (((TestTile) o).getX() == x && ((TestTile) o).getY() == y) {
				return true;
			}
		}
		return false;
	}
	
	public int hashCode() {
		return 1;
	}
}
