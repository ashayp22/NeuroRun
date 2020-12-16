/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

/**
 *
 * @author ashay
 */
public abstract class Rand {
    
    //used for dealing with random numbers
    
    
    public static int randomInt(int min, int max) {
        return (int)(Math.random() * (max - min + 1)) + min;
    }
    
    public static double randomNumber(double min, double max) {
        return Math.random() * (max - min) + min;
    }
    
}
