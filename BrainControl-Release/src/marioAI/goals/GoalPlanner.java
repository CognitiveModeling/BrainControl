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
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;

import marioAI.agents.CAEAgent;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.movement.AStarSimulator;
import marioAI.movement.EffectChainingSupervisor;

import marioUI.speechSynthesis.CooperationNegotiator;
import marioWorld.engine.PlayerWorldObject;

/**
 * Interface for all GoalPlanner. Each planner has to implement a computeGoal
 * method by itself
 */
public abstract class GoalPlanner extends Observable implements Observer {

	protected CAEAgent agent;
	protected AStarSimulator aStarSimulator;
	protected LevelSceneAdapter levelSceneAdapter;
	
	/**
	 * List of the common grounds, in which this agent takes part
	 */
	public ArrayList<CommonGround> commonGrounds = new ArrayList<CommonGround>();
	
	// TODO: this is merely used!! TODO: This does not work for effect goals
	// since these are translated to object interactions in AStar! Thus,
	// implement this in astar!!
	// protected HashSet<GoalStorage> unreachableGoals = new
	// HashSet<GoalStorage>();

	private ArrayList<Goal> currentGoal = null;
	
	private long timeStampOfLastAction;
	private boolean isSelfMotivated;
	
	private long waitingDuration = 5000; //in ms
	private boolean isReadyToPlan;
	
	private AtomicBoolean abort = new AtomicBoolean(false);
	/**
	 * Pushes forward a new goal to schematic planning and astar planning
	 * 
	 * @param goal
	 */
	protected final void invokeAStarPlanning(boolean append) {
		System.out.println(this + ": INVOKING ASTAR WITH NEW GOALS:");
		for (Goal g : getGoal()) {

			System.out.println("\t" + g);
		}

		if (getGoal().size() == 0) {
			System.err.println(this + ": No goal selected.");
			return;
		}

		setChanged();
		notifyObservers(new PlanningEvent(PlanningEventType.NEW_GOAL_SELECTED));

		aStarSimulator.setGoal(levelSceneAdapter, getGoal(), append);
	}

	protected final ArrayList<Goal> getGoal() {
		return currentGoal;
	}

	protected final void setGoal(ArrayList<Goal> goal) {
		currentGoal = goal;
	}

	protected abstract String getGoalSelectionFailureIdentifier();

	/**
	 * This method computes the actual goals -- has to be implemented in every
	 * child-class
	 * 
	 * must not return null!
	 * 
	 * @return
	 */
	protected abstract ArrayList<Goal> selectGoal();

	// STRATEGY-STORAGE----------------------------------------------------------------------------
	// here the aquired strategies which have worked in a previous situation
	// are stored
	protected StrategyStorage strategyStorage = new StrategyStorage(this,5);

	/**
	 * reservoir planning will be activated
	 * @param append
	 */
	public void selectAndApproachNewGoal(boolean append) {
		
		this.setReadyToPlan(false);
		abort.set(false);
		updateWaitingDuration();
		ArrayList<Goal> goals = selectGoal();
		

//		System.out.println("Goal: " + goals);

		

		
		if(!goals.isEmpty() && goals.get(0).isEffectChainingGoal()) {
			EffectChainingSupervisor sup = new EffectChainingSupervisor(goals.get(0),this.agent,this,append);
			new Thread(sup).start();
		} else {
				
			setGoal(goals);
			
			if(getGoal().size()>0)
			{
				invokeAStarPlanning(append);
			}
		}
		
		/*setGoal(goals);

		if (getGoal().size() > 0) {
			System.out.println("AGENT " + agent.getName() + " APPROACH NEW GOAL");
			System.out.println(goals);
			
			ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) agent.getPlanner();
			
			if(planner.activePlanner instanceof EsteemPlanner){
				
				// Prepare helping another player
				// planningPlayer suggests a plan, that is immediately accepted by the other player				
				System.out.println("Esteem");
				Strategy strategy = strategyStorage.createStrategyToDiscuss(goals);
				strategyStorage.setCurrentStrategy(strategy);
				setChanged();
				notifyObservers(StrategyPlanningEvent.newGoalplanContainsInteraction(
						strategy, agent.getPlayerIndex(), append));
				
			} 
			else
			{
				// "default" case: just start aStarPlanning on the given goal
				
				strategyStorage.setCurrentStrategy(null); // set to default for strategies without interaction
				invokeAStarPlanning(append);
			}
		}
		else{
			System.out.println("No Goals to approach");
		}*/
	}

	// goalListContainsInteraction checks if the current goallist goal of
	// GoalPlanner
	// contains any goals which contain interactions with other players
	// or if the any effects of these goals contain interactions with other
	// players
	// Hannah, Pavel
	private boolean goalListContainsInteraction() {
		for (Goal g : getGoal()) {
			// goal have a target
			PlayerWorldObject target = g.getTarget();
			PlayerWorldObject actor = this.agent.getPlayer().getType();
			if (actor.isAnotherPlayer(target)) {
				return true;
			}
			if (g.getEffect() != null) {
				PlayerWorldObject effectTarget = g.getEffect().getEffectTarget();
				if (actor.isAnotherPlayer(effectTarget)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * approach goal by preparing AStar planning
	 * if goal contains interactions or is motivated by esteem planner, invoke the appropriate processes
	 * @param goal
	 * @param append
	 */
	public void approachNewGoal(Goal originalGoal, boolean append) {
		
		this.setReadyToPlan(false);
		updateWaitingDuration();
		
		if(originalGoal.isEffectChainingGoal()) {
			EffectChainingSupervisor sup = new EffectChainingSupervisor(originalGoal,this.agent,this,append);
			new Thread(sup).start();
		} else {
			ArrayList<Goal> goallist = new ArrayList<Goal>();
			goallist.add(originalGoal);		
			setGoal(goallist);
			
			if(getGoal().size()>0)
			{
				invokeAStarPlanning(append);
			}
		}
	
		/*
		setGoal(goal);

		if (getGoal().size() > 0) {
			System.out.println("AGENT " + agent.getName() + " APPROACH NEW GOAL");
			System.out.println(goal);
			
			ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) agent.getPlanner();
			if(planner.activePlanner instanceof EsteemPlanner){
				
				// Prepare helping another player
				// planningPlayer suggests a plan, that is immediately accepted by the other player				
				System.out.println("Esteem");
				Strategy strategy = strategyStorage.createStrategyToDiscuss(goal);
				strategyStorage.setCurrentStrategy(strategy);
				setChanged();
				notifyObservers(StrategyPlanningEvent.newGoalplanContainsInteraction(
						strategy, agent.getPlayerIndex(), append));
				
			}
			
			/*else		
			if (goalListContainsInteraction()) {
				
				// Prepare strategy discussion by sending PlanningEvent to VoicePlanner
				
				Strategy strategy = strategyStorage.createStrategyToDiscuss(goal);	
				
				//------------------------------------------------------------------------------------
				// Testing strategy discussion by cheating compley strategies
				
				// Cheating for testing
//				ArrayList<GeneralizedGoal> cheatedGoals = new ArrayList<>();
//				cheatedGoals.add(new GeneralizedGoal(PlayerWorldObject.PETER, PlayerWorldObject.JAY, 
//						PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.ABOVE));
//				cheatedGoals.add(new GeneralizedGoal(ActionEffect.ENERGY_INCREASE, PlayerWorldObject.PETER, PlayerWorldObject.PETER, 
//						PlayerWorldObject.ENERGY_FRONT, PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.RIGHT));
//				Strategy strategy = new Strategy(cheatedGoals, true);
				
				// Cheating for testing
//				ArrayList<GeneralizedGoal> cheatedGoals = new ArrayList<>();
//				cheatedGoals.add(new GeneralizedGoal(ActionEffect.ENERGY_INCREASE, PlayerWorldObject.PETER, PlayerWorldObject.PETER, 
//						PlayerWorldObject.ENERGY_FRONT, PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.BELOW));
//				cheatedGoals.add(new GeneralizedGoal(PlayerWorldObject.PETER, PlayerWorldObject.JAY,
//						PlayerHealthCondition.LARGE_NON_BULB, CollisionDirection.ABOVE));
//				strategy = new Strategy(cheatedGoals, true);
				//------------------------------------------------------------------------------------------
				
				setChanged();
				notifyObservers(StrategyPlanningEvent.newGoalplanContainsInteraction(
						strategy, agent.getPlayerIndex(), append));
			}*/ /*
			else
			{
				// "default" case: just start aStarPlanning on the given goal
				
				strategyStorage.setCurrentStrategy(null); // set to default for strategies without interaction
				invokeAStarPlanning(append);
			}
		}
		else{
			System.out.println("No Goals to approach");
		} */
	}

	 /** 
	  * Force a certain Goal to be executed
	  *  Only to be used for DelayedAStraPlanning execution
	  *  if a strategy-plan needs to be discussed first
	  * 
	  * @param goalList
	  * @param append
	  */
	
	public void forceGoal(Strategy strategy, boolean append){

		// if the strategy was esteem-motivated from the beginning, execute original plan
		//(alright because the plan is accepted immediately during the "discussion")
		ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) agent.getPlanner();
		if(planner.activePlanner instanceof EsteemPlanner){
			System.out.println("Esteeem");
				setGoal(strategy.getOriginalGoalList());
				setChanged();
				 notifyObservers(new PlanningEvent(PlanningEventType.INTERACTION_GOALPLAN_FINISHED, getGoal()));	
		}
		else{
			
			//------------------------------------
			//GOAL TESTING AREA: cheat strategies to test degeneralization and execution
			/*
			PlayerWorldObject me = this.agent.getPlayer().getType();
			ArrayList<PlayerWorldObject> others = new ArrayList<PlayerWorldObject>();
			if(me != null) others.add(PlayerWorldObject.CLARK);
			if(me != null) others.add(PlayerWorldObject.BRUCE);
			if(me != null) others.add(PlayerWorldObject.PETER);
			if(me != null) others.add(PlayerWorldObject.JAY);
			ArrayList<Goal> g1 = new ArrayList<Goal>();
			g1.add(new Goal(new GlobalCoarse(0,0),others.get(2),null,others.get(3),null,CollisionDirection.BELOW,PlayerHealthCondition.IRRELEVANT));
			g1.add(new Goal(new GlobalCoarse(0,0),others.get(2),null,PlayerWorldObject.ENERGY_FRONT,null,CollisionDirection.IRRELEVANT,PlayerHealthCondition.IRRELEVANT));
			// Set currentStrategy; must happen before degeneralization!
			System.out.println("SD: Final generalized Goal: " + strategy.getFinalGoal());
			Strategy s1 = new Strategy(g1);
			*/
			//-----------------
			
			// if there was a discussion, its result has to be degeneralized 
			
			System.out.println("GENERALIZED GOALS: " + strategy.getPathToSuccess());
			strategyStorage.setCurrentStrategy(strategy); // for evaluation after execution
			ArrayList<Goal> goalList = strategyStorage.getDegeneralizedGoals(strategy);
			System.out.println("DAS GOAL HIER: " + goalList);
			setGoal(goalList);
			setChanged();
			notifyObservers(new PlanningEvent(PlanningEventType.INTERACTION_GOALPLAN_FINISHED, getGoal()));
		}
		
		// delayed invocation of AStar planning and execution
		invokeAStarPlanning(append);
	}

	public void init(CAEAgent agent, LevelSceneAdapter levelSceneAdapter) {
		this.levelSceneAdapter = levelSceneAdapter;
		this.agent = agent;

		this.aStarSimulator = agent.getAStarSimulator();
		// System.out.println(levelSceneAdapter.getPlayers().size());
		// System.out.println(levelSceneAdapter.getPlayers().get(0));
		// System.out.println(levelSceneAdapter.getPlayers().get(1));

	}

	public final LevelSceneAdapter getLevelSceneAdapter() {
		return levelSceneAdapter;
	}
	
	/**
	 * @param strategy
	 * @return true if one of the common grounds of this agent contains strategy
	 */
	protected boolean hasInCommonGround(Strategy strategy) {
		for (CommonGround cg : commonGrounds) {
			System.out.println("SD: Strategy is " + cg.isStored(strategy) + " in common ground with...");
			for (CAEAgent caeAgent : cg.getPartners(agent)) {
				System.out.println(caeAgent.getName());
			}
			if(cg.isStored(strategy)) return true;
		}
		return false;
	}

	/**
	 * Add a common ground
	 */
	public void addCommonGround(CommonGround commonGround) {
		commonGrounds.add(commonGround);
//		System.out.print("SD: " + agent.getName() + " knows about his common ground with ");
		for (CAEAgent caeAgent : commonGround.agents) {
			System.out.print(caeAgent.getName());
		}
		System.out.println();
	}

	/**
	 * Check for an existing common grounds between these players
	 * @param agents
	 * @return
	 */
	public boolean haveCommonGround(ArrayList<CAEAgent> agents) {
		return (findCommonGround(agents) != null);
	}

	/**
	 * Find the common ground with these players 
	 * Warning: Returns null if none exists!!!
	 */
	private CommonGround findCommonGround(ArrayList<CAEAgent> agents) {
		printAllCommonGrounds();
		for (CommonGround cg : commonGrounds) {
			ArrayList<CAEAgent> cgAgents = cg.agents;
			System.out.println("Looked up common ground of..");
			for (CAEAgent caeAgent : cg.agents) {
				System.out.println(caeAgent.getName());
			}
			System.out.println();
			if (listsEqual(cgAgents, agents))
				return cg;
		}
		return null;
	}

	/**
	 * Compare ArrayLists regardless of order
	 */
	private <E> boolean listsEqual(ArrayList<E> list1, ArrayList<E> list2) {
		if (list1.size() != list2.size())
			return false;
		for (E e : list1) {
			if (!list2.contains(e))
				return false;
		}
		return true;
	}
	
	/**
	 * evaluates the current strategy after execution: 
	 * updates its success-related attributes depending on success
	 * if the strategy has frequently worked, add it to common ground 
	 * @param costs as computed by AStar
	 * @param success: did the current strategy work?
	 */
	public void evaluateCurrentStrategy(boolean success, double costs){
	    Strategy currentStrategy = strategyStorage.getCurrentStrategy();
		
		if(currentStrategy != null){
			System.out.println(strategyStorage.getCurrentStrategy());
			System.out.println("Is evaluated; its success is " + success);
//			currentStrategy.printEvaluation();
			currentStrategy.evaluateStrategy(success);
			if (costs != Double.POSITIVE_INFINITY) strategyStorage.currentStrategy.setCosts(costs);
			currentStrategy.printEvaluation();
			
			// old implementation of costs update, now evaluated via AStar
	//		ReservoirMultiplePlanner planner = (ReservoirMultiplePlanner) this.agent.getPlanner();
	//		double costs = currentStrategy.costs - planner.getReservoir(ReservoirType.ENERGY).getActualValue()
			
			// check, if current strategy is good enough for adding it to common ground
			final int EXECUTION_THRESHOLD = 2; //Test value!
			final double SUCCESS_RATE_THRESHOLD = 0.8; //Test value!
			if(currentStrategy.getExecutions() > EXECUTION_THRESHOLD && strategyStorage.currentStrategy.successRate > SUCCESS_RATE_THRESHOLD){
				CommonGround cg = findCommonGround(currentStrategy.getInvolvedAgents());
				if (cg != null){
					cg.addStrategy(currentStrategy);
					System.out.println("SD: Strategy was added to common ground of...");
					for (CAEAgent caeAgent : strategyStorage.currentStrategy.getInvolvedAgents()) {
						System.out.println(caeAgent.getName());
					}
					System.out.println(findCommonGround(currentStrategy.getInvolvedAgents()).isStored(currentStrategy));
				} else {
					System.out.println("SD: Couldn't find common ground of..");
					for (CAEAgent caeAgent : strategyStorage.currentStrategy.getInvolvedAgents()) {
						System.out.println(caeAgent.getName());
					}
				}
			} 
		}
		// complete strategy handling by resetting to default
		strategyStorage.clearCurrentStrategy();
	}
	
	public void update(Observable o, Object arg) {
		if(arg instanceof PlanningEvent)
		{
			PlanningEventType type = ((PlanningEvent) arg).type;

			switch(type)
			{
			case EFFECT_CHAINING_STARTED:
				System.out.println("Number of observers: " + this.countObservers());
				this.setChanged();
				this.notifyObservers(new PlanningEvent(PlanningEventType.START_ICON));
				break;
			case EFFECT_CHAINING_ENDED:
				EffectChainingSupervisor sup = (EffectChainingSupervisor)o;
//				ArrayList<Goal>	goalsForAstar = new ArrayList<Goal>();
				
				CooperationNegotiator coop = new CooperationNegotiator(sup.getOriginalGoal(),sup.getGoallist(),this.agent,this,sup.isAppend());
				new Thread(coop).start();
				
				
//				if(sup.getGoallist() != null) {
//					goalsForAstar = sup.getGoallist();
//				}
//							
//				setGoal(goalsForAstar);
//				
//				if(getGoal().size()>0)
//				{
//					invokeAStarPlanning(sup.isAppend());
//				}
				
				this.setChanged();
				this.notifyObservers(new PlanningEvent(PlanningEventType.STOP_ICON));
				break;
			case NEGOTIATION_PLANNING_ENDED:
				CooperationNegotiator coopNegotiator = (CooperationNegotiator)o;
				ArrayList<GoalDistanceWrapper>	goallist = new ArrayList<GoalDistanceWrapper>();
				if(coopNegotiator.getNegotiatedGoals() != null) {
					goallist = coopNegotiator.getNegotiatedGoals();
				}

				ArrayList<Goal> goalsForAstar = new ArrayList<Goal>();
				for(GoalDistanceWrapper tmp : goallist){
					goalsForAstar.add(tmp.getPreviousInteraction());
				}
				
				setGoal(goalsForAstar);
				
				if(getGoal().size()>0)
				{
					invokeAStarPlanning(coopNegotiator.isAppend());
				}
				break;
			default:
				break;
			}


		}
		
	}
	/**
	 * Prints all common grounds of this player (for testing purposes)
	 */
	public void printAllCommonGrounds(){
		System.out.println("SD: Printing Common Grounds");
		for (CommonGround commonGround : commonGrounds) {
			System.out.println(commonGround);
		}
	}
	/**
	 * updates the waiting duration depending on the current energy a player has.
	 */
	private void updateWaitingDuration() {
		int energy = agent.getPlayer().getEnergys();
		long waitingDuration;
		//lineare skalierung
		if(energy <= 100) {
			waitingDuration = (long) (10000 - (5000 *  (energy/100.f)));
		} else if(energy <= 150) {
			waitingDuration = (long) (5000 - (2000 *  (((energy-100)*2)/100.f)));
		} else {
			waitingDuration = 2000;
		}
		
		setWaitingDuration(waitingDuration);
		
	}
	
	public boolean isReadyToPlan() {
		return isReadyToPlan;
	}



	public void setReadyToPlan(boolean isReadyToPlan) {
		this.isReadyToPlan = isReadyToPlan;
	}



	public boolean isSelfMotivated() {
		return isSelfMotivated;
	}



	public void setSelfMotivated(boolean isSelfMotivated) {
		this.isSelfMotivated = isSelfMotivated;
	}



	public long getWaitingDuration() {
		return waitingDuration;
	}

	

	public long getTimeStampOfLastAction() {
		return timeStampOfLastAction;
	}

	public void setTimeStampOfLastAction(long timeStampOfLastAction) {
		this.timeStampOfLastAction = timeStampOfLastAction;
	}

	public void setWaitingDuration(long waitingDuration) {
		this.waitingDuration = waitingDuration;
	}

	public AtomicBoolean getAbort() {
		return abort;
	}

	public void setAbort(AtomicBoolean abort) {
		this.abort = abort;
	}
	
	public void interruptReservoirGoalSelection() {
		abort.set(true);
	}
}
