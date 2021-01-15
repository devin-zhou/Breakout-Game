/*
 * Sachet, Yathartha, Devin
 * Breakout Game:The objective of the game is to keep the ball from reaching the bottom of the screen, while hitting the ball into the bricks to break them. 
 * If you run out of lives, you lose, and if you break all of the bricks you win.
 * June 3, 2019
 */

//imports


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class breakout extends JPanel {
	Point delta;
	Ellipse2D.Double ball;
	RoundRectangle2D.Double paddle;
	Rectangle2D.Double powerUp;

	// power up states (All power ups are off by default)
	boolean moveR = false;
	boolean moveL = false;
	boolean speedPower = false;
	boolean paddleIncPower = false;
	boolean paddleDecPower = false;
	boolean PowerUpLIFE = false;
	boolean PaddleIncSpeed = false;
	boolean PaddleDecSpeed = false;

	Bricks brick;

	// timers
	int timer = 0;
	int powerUpSpawnTimer = 0;
	int powerUpDuration = 0;

	int lives = 3;
	int powerUpSpawn; // represents if power up spawns and which type spawns
	int titlescreenTimer = 1000; // time before the game starts automatically, set to 1000 to give user time to
									// read the instructions and controls
	int PaddleSpeed = 5; // default paddle move speed
	int playSong = 0; //amount of times audio plays

	// win/lose booleans
	boolean win = false;
	boolean lose = false;

	Random rng = new Random();// setting up random

	// setting up images
	Image winImg;
	Image loseImg;
	Image fortnite;

	public breakout() {
		enableEvents(java.awt.AWTEvent.KEY_EVENT_MASK);
		requestFocus();
		// creating 2D shapes
		ball = new Ellipse2D.Double(500, 350, 20, 20);
		delta = new Point(-2, -5);
		paddle = new RoundRectangle2D.Double(400, 650, 150, 20, 20, 20);
		powerUp = new Rectangle2D.Double(-50, -50, 25, 25);
		brick = new Bricks(7, 10);
		// Setting up images
		loseImg = new ImageIcon("src/lose.png").getImage();
		winImg = new ImageIcon("src/win.png").getImage();
		fortnite = new ImageIcon("src/fortnite.gif").getImage();
		// timer schedule
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				if (titlescreenTimer == 0) {
					// main game functions. Actions/physics and painting the graphics
					doStuff();
					repaint();
				} else if (titlescreenTimer != 0) {
					// decreases titlescreenTimer until it reaches 0, where the game starts
					titlescreenTimer--;
				}
				// Win condition
				if (brick.getTotalBricks() == 0 && titlescreenTimer == 0) {
					win = true;
				}
				// Lose condition
				else if (lives == 0 && titlescreenTimer == 0) {
					lose = true;
				}
			}
		}, 15, 15);// delay for each cycle of timer schedule

		ballSlowDown();// decelerates ball
	}

	public void doStuff() {// method contains most of the physics and interaction methods

		ball.x += delta.x;
		ball.y -= delta.y;
		PaddleMove();
		BallBounce();
		BallDeath();
		Collision();
		BrickCollision();
		PowerUpMove();
		PowerUpCollision();
		PowerUpReset();
	}

	// method for ball to brick collision
	private void BrickCollision() {
		for (int row = 0; row < brick.getBricks().length; row++) {
			for (int col = 0; col < brick.getBricks()[0].length; col++) {
				int value = brick.getBricks()[row][col];
				// finding position of brick that got hit
				int brickx = col * brick.getBrickWidth();
				int bricky = row * brick.getBrickHeight() + brick.VERT_PAD;
				// creates a brick to check collision
				Rectangle actualBrick = new Rectangle(brickx, bricky, brick.getBrickWidth(), brick.getBrickHeight());
				if (brick.getBricks()[row][col] > 0) {
					if (ball.intersects(actualBrick)) {
						playSound("brick");
						brick.setBrick(row, col, value - 1); // lowers brick value (HP) so it becomes weaker or is
																// destroyed
						delta.y = -delta.y; // flips y value so the ball bounces off brick
						if (powerUpSpawnTimer == 0 && powerUpDuration == 0)
							SpawnPowerUp(brickx); // spawns power up where the brick broke
					}
				}
			}
		}
	}

	public void ballSlowDown() {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			public void run() {
				if (powerUpDuration != 0) {
					powerUpDuration--;// lowers power up duration at a constant rate
					if (powerUpDuration == 5) {
						timer = 5;
					}
				}
				if (timer != 0) {
					timer--; // incrementally decreases timer (for speed)
					if (delta.x > 2 || delta.x == -1) {
						delta.x--; // deceleration for right moving ball
					}
					if (delta.x < -2 || delta.x == 1) {
						delta.x++; // deceleration for left moving ball
					}
					if (delta.x == 0)
						delta.x += 2; // default speed
					if (delta.y == 0)
						delta.y += 5;// default speed
				}
			}

		}, 10, 400);// delay for each cycle of timer schedule

	}

	// method for ball to paddle collision
	public void Collision() {
		if (ball.intersects(paddle.getBounds2D())) {
			playSound("paddle");
			delta.y = -delta.y;// ball bounces off paddle sending it in the opposite direction
			if (moveL && delta.x > 0) {// if the ball and paddle are opposite directions the balls direction gets
										// flipped after colliding
				delta.x = -delta.x;
			}
			if (moveR && delta.x < 0) {// if the ball and paddle are opposite directions the balls direction gets
										// flipped after colliding
				delta.x = -delta.x;
			} else if (moveL && delta.x == -2 && timer == 0) {// if the ball and paddle are moving in the same direction
																// the ball gets sped up after colliding
				delta.x *= 4;
				timer = 10;
			} else if (moveR && delta.x == 2 && timer == 0) {// if the ball and paddle are moving in the same direction
																// the ball gets sped up after colliding
				delta.x *= 4;
				timer = 10;
			}
		}
	}

	// method for ball to bottom side collision (death/reset)
	public void BallDeath() {
		if (ball.y + 20 > 700) {
			playSound("death");
			// reset (default) coordinates for the ball
			ball.x = 500;
			ball.y = 350;
			delta = new Point(-2, -5);// gives the ball its initial change in direction
			paddle.x = 400;// default reset position for paddle
			lives--;// decrease lives
			powerUpDuration = 0; // stops currently acting power up
		}
	}

	// method for ball collision with sides and top wall
	public void BallBounce() {
		if (ball.y < 0) { // upper wall
			delta.y = -delta.y;
			playSound("sides");
		}
		if (ball.x < 0) { // left wall
			delta.x = -delta.x;
			playSound("sides");
		}
		if (ball.x + 20 > 1000) { // right wall
			delta.x = -delta.x;
			playSound("sides");
		}

	}

	// method handles paddle movement and wall boundaries
	public void PaddleMove() {
		if (paddleDecPower == true) { // if the paddle has been shrunk it can move further to the right against the
										// wall
			if (moveR && (paddle.x < 920)) {
				paddle.x += PaddleSpeed;
			} else if (moveL && (paddle.x > 0)) {
				paddle.x -= PaddleSpeed;
			} else
				paddle.x += 0;
		} else { // condition makes sure the paddle can't move off the screen
			if (moveR && (paddle.x < 845)) {
				paddle.x += PaddleSpeed;
			} else if (moveL && (paddle.x > 0)) {
				paddle.x -= PaddleSpeed;
			} else
				paddle.x += 0;
		}
	}

	// method spawns decides if power up will spawn, spawns power up where the block
	// broke,and sets a timer for when it disappears
	public void SpawnPowerUp(int spawnX) { // handles the spawn of power ups with rng
		powerUpSpawn = rng.nextInt(15);// random number generator
		// ((powerUpSpawn == 2) || (powerUpSpawn == 1) || (powerUpSpawn == 3) ||
		// (powerUpSpawn == 0))
		if (powerUpSpawnTimer == 0 && powerUpSpawn <= 5) { // if there isn't a power up spawned, and the random number
															// comes to 5 or under, a power up will spawn
			powerUp.x = spawnX + 45;// spawns where the block broke
			powerUp.y = 300;
			powerUpSpawnTimer = 350;
			PowerUpMove();
		}

	}

	// method causes power up to descend toward the paddle, and slow down when near
	// the bottom of the screen
	public void PowerUpMove() {
		if (powerUpSpawnTimer != 0) {
			if (powerUp.y < 611) {
				powerUp.y += 2.5;
			} else {
				powerUp.y += 1;
			}
			powerUpSpawnTimer--;
		} else {
			powerUp.y = -50;
		}
	}

	// method reset to default values after a power up has ended
	public void PowerUpReset() {
		if (powerUpDuration == 0) {
			PaddleSpeed = 5;
			paddle.width = 150;
			paddleIncPower = false;
			paddleDecPower = false;
			speedPower = false;
			PowerUpLIFE = false;
			PaddleIncSpeed = false;
			PaddleDecSpeed = false;
		}
	}

	// method checks for paddle to power up collision and calls to power up type
	// method to cause power up effect
	public void PowerUpCollision() { // checks for paddle and power up collision
		if (paddle.intersects(powerUp)) {
			playSound("powerup");
			powerUpSpawnTimer = 0;
			powerUp.y = -50; // moves power up off screen
			powerUpDuration = 20; // duration for power ups

			// checking for which power up to spawn
			if (powerUpSpawn == 0) {
				PowerUpLIFE();
			} else if (powerUpSpawn == 1) {
				PowerUpSPEED();
			} else if (powerUpSpawn == 2) {
				PowerUpSIZEinc();
			} else if (powerUpSpawn == 3) {
				PowerUpSIZEdec();
			} else if (powerUpSpawn == 4) {
				PowerUpPadSpdInc();
			} else if (powerUpSpawn == 5) {
				PowerUpPadSpdDec();
			}
		}

	}

	// Power Ups!

	private void PowerUpPadSpdDec() {// Decrease move speed of paddle
		PaddleSpeed = 3;
		PaddleDecSpeed = true;
	}

	private void PowerUpPadSpdInc() {// increase move speed of paddle
		PaddleSpeed = 8;
		PaddleIncSpeed = true;
	}

	private void PowerUpLIFE() {// gives additional life
		lives++;
		PowerUpLIFE = true;
		powerUpDuration = 5;
	}

	private void PowerUpSIZEdec() {// decreases size of paddle
		paddle.width = 70;
		paddleDecPower = true;
	}

	public void PowerUpSIZEinc() {// increases size of paddle
		paddle.width = 200;
		paddleIncPower = true;
	}

	public void PowerUpSPEED() {// increases move speed of ball
		if (powerUpDuration != 0 && speedPower == false) {
			delta.x *= 3;
			speedPower = true;
		}
	}

	@Override
	public void paintComponent(Graphics g) { // central method for graphics and handling what's drawn on screen
		super.paintComponent(g);// resets graphics
		this.setBackground(new Color(32, 32, 32));// background colour

		// Title screen graphics
		if (titlescreenTimer > 0)
			titlescreen(g);

		else if (titlescreenTimer == 0 && win == false && lose == false)
			mainGameGraphics(g);

		// Win Condition graphics
		else if (win)
			winscreen(g);

		// Lose Condition graphics
		else if (lose)
			losescreen(g);
	}

	// graphics for main game functions and objects
	public void mainGameGraphics(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// good power up
		if (powerUpSpawn == 2 || powerUpSpawn == 0 || powerUpSpawn == 4) {
			g2.setColor(new Color(11, 232, 129));// Green color
			g2.fill(powerUp);
		}
		// bad power up
		if (powerUpSpawn == 1 || powerUpSpawn == 3 || powerUpSpawn == 5) {
			g2.setColor(Color.red);
			// g2.setFont(new Font("Consolas", Font.BOLD, 20));
			g2.fill(powerUp);
		}
		g.setColor(Color.white);
		g2.fill(ball);
		g2.setStroke(new BasicStroke(1));
		g2.setColor(new Color(255, 71, 87));

		// paddle (and outline)
		g2.fill(paddle);
		g.setColor(Color.white);
		g2.draw(paddle);

		brick.draw(g2); // draws layout of bricks
		g2.setFont(new Font("Consolas", Font.PLAIN, 20));
		g2.setColor(new Color(241, 196, 15)); // This colour was called Sunflower. So I picked it cuz Sunflower - Post
												// Malone is a banger. -Devin
		// Useful information for user when playing game
		g2.drawString("Total Bricks Left: " + brick.getTotalBricks(), 10, 20);
		if (brick.getTotalBricks() == 1) {
			g2.drawString("We're in the endgame now. ", 350, 400);
		}
		g2.drawString("lives: " + lives, 600, 20);

		// Notification for the type power up, and the duration.
		g2.setFont(new Font("SansSerif", Font.BOLD, 15));
		if (PaddleDecSpeed) {
			g2.drawString(">Paddle Speed Decreased!", 400, 450);
			g2.drawString("Duration: " + powerUpDuration, 400, 500);
		}
		if (PaddleIncSpeed) {
			g2.drawString(">Paddle Speed Increased!", 400, 450);
			g2.drawString("Duration: " + powerUpDuration, 400, 500);
		}
		if (paddleIncPower) {
			g2.drawString(">Paddle Size Increased!", 400, 450);
			g2.drawString("Duration: " + powerUpDuration, 400, 500);
		}
		if (paddleDecPower) {
			g2.drawString(">Paddle Size Decreased!", 400, 450);
			g2.drawString("Duration: " + powerUpDuration, 400, 500);
		}
		if (speedPower && ((delta.x != 2) || (delta.x != -2))) {
			g2.drawString(">Ball Speed increased!", 400, 350);
			g2.drawString("Duration: " + powerUpDuration, 400, 400);
		}
		if (PowerUpLIFE) {
			g2.drawString(">An extra life!", 400, 450);
		}
	}

	// title screen graphics
	public void titlescreen(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		// Ball and lines
		g.setColor(Color.white);
		g.fillOval(5, 275, 100, 100);
		g2.drawLine(85, 269, 145, 239);
		g2.drawLine(95, 280, 152, 250);
		g2.drawLine(100, 292, 158, 262);
		// Title
		g2.setFont(new Font("SansSerif", Font.BOLD, 150));
		g2.setColor(new Color(183, 21, 64)); // jalapeno red
		g2.drawString("Breakout", 170, 300);
		// Rectangle Framing
		g2.drawRect(170, 185, 765, 250);
		g2.drawLine(170, 310, 935, 310);
		// The 2 coloured rectangles
		g2.setStroke(new BasicStroke(1));
		g2.setColor(new Color(41, 128, 185));
		g2.fillRect(840, 190, 85, 115);
		g2.setColor(new Color(231, 76, 60));
		g2.fillRect(840, 315, 85, 115);
		// Name Credits
		g2.setFont(new Font("Calibri", Font.PLAIN, 35));
		g2.setColor(new Color(183, 21, 64));
		g2.drawString("Created by: Sachet, Yathartha, and Devin", 250, 375);
		// Instructions
		g2.setFont(new Font("TimesRoman", Font.ITALIC, 30));
		g2.setColor(Color.white);
		g.drawString("Destroy the bricks and keep the ball from falling!", 165, 498);
		g.drawString("Green power ups are helpful, red power ups are detrimental.", 110, 530);
		// Controls
		g2.setFont(new Font("Calibri", Font.BOLD, 30));
		g.drawString("Use A and D to move the paddle.", 270, 575);
		g.drawString("To start the game immediately press A or D.", 210, 630);
		g2.setFont(new Font("Calibri", Font.ITALIC, 20));
		g.drawString("Note: You may only move in one direction at a time.", 260, 595);
	}

	// graphics for winning
	public void winscreen(Graphics g) {
		g.drawImage(winImg, 0, 0, null);
		delta.x = 0;
		delta.y = 0;
		if (playSong==0)
		playSound("winmusic");
		playSong=1;
	}

	// graphics for losing
	public void losescreen(Graphics g) {
		g.drawImage(loseImg, 0, 0, null);
		delta.x = 0;
		delta.y = 0;
		if (playSong==0)
		playSound("losemusic");
		playSong=1;
		g.drawImage(fortnite, -150, 150, null);
	}

	// Method processes keyboard input
	public void processKeyEvent(KeyEvent e) {
		if (e.getID() == KeyEvent.KEY_PRESSED) {
			if (titlescreenTimer >= 0)
				titlescreenTimer = 0;
			if ((e.getKeyCode() == KeyEvent.VK_A))
				moveL = true;
			if (e.getKeyCode() == KeyEvent.VK_D)
				moveR = true;
		}
		// when the key is released, the movement (in that direction) is stopped
		if (e.getID() == KeyEvent.KEY_RELEASED) {
			if (e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D) {
				moveL = false;
				moveR = false;
			}
		}
	}

	// method for audio and sounds to play during certain interactions
	public synchronized void playSound(String get) {
		AudioInputStream audio = null;
		try {

			if (get.equals("brick")) {
				audio = AudioSystem.getAudioInputStream(new File("src/brickhit.wav"));
			}
			if (get.equals("paddle")) {
				audio = AudioSystem.getAudioInputStream(new File("src/paddlehit.wav"));
			}
			if (get.equals("sides")) {
				audio = AudioSystem.getAudioInputStream(new File("src/Boing.wav"));
			}
			if (get.equals("death")) {
				audio = AudioSystem.getAudioInputStream(new File("src/bruh.wav"));
			}
			if (get.equals("powerup")) {
				audio = AudioSystem.getAudioInputStream(new File("src/powerup.wav"));
			}
			if (get.equals("winmusic")) {
				audio = AudioSystem.getAudioInputStream(new File("src/winscreenbruh.wav"));
			}
			if (get.equals("losemusic")) {
				audio = AudioSystem.getAudioInputStream(new File("src/Default.wav"));
			}
			Clip clip = AudioSystem.getClip();
			clip.open(audio);
			clip.start();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		breakout game = new breakout();
		JFrame win = new JFrame();
		win.setTitle("Breakout"); // title in top bar
		win.setSize(1010, 735); // size of window
		win.setResizable(false);
		win.setLayout(new BorderLayout());
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		win.add(game);
		win.setVisible(true);
	}

	public boolean isFocusable() {
		return true;
	}
}



