/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mygame;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.util.Scanner;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;


/**
 *
 * @author parikh1605
 */
public class TextFileReader {
    
    private String fileLocation;
    
    //ctor
    public TextFileReader(String loc) {
        fileLocation = loc;
    }
    
    
    
    //source: examples folder 
    
    public void saveGenome(Genome g, int depth, double score) { //passed in a genome
        //updates the text file with the genotype of the genome
        try {
            
            System.out.println("score is: " + score);
            
            if(getCurrentFitness() >= score) { return; } //fitness currently saved is higher, don't need to update
            
            PrintWriter out = new PrintWriter(fileLocation);
            
            //adds the text format of every neuron to the text file
            for(int i = 0; i < g.getNumGenes(); i++) {
                out.println(g.GetSNeuronGene(i).getTextFormat());
            }
            
            //adds the text format of every link to the text file

            for(int i = 0; i < g.getNumLinks(); i++) {
                out.println(g.getSLinkGene(i).getTextFormat());
            }
            out.println("D:" + depth);
            out.println("F:" + score);
            out.close();
            
        } catch (FileNotFoundException ex) {
            System.out.println("Something went wrong!");
        }
    }
    
    public void clearSavedGenome() { //clears the saved genome
        try {            
            PrintWriter out = new PrintWriter(fileLocation);
            out.close();
            
        } catch (FileNotFoundException ex) {
            System.out.println("Something went wrong!");
        }
    }
    
    public double getCurrentFitness() { //returns the fitness of the current genome saved
        try{
            FileReader reader = new FileReader(fileLocation);
            Scanner in = new Scanner(reader);
            
            while(in.hasNextLine())
            {
                String line = in.nextLine();
                String identifier = line.substring(0,1);
                String data = line.substring(2, line.length());
                if(identifier.equals("F")) { //identifier matches F
                    return Double.parseDouble(data); //returns the double, which was stored as a string
                }
            }
            
        } catch (FileNotFoundException ex) {
            System.out.println("SOMETHING HAS GONE HORRIBLY WRONG WE'RE ALL GONNA DIE!");
        }
        
        return -1;
    }  
   
    
    public NeuralNet getGenome() //loads a genome, returns a phenotype
    {
        
        //how to create a NN: create the genotype, then based on that, create the phenotype(basic, since direct encoding is used)
        //then returns the NN
        
        try{
            FileReader reader = new FileReader(fileLocation);
            Scanner in = new Scanner(reader);
            
            //the genotype
            List<GNeuron> vecNeurons = new ArrayList<>();
            List<GLink> vecLinks = new ArrayList<>();
            
            int depth = 1;
            
            //format of text
            //N:ID,NeuronType,isRecurrent,ActivationResponse,SplitX,SplitY
            //N:ID,NeuronType,isRecurrent,ActivationResponse,SplitX,SplitY
            //N:ID,NeuronType,isRecurrent,ActivationResponse,SplitX,SplitY
            //L:FromNeuron,ToNeuron,weight,isEnabled,isRecurrent,InnovationID
            //L:FromNeuron,ToNeuron,weight,isEnabled,isRecurrent,InnovationID
            //L:FromNeuron,ToNeuron,weight,isEnabled,isRecurrent,InnovationID
            //L:FromNeuron,ToNeuron,weight,isEnabled,isRecurrent,InnovationID
            //L:FromNeuron,ToNeuron,weight,isEnabled,isRecurrent,InnovationID
            
            while(in.hasNextLine())
            {
                String line = in.nextLine();
                String identifier = line.substring(0,1); //identifier, tells what type of data it is
                String data = line.substring(2, line.length()); //data after identifier
                if(identifier.equals("N")) { //neuron
                    //gets the data
                    int id = Integer.parseInt(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    String neuronType = data.substring(0, data.indexOf(","));
                    data = data.substring(data.indexOf(",") + 1);
                    boolean isRecurrent = data.substring(0, data.indexOf(",")).equals("true");
                    data = data.substring(data.indexOf(",") + 1);
                    double activationResponse = Double.parseDouble(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    double SplitX = Double.parseDouble(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    double SplitY = Double.parseDouble(data.substring(0, data.length()));
                    
                    //now creates neuron
                    GNeuron tempNeuron = new GNeuron(id, SplitX, SplitY, isRecurrent, neuronType);
                    tempNeuron.setActivation(activationResponse); //sets the activation
                    vecNeurons.add(tempNeuron); //adds to the list
                    
                } else if(identifier.equals("L")) { //link
                    //gets the data
                    int FromNeuron = Integer.parseInt(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    int ToNeuron = Integer.parseInt(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    double weight = Double.parseDouble(data.substring(0, data.indexOf(",")));
                    data = data.substring(data.indexOf(",") + 1);
                    boolean isEnabled = data.substring(0, data.indexOf(",")).equals("true");
                    data = data.substring(data.indexOf(",") + 1);
                    boolean isRecurrent = data.substring(0, data.indexOf(",")).equals("true");
                    data = data.substring(data.indexOf(",") + 1);
                    int innovationID = Integer.parseInt(data.substring(0, data.length()));
                    
                    //now creates neuron
                    GLink tempLink = new GLink(FromNeuron, ToNeuron, isEnabled, isRecurrent, weight, innovationID);
                    vecLinks.add(tempLink); //adds to the list
                } else if (identifier.equals("D")) {
                    depth = Integer.parseInt(data);
                }
            }
            
            //now creates the neural network
            
            //this will hold all the neurons required for the phenotype
        ArrayList<PNeuron> vecneurons = new ArrayList<>();

        //first, create all the required neurons
        for(int i = 0; i < vecNeurons.size(); i++)
        {
            PNeuron pNeuron = new PNeuron(vecNeurons.get(i).getNeuronType(), vecNeurons.get(i).getID(), vecNeurons.get(i).getSplitY(), vecNeurons.get(i).getSplitX(), vecNeurons.get(i).getActivation());
            vecneurons.add(pNeuron);
        }

        //now to create the links

        for(int cGene = 0; cGene < vecLinks.size(); cGene++)
        {
            //make sure that the link gene is enabled before the connection is created
            if(vecLinks.get(cGene).getIsEnabled())
            {
                //get the pointers to the relevant neurons
                int element = GetElementPos(vecLinks.get(cGene).getFromNeuron(), vecNeurons);

                PNeuron FromNeuron = vecneurons.get(element);

                element = GetElementPos(vecLinks.get(cGene).getToNeuron(), vecNeurons);

                PNeuron ToNeuron = vecneurons.get(element);

                //create a link between those two neurons and assign the weight stored in the gene

                PLink tmpLink = new PLink(vecLinks.get(cGene).getWeight(), FromNeuron, ToNeuron, vecLinks.get(cGene).getIsRecurrent());

                //add new links to the neuron
                FromNeuron.addOut(tmpLink);
                ToNeuron.addIn(tmpLink);
            }             
        }

        //now the neurons contain all the connectivity information, a neural network may be created from them
        NeuralNet Phenotype = new NeuralNet(vecneurons, depth);

        return Phenotype;
            
        } catch (FileNotFoundException ex) {
            System.out.println("SOMETHING HAS GONE HORRIBLY WRONG WE'RE ALL GONNA DIE!");
        }
        
        return null;
    }
    
    
    private int GetElementPos(int neuron_id, List<GNeuron> vecNeurons) //returns the index of a neuron set to the neuron id passed in
    {
        for(int i = 0; i < vecNeurons.size(); i++)
        {
            if(vecNeurons.get(i).getID() == neuron_id) //same neuron id
            {
                return i;
            }
        }
        return -1;
    }
    
}
