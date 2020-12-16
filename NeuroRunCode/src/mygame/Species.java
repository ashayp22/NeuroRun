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
public class Species {

    //keep a local copy of the first member of this species

    private Genome leader;

    private ArrayList<Genome> vecMembers; //pointers to all the genomes within this species

    private int speciesID; //id number of species

    private double bestFitness; //best fitness found so far by this species

    private double avgFitness; //average fitness of the species

    //generations since fitness has improved, we can use this info to kill off a species if required
    private int gensNoImprovement;

    private int age; //age of the species

    private double spawnsRequired; //spawns required for the next population

    
    public Species(Genome first, int id)
    {
        vecMembers = new ArrayList<Genome>();
        speciesID = id;
        leader = first;
        vecMembers.add(leader);
        gensNoImprovement = 0;
        avgFitness = 0;
    }


    //this method boosts the fitness of the young, penalizes the fitnesses of the old and then performs fitness sharing over all members of the species

    public void AdjustFitnesses()
    {
        double total = 0;
        for(int gen = 0; gen < vecMembers.size(); gen++)
        {
            double fitness = vecMembers.get(gen).getFitness();

            //boost the fitness scores if the species is young
            if(age < Main.YOUNG_BONUS_AGE_THRESHOLD)
            {
                fitness *= Main.YOUNG_FITNESS_BONUS;
            }

            //punish the older species

            if(age > Main.OLD_AGE_THRESHOLD)
            {
                fitness *= Main.OLD_AGE_PENALTY;
            }

            total += fitness;

            //apply fitness sharing to the adjusted fitnessed
            double AdjustedFitness = fitness / vecMembers.size();

            vecMembers.get(gen).setAdjustedFitness(AdjustedFitness);
        }

        double newAvg = total / vecMembers.size();

        if (avgFitness + 10 >= newAvg)
        {
            gensNoImprovement++;
        }

        avgFitness = newAvg;
    }

    //adds a new individual to the species
    public void AddMember(Genome new_org)
    {
        vecMembers.add(new_org);
    }


    public void Purge() //purges entire species except its leader
    {
        int bestIndex = 0;
        double bestFitness = vecMembers.get(0).getFitness();
        for(int i = 0; i < vecMembers.size(); i++)
        {
            if(vecMembers.get(i).getFitness() > bestFitness)
            {
                bestFitness = vecMembers.get(i).getFitness();
                bestIndex = i;
            }
        }

 
        leader = vecMembers.get(bestIndex); //sets the leader
        vecMembers = new ArrayList<>(); //purges
        vecMembers.add(leader); //only leader is there
    }

    //calculates how many offspring this species should spawn
    public void CalculateSpawnAmount(double averagePopulation)
    {
        spawnsRequired = 0;
        for(Genome genome : vecMembers) {
            spawnsRequired += genome.getAdjustedFitness() / averagePopulation;
        }
    }

    //spawns an individual from the species selected at random from the best survival rate
    public Genome Spawn()
    {
        //chooses tournament selection
        int position = 0;
        double bestFit = 0;
        for(int i = 0; i < vecMembers.size()/4; i++)
        {
            int newPos = Rand.randomInt(0, vecMembers.size() - 1);
            if(vecMembers.get(newPos).getAdjustedFitness() > bestFit)
            {
                position = newPos;
                bestFit = vecMembers.get(newPos).getAdjustedFitness();
            }
        }

        
        return vecMembers.get(position).clone();
        
    }


    //accesor methods

    public Genome getLeader()
    {
        return leader;
    }

    public double NumToSpawn()
    {
        return spawnsRequired;
    }

    public int NumMembers()
    {
        return vecMembers.size();
    }

    public int GensNoImprovement()
    {
        return gensNoImprovement;
    }

    public int ID()
    {
        return speciesID;
    }

    public double SpeciesLeaderFitness()
    {
        return leader.getFitness();
    }

    public double BestFitness()
    {
        return bestFitness;
    }

    public int Age()
    {
        return age;
    }



}
