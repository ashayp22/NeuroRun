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
public class Ga { //genetic algorithm class

//sources/help:  AI Techniques for Game Programming by Mat Buckland
//Evolving Neural Networks through Augmenting Topologies by Kenneth O. Stanley and Risto Miikkulainen (ideas + concepts)
    
//additional readings:
//https://towardsdatascience.com/neat-an-awesome-approach-to-neuroevolution-3eca5cc7930f
//https://www.cs.ucf.edu/~kstanley/neat.html    
    
//how much of this code is mine vs what other people wrote
//I have written all of the code, partly because there aren't java implementations of the algorithmm and I won't learn anything by copying and pasting code
//the ideas I got for creating the whole hierarchy comes from descriptions of methods and classes I should incorporate,
//along with snippets of code in different languages part of the most important methods
//plus pre/post conditions from the sources


    //fields

    private ArrayList<Genome> vecGenomes;

    private ArrayList<Species> vecSpecies;

    private ArrayList<NeuralNet> vecNeuralNet;
    
    private ArrayList<Double> averageFitnesses; //average fitness per generation
    
    private Innovation innovation;

    public int generation = 1;

    //constructor
    public Ga(int numAI, int inputs, int outputs) //parameters: number of ai/members of the population, the number of inputs for the NN, and the number of outputs for the NN
    { 
        vecGenomes = new ArrayList<Genome>();

        vecSpecies = new ArrayList<Species>();

        vecNeuralNet = new ArrayList<NeuralNet>();

        averageFitnesses = new ArrayList<>();
        
        innovation = new Innovation();
        
        //creates the appropirate innovations

        //neuron innovations
        for(int i = 0; i < inputs; i++)
        {
            innovation.CreateNewInnovation(-1, -1, "neuron", i + 1, "input"); //adds innovation
        }

        //bias
        innovation.CreateNewInnovation(-1, -1, "neuron", inputs + 1, "bias");

        for (int i = 0; i < outputs; i++)
        {
            innovation.CreateNewInnovation(-1, -1, "neuron", inputs + i + 2, "output"); //adds innovation
        }

        //link innovations

        for (int i = 0; i < inputs; i++) //every input is connecting to an output
        {
            for(int j = 0; j < outputs; j++)
            {
                innovation.CreateNewInnovation(i + 1, inputs + j + 2, "link", -1, "none");
            }
        }

        //bias link innovation
        for (int j = 0; j < outputs; j++)
        {
            innovation.CreateNewInnovation(inputs + 1, inputs + j + 2, "link", -1, "none");
        }
        

        //creates the ai and their neural networks
        for (int i = 0; i < numAI; i++)
        {
            vecGenomes.add(new Genome(Genome.NextGenomeID++, inputs, outputs, innovation)); //adds the new genome
            vecNeuralNet.add(vecGenomes.get(i).CreatePhenotype(CalculateNetDepth(vecGenomes.get(i)))); //creates its phenotype
        }


    }


    //methods

    //called when all the members of the population is dead
    
    //pre-condition: the AI must all be dead and have a certain fitness
    //parameter: list representing the fitness of each AI, length equals the number of AI
    //post-condition: epoch has occured, meaning a new generation of AI has been created through crossover and speciation
    public void Epoch(ArrayList<Double> FitnessScores)
    {
//        System.out.println("epochepochepochepochepochepochepochepochepochepochepoch");
        
        //first check to make sure we have the correct amount of fitness scores
        if(FitnessScores.size() != vecGenomes.size())
        {
            System.out.println("EPOCH WARNING");
        }
        
        //update average fitness
        
        double avg = 0;
        for(Double d : FitnessScores) {
            avg += d;
        }
        
        averageFitnesses.add(avg / FitnessScores.size());

        //after this, first of all, any phnotypes created during the previous generation are deleted. The program then examines each species
        //in turn and deletes all of its members apart from the best performing one. If a species hasn't made any fitness improvements, the species is killed off

        ResetAndKill();

        //update the genomes with the fitnesses scored in the last run
        for(int gen = 0; gen < vecGenomes.size(); gen++)
        {
            vecGenomes.get(gen).setFitness(FitnessScores.get(gen));
        }

        //sort genomes and keep a record of the best performers
        SortAndRecordAI();

        //separate the population into species of similar topology, adjust fitnesses and calculate spawn levels
        SpeciateAndCalculateSpawnLevels();

        //continue with epoch method...


        //holds the new population of genomes
        ArrayList<Genome> newPopulation = new ArrayList<>();

        //request the offspring from each species. The number of children 
        //to spawn is a double which we need to convert into an int
        int NumSpawnedSoFar = 0;

        Genome baby = new Genome();


//        System.out.println("working species");
        
        //now to iterate though each species selecting offspring to be mated and mutated
        for(int spc = 0; spc < vecSpecies.size(); spc++)
        {
            //becuase of the number to spawn from each species is a double
            //rounded down or up to an integer may cause an overflow of genomes spawned
            //this makes sure it doesn't happen

            if(NumSpawnedSoFar < Main.NUM_AI)
            {
                //this is the amount of offspring this species is requred to spawn
                //rounded simply rounds the double up or down
                int NumToSpawn = (int)Math.round((float)vecSpecies.get(spc).NumToSpawn());

                boolean ChosenBestYet = false;

                while(NumToSpawn > 0)
                {
//                    System.out.println("at top of while");
                    //first grab the best preforming genome from this species
                    //and transfer to the new population without mutation
                    //this provides per species elitism

                    if(!ChosenBestYet)
                    {
                        baby = vecSpecies.get(spc).getLeader();
                        ChosenBestYet = true;
                    } else
                    {
                        //if the number of individuals in this species is only one
                        //then we can only perform mutation
                        if(vecSpecies.get(spc).NumMembers() == 1)
                        {
                            //spawn a child
                            baby = vecSpecies.get(spc).Spawn();
                        } else
                        {
                            //if greater than one we can use the crossover operator

                            //spawn1
                            Genome g1 = vecSpecies.get(spc).Spawn();

                            if (Math.random() < Main.CROSSOVER_RATE)
                            {

                                //spawn2, make sure it is not the same as g1
                                Genome g2 = vecSpecies.get(spc).Spawn();

                                //number of attempts at finding a different genome
                                int NumAttempts = 5;

                                while ((g1.getGenomeID() == g2.getGenomeID()) && (NumAttempts > 0)) {
                                    g2 = vecSpecies.get(spc).Spawn();
                                    NumAttempts--;
                                }

                                if (g1.getGenomeID() != g2.getGenomeID())
                                {
                                    baby = Crossover(g1, g2);
                                    System.out.println("yah yheet");
                                }

                            }  else
                            {
                                baby = g1;
                            }

                        }

                        Genome.NextGenomeID++; //increases the id

                        baby.setGenomeID(Genome.NextGenomeID);
                        //now we have a spawned child lets mutate it! First there is a chance a neuron may be added

                        
                        if(baby.getNumGenes() < Main.MAX_PERMITTED_NEURONS)
                        {
                            System.out.println("currently adding a neuron");
                            baby.AddNeuron(Main.CHANCE_ADD_NODE, Main.NUM_TRYS_TO_FIND_OLD_LINK, innovation);
                        }

                        //nows there's a chance a link may be added
                        baby.AddLink(Main.CHANCE_ADD_LINK, Main.CHANCE_ADD_RECURRENT_LINK, Main.NUM_TRYS_TO_FIND_LOOPED_LINK, Main.NUM_ADD_LINK_ATTEMPTS, innovation);

//                        System.out.println("added link");
                        
                        //mutate the weights
                        baby.MutateWeights(Main.MUTATION_RATE, Main.PROBABILITY_WEIGHT_REPLACED, Main.MAX_WEIGHT_PERTUBATION);

//                        System.out.println("mutated");
                        
                        //mutate the activation response
                        baby.MutateActivationResponse(Main.ACTIVATION_MUTATION_RATE, Main.MAX_ACTIVATION_PERTUBATION);
                    }
                    
                    //sort the babies genes by their innvovation numbers
                    baby.SortGenes();

                    //add to new pop
                    newPopulation.add(baby);
                    
                    NumSpawnedSoFar++;

                    if (NumSpawnedSoFar == Main.NUM_AI)
                    {
                        NumToSpawn = 0; //oh no, too many ai spawned; must get out of the loop
                    }

                    NumToSpawn--;
                    
                } //end while

            } //end if
//            System.out.println("next species");
        } //next species

        
//        System.out.println("done spawning");
        
        //if there is an underflow due to a rounding error when adding up all the species spawn amounts, and the amount of offspring falls short of
        //the population size, additional children need to be created and added to the new population. This is achieved simply, by using tournament selection
        //over the entire population

        if(NumSpawnedSoFar < Main.NUM_AI)
        {
            //calculate the amount of additional children required
            int required = Main.NUM_AI-NumSpawnedSoFar;

            while(required > 0)
            {
                Genome copyGenome = TournamentSelection(Main.NUM_AI / 2);
                Genome.NextGenomeID++; //increases the id
                copyGenome.setGenomeID(Genome.NextGenomeID);
                newPopulation.add(copyGenome);
                required--;
            }
        }
        
//        System.out.println("done all, now creating");

        //replaces the current population with the new one
        vecGenomes = newPopulation;

        //create the phenotypes
        ArrayList<NeuralNet> new_phenotypes = new ArrayList<>();

        for(int gen = 0; gen < vecGenomes.size(); gen++)
        {
            //calculate the max network depth
            int depth = CalculateNetDepth(vecGenomes.get(gen));

            NeuralNet phenotype = vecGenomes.get(gen).CreatePhenotype(depth);

            new_phenotypes.add(phenotype);
        }

        //replaces the current phenotypes with new ones
        vecNeuralNet = new_phenotypes;

        generation++; //one more generation
        
//        System.out.println("all over");
        
    }

    private Genome TournamentSelection(int N)
    {
        double BestFitnessSoFar = 0;

        int chosenOne = 0;

        //Select N memebrs from the population at random testing against the best found so far
        for (int i = 0; i < N; i++) {
            int ThisTry = Rand.randomInt(0, vecGenomes.size() - 1);

            if(vecGenomes.get(ThisTry).getAdjustedFitness() > BestFitnessSoFar)
            {
                chosenOne = ThisTry;

                BestFitnessSoFar = vecGenomes.get(ThisTry).getAdjustedFitness();
            }
        }

        //return the champion
        return vecGenomes.get(chosenOne).clone();
    }


    private void ResetAndKill() //any phenotypes created during the previous generation are deleted
    { 
        vecNeuralNet = new ArrayList<>();
    }

    private void SortAndRecordAI() 
    {
        //then examines each species in turn and deletes all of its msmbers apart from the best performing one
        //if a species hasn't made any improvment, it is killed off

        for(Species species : vecSpecies)
        {
            species.Purge(); //purges except its best genome
        }

        ArrayList<Integer> spotsRemoved = new ArrayList<>();

        for(int i = 0; i < vecSpecies.size(); i++)
        {
            if(vecSpecies.get(i).GensNoImprovement() >= Main.NUM_GENS_ALLOWED_NO_IMPROVEMENT)
            {
                spotsRemoved.add(i);
            }
        }

        int removed = 0;
        //removes the bad species
        for(int i = 0; i < spotsRemoved.size(); i++)
        {
            //Debug.Log("species removed");
            vecSpecies.remove(spotsRemoved.get(i) - removed);
            removed++;
        }
    }

    private void SpeciateAndCalculateSpawnLevels()
    {
        //calculates the compatibility distance of each genome against the representative genome from each live species. 
        //if the value is within a set tolerance, the individual is added to that species
        //if no species match is found, then a new species is created and the genome is added to that

        for(Genome genome : vecGenomes)
        {
            double minimumCompatibility = 100000000;
            int speciesIndex = -1;

            int index = 0;
            for(Species species : vecSpecies)
            {
                double current = genome.GetCompatibilityScore(species.getLeader());

                if (current < minimumCompatibility && current <= 0.3) //has to be very closely compatible to count
                {
                    minimumCompatibility = current;
                    speciesIndex = index;
                }
                index++;
            }

            if(speciesIndex != -1) //add to species
            {
                vecSpecies.get(speciesIndex).AddMember(genome);
            } else //create new species
            {
                vecSpecies.add(new Species(genome, vecSpecies.size() + 1));
            }

        }


        //then, adjusts and shares the fitness scores

        for(Species species : vecSpecies)
        {
            species.AdjustFitnesses(); 
        }

        //next, calculate how many offspring each individual is predicted to spawn in the next generation
        //this is a floating point value calculated by dividing each genomes adjusted fitness score with the average adjusted fitness score of the population

        //calculates the population's average adjusted fitness score
        double averagePopulation = 0;

        for(Genome g : vecGenomes)
        {
            averagePopulation += g.getAdjustedFitness();
        }

        averagePopulation /= vecGenomes.size();

        //calculates the spawn amount for each species
        for(Species species : vecSpecies)
        {
            species.CalculateSpawnAmount(averagePopulation);
        }


    }


    private Genome Crossover(Genome mom, Genome dad) //crossover method
    {
        //first, calculate the genome we will using the disjoint/excess genes from
        //this is the fittst genome. if they are of equal fitnes use the shorter

        int bestParent; //1 is mom, 2 is dad

        if (mom.getFitness() == dad.getFitness())
        {
            //if they are of equal fitness and length just choose one at random
            if (mom.getNumLinks() == dad.getNumLinks())
            {
                if(Math.random() > 0.5) {
                    bestParent = 1;
                } else {
                    bestParent = 2;
                }
            }
            else
            {
                if (mom.getNumLinks() < dad.getNumLinks())
                {
                    bestParent = 1;
                }
                else
                {
                    bestParent = 2;
                }
            }
        }
        else
        {
            if (mom.getFitness() > dad.getFitness())
            {
                bestParent = 1;
            }
            else
            {
                bestParent = 2;
            }
        }

        //these vectors will hold the offspring's neurons and genes
        ArrayList<GNeuron> BabyNeurons = new ArrayList<>();
        ArrayList<GLink> BabyLinks = new ArrayList<>();

        ArrayList<Integer> vecneurons = new ArrayList<>(); //temporay list to store all added neuron IDs

        //create iterators so we can step through each parents genes and set 
        //them to the first gene of the parent

        int momIterator = 0;
        int dadIterator = 0;

        //this will hold a copy of the gene we wish to add at each step 
        GLink SelectedGene = new GLink();

        //step through each parents genes until we reach the end of both
        while (!((momIterator == mom.getNumLinks()) && (dadIterator == dad.getNumLinks())))
        {

            //the end of mom's genes have been reached
            if ((momIterator == mom.getNumLinks()) && (dadIterator != dad.getNumLinks()))
            {
                //if dad is fittest
                if (bestParent == 2)
                {
                    //adds dads genes
                    SelectedGene = dad.getSLinkGene(dadIterator);
                }

                //move onto dad's next gene
                dadIterator++;
            }
            else if ((dadIterator == dad.getNumLinks()) && (momIterator != mom.getNumLinks())) //end of dad's genes
            {
                //if mom is the fittest
                if (bestParent == 1)
                {
                    SelectedGene = mom.getSLinkGene(momIterator);
                }

                //move onto next mom
                momIterator++;
            }
            else if (mom.getSLinkGene(momIterator).getInnovationId() < dad.getSLinkGene(dadIterator).getInnovationId())
            {
                //mom's innovation number is less than dads
                //if mom is the fittest
                if (bestParent == 1)
                {
                    SelectedGene = mom.getSLinkGene(momIterator);
                }

                //move onto next mom
                momIterator++;
            }
            else if (mom.getSLinkGene(momIterator).getInnovationId() > dad.getSLinkGene(dadIterator).getInnovationId())
            {
                //dad's innovation number is less than moms
                //if dad is fittest
                if (bestParent == 2)
                {
                    //adds dads genes
                    SelectedGene = dad.getSLinkGene(dadIterator);
                }

                //move onto dad's next gene
                dadIterator++;

            }
            else if (mom.getSLinkGene(momIterator).getInnovationId() == dad.getSLinkGene(dadIterator).getInnovationId())
            {
                //innovation numbers are the same
                if (Math.random() < 0.5)
                {
                    SelectedGene = dad.getSLinkGene(dadIterator);
                }
                else
                {
                    SelectedGene = mom.getSLinkGene(momIterator);
                }

                //move onto next gene

                dadIterator++;
                momIterator++;

            }

            //add the selected gene if not already added
            if (BabyLinks.isEmpty())
            {
                BabyLinks.add(SelectedGene);
            }
            else
            {
                if (BabyLinks.get(BabyLinks.size() - 1).getInnovationId() != SelectedGene.getInnovationId())
                {
                    BabyLinks.add(SelectedGene);
                }
            }

            //Check if we already have the neurons refered to in SelectedGene
            //If not, they need to be added
            if (!checkInList(vecneurons, SelectedGene.getFromNeuron()))
                vecneurons.add(SelectedGene.getFromNeuron());

            if (!checkInList(vecneurons, SelectedGene.getToNeuron()))
                vecneurons.add(SelectedGene.getToNeuron());

            System.out.println("mom size: " + mom.getNumLinks() + " vs " + momIterator + "   dad size: " + dad.getNumLinks() + " vs " + dadIterator);
        } //end of while loop
        
        System.out.println("outside while loop");

        //now, create the required neurons. First sort them into order

        //quick selection sort
        for (int i = 0; i < vecneurons.size(); i++)
        {
            int min = i;
            for (int j = i + 1; j < vecneurons.size(); j++)
            {
                if (vecneurons.get(j) < vecneurons.get(min))
                {
                    min = j;
                }
            }

            int temp = vecneurons.get(min);
            vecneurons.set(min, vecneurons.get(i));
            vecneurons.set(i, temp);
        }

        //add neurons
        for (int i = 0; i < vecneurons.size(); i++)
        {
            //checks the mom
            int neuronID = vecneurons.get(i);
            int momIndex = mom.SNeuronGenePos(neuronID);
            int dadIndex = dad.SNeuronGenePos(neuronID);

            //checks mom first
            if (momIndex >= 0)
            {
                BabyNeurons.add(mom.GetSNeuronGene(momIndex)); //adds neuron gene
            }
            else
            {
                BabyNeurons.add(dad.GetSNeuronGene(dadIndex)); //adds dad's neuron gene
            }
        }

        //finally, create the genome
        Genome babyGenome = new Genome(++Genome.NextGenomeID, BabyNeurons, BabyLinks, mom.getNumInputs(), mom.getNumOutputs());

        
        System.out.println("finished crossover");
        
        return babyGenome; //returns the baby

    }


    private boolean checkInList(ArrayList<Integer> vecneurons, int neuron) //checks a neuron to be in a list of neurons
    {
        for(int i : vecneurons)
        {
            if (i == neuron)
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkInList2(ArrayList<Double> arr, double val) //checks a double to be in a list of doubles
    {
        for(double i : arr)
        {
            if (i == val)
            {
                return true;
            }
        }
        return false;
    }

    private int CalculateNetDepth(Genome genome) //calculates the depth of the NN
    {
        ArrayList<Double> yDepths = new ArrayList<>();
        int depth = -1;
        for(int i = 0; i < genome.getNumGenes(); i++)
        {
            double y = genome.GetSNeuronGene(i).getSplitX();
            if(!checkInList2(yDepths, y))
            {
                yDepths.add(y);
                depth++;
            }
        }
        return depth;
    }


    public ArrayList<Double> UpdateMember(int index, ArrayList<Double> inputs) //updates the AI
    {
        //pre-condition: the index of the Ai member(>0) and its inputs are passed in(must be appropriate size)
        //post-condition: a boolean representing if the AI will activate(do something, true) or not(false)
        ArrayList<Double> outputs = vecGenomes.get(index).UpdateGenome(inputs);
        return outputs;
    }


    //returns the neural network information of a bird


    public int getNNId(int index, int nodeIndex) //returns the id of a node based on its index and the index(specific neural net) a
    {
        return vecNeuralNet.get(index).getIdFromIndex(nodeIndex);
    }

    public int getNNNodeSize(int index) //returns the size of a neural network based on index a
    {
        return vecNeuralNet.get(index).getNNsize();
    }


    public Vector2 getNNNodePosFromID(int index, int id) //returns the position of a node based on its id a
    {
        return vecNeuralNet.get(index).getNodePositionFromID(id);
    }

    public ArrayList<PLink> getNNConnections(int index, int aiIndex) //returns the connections of a ai a
    {
        return vecNeuralNet.get(index).getForwardConnections(aiIndex);
    }

    //get best NN
    
    public Genome getGenome(int index) { //returns a genome, used only for getting the best one
        return vecGenomes.get(index);
    }
    
    public int getGenomeDepth(int index) {//returns a genome's depth, used only for getting the best one
        return CalculateNetDepth(vecGenomes.get(index));
    }
    
    //returns the number of species
    public int getNumSpecies() {
        return vecSpecies.size();
    }
    
    //returns average fitnesses
    
    public ArrayList<Double> getAverageFitnesses() {
        return averageFitnesses;
    }
    
    //returns the number of innovations
    
    public int getNumInnovations() {
        return innovation.databaseSize();
    }

    //clears the innovations
    
    public void clearInnovations() {
        innovation.ClearInnovations();
    }
}
