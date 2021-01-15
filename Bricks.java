/*
 * Sachet, Yathartha, Devin
 * Brick Class
 * June 3, 2019
 * This class lays the bricks out according to the 2D array that we created.
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Bricks {
	private int brickHeight, brickWidth;
	public final int VERT_PAD = 50;
/*2D array for the layout of the bricks. The 0 represents blank spaces, 1 represents
* one hit bricks, 2 represents two hit bricks and 3 represents 3 hit bricks.
*/
	int[][] brick =  new int[][]{
		{2,2,2,2,2,2,2,2,2,2},
		{2,2,2,2,1,1,2,2,2,2},
		{2,1,2,1,1,1,1,2,1,2},
		{3,1,1,1,0,0,1,1,1,3},
		{3,1,1,0,0,0,0,1,1,3},
		{0,1,1,1,0,0,1,1,1,0},
		{0,0,1,1,1,1,1,1,0,0},
		{0,0,0,1,1,1,1,0,0,0}
	};

	public Bricks(int row, int col) {		
		brickWidth = 995/col;
		brickHeight = (735 / 3 - VERT_PAD) / row;
	}
	
	/*this method is to draw the bricks. The for-loops go through the 2D array and checks
for its value and if the value of the index is more than 0, it displays the bricks with its   color according to the color set with if-statements. It will then fill and draw borders of the bricks.
*/
	public void draw(Graphics2D g) {
		for (int row = 0; row < brick.length; row++) {
			for (int col = 0; col < brick[0].length; col++) {
				if (brick[row][col] > 0) {
					Color brick1 = new Color(41, 128, 185);
					Color brick2 = new Color(231, 76, 60);
					Color brick3 = new Color (34, 47, 62);
					
					//Assigns color of the brick based on array index value. The int representing the amount of hits it takes to break
					if (brick[row][col] == 1)
						g.setColor(brick1);
					if (brick[row][col] == 2)
						g.setColor(brick2);
					if (brick[row][col] == 3)
						g.setColor(brick3);
					
					//drawing the each brick
					g.fillRect(col * brickWidth, row * brickHeight + VERT_PAD, brickWidth, brickHeight); //draws the actual rectangular brick
					g.setStroke(new BasicStroke(2));
					g.setColor(Color.white); //setting white stroke outline for brick
					g.drawRect(col * brickWidth, row * brickHeight + VERT_PAD, brickWidth, brickHeight); //draws the white outline to differentiate each brick
				}

			}
		}
	}

	public int[][] getBricks() {
		return brick;
	}
	
	//method for quickly setting new value of brick for an array index.
	public void setBrick(int row, int col, int value) {
		brick[row][col] = value;
	}

	//method returns brick width
	public int getBrickWidth() {
		return brickWidth;
	}
	
	//method returns brick height
	public int getBrickHeight() {
		return brickHeight;
	}
	//as the name implies, it returns the bricks that is left so in the breakout class we can          //check the player’s win conditions.
	public int getTotalBricks() {
		int bricksLeft = 0;
		for (int i = 0; i < brick.length; i++) {
			for (int j = 0; j < brick[0].length; j++) {
				bricksLeft += brick[i][j];
			}
		}
		return bricksLeft;
	}
}




