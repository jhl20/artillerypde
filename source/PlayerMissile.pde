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
  void reset(float x, float y) {
    position.x = x ;
    position.y = y ;
  }
  
  // The missile is displayed as a rectangle
  void draw() {
    fill(200) ;
    rect(position.x, position.y, missileWidth, missileHeight) ;
  }
  
  // handle movement. Returns true missile still flying
  // collision handled in main method
  // boolean flipped when missile outside play area
  boolean move() {
    
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
  void incMag() {
   strength += moveIncrement ;
  }
  
  // decrease magnitude of shot
  void decMag() {
   if (strength > moveIncrement) {
     strength -= moveIncrement ;
   }
   else {
     strength = 0;
   }
  }
  
  // increase angle of elevation in degrees, 0 being east and 180 being west, not allowed to fire below artillery.
  void incEle() {
   if (elevation < 180) {
     elevation += 1 ;
   }
   else {
     elevation = 180;
   }
  }
  
  // decrease angle of elevation in degrees,
  void decEle() {
   if (elevation > 0) {
     elevation -= 1 ;
   }
   else {
     elevation = 0;
   }
  }
}
