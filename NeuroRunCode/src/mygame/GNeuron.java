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
public class GNeuron { //genotype gene

    //field

    private int ID;

    private String NeuronType; //input, output, hidden, bias, none

    private boolean isRecurrent;

    private double ActivationResponse; //sets the curvature of the sigmoid function

    private double SplitY, SplitX;

    //constructor
    public GNeuron(int id, double x, double y, boolean r, String type)
    {
        NeuronType = type;
        isRecurrent = r;
        SplitX = x;
        SplitY = y;
        ID = id;
        ActivationResponse = 1;
    }

    //accessor + modifier

    public boolean getRecurrent()
    {
        return isRecurrent;
    }

    public boolean isBias()
    {
        return NeuronType.equals("bias");
    }

    public boolean isInput()
    {
        return NeuronType.equals("input");
    }

    public void setRecurrent()
    {
        isRecurrent = true;
    }

    public int getID()
    {
        return ID;
    }

    public double getSplitX()
    {
        return SplitX;
    }

    public double getSplitY()
    {
        return SplitY;
    }

    public String getNeuronType()
    {
        return NeuronType;
    }
    
    public void setActivation(double a) {
        ActivationResponse = a;
    }

    public double getActivation()
    {
        return ActivationResponse;
    }

    public void mutateActivationResponse(double max)
    {
        ActivationResponse += Rand.randomNumber(max * -1, max);
    }
    
    public String getTextFormat() {
        return "N:" + ID + "," + NeuronType + "," + isRecurrent + "," + ActivationResponse + "," + SplitX + "," + SplitY;
    }


}
