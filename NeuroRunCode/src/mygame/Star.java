/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

/**
 *
 * @author ashay
 */
public class Star {
 
    
    //instance fields
    
    public Geometry body;
    
    public Star(Material sphereMat, Vector3f location) {
        /** A bumpy rock with a shiny light effect.*/
        Sphere sphereMesh = new Sphere(32, 32, (float)Rand.randomNumber(0.1f, 3));
        body = new Geometry("Shiny rock", sphereMesh);
        sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
        //TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
        body.setMaterial(sphereMat);
        body.setLocalTranslation(location); // Move it a bit
        body.rotate(1.6f, 0, 0);          // Rotate it a bit
    }
    
    
}
