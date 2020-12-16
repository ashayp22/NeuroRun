/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;


import com.jme3.bounding.BoundingBox;
import com.jme3.bounding.BoundingVolume;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.WireBox;
import com.jme3.scene.shape.Box;
import com.jme3.bounding.*;

/**
 *
 * @author ashay
 */
public class Platform {
    
    //instance fields
    private Vector3f size;
    private Vector3f position;
    
    private float[] color;
    
    private int timesTouched; //times the player has hit it
    
    private int number; //number of the platform
    
    private Material mat;
    
    public RigidBodyControl rb;
    public Geometry geometry;
    public BoundingBox box;
    
    private boolean changedColor; //true if its color has been changed(because a player stepped on it), false otherwise
    
    //ctor
    
    public Platform(Vector3f size, Vector3f position, float[] color, int num, Material m) {
        //does data
        this.size = size;
        this.position = position;
        this.color = color;
        number = num;
        changedColor = false;
        
        //does the graphics + rb
        
        //creates a box
        Box b = new Box(size.x, size.y, size.z);
        
        //actual graphic
        geometry = new Geometry("Box", b);   
        
        mat = m.clone();
        
        mat.setColor("Color", new ColorRGBA((float)color[0], (float)color[1], (float)color[2], 1));
            
        //sets the material
        geometry.setMaterial(mat);
            
        //sets position
        geometry.setLocalTranslation(position);    
        
        geometry.setName("platform" + number);
        
        //adds physics stuff
                        
        //CollisionShape sceneShape = CollisionShapeFactory.createMeshShape((Node) s);
            
        rb = new RigidBodyControl(0);
        
        geometry.addControl(rb);
        
        //adds bounding box
        
        
        Vector3f center = new Vector3f(position.x, position.y, position.z);

        box = new BoundingBox(center, size.x, size.y, size.z);
                
        geometry.setModelBound(box);
    }
    
    
    //accessors
    
    public float[] getColor() {
        return color;
    }
    
    public boolean checkSameColor(float[] c) {
        return c[0] == color[0] && c[1] == color[1] && c[2] == color[2];
    }
    
    public Vector3f getPos() {
        return position;
    }
    
    public Vector3f getSize() {
        return size;
    }
    
    public int getNumber() {
        return number;
    }
    
     
    public boolean changeColor(float[] newColor) { //changes the color of the platform; will return true if changed, else false
        
        if(!changedColor) {
        
            //sets color to newColor
            color = new float[3];
            color[0] = newColor[0];
            color[1] = newColor[1];
            color[2] = newColor[2];
        
            mat.setColor("Color", new ColorRGBA((float)color[0], (float)color[1], (float)color[2], 1));
            
            //sets the material
            geometry.setMaterial(mat);
            changedColor = true;
            return true;
        }
        return false;
    }
    
}
