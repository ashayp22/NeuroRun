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
public class SubInnovation {
    //fields

    private final String InnovationType; //neuron or link

    private final int InnovationID;

    private final int NeuronIn;
    private final int NeuronOut;

    private final int NeuronID;

    private final String NeuronType;


    //constructor

    public SubInnovation(String type, int id, int neuronin, int neuronout, int neuronid, String neurontype)
    {
        InnovationID = id;
        InnovationType = type;
        NeuronIn = neuronin;
        NeuronOut = neuronout;
        NeuronID = neuronid;
        NeuronType = neurontype;
    }

    public boolean sameInputOutput(int input, int output)
    {
        return (NeuronIn == input) && (NeuronOut == output);
    }

    public String getInnovationType()
    {
        return InnovationType;
    }

    public int getInnovationNumber()
    {
        return InnovationID;
    }

    public int getNeuronId()
    {
        return NeuronID;
    }


}
