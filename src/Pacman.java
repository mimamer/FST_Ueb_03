
/* Drew Schuster */
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

/* This class contains the entire game... most of the game logic is in the Board class but this
   creates the gui and captures mouse and keyboard input, as well as controls the game states */
public class Pacman implements MouseListener, KeyListener {

	/*
	 * These timers are used to kill title, game over, and victory screens after a
	 * set idle period (5 seconds)
	 */
	long titleTimer = -1;
	long timer = -1;

	/* Create a new board */
	Board board = new Board();

	/* This timer is used to do request new frames be drawn */
	javax.swing.Timer frameTimer;

	public Pacman() {
		
		JFrame frame = new JFrame();
		frame.setSize(420, 460);
		frame.add(board, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setResizable(false);

		/* Set listeners for mouse actions and button clicks */
		board.requestFocus();
		board.addMouseListener(this);
		board.addKeyListener(this);

		/* Set the New flag to 1 because this is a new game */
		board.New = 1;

		/* Manually call the first frameStep to initialize the game. */
		stepFrame(true);

		/* Create a timer that calls stepFrame every 30 milliseconds */
		frameTimer = new javax.swing.Timer(30, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stepFrame(false);
			}
		});

		/* Start the timer */
		frameTimer.start();

		board.requestFocus();
	}

	/*
	 * This repaint function repaints only the parts of the screen that may have
	 * changed. Namely the area around every player ghost and the menu bars
	 */
	public void repaint() {
		if (board.player.teleport) {
			board.repaint(board.player.lastX - 20, board.player.lastY - 20, 80, 80);
			board.player.teleport = false;
		}
		board.repaint(0, 0, 600, 20);
		board.repaint(0, 420, 600, 40);
		board.repaint(board.player.x - 20, board.player.y - 20, 80, 80);

		for (Ghost ghost : board.ghosts) {
			board.repaint(ghost.x - 20, ghost.y - 20, 80, 80);
		}

	}

	/* Steps the screen forward one frame */
	public void stepFrame(boolean New) {
		/*
		 * If we aren't on a special screen than the timers can be set to -1 to disable
		 * them
		 */
		if (!board.titleScreen && !board.winScreen && !board.overScreen) {
			timer = -1;
			titleTimer = -1;
		}

		/*
		 * If we are playing the dying animation, keep advancing frames until the
		 * animation is complete
		 */
		if (board.dying > 0) {
			board.repaint();
			return;
		}

		/*
		 * New can either be specified by the New parameter in stepFrame function call
		 * or by the state of b.New. Update New accordingly
		 */
		New = New || (board.New != 0);

		/*
		 * If this is the title screen, make sure to only stay on the title screen for 5
		 * seconds. If after 5 seconds the user hasn't started a game, start up demo
		 * mode
		 */
		if (board.titleScreen) {
			if (titleTimer == -1) {
				titleTimer = System.currentTimeMillis();
			}

			long currTime = System.currentTimeMillis();
			if (currTime - titleTimer >= 5000) {
				board.titleScreen = false;
				board.demo = true;
				titleTimer = -1;
			}
			board.repaint();
			return;
		}

		/*
		 * If this is the win screen or game over screen, make sure to only stay on the
		 * screen for 5 seconds. If after 5 seconds the user hasn't pressed a key, go to
		 * title screen
		 */
		else if (board.winScreen || board.overScreen) {
			if (timer == -1) {
				timer = System.currentTimeMillis();
			}

			long currTime = System.currentTimeMillis();
			if (currTime - timer >= 5000) {
				board.winScreen = false;
				board.overScreen = false;
				board.titleScreen = true;
				timer = -1;
			}
			board.repaint();
			return;
		}

		/* If we have a normal game state, move all pieces and update pellet status */
		if (!New) {
			/*
			 * The pacman player has two functions, demoMove if we're in demo mode and move
			 * if we're in user playable mode. Call the appropriate one here
			 */
			if (board.demo) {
				board.player.demoMove();
			} else {
				board.player.move();
			}

			/* Also move the ghosts, and update the pellet states */
			for (Ghost ghost : board.ghosts) {
				ghost.move();
			}

			board.player.updatePellet();

			for (Ghost ghost : board.ghosts) {
				ghost.updatePellet();
			}
		}

		/*
		 * We either have a new game or the user has died, either way we have to reset
		 * the board
		 */
		if (board.stopped || New) {
			/* Temporarily stop advancing frames */
			frameTimer.stop();

			/* If user is dying ... */
			while (board.dying > 0) {
				/* Play dying animation. */
				stepFrame(false);
			}

			/* Move all game elements back to starting positions and orientations */
			board.move_all_to_starting_position();

			/* Advance a frame to display main state */
			board.repaint(0, 0, 600, 600);

			/* Start advancing frames once again */
			board.stopped = false;
			frameTimer.start();
		}
		/* Otherwise we're in a normal state, advance one frame */
		else {
			repaint();
		}
	}

	/* Handles user key presses */
	public void keyPressed(KeyEvent e) {
		/* Pressing a key in the title screen starts a game */
		if (board.titleScreen) {
			board.titleScreen = false;
			return;
		}
		/*
		 * Pressing a key in the win screen or game over screen goes to the title screen
		 */
		else if (board.winScreen || board.overScreen) {
			board.titleScreen = true;
			board.winScreen = false;
			board.overScreen = false;
			return;
		}
		/* Pressing a key during a demo kills the demo mode and starts a new game */
		else if (board.demo) {
			board.demo = false;
			/* Stop any pacman eating sounds */
			board.sounds.nomNomStop();
			board.New = 1;
			return;
		}

		/* Otherwise, key presses control the player! */
		switch (e.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			board.player.desiredDirection = 'L';
			break;
		case KeyEvent.VK_RIGHT:
			board.player.desiredDirection = 'R';
			break;
		case KeyEvent.VK_UP:
			board.player.desiredDirection = 'U';
			break;
		case KeyEvent.VK_DOWN:
			board.player.desiredDirection = 'D';
			break;
		}

		repaint();
	}

	/*
	 * This function detects user clicks on the menu items on the bottom of the
	 * screen
	 */
	public void mousePressed(MouseEvent e) {
		if (board.titleScreen || board.winScreen || board.overScreen) {
			/* If we aren't in the game where a menu is showing, ignore clicks */
			return;
		}

		/* Get coordinates of click */
		int x = e.getX();
		int y = e.getY();
		if (400 <= y && y <= 460) {
			if (100 <= x && x <= 150) {
				/* New game has been clicked */
				board.New = 1;
			} else if (180 <= x && x <= 300) {
				/* Clear high scores has been clicked */
				board.highscore.clearHighScores();
			} else if (350 <= x && x <= 420) {
				/* Exit has been clicked */
				System.exit(0);
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	/* Main function simply creates a new pacman instance */
	public static void main(String[] args) {
		Pacman c = new Pacman();
	}
}
