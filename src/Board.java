
/* Drew Schuster */
import java.awt.*;
import javax.swing.JPanel;
import java.lang.Math;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;

/*This board class contains the player, ghosts, pellets, and most of the game logic.*/
public class Board extends JPanel {

	public enum Field_type {
		EMPTY, PELLET, WALL, GHOST_DOOR
	}

	Field_type[][] initial_state;
	/* Initialize the images */
	/* For JAR File */

	Image titleScreenImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/titleScreen.jpg"));
	Image gameOverImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/gameOver.jpg"));
	Image winScreenImage = Toolkit.getDefaultToolkit().getImage(Pacman.class.getResource("img/winScreen.jpg"));

	/* Initialize the player and ghosts */
	Player player;
	Ghost[] ghosts = new Ghost[4];

	/* Timer is used for playing sound effects and animations */
	long timer = System.currentTimeMillis();

	/*
	 * Dying is used to count frames in the dying animation. If it's non-zero,
	 * pacman is in the process of dying
	 */
	int dying = 0;
	GameSounds sounds;
	HighScore highscore;

	int numLives = 2;

	/* Contains the state of all pellets */
	boolean[][] pellets;

	/* Game dimensions */
	int board_Size;
	int max;

	/* State flags */
	boolean stopped;
	boolean titleScreen;
	boolean winScreen = false;
	boolean overScreen = false;
	boolean demo = false;
	int New;

	int lastPelletEatenX = 0;
	int lastPelletEatenY = 0;

	/* This is the font used for the menus */
	Font font = new Font("Monospaced", Font.BOLD, 12);

	/* Constructor initializes state flags etc. */
	public Board() {
		highscore = new HighScore();
		sounds = new GameSounds();
		stopped = false;
		max = 400;
		board_Size = 19;
		New = 0;
		titleScreen = true;
		initial_state = new Field_type[board_Size][board_Size];
		try {
			String board_info = Files.readString(Paths.get(getClass().getResource("initial_board.txt").toURI()));
			process_board_info(board_info);
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initialize_pellets_and_state();

		player = new Player(200, 300, this);
		initialize_ghost();

	}

	private void initialize_pellets_and_state() {
		pellets = new boolean[board_Size][board_Size];
		for (int row = 0; row < initial_state.length; row++) {
			for (int column = 0; column < initial_state.length; column++) {
				pellets[row][column] = initial_state[row][column] == Field_type.PELLET ? true : false;
			}
		}
	}

	public void initialize_ghost() {
		ghosts[0] = new Ghost(180, 180, 0, this);
		ghosts[1] = new Ghost(200, 180, 1, this);
		ghosts[2] = new Ghost(220, 180, 2, this);
		ghosts[3] = new Ghost(220, 180, 3, this);
	}

	private void process_board_info(String board_info) {
		StringTokenizer st = new StringTokenizer(board_info, "\n");
		int row = 0;
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			for (int column = 0; column < line.length(); column++) {
				char type = line.charAt(column);
				initial_state[column][row] = type == ' ' ? Field_type.EMPTY
						: (type == '.' ? Field_type.PELLET : (type == 'W' ? Field_type.WALL : Field_type.GHOST_DOOR));// todo
			}
			row++;
		}
	}

	public void reset() {
		numLives = 2;
		initialize_pellets_and_state();

	}

	public void drawBoard(Graphics graphics) {
		drawMargins(graphics);
		drawWalls(graphics);
		drawLives(graphics);
	}
	
	public void drawLives(Graphics g) {
		g.setColor(Color.BLACK);

		/* Clear the bottom bar */
		g.fillRect(0, max + 5, 600, board_Size);
		g.setColor(Color.YELLOW);
		for (int i = 0; i < numLives; i++) {
			/* Draw each life */
			g.fillOval(board_Size * (i + 1), max + 5, board_Size, board_Size);
		}
		/* Draw the menu items */
		g.setColor(Color.YELLOW);
		g.setFont(font);
		g.drawString("Reset", 100, max + 5 + board_Size);
		g.drawString("Clear High Scores", 180, max + 5 + board_Size);
		g.drawString("Exit", 350, max + 5 + board_Size);
	}

	private void drawMargins(Graphics graphics) {
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 600, 600);
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 420, 420);

		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 20, 600);
		graphics.fillRect(0, 0, 600, 20);
		graphics.setColor(Color.WHITE);
		graphics.drawRect(19, 19, 382, 382);
		graphics.setColor(Color.BLUE);
	}

	private void drawWalls(Graphics graphics) {
		int field_size = 20; // quadratic
		int oberer_reiter = 20;
		int seiten_margin = 20;
		for (int column = 0; column < initial_state.length; column++) {
			for (int row = 0; row < initial_state.length; row++) {
				if (initial_state[column][row] == Field_type.WALL) {
					graphics.fillRect(column * field_size + seiten_margin, row * field_size + oberer_reiter, field_size,
							field_size);
				}
			}
		}
	}

	/* Draws the pellets on the screen */
	public void drawPellets(Graphics g) {
		g.setColor(Color.YELLOW);
		for (int i = 1; i < 20; i++) {
			for (int j = 1; j < 20; j++) {
				if (pellets[i - 1][j - 1])
					g.fillOval(i * 20 + 8, j * 20 + 8, 4, 4);
			}
		}
	}

	public void fillPellet(int x, int y, Graphics g) {
		g.setColor(Color.YELLOW);
		g.fillOval(x * 20 + 28, y * 20 + 28, 4, 4);
	}

	/*
	 * This is the main function that draws one entire frame of the game, gameloop
	 */
	public void paint(Graphics graphics) {

		if (dying > 0) {
			dying_sequence(graphics);
			return;
		}

		if (titleScreen) {
			setScreen(graphics, titleScreenImage);
			return;
		} else if (winScreen) {
			setScreen(graphics, winScreenImage);
			return;
		} else if (overScreen) {
			setScreen(graphics, gameOverImage);
			return;
		}

		/* If need to update the high scores, redraw the top menu bar */
		if (highscore.clearHighScores) {
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 600, 18);

			highscore.clearHighScores = false;
			draw_header(graphics);

		}

		/* Game initialization */
		if (New == 1) {
			reset();
			player = new Player(200, 300, this);

			initialize_ghost();

			highscore.setCurrScore(0);

			initialize_pellets_and_state();
			drawBoard(graphics);
			drawPellets(graphics);
			drawLives(graphics);
			draw_header(graphics);
			New++;
		}
		/* Second frame of new game */
		else if (New == 2) {
			New++;
		}
		/* Third frame of new game */
		else if (New == 3) {
			New++;
			/* Play the newGame sound effect */
			sounds.newGame();
			timer = System.currentTimeMillis();
			return;
		}
		/* Fourth frame of new game */
		else if (New == 4) {
			/* Stay in this state until the sound effect is over */
			long currTime = System.currentTimeMillis();
			if (currTime - timer >= 5000) {
				New = 0;
			} else
				return;
		}

		/* Drawing optimization */
		graphics.copyArea(player.x - 20, player.y - 20, 80, 80, 0, 0);

		for (Ghost ghost : ghosts) {
			graphics.copyArea(ghost.x - 20, ghost.y - 20, 80, 80, 0, 0);
		}

		/* Detect collisions */

		boolean collision_detected = detect_collision();

		/* Kill the pacman */
		if (collision_detected && !stopped) {
			/* 4 frames of death */
			dying = 4;

			/* Play death sound effect */
			sounds.death();
			/* Stop any pacman eating sounds */
			sounds.nomNomStop();

			/*
			 * Decrement lives, update screen to reflect that. And set appropriate flags and
			 * timers
			 */
			numLives--;
			stopped = true;
			drawLives(graphics);
			timer = System.currentTimeMillis();
		}

		/* Delete the players and ghosts */
		graphics.setColor(Color.BLACK);
		graphics.fillRect(player.lastX, player.lastY, 20, 20);
		for (Ghost ghost : ghosts) {
			graphics.fillRect(ghost.lastX, ghost.lastY, 20, 20);
		}

		/* Eat pellets */

		if (pellets[player.pelletX][player.pelletY] && New != 2 && New != 3) {

			pellet_handing();
			/* Update the screen to reflect the new score */
			graphics.setColor(Color.BLACK);
			graphics.fillRect(0, 0, 600, 20);
			draw_header(graphics);

			/* If this was the last pellet */
			if (player.pelletsEaten == 173) {
				/* Demo mode can't get a high score */
				if (!demo) {
					if (highscore.currScore > highscore.highScore) {
						highscore.updateScore(highscore.currScore);
					}
					winScreen = true;
				} else {
					titleScreen = true;
				}
				return;
			}
		}

		/* If we moved to a location without pellets, stop the sounds */
		else if ((player.pelletX != lastPelletEatenX || player.pelletY != lastPelletEatenY) || player.stopped) {
			/* Stop any pacman eating sounds */
			sounds.nomNomStop();
		}

		/* Replace pellets that have been run over by ghosts */
		for (Ghost ghost : ghosts) {
			if (pellets[ghost.lastPelletX][ghost.lastPelletY])
				fillPellet(ghost.lastPelletX, ghost.lastPelletY, graphics);
		}

		/* Draw the ghosts */
		if (ghosts[0].frameCount < 5) {
			/* Draw first frame of ghosts */
			for (Ghost ghost : ghosts) {
				graphics.drawImage(ghost.ghost_right, ghost.x, ghost.y, Color.BLACK, null);
			}

			ghosts[0].frameCount++;
		} else {
			/* Draw second frame of ghosts */
			for (Ghost ghost : ghosts) {
				graphics.drawImage(ghost.ghost_left, ghost.x, ghost.y, Color.BLACK, null);
			}

			if (ghosts[0].frameCount >= 10)
				ghosts[0].frameCount = 0;
			else
				ghosts[0].frameCount++;
		}

		/* Draw the pacman */
		if (player.frameCount < 5) {
			/* Draw mouth closed */
			graphics.drawImage(player.pacmanImage, player.x, player.y, Color.BLACK, null);
		} else {
			/* Draw mouth open in appropriate direction */
			if (player.frameCount >= 10)
				player.frameCount = 0;

			switch (player.currDirection) {
			case 'L':
				graphics.drawImage(player.pacmanLeftImage, player.x, player.y, Color.BLACK, null);
				break;
			case 'R':
				graphics.drawImage(player.pacmanRightImage, player.x, player.y, Color.BLACK, null);
				break;
			case 'U':
				graphics.drawImage(player.pacmanUpImage, player.x, player.y, Color.BLACK, null);
				break;
			case 'D':
				graphics.drawImage(player.pacmanDownImage, player.x, player.y, Color.BLACK, null);
				break;
			}
		}

		graphics.setColor(Color.WHITE);
		graphics.drawRect(19, 19, 382, 382);

	}

	private void draw_header(Graphics graphics) {
		graphics.setColor(Color.YELLOW);
		graphics.setFont(font);
		if (demo)
			graphics.drawString("DEMO MODE PRESS ANY KEY TO START A GAME\t High Score: " + highscore.highScore, 20, 10);
		else
			graphics.drawString("Score: " + (highscore.currScore) + "\t High Score: " + highscore.highScore, 20, 10);
	}

	private void pellet_handing() {
		lastPelletEatenX = player.pelletX;
		lastPelletEatenY = player.pelletY;

		/* Play eating sound */
		sounds.nomNom();

		/* Increment pellets eaten value to track for end game */
		player.pelletsEaten++;

		/* Delete the pellet */
		pellets[player.pelletX][player.pelletY] = false;

		/* Increment the score */
		highscore.currScore += 50;
	}

	private void dying_sequence(Graphics graphics) {
		/* Stop any pacman eating sounds */
		sounds.nomNomStop();

		/* Draw the pacman */
		graphics.drawImage(player.pacmanImage, player.x, player.y, Color.BLACK, null);
		graphics.setColor(Color.BLACK);

		/* Kill the pacman */
		if (dying == 4)
			graphics.fillRect(player.x, player.y, 20, 7);
		else if (dying == 3)
			graphics.fillRect(player.x, player.y, 20, 14);
		else if (dying == 2)
			graphics.fillRect(player.x, player.y, 20, 20);
		else if (dying == 1) {
			graphics.fillRect(player.x, player.y, 20, 20);
		}

		/*
		 * Take .1 seconds on each frame of death, and then take 2 seconds for the final
		 * frame to allow for the sound effect to end
		 */
		long currTime = System.currentTimeMillis();
		long temp;
		if (dying != 1)
			temp = 100;
		else
			temp = 2000;
		/* If it's time to draw a new death frame... */
		if (currTime - timer >= temp) {
			dying--;
			timer = currTime;
			/* If this was the last death frame... */
			if (dying == 0) {
				if (numLives == -1) {
					/* Demo mode has infinite lives, just give it more lives */
					if (demo)
						numLives = 2;
					else {
						/* Game over for player. If relevant, update high score. Set gameOver flag */
						if (highscore.currScore > highscore.highScore) {
							highscore.updateScore(highscore.currScore);
						}
						overScreen = true;
					}
				}
			}
		}
	}

	private boolean detect_collision() {
		for (Ghost ghost : ghosts) {
			if (player.x == ghost.x && Math.abs(player.y - ghost.y) < 10) {
				return true;
			}
			if (player.y == ghost.y && Math.abs(player.x - ghost.x) < 10) {
				return true;
			}
		}
		return false;
	}

	private void setScreen(Graphics graphics, Image screen_image) {
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, 600, 600);
		graphics.drawImage(screen_image, 0, 0, Color.BLACK, null);

		sounds.nomNomStop();
		New = 1;
	}

	public boolean isValidDest(int x, int y, boolean player) {
		if ((((x) % 20 == 0) || ((y) % 20) == 0) && 20 <= x && x < 400 && 20 <= y && y < 400
				&& initial_state[x / 20 - 1][y / 20 - 1] != Field_type.WALL) {
			if (player && initial_state[x / 20 - 1][y / 20 - 1] == Field_type.GHOST_DOOR) {
				return false;
			}
			// ghost
			return true;
		}
		return false;
	}

	public void move_all_to_starting_position() {
		player.currDirection = 'L';
		player.direction = 'L';
		player.desiredDirection = 'L';
		player.x = 200;
		player.y = 300;

		ghosts[0].x = 180;
		ghosts[0].y = 180;
		ghosts[1].x = 200;
		ghosts[1].y = 180;
		ghosts[2].x = 220;
		ghosts[2].y = 180;
		ghosts[3].x = 220;
		ghosts[3].y = 180;
		
	}
}
