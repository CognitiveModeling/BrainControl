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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import marioAI.brain.Generalizer;
import marioAI.movement.EffectChaining;
import marioWorld.engine.PlayerWorldObject;
/**
 *  In a StrategyStorage, various Strategies for different Players are stored in
 *  subgroups depending on the generalized finalGoal stored in the Strategies.
 *  A StategyStorage also acts as a management system for strategies.
 *  
 *  
 */
public class StrategyStorage {
	//a reference to the corresponding goalPlanner:
	private GoalPlanner goalPlanner;
	//a limit, how many strategies for each degeneralized finalGoal can be stored:
	private final int strategyLimit;
	//the currentStrategy used/executed in the game:
	protected Strategy currentStrategy;
	//a mapping from degeneralized finalgoals to lists of strategies
	private Map<GeneralizedGoal,ArrayList <Strategy>> mapping = new HashMap<GeneralizedGoal,ArrayList<Strategy>>();
	//a strategy in discussion not yet added to the StrategyStorage:
	private Strategy strategyToDiscuss = null;
	
	/**
	 * The main constructor of the StrategyStorage class.
	 * 
	 * @param goalPlanner a reference to the corresponding goalPlanner
	 * @param strategyLimit limits how many Strategy objects per finalGoal are stored
	 */
	StrategyStorage(GoalPlanner goalPlanner,int strategyLimit){
		if(strategyLimit > 0){
			this.strategyLimit = strategyLimit;
		} else {
			this.strategyLimit = 1;
		}
		this.goalPlanner = goalPlanner;
	}
	
	/**
	 * Creates a Strategy to be used in a discussion without actually storing it.
	 * @param goalList the list of Goals from which the Strategy will be created
	 */
	public Strategy createStrategyToDiscuss(ArrayList<Goal> goalList) {
		this.strategyToDiscuss = new Strategy(goalList);
		return strategyToDiscuss;
	}
	/**
	 * changes the strategy used in a current discussion
	 * @param strategy the new strategy to be used as the strategyToDiscuss
	 */
	public void setStrategyToDiscuss(Strategy strategy){
		this.strategyToDiscuss = strategy;
	}
	
	
	public Strategy getStrategyToDiscuss(){
		return this.strategyToDiscuss;
	}
	public boolean hasStrategyToDiscuss(){
		if(strategyToDiscuss == null){
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Degeneralizes the goals contained in a Strategy and returns the list
	 * of degeneralized goals.
	 * @param strategy The Strategy of which the generalizedGoalList has to be degeneralized
	 * @return a list of goals, the degeneralized pathtosucess list of the strategy
	 */
	public ArrayList<Goal> getDegeneralizedGoals(Strategy strategy){
		ArrayList<Goal> res = degeneralizeGoalList(strategy.getPathToSuccess());
		return res;
	}
	
	/**
	 * Adds a new Strategy to the StrategyStorage, the other Strategies are sorted/updated
	 * accordingly. If an equal Strategy is already contained in the StrategyStorage,
	 * nothing happens.
	 * @param newStrategy the Strategy to be added
	 * @return a reference either to the newly added Strategy or to an equal one in the storage
	 */
	public Strategy addNewStrategy(Strategy newStrategy){
		slowlyForget();
		purge();
		GeneralizedGoal finalGoal = newStrategy.getFinalGoal();
		
//		System.out.println(this.toString());
		GeneralizedGoal generalizedGoalKey = null;
		for(GeneralizedGoal key: mapping.keySet()){
			if(finalGoal.equals(key)){
				generalizedGoalKey = key;
			}
		}
//		System.out.println("Printing mapping");
//		for (GeneralizedGoal entry : mapping.keySet()) {
//			System.out.println(entry);
//			for (Strategy strategyElement : mapping.get(entry)) {
//				System.out.println(strategyElement);
//			}
//			
//		}
//		System.out.println("New strategy is " + newStrategy);
		if(generalizedGoalKey == null){
//			System.out.println("Could not find strategy in storage");
			ArrayList<Strategy> arrayList = new ArrayList<Strategy>();
			arrayList.add(newStrategy);
			mapping.put(finalGoal,arrayList);
			for (GeneralizedGoal goal : newStrategy.getPathToSuccess()) {
			}
		} else {
//			System.out.println("Found strategy in storage");
			System.out.println(this.toString());
			ArrayList<Strategy> lookUp = mapping.get(generalizedGoalKey);
			if (lookUp.isEmpty()) System.out.println("Lookup is empty!");
			for(Strategy strategy: lookUp){
				System.out.println("Compared with " + strategy);
				if(strategy.equals(newStrategy)){
					return strategy;
				}
			}
			sortStrategies(lookUp);
			if(lookUp.size() > strategyLimit){
				lookUp.remove(lookUp.size() - 1);
			}
			lookUp.add(newStrategy);
			
		}
		return newStrategy;
	}
	
	/**
	 * This is a workaround, since a key generalizedGoal object is needed for lookup. This returns the
	 * actual key to be used in the mapping.
	 * @param goal the generalizedGoal for which a key has to be found
	 * @return returns the reference to the actual GeneralizedGoal object to be used as a key
	 */
	public GeneralizedGoal getExactKey(GeneralizedGoal goal) {
		GeneralizedGoal generalizedGoalKey = null;
		for(GeneralizedGoal key: mapping.keySet()){
			if(goal.equals(key)){
				generalizedGoalKey = key;
			}
		}
		return generalizedGoalKey;
	}
	
	/**
	 * determines whether a Strategy is applicable in a current Situation
	 * @param strategy the Strategsy to be tested for applicability
	 * @return the boolean value whether the Strategy is applicable
	 */
	private boolean isStrategyApplicable(Strategy strategy){
		ArrayList<GeneralizedGoal> generalizedGoalList = strategy.getPathToSuccess();
		for(GeneralizedGoal generalizedGoal: generalizedGoalList){
			ArrayList<PlayerWorldObject> goalTypes = new ArrayList<PlayerWorldObject>();
			goalTypes.add(generalizedGoal.getActor());
			Goal result = PlayerWorldObject.getNearWorldObjectGoal(goalTypes,
					this.goalPlanner.agent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene().getPlanningPlayer(),
					generalizedGoal.getCollisionAtPosition().getDirection(),
					this.goalPlanner.agent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene());
			if(result == null){
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * This function filters a Strategylist for those strategies, which are applicable in a current situation.
	 * @param strategyList the list of strategies to be filtered
	 * @return the list of strategies which are applicable in a current situation
	 */
	private ArrayList<Strategy> filterCurrentlyApplicableStrategies(ArrayList<Strategy> strategyList){
		ArrayList<Strategy> result = new ArrayList<Strategy>();
		for(Strategy strategy: strategyList){
			if(isStrategyApplicable(strategy)){
				result.add(strategy);
			}
		}
		return result;
	}
	
	
	protected Strategy getStoredStrategyFor(GeneralizedGoal generalizedGoal){
		// TODO: maybe randomize?d
		
		double alpha = 0.75; //TODO: What is this? Avoid hardcoding!
		                     //-> this is supposed to be a 'barrier',
		                     //   goals under 0.75 * the best successRate
		                     //   are not selected for a possible Strategy choice
		                     //   TODO: one of the best selected goals should be
		                     //   picked by chance, so that the best goal is not
		                     //   selected everytime this function is called

        ArrayList<Strategy> strategiesFor = mapping.get(getExactKey(generalizedGoal));
        if((strategiesFor != null) && (strategiesFor.size() > 0)){
        	    ArrayList<Strategy> filteredStrategies = filterCurrentlyApplicableStrategies(strategiesFor);
        	
                double maximalSuccessRate = 0;
                for(Strategy strategy: filteredStrategies){
                        double successRate = strategy.getSuccessRate();
                        if(successRate >= maximalSuccessRate){
                                maximalSuccessRate = successRate;
                        }
                }
                for(int i = 0;i < filteredStrategies.size() - 1;i++){
                        if(filteredStrategies.get(i).getSuccessRate() < alpha * maximalSuccessRate){
                                filteredStrategies.remove(i);
                        }
                }
                return filteredStrategies.get(0);
        } else {
                return null;
        }
	}

	//handling of current strategy:
	public void setCurrentStrategy(Strategy strategy){
		this.currentStrategy = strategy;
	}
	public void eraseCurrentStrategy(){
		this.currentStrategy = null;
	}
	public Strategy getCurrentStrategy(){
		return this.currentStrategy;
	}
	
	/**
	 * invokes the decay functions of all stored strategies.
	 */
	public void slowlyForget() {
		for (GeneralizedGoal generalizedGoal : mapping.keySet()) {
			for(Strategy strategy: mapping.get(generalizedGoal))
				strategy.decay();
		}
	}
	
	/**
	 * clears the stored strategies
	 */
	public void forgetEverything() {
		mapping.clear();
	}
	
	@Override
	public String toString(){
		String res = "------------------------------\n";
		res += "THE STRATEGYSTORAGE OF " + this.goalPlanner.agent.getPlayer().getType().name() + " CONTAINS: \n";
		for(GeneralizedGoal generalizedGoal: mapping.keySet()){
			res += "STRATEGIES FOR THE FINAL GOAL:" + generalizedGoal.toString() + "\n";
			for(Strategy strategy: mapping.get(generalizedGoal)){
				res += strategy.toString() + "\n";
			}
			res += "\n";
		}
		res += "------------------------------\n";
		return res;
	}
	
	/**
	 * Sorts a given list of strategies based on their successRate
	 * 
	 * @param strategyList the list of strategies to be sorted
	 * @return the sorted list of strategies
	 */
	private ArrayList<Strategy> sortStrategies(ArrayList<Strategy> strategyList){
		Collections.sort(strategyList, new Comparator<Strategy>() 
		{
		    @Override
		    public int compare(Strategy s, Strategy t) 
		    {
		        return (int)(s.getSuccessRate() - t.getSuccessRate());
		    }
		});	
		return strategyList;
	}
	
	/**
	 * Erases the forgotten strategies from the StrategyStorage.
	 * A Strategy is forgotten, when its successRate drops below the
	 * MEMORY_BARRIER constant defined in the Strategy class
	 */
	private void purge(){
		if(mapping.size() != 0){
			for(GeneralizedGoal generalizedGoal: mapping.keySet()){
				ArrayList<Strategy> list = mapping.get(generalizedGoal);
				for(int i=0;i<list.size();i++){
					if(list.get(i).isForgotten()){
						list.remove(i);
					}
				}
			}
		}	
	}
	
	/**
	 * Generalizes a list of goals to be stored in the StrategyStorage
	 * @param goalList a list of Goal objects to be generalized
	 * @return a list of GeneralizedGoal objects generated from the input list
	 */
	protected ArrayList<GeneralizedGoal> generalizeGoalList(ArrayList<Goal> goalList){
		ArrayList<GeneralizedGoal> generalizedGoalList = new ArrayList<GeneralizedGoal>();
		for(Goal goal: goalList){
			GeneralizedGoal generalizedGoal = generalizeGoal(goal);
			generalizedGoalList.add(generalizedGoal);
		}
		return generalizedGoalList;
	}
	
	/**
	 * Degeneralizes a list of GeneralizedGoal objects with the use of EffectChaining and
	 * ConditionVertex.
	 * 
	 * @param generalizedGoalList the list of GeneralizedGoal objects to be degeneralized
	 * @return a list of Goal objects generated from the input list
	 */
	public ArrayList<Goal> degeneralizeGoalList(ArrayList<GeneralizedGoal> generalizedGoalList){
		ArrayList<GeneralizedGoal> g = new ArrayList<GeneralizedGoal>();
		for(GeneralizedGoal generalizedGoal: generalizedGoalList){
			g.add(generalizedGoal);
		}
		// update the reachabilities in the simulated levelscene for the current real level scene
		this.goalPlanner.agent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene().updateReachabilityMaps();
		EffectChaining effectChaining =
				new EffectChaining(this.goalPlanner.agent.getPlanner().getLevelSceneAdapter().getSimulatedLevelScene(),
				g,
				degeneralizeGoal(generalizedGoalList.get(generalizedGoalList.size()-1)));
		ArrayList<Goal> degeneralizedGoals = new ArrayList<Goal>();
		effectChaining.addObserver(this.goalPlanner);
		degeneralizedGoals = effectChaining.getGoalsDGM();
		return degeneralizedGoals;
	}
	
	/**
	 * Generalizes a single goal by using the constructor of the GeneralizedGoal class.
	 * @param goal the Goal object to be generalized
	 * @return the GeneralizedGoal object generated from the input Goal
	 */
	private GeneralizedGoal generalizeGoal(Goal goal){
		return new GeneralizedGoal(goal);
	}
	
	//degeneralize a single goal
	private Goal degeneralizeGoal(GeneralizedGoal goal){
		return PlayerWorldObject.getNearWorldObjectGoal(
				Generalizer.degeneralizePlayerWorldObject(goal.getTarget()), 
				goal.getActor().findAgent().getPlanner().getLevelSceneAdapter().getSimulatedLevelScene().getPlanningPlayer(),
				goal.getCollisionAtPosition().getDirection(),
				goalPlanner.getLevelSceneAdapter().getSimulatedLevelScene());
	}
	
	public void clearCurrentStrategy(){
		this.currentStrategy = new Strategy(new ArrayList<Goal>());
		this.currentStrategy = null;
	}
}
