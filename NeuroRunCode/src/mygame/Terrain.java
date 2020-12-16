/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import java.util.ArrayList;

/**
 *
 * @author ashay
 */
public class Terrain {
    
    //instance fields
    
    private ArrayList<Platform> platformList; //list of boxes on the screen(not graphical)
    
    private Vector3f spawnPosition; //position of the box being created
    
    private int platformsSpawned;
    
    //ctor
    
    public Terrain() { 
        spawnPosition = new Vector3f(0, 0, 0);
        platformList = new ArrayList<>();
        platformsSpawned = 0;
    }
    
    
    //methods
    
    
    public Platform addPlatform(Material m, boolean addZ, boolean widen) { //adds a platform
        platformsSpawned += 1;
        
        //gets new position of platform
        if(platformList.size() >= 1) {
                    
            if(widen) //make the terrain wider
                //this finds a spawn position x that isn't close to any other platforms
                do {
                    spawnPosition.x = (float)Rand.randomNumber(-40.0, 40.0);
                } while(collidingPlatforms(spawnPosition.x));
            else //keep it close
                spawnPosition.x = (float)Rand.randomNumber(-18.0, 18.0);
            
            if(addZ) spawnPosition.z += (float)Rand.randomNumber(70, 80.0);
            else 
                //spawnPosition.z += (float)Rand.randomNumber(20, 30);
            spawnPosition.y = 0;
        }
        
        //gets the size of the platform       
        Vector3f size = new Vector3f(3, 3, Rand.randomInt(20, 25));
        
        if(platformList.isEmpty())
            size.z = 30;
        
        //gets the color of the platform
        float[] color = new float[3];
        
        do {
            for(int i = 0; i < 3; i++) color[i] = (float)Math.random();
        } while(matchingColor(color));
        
        //initialize and add
        Platform p = new Platform(size, new Vector3f(spawnPosition.x, spawnPosition.y, spawnPosition.z), color, platformsSpawned, m);     
        platformList.add(p);
        
        return p; //returns it
    }
    
    private boolean matchingColor(float[] c1) { //check the color passed in to the color of the players
        boolean match = false;
        
        for(int i = 0; i < Main.COLOR_LIST.length; i++) {
            match = match || checkSameColor(c1, Main.COLOR_LIST[i]);
        }
        return match;
    }
    
    private boolean checkSameColor(float[] c1, float[] c2) { //checks if two colors are the same
        return c1[0] == c2[0] && c1[1] == c2[1] && c1[2] == c2[2];
    }
    
    private boolean collidingPlatforms(float pos) {
        int platformsInLine = (platformsSpawned -2) % 3; //gets the number of platforms in its line
        
        boolean interferes = false; //assume that it doesn't collide with another platform
        
        for(int i = 0; i < platformsInLine; i++) {
            if(Math.abs(platformList.get(platformList.size()-1-i).getPos().x - pos) <  7) { //too close
                interferes = true;
            }
        }
        
        return interferes;
    }
       
    
    public void removePlatform(int index) { //deletes platform at index
        platformList.remove(index);
    }
    
    public void deleteTerrain() { //deletes the terrain(essentially reset)
        platformList = new ArrayList<>();
        spawnPosition = new Vector3f(0, 0, 0);
        platformsSpawned = 0;
    }
    
    //accessors
    
    public Platform getPlatform(int i) {
        return platformList.get(i);
    }
    
    public int getNumPlatforms() {
        return platformList.size();
    }
    
    public Platform getPlatformFromNumber(int n) { //returns a platform based on the number passed in
        
        for(Platform p : platformList) {
            if(p.getNumber() == n) {
                return p;
            }
        }
        
        return null;
    }
    
}
