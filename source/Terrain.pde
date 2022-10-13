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
  void draw() {
    fill(131,101,57);
    rect(position.x, position.y, terrainWidth, terrainHeight) ;
  }
}
