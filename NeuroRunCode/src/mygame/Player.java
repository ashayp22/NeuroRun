/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector3f;
import com.jme3.scene.Spatial;
import com.jme3.bullet.control.*;

/**
 *
 * @author ashay
 */
public class Player {
    
    //instance fields
    
    private String name;
    private String characterName;
    private int score; //game score
    private int fitness; //fitness
    private boolean isAI;
    //for jumping too much
    private boolean canJump; //true if it can jump, false if it can't
    private int counter; //counter for when it can't jump, once it hits 3, then Ai can jump
    
    private int nextPlatform; //number of platform the ai is targeting(-1 for not AI)
    
    
    //body
    public Spatial geometry;
    public RigidBodyControl rb;
        
    private boolean isDead; //is dead for the round 
    private boolean alwaysDead; //is always dead, not being used - remember in this, 
    //the player is just not being rendered; it still is on the screen, moving and being affected by physics
    
    private float[] color; //color of the player
    
    private Vector3f startingSpot;
    
    //ctor
    
    public Player(String name, int initialScore, boolean isAI, Vector3f startingLocation, Spatial body, int target, float[] color, String charactername) {
        //sets up data for the class
        this.startingSpot = startingLocation;
        this.color = color;
        characterName = charactername;
        this.name = name;
        score = initialScore;
        fitness = initialScore;
        this.isAI = isAI;
        isDead = false;
        canJump = true;
        counter = 0;
        nextPlatform = target;
        //sets up the geometry and physics
        geometry = body;
        geometry.setLocalTranslation(startingLocation);
        
        
        rb = new RigidBodyControl(1f);
        
        rb.setCollisionGroup(1);
        rb.setCollideWithGroups(1);
        
        geometry.addControl(rb);
        
        //rb.setGravity(new Vector3f(0, -40f, 0));
        
        rb.setPhysicsLocation(startingLocation);
    }
    
    public void changeSpatial(Spatial newS) { //changes how the graphic of a player looks
        
        Vector3f originalSpot = geometry.getLocalTranslation();
        
        geometry = newS;
        geometry.setLocalTranslation(originalSpot);
        
        rb = new RigidBodyControl(1f);
        
        rb.setCollisionGroup(1);
        rb.setCollideWithGroups(1);
        
        geometry.addControl(rb);
        
        rb.setPhysicsLocation(originalSpot);
        
        rb.setKinematic(true);

    }
    
    //methods
    
    public float[] getColor() { return color; }
    
    public void increaseScore() {
        score++;
    }
    
    public int getScore() {
        return score / 30;
    }
    
    public void reset() {
        score = 0;
        counter = 0;
        fitness = 0;
        canJump = true;
        nextPlatform = 2;
        if(!alwaysDead)  {
            this.geometry.setCullHint(Spatial.CullHint.Never);
            isDead = false;
        } else { //is always ded
            isDead = true;
            this.geometry.setCullHint(Spatial.CullHint.Always);
        }
        
    }
    
    public String getName() {
        return name;
    }
    
    public String getCharacterName() {
        return characterName;
    }
    
    //setting/getting dead 
    
    public void setDead() {
        isDead = true;
    }
    
    public boolean getDead() {
        return isDead;
    }
    
    public void setAlwaysDead(boolean t) {
        alwaysDead = t;
        isDead = t;
        if(t) { //dead
            this.geometry.setCullHint(Spatial.CullHint.Always);
            this.rb.setKinematic(true);
        } else { //not dead
            this.geometry.setCullHint(Spatial.CullHint.Never);
            this.rb.setKinematic(false);
        }
    }
    
    
    //controlling (ai) movement
    
    public void jump() {
        if(isAI) {
            this.rb.applyCentralForce(new Vector3f(0, 2300, 0));  //jump ai
            canJump = false;
        } else {
            this.rb.applyCentralForce(new Vector3f(0, 2300, 0));  //jump player
        }
    }
    
    public void move(float move) {
        this.rb.applyCentralForce(new Vector3f(move, 0, 0));
    }
    
    public void moveAI(float move) {
        Vector3f current = this.rb.getPhysicsLocation();
        current.x += move;
        this.rb.setPhysicsLocation(current);
    }
    
    //jump methods
    
    //this prevents player from jumping too much
    
    public void updateCanJump() {
        if(!canJump) { //currently not able to jump
            counter++;
        }
        
        if(counter > 3) {
            canJump = true;
            counter = 0;
        }
    }
    
    public boolean getCanJump() {
        return canJump;
    }
    
    //platform stuff for the ai: deciding where to jump
    
    public void changePlatform(int num) {
        nextPlatform = num;
    }
    
    public int getNextPlatform() {
        return nextPlatform;
    }
    
    //for the fitness
    
    public void increaseFitness(int value) {
        fitness += value;
    }
    
    public int getFitness() {
        return fitness;
    }
    
}
