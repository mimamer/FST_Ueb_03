import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

public class HighScore {
	/* Score information */
	int currScore;
	int highScore;
	boolean clearHighScores;
	
	public HighScore() {
		clearHighScores= false;
		initHighScores();
		currScore=0;
	}

	public void initHighScores() {
		File file = new File("highScores.txt");
		Scanner sc;
		try {
			sc = new Scanner(file);
			highScore = sc.nextInt();
			sc.close();
		} catch (Exception e) {
		}
	}

	public void updateScore(int score) {
		PrintWriter out;
		try {
			out = new PrintWriter("highScores.txt");
			out.println(score);
			out.close();
		} catch (Exception e) {
		}
		highScore = score;
		clearHighScores = true;
	}

	public void clearHighScores() {
		updateScore(0);
	}

	void setCurrScore(int i) {
		currScore=i;
	}

}
