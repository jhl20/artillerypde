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
  void draw() {
    fill(204) ;
    rect(position.x, position.y, cloudWidth, cloudHeight) ;
  }
  
  // moves the clouds left or right, if the cloud leaves the screen, it re-enters from the other side.
  void move() {
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
