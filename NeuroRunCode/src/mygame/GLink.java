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
public class GLink {

    //the IDs of the two neurons this link connects

    private int FromNeuron, ToNeuron;

    private double weight;

    private boolean isEnabled; //flag to indicate if it is enabled

    private boolean isRecurrent; //flag to indicate if it is recurrent

    private int InnovationID; //innovation id

    //constructor
    public GLink(int inN, int outN, boolean enable, boolean rec, double w, int tag)
    {
        FromNeuron = inN;
        ToNeuron = outN;
        weight = w;
        isEnabled = enable;
        isRecurrent = rec;
        InnovationID = tag;
    }

    public GLink() //temporary constructor
    {
        
    }

    public double getWeight()
    {
        return weight;
    }

    public void changeWeight(double newWeight)
    {
        weight += newWeight;

        if(weight <= -1)
        {
            weight = -1;
        }

        if(weight > 1)
        {
          weight = 1;
        }

    }

    //all the accessors
    
    public boolean sameLink(int i, int o)
    {
        return (i == FromNeuron) && (o == ToNeuron);
    }
    

    public int getFromNeuron()
    {
        return FromNeuron;
    }

    public int getToNeuron()
    {
        return ToNeuron;
    }

    public boolean getIsEnabled()
    {
        return isEnabled;
    }

    public boolean getIsRecurrent()
    {
        return isRecurrent;
    }


    public void disableGene()
    {
        isEnabled = false;
    }

    public int getInnovationId()
    {
        return InnovationID;
    }
    
    public String getTextFormat() {
        return "L:" + FromNeuron + "," + ToNeuron + "," + weight + "," + isEnabled + "," + isRecurrent + "," + InnovationID;
    }


}
