import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ArtillerySketch extends PApplet {


// Some constants used to derive sizes for game elements from display size
final int PLAYER_WIDTH_PROPORTION = 50,
          PLAYER_HEIGHT_PROPORTION = 35,
          PLAYER_INIT_X_PROPORTION = 2,
          PLAYER_INCREMENT_PROPORTION = 500 ;
final int MISSILE_WIDTH_PROPORTION = 6,
          MISSILE_HEIGHT_PROPORTION = 4,
          MISSILE_INCREMENT_PROPORTION = 10 ;
final int SPAWN_POINT_PROPORTION = 20 ;

// temporarily contains the velocity of missile
PVector force; 
// The player and the missile
PlayerShip player ;
PlayerShip player2 ;
PlayerMissile missile ;
// Booleans are used instead of polling the keyboard every frame.
boolean firing;
boolean movingLeft;
boolean movingRight;
boolean incElevation;
boolean decElevation;
boolean incStrength;
boolean decStrength;
// Locks game state in the event a player wins
boolean lock;
// Start screen, if false, game starts
boolean startScreen;
// indication of current and next player, swapped often
PlayerShip currentPlayer;
PlayerShip nextPlayer;
// count the number of turns the same wind has occured, resets after 10 turns
int windTurnCount;

int restartX, restartY; // coordinates for win button
int restartSize;  // size of button
int restartColor;  // color of restart button
int restartHighlight; // highlight color
boolean restartOver; // check if hovering over restart button

// used to generate the terrain
int locX;
int locY;
float blockSizeX;
float blockSizeY;
int numBlocks;
ArrayList<Terrain> terrain;

// Cloud objects
Cloud cloud;
Cloud cloud2;
Cloud cloud3;

// Holds the PVector for wind used for accumulated forces and to shift the clouds
PVector wind;

// Initialise display and game elements
public void setup() {
  // Program made to run in fullscreen
  
  //frameRate(40) ;
  textSize(20) ;
  startScreen = true;
  reset();
}

// when the game ends, used to restart the game if the player wants to
public void reset() {
  // initialise the player. 
  int playerWidth = displayWidth/PLAYER_WIDTH_PROPORTION ;
  int playerHeight = displayHeight/PLAYER_HEIGHT_PROPORTION;
  int playerInitX = displayWidth/SPAWN_POINT_PROPORTION ;
  int playerInitY = displayHeight - playerHeight;
  int playerIncrement = displayWidth/PLAYER_INCREMENT_PROPORTION ;    
  
  // defaulting
  firing = false ;
  movingLeft = false ;
  movingRight = false ;
  incElevation = false ;
  decElevation = false ;
  incStrength = false ;
  decStrength = false ;
  lock = false ;
  windTurnCount = 0;
  restartSize = 90;
  restartOver = false;
  terrain = new ArrayList<Terrain>() ;
  
  // restart button initializing for end of the game usage
  restartColor = color(255);
  restartHighlight = color(204);
  restartX = displayWidth/2-restartSize-10;
  restartY = displayHeight/2-restartSize/2;
  
  // initialise players
  player = new PlayerShip("Player 1", playerInitX, playerInitY, playerWidth, playerHeight, playerIncrement) ;
  player2 = new PlayerShip("Player 2", displayWidth - playerInitX - playerWidth, playerInitY, playerWidth, playerHeight, playerIncrement) ;
  // not initially firing, but initialise missile object
  missile = new PlayerMissile(0, 0, playerWidth/MISSILE_WIDTH_PROPORTION, playerHeight/MISSILE_HEIGHT_PROPORTION, MISSILE_INCREMENT_PROPORTION) ;
  // set current and next players
  currentPlayer = player;
  nextPlayer = player2;
  // calculate the initial wind
  wind = new PVector(random(-0.3f, 0.3f), 0) ;
  // set the wind force for the missile to be accumulated
  missile.setWind(wind) ;
  // initialise the clouds
  cloud = new Cloud(displayWidth/10, displayHeight/10, playerWidth, playerHeight);
  cloud2 = new Cloud(displayWidth/2, displayHeight/7, playerWidth, playerHeight);
  cloud3 = new Cloud(displayWidth - displayWidth/10, displayHeight/9, playerWidth, playerHeight);
  // provide the clouds with wind force
  cloud.setVel(wind);
  cloud2.setVel(wind);
  cloud3.setVel(wind);
  
  // Terrain generation values
  locX = 0;
  locY = displayHeight;
  blockSizeX = player.getWidth()*2 ;
  blockSizeY = player.getHeight() ;
  //initial left most column of terrain blocks can be 4 to 14 blocks high
  numBlocks = (int) random(4, 15);
  
  // creates and adds terrain into the terrain arraylist
  while (locX < displayWidth) {
    for (int i = numBlocks; i >= 0; i--) {
      terrain.add(new Terrain(locX, locY, blockSizeX, blockSizeY));
      locY -= blockSizeY;
    }
    // back to the bottom of the screen
    locY = displayHeight;
    // move to next column of terrain
    locX += blockSizeX;
    // randomizes the next number of blocks from removing a block to adding a block to randomly generate the terrain
    numBlocks += random(-1, 2) ;
    // ensures there is always a block and the blocks don't go negative
    if (numBlocks < 1) {
      numBlocks = 1;
    }
  }
}


// update and render
public void draw() {
  
  // Checks if the start screen is up and when its flipped to false start the rendering
  if (!startScreen) {
  
  // update mouse when needed for the restart button
  update();
  
  // sets the background to sky blue
  background(135,206,235) ;
  
  // render clouds and cloud movement
  cloud.draw();
  cloud2.draw();
  cloud3.draw();
  cloud.move();
  cloud2.move();
  cloud3.move();
  
  // renders the terrain
  for (Terrain blocks : terrain ) {
    blocks.draw();
  }
  
  // Fills green text for current game state
  fill(44, 176, 55) ;
  text("Player 1 Score: " + player.getPoints(), 0, 20);
  text("Player 2 Score: " + player2.getPoints(), 0, 40);
  
  if (currentPlayer == player) {
    text("Current Player: " + currentPlayer.getPName() , displayWidth/2-displayWidth/10, 20);
    text("Elevation: " + missile.getElevation() + " degrees", displayWidth/2-displayWidth/10, 120);
  }
  
  if (currentPlayer == player2) {
    text("Current Player: " + currentPlayer.getPName() , displayWidth/2-displayWidth/10, 20);
    text("Elevation: " + (180 - missile.getElevation()) + " degrees", displayWidth/2-displayWidth/10, 120);
  }
  
  text("Wind: " + missile.getWind().x, displayWidth/2-displayWidth/10, 40);
  
  if (missile.getWind().x < -0.1f) {
    text("Strong Wind Towards West", displayWidth/2-displayWidth/10, 80);
  }
  else if (missile.getWind().x > 0.1f) {
    text("Strong Wind Towards East", displayWidth/2-displayWidth/10, 80);
  }
  else if (missile.getWind().x >= -0.1f && missile.getWind().x < -0.01f) {
    text("Weak Wind Towards West", displayWidth/2-displayWidth/10, 80);
  }
  else if (missile.getWind().x <= 0.1f && missile.getWind().x > 0.01f) {
    text("Weak Wind Towards East", displayWidth/2-displayWidth/10, 80);
  }
  else {
    text("Very Weak Wind (Negligible)", displayWidth/2-displayWidth/10, 80);
  }
  
  text("Turns Until Wind Changes: " + (20-windTurnCount), displayWidth/2-displayWidth/10, 60);
  text("Magnitude: " + missile.getStrength(), displayWidth/2-displayWidth/10, 100);
  text("Velocity Of Missile: (" +missile.getVelocityx()+ ", " +missile.getVelocityy()+ ")", displayWidth/2-displayWidth/10, 140);
  
  // calculates the artillary on the terrain for each position on the terrain to ensure its above the terrain
  for (Terrain blocks : terrain ) {
   player.onTerrain(blocks);
   player2.onTerrain(blocks);
  }
  
  // turns on gravity if the block is not already at the bottom of the screen
  if (player.getPos().y == displayHeight - player.getHeight()) {
    player.setFall(false);
  } else {
    player.setFall(true);
  }
  // turns on gravity if the block is not already at the bottom of the screen
  if (player2.getPos().y == displayHeight - player.getHeight()) {
    player2.setFall(false);
  } else {
    player2.setFall(true);
  }
  

  // initial attempt to keep artillary above terrain
  //if (!top) {
  //for (int i = 0; i < terrain.size(); i++) {
  //  if (player.getPos().x + player.getWidth() >= terrain.get(i).getPos().x &&
  //      player.getPos().x <= terrain.get(i).getPos().x + terrain.get(i).getWidth() &&
  //      player.getPos().y + player.getHeight() >= terrain.get(i).getPos().y &&
  //      player.getPos().y <= terrain.get(i).getPos().y + terrain.get(i).getWidth()) {
  //    player.addHeight(terrain.get(i).getHeight());
  //  }
  //  if (player2.getPos().x + player2.getWidth() >= terrain.get(i).getPos().x &&
  //      player2.getPos().x <= terrain.get(i).getPos().x + terrain.get(i).getWidth() &&
  //      player2.getPos().y + player2.getHeight() >= terrain.get(i).getPos().y &&
  //      player2.getPos().y <= terrain.get(i).getPos().y + terrain.get(i).getWidth()) {
  //    player2.addHeight(terrain.get(i).getHeight());
  //  }
  //}
  //top = true;
  //}
  //for (int i = 0; i < terrain.size(); i++) {
  //  if (player.getPos().x + player.getWidth() + player.getMoveIncrement() >= terrain.get(i).getPos().x &&
  //      player.getPos().x <= terrain.get(i).getPos().x + terrain.get(i).getWidth() &&
  //      player.getPos().y + player.getHeight() >= terrain.get(i).getPos().y &&
  //      player.getPos().y <= terrain.get(i).getPos().y + terrain.get(i).getWidth()) {
  //    movingRight = false;
  //  }
  //}
  //for (int i = 0; i < terrain.size(); i++) {
  //  if (player.getPos().x + player.getWidth() >= terrain.get(i).getPos().x &&
  //      player.getPos().x - player.getMoveIncrement() <= terrain.get(i).getPos().x + terrain.get(i).getWidth() &&
  //      player.getPos().y + player.getHeight() >= terrain.get(i).getPos().y &&
  //      player.getPos().y <= terrain.get(i).getPos().y + terrain.get(i).getWidth()) {
  //    movingLeft = false;
  //  }
  //}
  
  // limit first artilery movement
  if (player.getPos().x > displayWidth/4) {
     player.getPos().x = displayWidth/4; 
    }
    
  // limit second artillery movement
  if (player2.getPos().x < displayWidth - displayWidth/4) {
     player2.getPos().x = displayWidth - displayWidth/4; 
  }

  // renders both artillery
  player.draw() ;
  player2.draw() ;
  
  // checks if the state is locked, state is locked when a winner is found
  if (!lock) {
  // the current player can move and change the strength and elevation of the artillery when lock = false
  if (movingLeft) {
    currentPlayer.moveLeft() ;
  }
  else if (movingRight) {
    currentPlayer.moveRight() ; 
  }
  if (incStrength) {
    missile.incMag() ;
  } 
  else if (decStrength) {
    missile.decMag() ;
  }
  if (incElevation) {
    missile.incEle() ;
  } 
  else if (decElevation) {
    missile.decEle() ;
  }
  
  // the missile
  if (firing) {
    // check if missile is moving, integration of accunmulate forces occurs here.
    if (missile.move()) {
      // Collision detection on artillery
      if (missile.getPos().x + missile.getWidth() >= nextPlayer.getPos().x &&        // shell right edge past artillery left
          missile.getPos().x <= nextPlayer.getPos().x + nextPlayer.getWidth() &&     // shell left edge past artillery right
          missile.getPos().y + missile.getHeight() >= nextPlayer.getPos().y &&       // shell top edge past artillery bottom
          missile.getPos().y <= nextPlayer.getPos().y + nextPlayer.getHeight()) {    // shell bottom edge past artillery top
        // Score the current player
        currentPlayer.addPoint();
        // checks to see if current player reached a score of 10, if not swap players
        checkScore(); 
      }
      // Collision detection for blocks. Has a "feature" in which when two blocks are destroyed with one pellet, the current player does not change. Similar to above collision detection.
      for (int i = 0; i < terrain.size(); i++ ) {
        if (missile.getPos().x + missile.getWidth() >= terrain.get(i).getPos().x &&        
            missile.getPos().x <= terrain.get(i).getPos().x + terrain.get(i).getWidth() &&     
            missile.getPos().y + missile.getHeight() >= terrain.get(i).getPos().y &&       
            missile.getPos().y <= terrain.get(i).getPos().y + terrain.get(i).getHeight()) { 
         terrain.remove(i);
         i--;
         // swap players after a piece of terrain is destroyed
         swapPlay();
      }
      }
      // rendering missile
      missile.draw() ;
    }
    else {
      // swap players in the event of a missing a target or terrain
      swapPlay();
    }
  }
  // check if same wind occurs for 20 turns, when it happens, generate new wind and set the clouds to follow the wind and reset the wind turn counter
  if (windTurnCount == 20) {
    wind = new PVector(random(-0.3f, 0.3f), 0);
    missile.setWind(wind);
    cloud.setVel(wind);
    cloud2.setVel(wind);
    cloud3.setVel(wind);
    windTurnCount = 0 ;
  }
  }
  // enters the locked state if a player won
  if (lock) {
    // displays player who won
    text(currentPlayer.getPName() + " Wins", displayWidth/2-displayWidth/10, displayHeight/6);
    // restart button created which highlights when hovered on and restarts the game when pressed
    if (restartOver) {
      fill(restartHighlight);
    } else {
      fill(restartColor);
    }
    stroke(0);
    rect(restartX, restartY, restartSize, restartSize/2);
    fill(0);
    text("Restart", restartX+restartSize/8, restartY+restartSize/3);
  }
  }
  // start screen for the artillery game displaying the goal, controls, an additional note for the second players and how to start the multiplayer
  if (startScreen) {
    fill(0);
    text("The Artillary Game", displayWidth/2-displayWidth/10, 30);
    text("The goal is to hit the opponent with 10 shells", displayWidth/2-displayWidth/6, 60);
    text("Keybinds: ", 10, 30);
    text("Right Arrow Key = move right", 10, 60);
    text("Left Arrow Key = move left", 10, 90);
    text("Up Arrow Key = increase angle in degrees of artillary", 10, 120);
    text("Down Arrow Key = decrease angle in degrees of artillary", 10, 150);
    text("> = increase magnitude of shot", 10, 180);
    text("< = decrease magnitude of shot", 10, 210);
    text("*Note that the 2nd player starts at 180 degrees and both up and down arrows keys perform the opposite tasks ", 10, 240);
    text("Press enter for multiplayer", 10, 270);
  }
}



// Read keyboard for input.
public void keyPressed() {
  // space to fire   
  if (key == ' ') {
    fire() ; 
  }  
  
  // keys to move, change elevation and change strength of shell.
  if (!firing) {
    if (key == CODED) {
       switch (keyCode) {
         case LEFT :
           movingLeft = true ;
           break ;
         case RIGHT :
           movingRight = true ;
           break ;
         case UP :
           incElevation = true ;
           break ;
         case DOWN :
           decElevation = true ;
           break ;
       }
    }
    if (key == '.') {
      incStrength = true ;
    }
    if (key == ',') {
      decStrength = true ;
    }
    // click enter to proceed after start screen
    if (keyCode == ENTER ) {
      startScreen = false;
    }
  }
}
public void keyReleased() {
  if (key == CODED) {
     switch (keyCode) {
       case LEFT :
         movingLeft = false ;
         break ;
       case RIGHT :
         movingRight = false ;
         break ;
       case UP :
         incElevation = false ;
         break ;
       case DOWN :
         decElevation = false ;
         break ;
     }
  }  
  if (key == '.') {
    incStrength = false ;
  }
  if (key == ',') {
    decStrength = false ;
  }
}

// mouse press to restart the game when ended
public void mousePressed() {
  if (restartOver) {
    reset();
  }
}


// initiate firing, if missile not already there.
public void fire() {
    if (!firing) {
    // saved elevation and strength state for individual players
    currentPlayer.setElevation(missile.getElevation());
    currentPlayer.setStrength(missile.getStrength());
    // calculate the normal vector based off the (1, 0) vector and and angle to find the normal at the specific angle
    force = centerRotation(new PVector(1, 0), missile. getElevation()) ;
    // multiply the normal vector with the magnitude which was divided by 100 (as it was too fast) to get the velocity
    force.mult(missile.getStrength()/100) ;
    // set the missiles initial velocity
    missile.velocity = force ;
    // both cannons are fired at the top left/right corners
    if (currentPlayer == player) {
      missile.reset(currentPlayer.getX() + currentPlayer.playerWidth, currentPlayer.getY() - currentPlayer.playerHeight/MISSILE_WIDTH_PROPORTION) ;
    }
    if (currentPlayer == player2) {
      missile.reset(currentPlayer.getX(), currentPlayer.getY() - currentPlayer.playerHeight/MISSILE_WIDTH_PROPORTION) ;
    }
    // start the firing
    firing = true ;
  }
}

// calculation to get the normal vector at an angle 
public PVector centerRotation(PVector p, double theta) {
  // -theta was used to ensure the angle was anti-clockwise about the normal vector (1, 0) and converted to radians for calculation
  theta = -theta * Math.PI / 180 ;
  float sinT = (float) Math.sin(theta);
  float cosT = (float) Math.cos(theta);
  // x = rcosB, y = rsinB
  // x’ = rcos(A+B) = r(cosAcosB – sinAsinB) = rcosBcosA – rsinBsinA = xcosA – ysinA 
  // y’ = rsin(A+B) = r(sinAcosB + cosAsinB) = rcosBsinA + rsinBcosA = xsinA + ycosA
  // used to find x’ and y’ values which is the transformed vector
  return new PVector(cosT * p.x - sinT * p.y, sinT * p.x + cosT * p.y);
}
  
// checks to see if the missile has stopped firing, swapes both players and increments wind turn count
public void swapPlay() {
  firing = false ;
  if (currentPlayer == player) {
    windTurnCount += 1 ;
    currentPlayer = player2 ; 
    nextPlayer = player;
  } else {
    windTurnCount += 1;
    currentPlayer = player ;
    nextPlayer = player2 ;
  }
  missile.setStr(currentPlayer.getStrength()) ;  
  missile.setEle(currentPlayer.getElevation()) ;  
}

// checks to see if current player reached a score of 10, if not swap players
public void checkScore() {
  if (currentPlayer.getPoints() == 10) {
    lock = true;
  } else {
    swapPlay();
  }
}

// updates when the mouse is over the restart rectangle
public void update() {
  if(overRect(restartX, restartY, restartSize, restartSize/2)) {
    restartOver = true;
  } else {
    restartOver = false;
  }
}

// cursor over rectangle
public boolean overRect(int x, int y, int width, int height)  {
  if (mouseX >= x && mouseX <= x+width && 
      mouseY >= y && mouseY <= y+height) {
    return true;
  } else {
    return false;
  }
}
final class Cloud {
  // cloud represents which way the wind is blowing and strength of wind
  PVector position, velocity;
  float cloudWidth, cloudHeight ;
  
  // setter 
  public void setVel(PVector wind){velocity = wind;}
  
  // constructor
  Cloud(int x, int y, float cloudWidth, float cloudHeight) {
   position = new PVector(x, y);
   this.cloudWidth = cloudWidth;
   this.cloudHeight = cloudHeight;
   velocity = new PVector(0, 0);
  }
  
  // same shape as artillery but grey color
  public void draw() {
    fill(204) ;
    rect(position.x, position.y, cloudWidth, cloudHeight) ;
  }
  
  // moves the clouds left or right, if the cloud leaves the screen, it re-enters from the other side.
  public void move() {
   // Add velocity twice to make clouds appear faster
   position.add(velocity) ;
   position.add(velocity) ;
   if (position.x + cloudWidth <= 0) {
    position.x = displayWidth - cloudWidth; 
   }
   if (position.x > displayWidth) {
    position.x = 0; 
   }
  }
  
}
final class PlayerMissile {
  // The missile is represented by a small rectangle for easier implementation of collision.
  // the position field indicates the top-left of that rectangle.  
  PVector position, velocity;
  PVector gravity = new PVector(0, 0.49f) ;
  PVector wind = new PVector(0, 0) ;
  float missileWidth, missileHeight ;
  int moveIncrement ;
  int elevation ;
  int strength ;
  private static final float DAMPING = .995f ;
  
  // getters and setters
  public int getStrength() {return strength ;}
  public int getElevation() {return elevation ;}
  public float getVelocityx() {return velocity.x ;}
  public float getVelocityy() {return velocity.y ;}
  public int setStr(int val) {return strength = val ;}
  public int setEle(int val) {return elevation = val ;}
  public PVector setWind(PVector wind) {return this.wind = wind ;}
  public PVector getWind() {return wind ;}
  public PVector getPos() {return position ;}
  public float getWidth() {return missileWidth;}
  public float getHeight() {return missileHeight;}
  public int getMoveIncrement() {return moveIncrement;}
  
  // constructor
  PlayerMissile(int x, int y, float missileWidth, float missileHeight, int moveIncrement) {;
    position = new PVector(x, y) ;
    this.missileWidth = missileWidth ;
    this.missileHeight = missileHeight ;
    this.moveIncrement = moveIncrement ;
    elevation = 0;
    strength = 0;
    velocity = new PVector(0, 0) ;
    wind = new PVector(0, 0) ;
  }
 
  // reuse this object rather than go through object creation
  public void reset(float x, float y) {
    position.x = x ;
    position.y = y ;
  }
  
  // The missile is displayed as a rectangle
  public void draw() {
    fill(200) ;
    rect(position.x, position.y, missileWidth, missileHeight) ;
  }
  
  // handle movement. Returns true missile still flying
  // collision handled in main method
  // boolean flipped when missile outside play area
  public boolean move() {
    
    // update position
    position.add(velocity) ;
    
    // update velocity
    velocity.add(gravity) ;
    velocity.add(wind) ;
    
    // apply damping
    velocity.mult(DAMPING) ;
      
    
    return (position.y) >= 0 && position.y <= displayHeight && position.x >= 0 && position.x <= displayWidth;
  }  
  
  // increase magnitude of shot
  public void incMag() {
   strength += moveIncrement ;
  }
  
  // decrease magnitude of shot
  public void decMag() {
   if (strength > moveIncrement) {
     strength -= moveIncrement ;
   }
   else {
     strength = 0;
   }
  }
  
  // increase angle of elevation in degrees, 0 being east and 180 being west, not allowed to fire below artillery.
  public void incEle() {
   if (elevation < 180) {
     elevation += 1 ;
   }
   else {
     elevation = 180;
   }
  }
  
  // decrease angle of elevation in degrees,
  public void decEle() {
   if (elevation > 0) {
     elevation -= 1 ;
   }
   else {
     elevation = 0;
   }
  }
}
final class PlayerShip {
  // The player's artillery is represented as a rectangle in which the top cornet which faces the oppenent is where the shell fires from.
  // the position field indicates the top-left of that rectangle.
  PVector gravity = new PVector(0, 0.1f) ;
  PVector position ;
  float playerWidth, playerHeight ;
  int moveIncrement ;
  int points ;
  String name;
  int elevation;
  int strength;
  boolean fall; 
  
  // getters and setters
  public float getX() {return position.x ;}
  public float getY() {return position.y ;}
  public PVector getPos() {return position;}
  public float getWidth() {return playerWidth;}
  public float getHeight() {return playerHeight;}
  public int getPoints() {return points;}
  public String getPName() {return name;}
  public int getElevation() {return elevation;}
  public int getStrength() {return strength;}
  public int getMoveIncrement() {return moveIncrement;}
  public void setElevation(int elevation) {this.elevation = elevation ;}
  public void setStrength(int strength) {this.strength = strength ;}
  public void setFall(boolean boo) {this.fall = boo;}
  
  // constructor
  PlayerShip(String name, int x, int y, float playerWidth, float playerHeight, int moveIncrement) {
    position = new PVector(x, y) ;
    this.name = name;
    this.playerWidth = playerWidth ;
    this.playerHeight = playerHeight ;
    this.moveIncrement = moveIncrement ;
    points = 0;
    elevation = 0;
    strength = 0;
    fall = true;
  }
  
  // adding points to players
  public void addPoint() {
    points += 1;
  }
  
  // The player's artillery is displayed as a rectangle
  public void draw() {
    fill(255) ;
    rect(position.x, position.y, playerWidth, playerHeight) ;
  }
  
  // Handle movement (left and right, ensures the artillery does not leave the play area)
  public void moveLeft() {
    position.x -= moveIncrement ;
    if (position.x < 0) position.x = 0 ;
  }
  public void moveRight() {
    position.x += moveIncrement ;
    if (position.x > displayWidth-playerWidth) 
      position.x = displayWidth-playerWidth ;
  }  
  
  // Handles the placement of the artillery
  public void onTerrain(Terrain terrain) {
    // Adds gravity to ensure the artillery is on the terrain
    if(fall) {
      position.add(gravity);
    }
    // gets the x position of the artillery and finds terrain within the same x axis range
    if (position.x + playerWidth > terrain.getPos().x && position.x + playerWidth < terrain.getPos().x + (terrain.getWidth() + playerWidth)) {
      // check if the artillary is lower than the terrains y coordinate
      if (position.y > terrain.getPos().y - playerHeight) {
       // if artillary is lower, shift it higher and stop gravity in case the object falls below the terrain.
       position.y = terrain.getPos().y - playerHeight;
       fall = false;
      }
    }
    // checks if the artillery is below the play area and sets it to the play area if it isn't
    if (position.y >= displayHeight) {
     position.y = displayHeight - playerHeight;
     fall = false;
    }
  }
  
}
final class Terrain {
  // terrain generation for the blocks under the artillery.
  PVector position;
  float terrainWidth, terrainHeight;
  
  // getters
  public PVector getPos() {return position ;}
  public float getWidth() {return terrainWidth ;}
  public float getHeight() {return terrainHeight ;}
  
  // constructors
  Terrain(int x, int y, float terrainWidth, float terrainHeight) {
    position = new PVector(x, y);
    this.terrainWidth = terrainWidth;
    this.terrainHeight = terrainHeight;
  }  
  
  // draw the terrain blocks with a dirt brown color
  public void draw() {
    fill(131,101,57);
    rect(position.x, position.y, terrainWidth, terrainHeight) ;
  }
}
  public void settings() {  fullScreen() ; }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ArtillerySketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
