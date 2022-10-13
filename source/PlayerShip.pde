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
  void addPoint() {
    points += 1;
  }
  
  // The player's artillery is displayed as a rectangle
  void draw() {
    fill(255) ;
    rect(position.x, position.y, playerWidth, playerHeight) ;
  }
  
  // Handle movement (left and right, ensures the artillery does not leave the play area)
  void moveLeft() {
    position.x -= moveIncrement ;
    if (position.x < 0) position.x = 0 ;
  }
  void moveRight() {
    position.x += moveIncrement ;
    if (position.x > displayWidth-playerWidth) 
      position.x = displayWidth-playerWidth ;
  }  
  
  // Handles the placement of the artillery
  void onTerrain(Terrain terrain) {
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
