import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Set;

class Ghost extends Mover {

	Image ghost_right;
	Image ghost_left;

	/* Direction ghost is heading */
	char direction;

	/* Last ghost location */
	int lastX;
	int lastY;

	/* The pellet the ghost is on top of */
	int pelletX, pelletY;

	/* The pellet the ghost was last on top of */
	int lastPelletX, lastPelletY;

	/* Constructor places ghost and updates states */
	public Ghost(int x, int y, int photo_id, Board board) {
		direction = 'L';
		pelletX = x / gridSize - 1;
		pelletY = x / gridSize - 1;
		lastPelletX = pelletX;
		lastPelletY = pelletY;
		this.lastX = x;
		this.lastY = y;
		this.x = x;
		this.y = y;
		this.board = board;
		// try-catch
		ghost_right = Toolkit.getDefaultToolkit()
				.getImage(Pacman.class.getResource("img/ghost" + (photo_id) + "0.jpg"));
		ghost_left = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/ghost" + (photo_id) + "1.jpg"));
	}

	/* update pellet status */
	public void updatePellet() {
		int tempX, tempY;
		tempX = x / gridSize - 1;
		tempY = y / gridSize - 1;
		if (tempX != pelletX || tempY != pelletY) {
			lastPelletX = pelletX;
			lastPelletY = pelletY;
			pelletX = tempX;
			pelletY = tempY;
		}

	}

	/* Chooses a new direction randomly for the ghost to move */
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
		/* While we still haven't found a valid direction */
		while (newDirection == backwards || !isValidDest(lookX, lookY)) {
			/* If we've tried every location, turn around and break the loop */
			if (set.size() == 3) {
				newDirection = backwards;
				break;
			}

			lookX = x;
			lookY = y;

			/* Randomly choose a direction */
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

	/* Random move function for ghost */
	public void move() {
		lastX = x;
		lastY = y;

		/* If we can make a decision, pick a new direction randomly */
		if (isChoiceDest()) {
			direction = newDirection();
		}

		/* If that direction is valid, move that way */
		switch (direction) {
		case 'L':
			if (isValidDest(x - increment, y))
				x -= increment;
			break;
		case 'R':
			if (isValidDest(x + gridSize, y))
				x += increment;
			break;
		case 'U':
			if (isValidDest(x, y - increment))
				y -= increment;
			break;
		case 'D':
			if (isValidDest(x, y + gridSize))
				y += increment;
			break;
		}
	}
}
