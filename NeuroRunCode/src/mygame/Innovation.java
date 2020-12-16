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
public class Innovation {

    
    //static class of all the innovation values
    private ArrayList<SubInnovation> dataBase;
    
        
    public Innovation() {
        dataBase = new ArrayList<SubInnovation>();
    }
    
    public int CheckInnovation(int input, int output, String type) //checks to see if an innovation exists
    {
        for(SubInnovation innovation : dataBase)
        {
            if (innovation.sameInputOutput(input, output) && innovation.getInnovationType().equals(type)) //same innovation
            {
                return innovation.getInnovationNumber(); //returns its id
            }
        }
        return -1;
    }

    public void CreateNewInnovation(int neuron1, int neuron2, String type, int neuronID, String typeNeuron)
    {
        SubInnovation newInnovation = new SubInnovation(type, dataBase.size() + 1, neuron1, neuron2, neuronID, typeNeuron); //creates a new innovation that is link
        dataBase.add(newInnovation);
    }

    public int GetNeuronId(int id) //returns the id of a neuron given its innovation id(not same)
    {
        for (SubInnovation innovation : dataBase)
        {
            if(innovation.getInnovationNumber() == id)
            {
                return innovation.getNeuronId();
            }
        }
        return -1;
    }

    public int NextNumber() //next number
    {
        return dataBase.size() + 1;
    }
    
    public void ClearInnovations() {
        dataBase = new ArrayList<>();
    }
    
    
    public int databaseSize() {
        return dataBase.size();
    }


}
