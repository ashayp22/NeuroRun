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
public class PLink {

    //pointers to the neurons this link connects

    private PNeuron pIn;
    private PNeuron pOut;

    //the connection weights

    private double weight;


    //is this link a recurrent link

    private boolean recurrent;

    public PLink(double w, PNeuron inn, PNeuron outn, boolean r)
    {
        pIn = inn;
        pOut = outn;
        weight = w;
        recurrent = r;
    }
    //accessors
    
    public boolean getRecurrent() {
        return recurrent;
    }
    
    public double getWeight() {
        return weight;
    }
    
    public PNeuron getPIn() {
        return pIn;
    }
    
    public PNeuron getPOut() {
        return pOut;
    }
    
    public String getTextFormat() { //returns a text encoding of the link
        String encoding = "PL:";
        encoding += pIn + "," + pOut + "," + weight + "," + recurrent;
        return encoding;
    }


}
