import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.Set;

class Player extends Mover {

	Image pacmanImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacman.jpg"));
	Image pacmanUpImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanup.jpg"));
	Image pacmanDownImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmandown.jpg"));
	Image pacmanLeftImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanleft.jpg"));
	Image pacmanRightImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/pacmanright.jpg"));
	/*
	 * Direction is used in demoMode, currDirection and desiredDirection are used in
	 * non demoMode
	 */

	char currDirection;
	char desiredDirection;

	/* Keeps track of pellets eaten to determine end of game */
	int pelletsEaten;

	/* Which pellet the pacman is on top of */
	int pelletX;
	int pelletY;

	/* teleport is true when travelling through the teleport tunnels */
	boolean teleport;

	/* Stopped is set when the pacman is not moving or has been killed */
	boolean stopped = false;

	/* Constructor places pacman in initial location and orientation */
	public Player(int x, int y, Board board) {
		super(x, y, board);

		teleport = false;
		pelletsEaten = 0;
		pelletX = x / gridSize - 1;
		pelletY = y / gridSize - 1;

		currDirection = 'L';
		desiredDirection = 'L';
	}

	public void demoMove() {
		lastX = x;
		lastY = y;
		if (isChoiceDest()) {
			direction = newDirection();
		}
		switch (direction) {
		case 'L':
			if (isValidDest(x - increment, y)) {
				x -= increment;
			} else if (y == 9 * gridSize && x < 2 * gridSize) {
				x = max - gridSize * 1;
				teleport = true;
			}
			break;
		case 'R':
			if (isValidDest(x + gridSize, y)) {
				x += increment;
			} else if (y == 9 * gridSize && x > max - gridSize * 2) {
				x = 1 * gridSize;
				teleport = true;
			}
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
		currDirection = direction;
		frameCount++;
	}

	public void move() {
		lastX = x;
		lastY = y;

		/* Try to turn in the direction input by the user */
		/* Can only turn if we're in center of a grid */
		if (x % 20 == 0 && y % 20 == 0 ||
		/* Or if we're reversing */
				(desiredDirection == 'L' && currDirection == 'R') || (desiredDirection == 'R' && currDirection == 'L')
				|| (desiredDirection == 'U' && currDirection == 'D')
				|| (desiredDirection == 'D' && currDirection == 'U')) {
			move_coordinates(desiredDirection);
		}
		/*
		 * If we haven't moved, then move in the direction the pacman was headed anyway
		 */
		if (lastX == x && lastY == y) {
			switch (currDirection) {
			case 'L':
				if (isValidDest(x - increment, y))
					x -= increment;
				else if (y == 9 * gridSize && x < 2 * gridSize) {
					x = max - gridSize * 1;
					teleport = true;
				}
				break;
			case 'R':
				if (isValidDest(x + gridSize, y))
					x += increment;
				else if (y == 9 * gridSize && x > max - gridSize * 2) {
					x = 1 * gridSize;
					teleport = true;
				}
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

		/* If we did change direction, update currDirection to reflect that */
		else {
			currDirection = desiredDirection;
		}

		/* If we didn't move at all, set the stopped flag */
		if (lastX == x && lastY == y)
			stopped = true;

		/*
		 * Otherwise, clear the stopped flag and increment the frameCount for animation
		 * purposes
		 */
		else {
			stopped = false;
			frameCount++;
		}
	}

	/* Update what pellet the pacman is on top of */
	public void updatePellet() {
		if (x % gridSize == 0 && y % gridSize == 0) {
			pelletX = x / gridSize - 1;
			pelletY = y / gridSize - 1;
		}
	}
}