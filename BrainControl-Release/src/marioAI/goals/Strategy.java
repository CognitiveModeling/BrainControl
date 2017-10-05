/*******************************************************************************
 * Copyright (C) 2017 Cognitive Modeling Group, University of Tuebingen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * You can contact us at:
 * University of Tuebingen
 * Department of Computer Science
 * Cognitive Modeling
 * Sand 14
 * 72076 Tübingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioAI.goals;

import java.util.ArrayList;
import java.util.Random;
import marioAI.agents.CAEAgent;
import marioWorld.engine.PlayerWorldObject;

/**
 * Strategies are used in the StrategyStorage
 * and in the Voiceplanner discussion between
 * two players to save aquired strategies, measure their
 * successrate (over time) and communicate them 
 * to other players.
 * 
 */
public class Strategy {
	//number of successes and failures
	private int successes             = 1;
	private int failures              = 0;
	//the successrate of the Strategy:
	protected double successRate      = 1;
	//the players recollection of the strategy, fades away over time
	private double recollection       = 1;
	//the steps since the strategy was called the last time
	private double stepsSinceLastCall = 0;
	//determines whether a Strategy is forgotten, a Strategy
	//is forgotten when it's recollection drops below
	//the define MEMORY_BARRIER constant
	private boolean forgotten         = false;
	//the costs of the Strategy
	private double costs              = 1.0; // before first successfull execution: high value (never seen costs >= 1.0) as default
	private double probability 		  = 0.0; // overwritten by 
	
	//getters and setters for costs and probability
	protected double getProbability() {
		return probability;
	}
	protected void setProbability(double probability) {
		this.probability = probability;
	}
	protected double getCosts() {
		return costs;
	}
	protected void setCosts(double costs) {
		this.costs = costs;
	}

	//the stored list of GeneralizedGoals which describes each steps of the strategy
	private ArrayList<GeneralizedGoal> pathToSuccess;
	//the original list of goals from which the Strategy was created
	private ArrayList<Goal> originalGoalList = new ArrayList<Goal>();
	//the extracted PlayerWorldObjcets which occur as targets in the generalized goals of the Strategy:
	private ArrayList<PlayerWorldObject> requiredObjects;
	//the final generalized goal of the Strategy
	private GeneralizedGoal finalGoal;
	
	//constants for forgetting strategies
	private static final double MEMORY_BARRIER = 0.1;
	private static final double TIME_RATE      = 0.1;
	private static final double decayFunction(double x){
		return Math.exp(-x);
	}
	
	/**  The main constructor of a strategy takes a list of goals,
	 *  degeneralizes it and creates a new Strategy object
	 *  containing the generalizedGoal list as its
	 *  pathToSuccess field.
	 *  
	 *  @param goalList the list of goals from which the Strategy is created
	 */
	public Strategy(ArrayList<Goal> goalList){
		originalGoalList= goalList;
		this.pathToSuccess = new ArrayList<GeneralizedGoal>();
		for(Goal goal: goalList){
			GeneralizedGoal generalizedGoal = new GeneralizedGoal(goal);
			this.pathToSuccess.add(generalizedGoal);
		}
		filterForObjects(pathToSuccess);
		if (! pathToSuccess.isEmpty())this.finalGoal       = pathToSuccess.get(pathToSuccess.size() - 1);
	}
	
	// for testing purposes
	Strategy(ArrayList<GeneralizedGoal> goalList, boolean forTesting){
		if (forTesting){
			this.pathToSuccess = goalList;
			this.finalGoal = pathToSuccess.get(pathToSuccess.size() - 1);
		}
		else System.out.println("Error! Code too dirty!");
	}
	
	
	/** 
	 * Simulates that a strategy is slowly forgotten. It increments the
	 * steps since the strategy was last called (by the constant TIME_RATE
	 * defined in the Strategy class) and reduces the recollection value.
	 * The recollection value is calculated by applying the static dacayFunction
	 * defined in the Strategy class to the number of steps since its last call.
	 * A strategy becomes forgotten if its recollection value drops below the
	 * MEMORY_BARRIER constant specified in the Strategy class.
	 */
	public void decay(){
		stepsSinceLastCall += TIME_RATE;
		recollection = decayFunction(stepsSinceLastCall);
		//if the recollection fades below a certain level, 
		forgotten = (recollection < MEMORY_BARRIER);
		updateSuccessRate();
	}
	
	/** 
	 *  Returns the successRate, a number combining the
	 *  success/failure ratio, the steps since the Strategys
	 *  last call, and the costs of the Stretgy.
	 *  
	 *  @return the successRate as a double value
	 */
	public double getSuccessRate(){
		return this.successRate;
	}
	/** 
	 * Returns the last element of the list pathToSuccess which
	 * contains the generalized Goals for the Strategy to be
	 * executed.
	 * 
	 * @return the final Goal as a GeneralizedGoal object
	 */
	public GeneralizedGoal getFinalGoal(){
		return this.finalGoal;
	}
	
	/** 
	 * Returns a list of the PlayerWorldObjects which occur as
	 * targets in the generalizedGoal list pathToSuccess 
	 * @return a list of PlayerWorldObjects representing the required targets
	 */
	public ArrayList<PlayerWorldObject> getRequiredObjects(){
		return this.requiredObjects;
	}
	/**
	 * Returns the generalizedGoal list contained in the Strategy. The core
	 * information of every Strategy
	 * @return the Strategy's pathToSuccess, a list of GeneralizedGoal objects
	 */
	public ArrayList<GeneralizedGoal> getPathToSuccess(){
		return this.pathToSuccess;
	}
	
	public ArrayList<Goal> getOriginalGoalList(){
		return this.originalGoalList;
	}
	/**
	 * Checks if the Strategy is forgotten. A Strategy is forgotten, when its
	 * recollection drops below a MEMORY_BARRIER constant specified in the
	 * Strategy class. 
	 * @return the boolean value whether the Strategy is forgotten
	 */
	public boolean isForgotten(){
		return this.forgotten;
	}
	
	
	/**
	 * Scans a Strategy's generalizedGoalList and adds the contained targets of
	 * each generalizedGoal to the Strategy's requiredObjects list. 
	 * @param generalizedGoalList the Strategy's list of generalizedGoal objects
	 */
	private void filterForObjects(ArrayList<GeneralizedGoal> generalizedGoalList){
		requiredObjects = new ArrayList<PlayerWorldObject>();
		for(GeneralizedGoal generalizedGoal: generalizedGoalList){
			PlayerWorldObject pwo = generalizedGoal.getTarget();
			requiredObjects.add(pwo);
		}
	}
	
	private void updateSuccessRate(){
		if (successes + failures == 0) successRate = 0; 
		else successRate = (successes*recollection)/(successes+failures);
	}
	
	/**
	 * Compares two Strategies for equality. Strategies are equal if their
	 * contained pathToSuccess lists are equal (contain equal generalizedGoals
	 * in the exact same order)
	 * @param other an other Strategy to compare this Strategy to
	 * @returns a boolean value whether the Strategy objects are equal or not
	 */
	@Override
	public boolean equals(Object other){
		if(other instanceof Strategy){
			Strategy otherStrategy = (Strategy) other;
			if(this.getPathToSuccess().equals(otherStrategy.getPathToSuccess())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "Plan: " + pathToSuccess +
			   ",\n Success Rate: " + successRate +
			   ", Probability: " + probability +
			   ", Costs: " + costs;
			}
	
	/**
	 * evaluates the Strategy and updates its stats
	 * @param success whether the usage of this Strategy was successful or not
	 */
	public void evaluateStrategy(boolean success){
		if(success){
			this.successes = this.successes + 1;
			System.out.println("New success number is " + successes);
		}
		else {
			this.failures++;
		}
		this.recollection       = 1;
		this.stepsSinceLastCall = 0;
		updateSuccessRate();
	}
	public void updateCosts(double newCosts){
		this.costs =  newCosts;
	}
	
	protected ArrayList<CAEAgent> getInvolvedAgents(){
		ArrayList<CAEAgent> agents = new ArrayList<CAEAgent>();
		for(GeneralizedGoal generalizedGoal: pathToSuccess){
			PlayerWorldObject target = generalizedGoal.getTarget();
			if(target.isPlayer()){
				if(!agents.contains(target))
					agents.add(target.findAgent());
			}
			PlayerWorldObject actor = generalizedGoal.getActor();
			if((actor != null) && (actor.isPlayer())){
				if(!agents.contains(actor))
					agents.add(actor.findAgent());
			}
		}
		return agents;
	}
	
	// if equal, returns strategy2
	public static Strategy returnBetterStrategy(Strategy strategy1, Strategy strategy2){
		System.out.print("SD: Strategy comparison: ");
		if (strategy1 == null) System.out.println("There is no strategy1!");
		if (strategy2 == null) System.out.println("There is no strategy1!");
		if ((strategy1 != null) && (strategy2 != null)) 
			System.out.println("Compare succesRates: " + strategy1.successRate + " vs. " + strategy2.successRate);
		if((strategy1 != null) && (strategy1.isBetterThan(strategy2))) return strategy1;
		else return strategy2;
	}
	
	/**
	 * compare strategies
	 * @param strategyToCompare
	 * @return true if this is better than strategyToCompare
	 */
	private boolean isBetterThan(Strategy strategyToCompare) {
		if (strategyToCompare == null) return true;
		System.out.println("Comparing strategy " + this + ", rated " + this.getRating() + ", to strategy " + strategyToCompare + ", rated " + strategyToCompare.getRating());
		return ((this.getRating() > strategyToCompare.getRating())
				|| ((this.getRating() == strategyToCompare.getRating()) && (this.getExecutions() > strategyToCompare.getExecutions())));
		//TODO: use probability and costs
	}

	/**
	 * Get the total number of how many times this Strategy was executed
	 * @return the sum of successes and failures
	 */
	public int getExecutions(){
		return successes + failures;
	}
	public void printEvaluation() {
		System.out.println("SD: Strategy evaluated with successes: " + successes + "   failures: " + failures + "   recollection: " + recollection);
	}
	
	
	/**
	 * Rate this strategy based on successRate, strategic probability and AStar costs
	 * Warning: This formula is arbitrary and should be tested and improved!
	 * @return rating to compete with other strategies
	 */
	private double getRating() {
		if (getExecutions() > 1) return successRate * probability * (1 - costs);
		else {
			final double default_rating_threshold = 0.5; // to be tested
			return new Random().nextDouble() * default_rating_threshold;
		}
	}
}
