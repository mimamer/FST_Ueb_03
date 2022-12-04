
class Mover
{
  /* Framecount is used to count animation frames*/
  int frameCount=0;
	/* Current location */
	int x;
	int y;
	Board board;
  /* State contains the game map */
  boolean[][] state;

  /* gridSize is the size of one square in the game.
     max is the height/width of the game.
     increment is the speed at which the object moves,
     1 increment per move() call */
  int gridSize;
  int max;
  int increment;

  /* Generic constructor */
  public Mover()
  {
    gridSize=20;
    increment = 4;
    max = 400;
    
  }



  /* Determines if a set of coordinates is a valid destination.*/
	/* Determines if the location is one where the ghost has to make a decision */
  public boolean isValidDest(int x, int y)
  {
	  boolean player=this.getClass().equals(Player.class);
	  return board.isValidDest(x,y,player);
    /* The first statements check that the x and y are inbounds.  The last statement checks the map to
       see if it's a valid location */
    
  } 
	/*
	 * This function is used for demoMode. It is copied from the Ghost class. See
	 * that for comments
	 */
	public boolean isChoiceDest() {
		if (x % gridSize == 0 && y % gridSize == 0) {
			return true;
		}
		return false;
	}
}
