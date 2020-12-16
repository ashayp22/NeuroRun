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
public class Genome {

    //fields

    private int GenomeId; //id num

    private ArrayList<GNeuron> vecNeurons; //all the neurons which make up this genome

    private ArrayList<GLink> vecLinks; //all the links

    private NeuralNet Phenotype; //phenotype

    private double fitness; //raw fitness score

    private double adjustedFitness; //after has been placed into species and adjusted accordingly

    private double amountToSpwn; //number of offspring this individual is required to spawn for the next generation

    private int numInputs, numOutputs; //keeps track of num of outputs and inputs

    private int speciesNum; //keeps track of whuch species this genome is in(used for display purposes)


    //static, for the genome ids
    public static int NextGenomeID = 0;


    //constructor

    //creates a minimal genome where there are output and input neurons and every input neuron is connected to each output neuron
    public Genome(int id, int inputs, int output, Innovation innovation)
    {
        GenomeId = id;
        numInputs = inputs;
        numOutputs = output;

        //now create the minimal genome where every input is connected to an output

        //create neurons

        vecLinks = new ArrayList<>();
        vecNeurons = new ArrayList<>();

        //inputs
        for(int i = 0; i < numInputs; i++)
        {
            vecNeurons.add(new GNeuron(i + 1, 0, i*4, false, "input"));
        }

        //bias
        vecNeurons.add(new GNeuron(inputs + 1, 0, inputs * 4, false, "bias"));

        //outputs

        for(int i = 0; i < numOutputs; i++)
        {
            vecNeurons.add(new GNeuron(inputs + i + 2, 12, i * 4, false, "output"));
        }

        //now create the links between them
        for (int i = 0; i < numInputs; i++)
        {
            for(int j = 0; j < numOutputs; j++)
            {
                int in = innovation.CheckInnovation(i + 1, inputs + j + 2, "link");
                System.out.println("innovation: " + in);
                vecLinks.add(new GLink(i + 1, inputs + j + 2, true, false, Math.random(), in)); //adds to vector

                if (in == -1)
                {
                    System.out.println("error innovation not found");
                }
            }
        }

        //every bias to every output

        for(int i = 0; i < numOutputs; i++)
        {
            int in = innovation.CheckInnovation(inputs + 1, inputs + i + 2, "link");
            System.out.println("bias innovation: " + in);
            if (in == -1)
            {
                System.out.println("error innovation not found");
            }
            vecLinks.add(new GLink(inputs + 1, inputs + i + 2, true, false, -1, in)); //adds to list
        }
        
        //now, disable random links
        for (GLink vecLink : vecLinks) {
            if(Math.random() < 0.5) {
                vecLink.disableGene();
            } 
        }
        
        
    }


    //this constructor creates a genome from a vector of SLinkGenes and a vector of SNeuronGenes and an ID number

    public Genome(int id, ArrayList<GNeuron> neurons, ArrayList<GLink> links, int inputs, int outputs)
    {
        GenomeId = id;
        numInputs = inputs;
        numOutputs = outputs;
        vecNeurons = neurons;
        vecLinks = links; 
    }

    //temporary constructor
    public Genome()
    {
        //none
    }

    //main methods

    public NeuralNet CreatePhenotype(int depth) //create a neural network from the genome
    {
        DeletePhenotype(); //make sure there is no existing phenotype for this genome

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
                int element = GetElementPos(vecLinks.get(cGene).getFromNeuron());


                PNeuron FromNeuron = vecneurons.get(element);

                element = GetElementPos(vecLinks.get(cGene).getToNeuron());

                PNeuron ToNeuron = vecneurons.get(element);

                //create a link between those two neurons and assign the weight stored in the gene

                PLink tmpLink = new PLink(vecLinks.get(cGene).getWeight(), FromNeuron, ToNeuron, vecLinks.get(cGene).getIsRecurrent());

                //add new links to the neuron
                FromNeuron.addOut(tmpLink);
                ToNeuron.addIn(tmpLink);
            }
        }

        //now the neurons contain all the connectivity information, a neural network may be created from them
        Phenotype = new NeuralNet(vecneurons, depth);

        return Phenotype;

    }

    public void DeletePhenotype() //deletes the neural network
    {
        Phenotype = null;
    }

    public void AddLink(double MutationRate, double ChanceOfLooped, int NumTrysToFindLoop, int NumTrysToAddLink, Innovation innovation) //adds a link
    {
        if (Math.random() > MutationRate) return; //returns dependent on mut rate

        //define holders for the two neurons being linked. If we find two valid neurons to link these values will become >= 0

        int ID_neuron1 = -1;
        int ID_neuron2 = -1;

        boolean recurrent = false; //flag set if a recurrent link is selected to be added

        //frist test to see if an attempt should be made to create a link that loops back into the same neuron
        if(Math.random() < ChanceOfLooped)
        {
            //YES: try NumTrysToFindLoop times to find a neuron that is not an input or bias neuron and does not already have a loopback connection
            while(NumTrysToFindLoop > 0)
            {
                System.out.println("loop: " + NumTrysToFindLoop);
                //grab a random neuron
                int NeuronPos = Rand.randomInt((int)numInputs + 1, vecNeurons.size() - 2);
                System.out.println("size: " + vecNeurons.size());
                System.out.println("chosen: " + NeuronPos);

                //check to make sure the neuron does not already have a loopback link and that it is not an input or bias neuron
                if(!vecNeurons.get(NeuronPos).getRecurrent() && !vecNeurons.get(NeuronPos).isBias() && !vecNeurons.get(NeuronPos).isInput())
                {
                    ID_neuron1 = ID_neuron2 = vecNeurons.get(NeuronPos).getID();

                    vecNeurons.get(NeuronPos).setRecurrent();

                    recurrent = true;

                    NumTrysToFindLoop = 0;

                }

                NumTrysToFindLoop--;
            }
            
        } else
        {
            //NO: try to find two unlinked neurons. Make NumTrysToAddLink attempts

            while(NumTrysToAddLink > 0)
            {
                int index1 = Rand.randomInt(0, vecNeurons.size() - 1);
                int index2 = Rand.randomInt(numInputs + 1, vecNeurons.size() - 1);
                ID_neuron1 = vecNeurons.get(index1).getID();
                ID_neuron2 = vecNeurons.get(index2).getID();

                if(ID_neuron2 == 2)
                {
                    continue; //try again
                }

                //make sure that these two are not already linked and that they are not the same neuron
                if(!DuplicateLink(ID_neuron1, ID_neuron2) && !(ID_neuron1 == ID_neuron2))
                {
                    if((vecNeurons.get(index1).getNeuronType().equals("output")) || linkNoWork(vecNeurons.get(index1).getSplitX(), vecNeurons.get(index2).getSplitX())) //the starting neuron is the output, can't happen; also the starting neuron has to be behind the ending neuron
                    {
                        ID_neuron1 = -1;
                        ID_neuron2 = -1;
                    } else
                    {
                        NumTrysToAddLink = 0;
                    }
                } else
                {
                    ID_neuron1 = -1;
                    ID_neuron2 = -1;
                }

                 NumTrysToAddLink--;
            }


        }

        if((ID_neuron1 < 0) || (ID_neuron2 < 0)) //return if unsuccessful in finding a link
        {
            return;
        }

        //check to see if we have already created this innovation(found by another genome)
        int id = innovation.CheckInnovation(ID_neuron1, ID_neuron2, "link");

        //is this link recurrent(the x pos will be higher for the first)
        if(vecNeurons.get(GetElementPos(ID_neuron1)).getSplitX() > vecNeurons.get(GetElementPos(ID_neuron2)).getSplitX())
        {
            System.out.println("is recurrent");
            recurrent = true;
        }

        //System.out.println("recurrent: " + recurrent + " with " + ID_neuron1 + " , " + ID_neuron2);

        if(id < 0) //create a new innovation
        {
            System.out.println("happened: " + ID_neuron1 + " and " + ID_neuron2);

            //need to create new
            innovation.CreateNewInnovation(ID_neuron1, ID_neuron2, "link", -1, "none");

            System.out.println("added new link ---------------------------------");
            
            //now create the gene
            int linkId = innovation.NextNumber() - 1; //change here

            GLink newGene = new GLink(ID_neuron1, ID_neuron2, true, recurrent, Rand.randomNumber(-1f, 1f), linkId);

            vecLinks.add(newGene); //adds to the list

        } else
        {
            //the innovation has already been created so all we need to do is create the new gene using existing innovation id

            GLink newGene = new GLink(ID_neuron1, ID_neuron2, true, recurrent, Rand.randomNumber(-1f, 1f), id);

            vecLinks.add(newGene); //adds to the list
        }

    }

    private boolean linkNoWork(double inNX, double outNX) //makes sure that the link is valid
    {
        //the out neuron is before the in neuron(makes recurrent, don't want that in this case)
        //foreach(SNeuronGene s in vecNeurons) //this makes sure that the links being created are between neurons that are one layer apart
        //{
        //if(s.getSplitX() > inNX && s.getSplitX() > outNX) //the neuron is in between the two neurons being connected, meaning that the link won't be one layer
        //{
        //return true;
        //}
        //}
        

        return outNX <= inNX;
    }

    public void AddNeuron(double MutationRate, int NumTrysToFindOldLink, Innovation innovation) //add a neuron
    {   
        System.out.println("here");
        if (Math.random() > MutationRate) return; //returns dependent on mut rate

        //if a valid link is found into which to insert the new neuron
        //this value is set to true

        boolean bDone = false;

        //this will hold the index into vecLinks of the chosen link gene 
        int ChosenLink = 0;

        //first a link is chosen to split. If the genome is small the code makes sure
        //one of the older links is slit to ensure a chaining effect does not occur.
        //Here, if the genome contains less than some amount of hidden neurons it is considered to be too small
        //to select a link at random.

        int SizeThreshold = numInputs + numOutputs + 5;

        if(vecLinks.size() < SizeThreshold)
        {
            System.out.println("we in");
            while(NumTrysToFindOldLink > 0)
            {
                //choose a link with a bias towards the older links in the genome
                ChosenLink = Rand.randomInt(0, vecLinks.size() - 1 - (int)Math.sqrt(vecLinks.size()));

                //make sure that the link is enabled and that it is not a recurrent link

                int FromNeuron = vecLinks.get(ChosenLink).getFromNeuron();

                //link is enabled, not recurrent, or has a bias input

                if((vecLinks.get(ChosenLink).getIsEnabled()) && (!vecLinks.get(ChosenLink).getIsRecurrent()) && (!vecNeurons.get(GetElementPos(FromNeuron)).isBias()))
                {
                    bDone = true;

                    NumTrysToFindOldLink = 0;
                }

                NumTrysToFindOldLink--;
            }

            if (!bDone)
            {
                //failed to find a descent link
                System.out.println("failed to find a descent link");
                return;
            }
        } else
        {
            
            int numTrys = 0;
            
            //the genome is of sufficent size for any link to be acceptable
            while(!bDone && numTrys < vecLinks.size())
            {
                System.out.println(vecLinks.size());
                ChosenLink = Rand.randomInt(0, vecLinks.size() - 1);

                //make sure that the link is enabled and that it is not a recurrent link or has a BIAS input

                int FromNeuron = vecLinks.get(ChosenLink).getFromNeuron();

                if ((vecLinks.get(ChosenLink).getIsEnabled()) && (!vecLinks.get(ChosenLink).getIsRecurrent()) && (!vecNeurons.get(GetElementPos(FromNeuron)).isBias()))
                {
                    bDone = true;
                }
                
                boolean a1 = vecLinks.get(ChosenLink).getIsEnabled();
                boolean a2 = !vecLinks.get(ChosenLink).getIsRecurrent();
                boolean a3 = !vecNeurons.get(GetElementPos(FromNeuron)).isBias();
                
                System.out.println(a1 + " " + a2 + " " + a3);

                numTrys++;
            }
            
            if(numTrys == vecLinks.size()) {
                System.out.println("didn't add neuron********************************************************************************");
                return;
            }
            
        }
        
        System.out.println("passed");


        //disable this gene 

        vecLinks.get(ChosenLink).disableGene();


        //grab the weight from the gene (we want to use this for the weight of one of the new links
        //so the split does not distrub anything the NN may have already learned)

        double OriginalWeight = vecLinks.get(ChosenLink).getWeight();

        //identify the neurons this link connects
        int from = vecLinks.get(ChosenLink).getFromNeuron();
        int to = vecLinks.get(ChosenLink).getToNeuron();

        //calculate the depth and width of the new neuron. We can use the depth to see if the link feeds backwards or forwards

        double newDepth = (vecNeurons.get(GetElementPos(from)).getSplitY() + vecNeurons.get(GetElementPos(to)).getSplitY()) / 2;

        double newWidth = (vecNeurons.get(GetElementPos(from)).getSplitX() + vecNeurons.get(GetElementPos(to)).getSplitX()) / 2;

        //Now to see if this innovation has been created previously by
        //another memeber of the population

        int id = innovation.CheckInnovation(from, to, "neuron");

        /*
         * It is possible for Neat to repeatdly do the folowing
         * 
         * 1. Find a link. Lest say we choose link 1 - 5
         * 2. Disable the link
         * 3. Add a new neuron and two new links
         * 4. The link disabled in Step 2 may be re-enabled when this genome is recombined with a genome that has that link enabled
         * 5. etc etc
         * 
         * Therefore, the following checks to see if a neuron ID is already being used. If it is, the function
         * creates a new innovation for the neuron
         */
        
        if(id >= 0)
        {
            int NeuronId = innovation.GetNeuronId(id);

            if(AlreadyHaveThisNeuronId(NeuronId))
            {
                id = -1;
            }
        }

        //since there is already a disabled gene between to and from, the new innovation will be created and not replace the disabled one

        if (id < 0) //this is a new innovation
        {
            int NewNeuronId = vecNeurons.size() + 1; //next id //change here
            innovation.CreateNewInnovation(from, to, "neuron", NewNeuronId, "hidden");

            System.out.println("added new neuron ----------------------------");
            
            GNeuron newGene = new GNeuron(NewNeuronId, newWidth, newDepth, false, "hidden");

            vecNeurons.add(newGene); //adds to the list

            //two new link innovations are required, one for each of the new links created when this gene is split


            //--------------------------------------------------------------first link

            int idLink1 = innovation.NextNumber();  //change here

            //create the new innovation

            innovation.CreateNewInnovation(from, NewNeuronId, "link", -1, "none");

            System.out.println("added new link ----------------------------");
            
            //create the new gene

            GLink link1 = new GLink(from, NewNeuronId, true, false, 1.0, idLink1); //has the same id as its innovation
            vecLinks.add(link1);

            //----------------------------------------------------------------second link

            int idLink2 = innovation.NextNumber(); //change here

            //create the new innovation

            innovation.CreateNewInnovation(NewNeuronId, to, "link", -1, "none");

            System.out.println("added new link ----------------------------");
            
            GLink link2 = new GLink(NewNeuronId, to, true, false, OriginalWeight, idLink2); //has the same id as its innovation
            vecLinks.add(link2);

        } else //existing innovation
        {
            //this innovation has already been created so grab the relevant neuron and link info from the innovation database

            int NewNeuronId = innovation.GetNeuronId(id);

            //get the innovation IDs for the two new link genes
            int idLink1 = innovation.CheckInnovation(from, NewNeuronId, "link");
            int idLink2 = innovation.CheckInnovation(NewNeuronId, to, "link");

            //this should never happen because the innovations *should* have already occured

            if((idLink1 < 0) || (idLink2 < 0))
            {
                System.out.println("error: add node cgenome problem");
                return;
            }

            //now need to create 2 new genes to represent the new links 

            GLink link1 = new GLink(from, NewNeuronId, true, false, 1.0, idLink1);
            GLink link2 = new GLink(NewNeuronId, to, true, false, OriginalWeight, idLink2);

            vecLinks.add(link1);
            vecLinks.add(link2);

            //create the new neuron

            GNeuron gene = new GNeuron(NewNeuronId, newWidth, newDepth, false, "hidden");

            //and add it

            vecNeurons.add(gene);
        }

    }

    public void MutateWeights(double mut_rate, double prob_new_mut, double MaxPertubation) //mutates the connection weights
    {
        for(GLink link : vecLinks)
        {
            if(Math.random() < mut_rate) //less than mutation rate
            {
                if(Math.random() < prob_new_mut) //changes the weight
                {
                    link.changeWeight(Rand.randomNumber(-0.4f, 0.4f));
                } else
                {
                    link.changeWeight(link.getWeight() + Rand.randomNumber(-1 * (float)MaxPertubation, (float)MaxPertubation));
                }
            }
        }
    }


    public void MutateActivationResponse(double mut_rate, double MaxPertubation) //changes the activation responses of the neurons
    {
        for(GNeuron gene : vecNeurons)
        {
            if(Math.random() < mut_rate)
            {
                //gene.mutateActivationResponse(MaxPertubation); //don't want to mutate activation in this project
            }
        }
    }

    public double GetCompatibilityScore(Genome genome) //calculates the cinoatibility score bwteen two genomes
    {
        //travel down the length of each genome counting the number of disjoint genes, the number of excess genes and the number of matched genes
        double NumDisjoint = 0;
        double NumExcess = 0;
        double NumMatched = 0;

        //this records the summed difference of weights in matched genes
        double WeightDifference = 0;

        //indexes in each genome. They are incremented as we step down each genomes length

        int g1 = 0;
        int g2 = 0;

        while( (g1 < vecLinks.size() - 1) || (g2 < genome.getNumLinks() - 1))
        {
            //we've reached the end of genome1 but not genome2 so increment the excess score
            if(g1 == vecLinks.size() - 1)
            {
                g2++;
                NumExcess++;

                continue;
            }

            //and vice versa
            if(g2 == genome.getNumLinks() - 2)
            {
                g1++;
                NumExcess++;
                continue;
            }

            //get the innovation numbers for each gene at this point

            int id1 = vecLinks.get(g1).getInnovationId();
            int id2 = genome.getSLinkGene(g2).getInnovationId();

            if(id1 == id2) //same innovation number
            {
                //get the weight difference between these two genes

                WeightDifference += Math.abs((float)vecLinks.get(g1).getWeight() - (float)genome.getSLinkGene(g2).getWeight());

                g1++;
                g2++;
                NumMatched++;
            }

            //innovation numbers are different so increment the disjoint score
            if(id1 < id2)
            {
                NumDisjoint++;
                g1++;
            }

            if(id1 > id2)
            {
                NumDisjoint++;
                g2++;
            }


        } //end while

        //get the length of the longest genome

        int longest = genome.getNumLinks() + genome.getNumGenes();

        if(getNumGenes() + getNumLinks() > longest)
        {
            longest = getNumGenes() + getNumLinks();
        }

        //these are the multipliers used to tweak the final score

        double mDisjoint = 1;
        double mExcess = 1;
        double mMatched = 0.4;

        if(NumMatched == 0)
        {
            NumMatched = 0.01;
        }

        double score = (mExcess * NumExcess / (double)longest) + (mDisjoint * NumDisjoint / (double)longest) + (mMatched * WeightDifference / NumMatched);

        return score;

    }




    //methods on the side

    private boolean DuplicateLink(int NeuronIn, int NeuronOut) //returns true if the specified link is already part of the genome
    {
        for(GLink link : vecLinks)
        {
            if(link.sameLink(NeuronIn, NeuronOut)) //if it is the same link
            {
                return true;
            }
        }

        return false;
    }


    private int GetElementPos(int neuron_id) //returns the position of a neuron id
    {
        for(int i = 0; i < vecNeurons.size(); i++)
        {
            if(vecNeurons.get(i).getID() == neuron_id)
            {
                return i;
            }
        }
        return -1;
    }

    private boolean AlreadyHaveThisNeuronId(int ID) //tests if the passed ID is the same as any existing neuron IDs, used in AddNeuron
    {
        for(GNeuron gene : vecNeurons)
        {
            if(gene.getID() == ID)
            {
                return true;
            }
        }
        return false;
    }


    //accessors

    public double getFitness()
    {
        return fitness;
    }

    public double getAdjustedFitness()
    {
        return adjustedFitness;
    }
    
    public ArrayList<GNeuron> getAllNeurons() {
        return vecNeurons;
    }
    
    public ArrayList<GLink> getAllLinks() {
        return vecLinks;
    }

    public int getNumGenes()
    {
        return vecNeurons.size();
    }

    public int getNumLinks()
    {
        return vecLinks.size();
    }


    public GLink getSLinkGene(int i)
    {
        return vecLinks.get(i);
    }

    public GNeuron GetSNeuronGene(int i)
    {
        return vecNeurons.get(i);
    }

    public int SNeuronGenePos(int neuronID)
    {
        for(int i = 0; i < vecNeurons.size(); i++)
        {
            if(vecNeurons.get(i).getID() == neuronID)
            {
                return i;
            }
        }
        return -1;
    }

    public int getNumInputs()
    {
        return numInputs;
    }

    public int getNumOutputs()
    {
        return numOutputs;
    }

    public void setFitness(double fit)
    {
        fitness = fit;
    }

    public void setAdjustedFitness(double fit)
    {
        adjustedFitness = fit;
    }

    public int getGenomeID()
    {
        return GenomeId;
    }

    public void setGenomeID(int newid)
    {
        GenomeId = newid;
    }

    public void SortGenes() //sorts the links and neurons based on innovation id 
    {
        //sorts using selection sort
        
        //sorts the links

        for(int i = 0; i < vecLinks.size(); i++)
        {
            int min = i;
            for(int j = i + 1; j < vecLinks.size(); j++)
            {
                if(vecLinks.get(j).getInnovationId() < vecLinks.get(min).getInnovationId())
                {
                    min = j;
                }
            }

            GLink temp = vecLinks.get(min);
            vecLinks.set(min, vecLinks.get(i));
            vecLinks.set(i, temp);
        }

        //sorts the neurons
        for (int i = 0; i < vecNeurons.size(); i++)
        {
            int min = i;
            for (int j = i + 1; j < vecNeurons.size(); j++)
            {
                if (vecNeurons.get(j).getID() < vecNeurons.get(min).getID())
                {
                    min = j;
                }
            }

            GNeuron temp = vecNeurons.get(min);
            vecNeurons.set(min, vecNeurons.get(i));
            vecNeurons.set(i, temp);
        }
    }

    public ArrayList<Double> UpdateGenome(ArrayList<Double> inputs)
    {
        return Phenotype.Update(inputs, "snapshot");
    }
    
    @Override
    public Genome clone() {  //clones the genome
        ArrayList<GNeuron> clonedNeurons = new ArrayList<>();
        ArrayList<GLink> clonedLinks = new ArrayList<>();
        
        for(GNeuron g: vecNeurons) {
            clonedNeurons.add(new GNeuron(g.getID(),g.getSplitX(), g.getSplitY(), g.getRecurrent(), g.getNeuronType()));
        }
        
        for(GLink g : vecLinks) {
            clonedLinks.add(new GLink(g.getFromNeuron(), g.getToNeuron(), g.getIsEnabled(), g.getIsRecurrent(), g.getWeight(), g.getInnovationId()));
        }
        
        Genome copy = new Genome(this.GenomeId, clonedNeurons, clonedLinks, this.numInputs, this.numOutputs);
        
        return copy;
    }



}
