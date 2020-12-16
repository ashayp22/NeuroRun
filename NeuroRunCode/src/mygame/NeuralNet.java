/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.util.ArrayList;

/**
 *
 * @author ashay
 */
public class NeuralNet {

    private ArrayList<PNeuron> vecpNeurons;

    private int depth;

    public NeuralNet(ArrayList<PNeuron> n, int d)
    {
        vecpNeurons = n;
        depth = d;
    }

    //update method for the neural network
    public ArrayList<Double> Update(ArrayList<Double> inputs, String runtype) //parameter is 'snapshot' or 'active' 
    {
        //create a vector for the outputs
        ArrayList<Double> outputs = new ArrayList<>();

        //if the mode is snapshot, then we require all the neurons to be 
        //iterated through as many times as the network is deep. If the 
        //mode is set to active, then the method can return an output after just one iteration

        int FlushCount;

        if(runtype.equals("snapshot"))
        {
            FlushCount = depth;
        } else
        {
            FlushCount = 1;
        }


        //iterate through the network FlushCount times

        for (int i = 0; i < FlushCount; i++)
        {
            //clear the output vector
            outputs = new ArrayList<>();

            //this is an index into the current neuron
            int cNeuron = 0;

            //first set the outputs of the 'input' neurons to be equal 
            //to the values passed into the function in inputs

            while(vecpNeurons.get(cNeuron).getNeuronType().equals("input"))
            {
                vecpNeurons.get(cNeuron).setOutput(inputs.get(cNeuron));
                cNeuron++;
            }

            //set the output of the bias to 1
            vecpNeurons.get(cNeuron).setOutput(1);
            
            //then we step through the network a neuron at a time

            while(cNeuron < vecpNeurons.size())
            {
                //this will hold the sum of all the inputs * weights
                double sum = 0;

                //sum this neuron's inputs by iterating through all the links into the neuron
                for(int lnk = 0; lnk < vecpNeurons.get(cNeuron).getVecLinkInSize(); lnk++)
                {
                    //get this link's weight
                    double Weight = vecpNeurons.get(cNeuron).getInLink(lnk).getWeight();

                    //get the output from the neuron this link is coming from
                    double NeuronOutput = vecpNeurons.get(cNeuron).getInLink(lnk).getPIn().getOutput();
                    

                    //add to sum
                    sum += Weight * NeuronOutput;
                }

                //System.out.println(sum);
                
                //now we put the sum through the activation function and assign the 
                //value to the neuron's output
                vecpNeurons.get(cNeuron).setOutput(Sigmoid(sum, vecpNeurons.get(cNeuron).getActivationResponse())); //sigmoid the dinosaur(hehe)

                if(vecpNeurons.get(cNeuron).getNeuronType().equals("output"))
                {
                    //add to out outputs
                    outputs.add(vecpNeurons.get(cNeuron).getOutput());
                }

                //next neuron
                cNeuron++;
            }

        } //next ieration through the network


        //the network outputs need to be reset if tis type of update is performed
        //otherweise it is possible for dependencies to be built on the order the training data is presented

        if(runtype.equals("snapshot"))
        {
            for(int n = 0; n < vecpNeurons.size(); n++)
            {
                vecpNeurons.get(n).setOutput(0);
            }
        }

        //return the outputs
        return outputs;
    }

    public double Sigmoid(double sum, double activation) //activation function
    {
        return 1.0 / (1 + Math.pow(Math.E, (-1 * sum) / activation));
    }

    //draws the neural network on the screen
    public int getNNsize()
    {
        return vecpNeurons.size();
    }

    public int getIdFromIndex(int index)
    {
        return vecpNeurons.get(index).getNeuronId();
    }

    public Vector2 getNodePosition(int index) //returns the position of a node based on its index
    {
        return new Vector2((float)vecpNeurons.get(index).getSplitX(), (float)vecpNeurons.get(index).getSplitY());
    }

    public Vector2 getNodePositionFromID(int id) //returns the position of a node based on its ID
    {
        for(PNeuron s : vecpNeurons)
        {
            if(s.getNeuronId() == id)
            {
                return new Vector2((float)s.getSplitX(), (float)s.getSplitY());
            }
        }
        return new Vector2(0, 0);
    }
    
    public ArrayList<PLink> getForwardConnections(int index)
    {
        return vecpNeurons.get(index).getVecLinksOut();
    } 

    public String getTypeFromId(int id) //gets the type of neuron from id
    {
        for(PNeuron n : vecpNeurons)
        {
            if(n.getNeuronId() == id)
            {
                return n.getNeuronType();
            }
        }
        return "";
    }
    
    
    //returns a pneuron based on its index
    
    public PNeuron getNeuronFromIndex(int index) {
        return vecpNeurons.get(index);
    }
    

}
