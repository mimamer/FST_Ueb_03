import java.util.HashSet;
import java.util.Set;

class Mover {
	/* Framecount is used to count animation frames */
	int frameCount = 0;
	/* Current location */
	int x;
	int y;
	char direction;
	int lastX;
	int lastY;

	Board board;
	/* State contains the game map */

	/*
	 * gridSize is the size of one square in the game. max is the height/width of
	 * the game. increment is the speed at which the object moves, 1 increment per
	 * move() call
	 */
	int gridSize;
	int max;
	int increment;

	/* Generic constructor */
	public Mover(int x, int y, Board board) {
		this.x = x;
		this.y = y;
		this.lastX = x;
		this.lastY = y;
		this.board = board;
		gridSize = 20;
		increment = 4;
		max = 400;

	}

	/* Determines if a set of coordinates is a valid destination. */
	/* Determines if the location is one where the ghost has to make a decision */
	public boolean isValidDest(int x, int y) {
		boolean player = this.getClass().equals(Player.class);
		return board.isValidDest(x, y, player);

	}

	/*
	 * This function is used for demoMode.
	 */
	public boolean isChoiceDest() {
		if (x % gridSize == 0 && y % gridSize == 0) {
			return true;
		}
		return false;
	}

	public char newDirection() {
		int random;
		char backwards = 'U';
		int lookX = x, lookY = y;
		Set<Character> set = new HashSet<Character>();
		switch (direction) {
		case 'L':
			backwards = 'R';
			break;
		case 'R':
			backwards = 'L';
			break;
		case 'U':
			backwards = 'D';
			break;
		case 'D':
			backwards = 'U';
			break;
		}
		char newDirection = backwards;
		while (newDirection == backwards || !isValidDest(lookX, lookY)) {
			if (set.size() == 3) {
				newDirection = backwards;
				break;
			}
			lookX = x;
			lookY = y;
			random = (int) (Math.random() * 4) + 1;
			if (random == 1) {
				newDirection = 'L';
				lookX -= increment;
			} else if (random == 2) {
				newDirection = 'R';
				lookX += gridSize;
			} else if (random == 3) {
				newDirection = 'U';
				lookY -= increment;
			} else if (random == 4) {
				newDirection = 'D';
				lookY += gridSize;
			}
			if (newDirection != backwards) {
				set.add(new Character(newDirection));
			}
		}
		return newDirection;
	}
}
