/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ashay
 */
public class PNeuron {

    private ArrayList<PLink> vecLinksIn;
    private ArrayList<PLink> vecLinksOut;
    
    //sum of weights x inputs

    private double sumActivation;

    //the output from this neuron

    private double dOutput;

    //what type of neuron this is?
    private String neuronType;

    //id
    private int neuronId;

    //sets the curvature of the sigmoid function
    private double activationResponse;

    //used in visualization of the phenotype
    private int PosX, PosY;
    private double SplitX, SplitY;

    //constructor

    public PNeuron(String type, int id, double y, double x, double ActResponse)
    {
        vecLinksIn = new ArrayList<>();
        vecLinksOut = new ArrayList<>();
        neuronType = type;
        neuronId = id;
        sumActivation = 0;
        dOutput = 0;
        PosX = 0;
        PosY = 0;
        SplitX = x;
        SplitY = y;
        activationResponse = ActResponse;
    }
    
    //accessors + mutators
    
    public ArrayList<PLink> getVecLinksIn() {
        return vecLinksIn;
    }
    
    public ArrayList<PLink> getVecLinksOut() {
        return vecLinksOut;
    }

    public int getVecLinkInSize() {
        return vecLinksIn.size();
    }
    
    public int getVecLinkOutSize() {
        return vecLinksOut.size();
    }
    
    public PLink getInLink(int i) {
        return vecLinksIn.get(i);
    }
    
    public PLink getOutLink(int i) {
        return vecLinksOut.get(i);
    }
    
    public void addIn(PLink arr)
    {
        vecLinksIn.add(arr);
    }

    public void addOut(PLink arr)
    {
        vecLinksOut.add(arr);
    }

    public String gettype()
    {
        return neuronType;
    }

    
    public double getSumActivation() {
        return sumActivation;
    }
    
    public double getOutput() {
        return dOutput;
    }
    
    public void setOutput(double d) {
        dOutput = d;
    }
    
    public String getNeuronType() {
        return neuronType;
    }
    
    public int getNeuronId() {
        return neuronId;
    }
    
    public double getActivationResponse() {
        return activationResponse;
    }
    
    public int getPosX() {
        return PosX;
    }
    
    public int getPosY() {
        return PosY;
    }
    
    public double getSplitX() {
        return SplitX;
    }
    
    public double getSplitY() {
        return SplitY;
    }
    
    //text format stuff
    
    public String getTextFormat() { //returns the string format of the neuron
        String encoding = "PN:";
        encoding += "0,0," + neuronType + "," + neuronId + "," + activationResponse + "," + PosX + "," + PosY + "," + SplitX + "," + SplitY;
        
        return encoding;
    }    
    

}
