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
package marioAI.movement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.CopyOnWriteArrayList;

import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.ReachabilityMap;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.goals.GeneralizedGoal;
import marioAI.goals.Goal;
import marioAI.goals.GoalPlanner;
import marioAI.goals.ReservoirMultiplePlanner;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * This class is used as part of the search of Condition/Effect mappings to
 * fulfill certain criteria for a goal.
 * 
 * @author marcel
 *
 */
public class ConditionVertex implements Comparable<ConditionVertex> {
	// list of already visited nodes:
	public LinkedList<ConditionDirectionPair> visited = new LinkedList<ConditionDirectionPair>();
	// knowledge to extract known mappings:
	public TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge;
	// Simulated scene:
	public SimulatedLevelScene scene;
	public PlayerWorldObject[][] movingMap;
	public PlayerWorldObject[][] staticMap;
	// values of a ConditionDirectionPair:
	public CollisionAtPosition state;
	public int depth;
	public TreeMap<Effect, Double> effects;
	// list of nodes connected to this one
	public LinkedList<ProbabilityEdge> adjacencies = new LinkedList<ProbabilityEdge>();
	// Cost from source
	public double distance = Double.POSITIVE_INFINITY;
	public Effect effectOfThisNode;
	// previous node to reconstruct path
	public ConditionVertex prev;
	public Goal previousInteraction;
	public Effect importantEffect;
	private double topProbability; // For strategy evaluation

	/**
	 * expandPossibleGoalsDGM is the goaldegeneralization version of expandPossibleGoals.
	 * Like expandPossibleGoals, it has two arguments:
	 * An ArrayList of (Goal,Double) entries created from the current DegeneralizedGoal
	 * to be generalized and a ConditioDirectionPair extracted from that DegeneralizeGoal.
	 * For each goal It creates a new Node and adds an adjacency with a calculated Probability from
	 * the actual node to the newly generated node.
	 * @param goals an ArrayList of (Goal,Double)-Entries
	 * @param cdp   a CollisionDirectionPair
	 */
	private void expandPossibleGoalsDGM(ArrayList<Entry<Goal,Double>> goals,ConditionDirectionPair cdp){
		for(Entry<Goal,Double> goal: goals){
			GlobalCoarse positionAfterInteraction = goal.getKey().getPosition();
			SimulatedLevelScene newScene =
					new SimulatedLevelScene(this.scene,true);
			CollisionAtPosition cap =
					new CollisionAtPosition(cdp, positionAfterInteraction, goal.getKey().getTargetSprite(), goal.getKey().getActorSprite());
			System.out.println("CURRENT COLLISION AT POSITION");
			System.out.println(cap);
			TreeMap<Effect, Double> simulatedEffectList = newScene.simulateCollision(cap, true);
//			if (simulatedEffectList.size() == 0) System.out.println("SD: Seems like no effects are simulated!");
			newScene.getPlanningPlayer().setPosition(positionAfterInteraction.toGlobalContinuous());
			//newScene.recomputeReachabilityMap();
			System.out.println(newScene);
			System.out.println("PREVIOUS GOAL: " + this.previousInteraction);
			//newScene.Rmap.showMap("DEPTH: " + this.depth);
			double prob = 1.0;
			
			Effect topEffect = getMostImportantEffect(simulatedEffectList);
			double newTopProbability = 1.0;
//			System.out.println("Checking top probability for goal " + ((topEffect != null) ? topEffect.getType() : ActionEffect.UNDEFINED));
			if ((topEffect != null) && (simulatedEffectList.containsKey(topEffect))){
//				System.out.println("SD: Set vertex probability to: " + simulatedEffectList.get(topEffect) + " for top effect " + ((topEffect != null) ? topEffect.getType() : ActionEffect.UNDEFINED));
				newTopProbability = simulatedEffectList.get(topEffect);
			}
			
			ConditionVertex next =
					new ConditionVertex(cap, newScene, knowledge, simulatedEffectList, this.depth + 1, goal.getKey(), topEffect, newTopProbability);
			adjacencies.add(new ProbabilityEdge(next, -Math.log(prob)));
		}
	}
	
	/**
	 * expandPossibleGoals expands the current node with a list of (Goal,Double) Entries and a
	 * ConditionDirectionPair obtained from the knowledge. For each goal a new node is created,
	 * a probability is calculated and an adjacency from the current node to the newly created
	 * node is added.
	 * 
	 * @param goals the list of (Goal,Double) calculated by getReachableObjectGoals in expandNode
	 * @param cdp a ConditionDirectionPair obtained from the knowledge of a player
	 */
	private void expandPossibleGoals(ArrayList<Entry<Goal,Double>> goals, ConditionDirectionPair cdp)
	{
		System.out.println("        EXPANDPOSSIBLEGOALS IS INVOKED WITH: ");
		System.out.println("        " + goals);
		for(Entry<Goal,Double> goal : goals)
		{												
			//get position
			GlobalCoarse place = goal.getKey().getPosition();
			TreeMap<Effect, Double> cdp_knowledge = this.knowledge.get(cdp);
			Set<Entry<Effect, Double>> known_effects = new TreeSet<Entry<Effect, Double>>();
			if(cdp_knowledge!=null)
				known_effects = cdp_knowledge.entrySet();

			//sample n effects... TODO: find better measure (there are actually 2^n)
			int n = known_effects.size()+1;	//+1 for implicit nothing happens effect when goal is reached
			for(int i=0;i<n;i++)
			{
				SimulatedLevelScene newScene = null;
				try {
					newScene = (SimulatedLevelScene) this.scene.clone();
				} 
				catch (CloneNotSupportedException e1){
					e1.printStackTrace();
				}			
				
			
				CollisionAtPosition cap =
						new CollisionAtPosition(cdp, place,  goal.getKey().getActorSprite(),goal.getKey().getTargetSprite());
//				System.out.println("Cap here:");
//				System.out.println(cap);
				TreeMap<Effect, Double> simulatedEffectList = newScene.simulateCollision(cap, true);
				
				//newScene.expandReachabilityMap(newScene.Rmap);
				
				//use max prob:
				double prob=0.0;
				for(Effect e : simulatedEffectList.keySet())
				{
					if(simulatedEffectList.get(e)>prob)
					{
						prob=simulatedEffectList.get(e);
					}
				}
				

				
//				System.out.println("SD: Refresh top probability: " + simulatedEffectList.get(topEffect));
//				if (simulatedEffectList.keySet().contains(topEffect)) this.topProbability = simulatedEffectList.get(topEffect);
				/*
				// TODO: move player
				GlobalCoarse positionAfterInteraction = place;
								
				//TODO: doing this only for position goals is necessary right now since moving the player corrupts the localStaticMap and thus the reachability map, somehow!
				boolean positiongoal=
						goal.getKey().getTarget()==PlayerWorldObject.NONE; /*||
						goal.getKey().getTarget()==null; //goals with NO target are also position goals*/
				
				/*
				if(positiongoal)	//position goal (DIRTY)
				{
					System.out.println("POSITIONCODE ENTERED");
					newScene.getPlanningPlayer().setPosition(positionAfterInteraction.toGlobalContinuous());
					System.out.println("Setting player to position "+positionAfterInteraction);
					prob=1.0;
				}	*/	
				
				GlobalCoarse positionAfterInteraction = new GlobalCoarse(place.getX(), place.getY());
				switch(cdp.collisionDirection)
				{
				case LEFT:
					positionAfterInteraction.x--;
					break;
				case RIGHT:
					positionAfterInteraction.x++;
					break;
				case ABOVE:
					positionAfterInteraction.y--;
					break;
				case BELOW:
					positionAfterInteraction.y++;
					break;
				default:
					break;
				}
				
				//TODO: doing this only for position goals is necessary right now since moving the player corrupts the localStaticMap and thus the reachability map, somehow!
				boolean positiongoal= (goal.getKey().getTarget()==PlayerWorldObject.TOP_DIRT_BORDER_GROUND) || (goal.getKey().getEffect()!= null && goal.getKey().getEffect().getType().equals(ActionEffect.PROGRESS));
				if(positiongoal)	//position goal (DIRTY)
				{
					newScene.getSpriteBasedOnType(goal.getKey().getActor()).setPosition(positionAfterInteraction.toGlobalContinuous());
					System.out.println("Setting player to position "+positionAfterInteraction);
					positionAfterInteraction.y--;
					newScene.getSpriteBasedOnType(goal.getKey().getActor()).setPositionOfCarriedSprite(positionAfterInteraction.toGlobalContinuous());
					
					prob=0.99;
					for(ReachabilityNode tmp : newScene.reachabilityNodes) {
						tmp.addDismountCollision(newScene);
					}

					simulatedEffectList.put(new Effect(ActionEffect.TRANSPORTATION,goal.getKey().getTarget()), prob);

				}
				
				// note: importance != likelihood
				Effect topEffect = getMostImportantEffect(simulatedEffectList);
				effectOfThisNode = topEffect;
				double newTopProbability = 1.0;
//				System.out.println("Checking top probability for goal " + ((topEffect != null) ? topEffect.getType() : ActionEffect.UNDEFINED));
				if ((topEffect != null) && (simulatedEffectList.containsKey(topEffect))){
//					System.out.println("SD: Set vertex probability to: " + simulatedEffectList.get(topEffect) + " for top effect " + ((topEffect != null) ? topEffect.getType() : ActionEffect.UNDEFINED));
					newTopProbability = simulatedEffectList.get(topEffect);
				}	
				
				
				boolean newSceneEqual = !newScene.equals(scene);
				boolean adjacenciesCheck = !checkAdjacenciesForLevelScene(newScene);
				
				//check if scene is new:
				if (positiongoal || (!simulatedEffectList.isEmpty() && !isUselessEffect(topEffect) && !newScene.equals(scene) && !checkAdjacenciesForLevelScene(newScene)) /* && this.depth < EffectChaining.MAX_DEPTH*/)
				{		
					goal.getKey().setNegotiationEffect(simulatedEffectList);
					ConditionVertex next = new ConditionVertex
												(	cap, 
													newScene, 
													knowledge, 
													simulatedEffectList, 
													this.depth + 1, 
													goal.getKey(), 
													topEffect, 
													newTopProbability
												);
					//System.out.println("        added to adjacencies " + next);
					adjacencies.add(new ProbabilityEdge(next, -Math.log(prob)));
					
				}
			}
		}
		System.out.println("        EXPANDPOSSIBLEGOALS TERMINATES");
		System.out.println("        ------------------------------");
	}
	/**Checks the current priority list of adjacencies if a node contains a equal levelscene to the currently simulated one
	 * i.e. if there already exists an equal simulatedlevelscene from a previous effect sampling.
	 * 
	 * @param newScene the scene which is checked against all known levelscenes in the adjacencies list
	 * @return true, if an equal scene already exists, false if newScene is unique
	 */
	private boolean checkAdjacenciesForLevelScene(SimulatedLevelScene newScene) {
		boolean equalLevelScene = false;
		ConditionVertex equalConditionVertex = null;
		for (Iterator<ProbabilityEdge> iterator = adjacencies.iterator(); iterator.hasNext();) {
			ProbabilityEdge probabilityEdge = (ProbabilityEdge) iterator.next();
			if(probabilityEdge.target.scene.equals(newScene)){
				equalLevelScene = true;
				equalConditionVertex = probabilityEdge.target;
			}
		}
		
		if(equalLevelScene && equalConditionVertex.importantEffect == effectOfThisNode) {
			return true;
		}
		return false;
	}
	/**
	 * Checks if this interaction is a direct counterpart to the precious interaction.
	 * e.g. previous interaction was mount, this interaction is unmount.
	 * @return true if a contradiction is found
	 */
	private boolean isUselessEffect(Effect currentEffect) {
		
		if(importantEffect != null) {
			if(importantEffect.getEffectTarget() == currentEffect.getEffectTarget()) {
				return Effect.contradictingEffects(importantEffect, currentEffect);
			}
			
		}
		return false;
	}
	
	
	/**
	 * gets the corresponding SimulatedPlayer for a PlayerWorldObject
	 * 
	 * @param actorType the PlayerWorldObject for which the simulatedPlayer has to be found
	 * @return the SimulatedPlayer object corresponding to the PlayerWorldObject input
	 */
	private SimulatedPlayer getActorSpriteInScene(PlayerWorldObject actorType)
	{
		for(SimulatedPlayer player : scene.getSimulatedPlayers())
		{
			if(player.getType()==actorType)
				return player;
		}
		return null;
	}	
	
	/**
	 * expandNodeDGM is the goaldegeneralization version of expandNode.
	 * It expands a node with a current generalized goal obtained from 
	 * the initial list of degeneralized goals. It utilizes expandPossibleGoalsDGM
	 * 
	 * @param currentGeneralizedGoal a DegeneralizedGoal from which the information for expanding is extracted
	 */
	public void expandNodeDGM(GeneralizedGoal currentGeneralizedGoal){
		//extract the needed information from currentGeneralizedGoal:
		PlayerWorldObject target = currentGeneralizedGoal.getTarget();
		ArrayList<PlayerWorldObject> goalTypes =
				new ArrayList<PlayerWorldObject>();
		goalTypes.add(target);
		SimulatedPlayer actor = getActorSpriteInScene(currentGeneralizedGoal.getActor());
		CollisionDirection collisionDirection = currentGeneralizedGoal.getCollisionDirection();
		ArrayList<Entry<Goal,Double>> goals = PlayerWorldObject.getReachableObjectGoals(goalTypes,actor,collisionDirection,scene);
		System.out.println(goals);
		//construction of a ConditionDirectionPair from currentGeneralizedGoal
		ConditionDirectionPair cdp = 
				new ConditionDirectionPair(
						new Condition(
								currentGeneralizedGoal.getActor(),
								currentGeneralizedGoal.getTarget(),
								currentGeneralizedGoal.getPlayerHealthCondition()),
						currentGeneralizedGoal.getCollisionDirection());
		//invoking expandPossibleGoalsDGM with the extracted data
		
		if(!goals.isEmpty()){
			expandPossibleGoalsDGM(goals,cdp);
		}
	}
	
	/**
	 * expandNode expands the current node with information obtained from the finalgoal.
	 * It obtains a list of ConditionDirectionPair objects from the knowledge of the
	 * player and invokes expandPossibleGoals for each ConditionDirectionPair
	 * 
	 * @param finalgoal the final goal to be reached 
	 */
	public void expandNode(Goal finalgoal){
		//extract the ConditionDirectionPairs from the knowledge:
		List<ConditionDirectionPair> set = new CopyOnWriteArrayList<ConditionDirectionPair>();
		set.addAll(this.knowledge.navigableKeySet());
		
		//if the goal is only a positiongoal:
//		if(!finalgoal.hasEffect() /*&& scene.isEasilyReachable(finalgoal.getPosition(), finalgoal.getDirection())*/){			
//			Double distance = scene.getPlanningPlayer().getPosition(0).distance(finalgoal.getPosition().toGlobalContinuous());
//			SimpleEntry<Goal,Double> newEntry = new SimpleEntry<Goal,Double>(finalgoal,distance);
//			ArrayList<Entry<Goal,Double>> newEntryList = new ArrayList<Entry<Goal,Double>>();
//			newEntryList.add(newEntry);	
//			expandPossibleGoals(newEntryList,finalgoal.collisionAtPosition.conditionDirectionPair);
//		}
		
		
//		List<ConditionDirectionPair> cdpsWithSameActorAsFinalGoal = cdpsWithActorInFinalGoal(set, finalgoal);
//		List<ConditionDirectionPair> cdpsWithDifferentActorAsFinalGoal = cdpsWithoutActorInFinalGoal(set, finalgoal);
		
		//for each ConditionDirectionPair, get the corresponding reachable goals, and expand the node accordingly
		for (ConditionDirectionPair cdp : set){			
			if (!visited.contains(cdp)) {
				this.visited.add(cdp);
				
				//if the playerHealthCondition fits or is irrelevant in the ConditionDirectionPair
				boolean cond = 
						cdp.condition.playerHealthCondition.toInt() == PlayerHealthCondition.fromInt(this.scene.getPlanningPlayer().getHealth(), false, false).toInt() ||
						cdp.condition.playerHealthCondition==PlayerHealthCondition.IRRELEVANT;
				if (cond){						
					PlayerWorldObject target = cdp.condition.interactors.getTarget();
					ArrayList<PlayerWorldObject> list = new ArrayList<PlayerWorldObject>();
					list.add(target);
					SimulatedPlayer actor = getActorSpriteInScene(cdp.condition.getActor());
					if(actor==null)
						continue;
					ArrayList<Entry<Goal,Double>> goals = PlayerWorldObject.getReachableObjectGoals(list,actor,cdp.collisionDirection,scene);
						
					//invoke expandPossibleGoals with the obtained data
					if(!goals.isEmpty()){
						expandPossibleGoals(goals,cdp);
					}
				}
			}
		}
		
		ArrayList<Entry<Goal,Double>> goals = PlayerWorldObject.getReachablePositionGoals(CollisionDirection.ABOVE,scene,finalgoal);
		System.out.println("ConditionVertex:"+ goals);
		if(goals.size()>0) {
			Goal tmp = goals.get(0).getKey();
			ConditionDirectionPair cdp = tmp.getCollisionAtPosition().conditionDirectionPair;
				expandPossibleGoals(goals, cdp);
		} 
	}
	
//	private List<ConditionDirectionPair> cdpsWithActorInFinalGoal(List<ConditionDirectionPair> set, Goal finalGoal) {
//		List<ConditionDirectionPair> result = new ArrayList<ConditionDirectionPair>(set);
//		
//		result.removeIf(cdp -> !(cdp.condition.getActor() == finalGoal.getActor()) );
//		
//		return result;
//	}
//	private List<ConditionDirectionPair> cdpsWithoutActorInFinalGoal(List<ConditionDirectionPair> set, Goal finalGoal) {
//		List<ConditionDirectionPair> result = new ArrayList<ConditionDirectionPair>(set);
//		result.removeIf(cdp -> cdp.condition.getActor().equals(finalGoal.getActor()) );
//		
//		return result;
//	}
	
	/**
	 * The constructor for chaining nodes inside of ConditionVertex.
	 * 
	 * @param state the actual CollisionAtPosition
	 * @param scene the actual SimulatedLevelScene (has to updated befor)
	 * @param knowledge (not actually used in this constructor) knowledge is extracted from the scene (?)
	 * @param effects the known effects
	 * @param depth the depth of the actual node (incremented in everytime a node is expanded)
	 * @param previousInteraction the goal which was reached in the previous node in the simulation
	 * @param relevantEffect the relevant Effect
	 * @param topProbability the maximum probability
	 */
	public ConditionVertex(CollisionAtPosition state,
			SimulatedLevelScene scene,
			TreeMap<ConditionDirectionPair, TreeMap<Effect, Double>> knowledge,
			TreeMap<Effect, Double> effects, 
			int depth,
			Goal previousInteraction, Effect relevantEffect, double topProbability) 
	{
		this.scene = scene;
		this.depth = depth;
		this.effects = effects;
		this.previousInteraction = previousInteraction;
		this.state = state;
		this.knowledge = scene.getKnowledge();
		this.importantEffect = relevantEffect;
		this.topProbability = topProbability;
	}

	/**
	 * The constructor which is used in EffectChaining to create the source node
	 * 
	 * @param health the health condition of a player
	 * @param scene the current SimulatedLevelScene
	 * @param knowledge the knowledge of the player
	 */
	public ConditionVertex
	(
			PlayerHealthCondition health,
			SimulatedLevelScene scene,
			TreeMap<ConditionDirectionPair, 
			TreeMap<Effect, Double>> knowledge) 
	{
		this
		(
				new CollisionAtPosition
				(
						new ConditionDirectionPair
						(
								new Condition
								(
										null, 
										null, 
										health
								), 
								CollisionDirection.IRRELEVANT
						), 
						scene.getPlanningPlayerPos().toGlobalCoarse(), 
						null,
						null
				), 
				scene, 
				knowledge,
				new TreeMap<Effect, Double>(), 
				0, 
				null,
				null,
				0.0
		);
	}

	/**
	 * comparator for conditionvertices. Simply uses the comparator for Double invoked with the distances
	 */
	@Override
	public int compareTo(ConditionVertex o) {
		return Double.compare(distance, o.distance);
	}
	
	/**
	 * compute effect relevance
	 * for player targets, relevance is high, if a reservoir, which is related to the effect type, is activated
	 * @param effect an Effect
	 */
	private double getEffectRelevance(Effect effect) {
        final double DEFAULT_RELEVANCE = 1.0;
        double effectRelevance = DEFAULT_RELEVANCE;
        if (effect.getEffectTarget().isPlayer()){
                try {
                    GoalPlanner relevancePlanner = effect.getEffectTarget().findAgent().getPlanner();
                    if (relevancePlanner instanceof ReservoirMultiplePlanner) effectRelevance = ((ReservoirMultiplePlanner) relevancePlanner).getEffectRelevance(effect, DEFAULT_RELEVANCE);}
                catch (java.lang.NullPointerException exception) {
                        System.out.println("Generic player as effect target can not be motivated!");}
                //TODO: Player (generic) should never be an effect target (Done?)
        }
        return effectRelevance;
	}
	
	@Override
	public String toString(){
		String res = "CONDITIONVERTEX:<";
		res += "h:" + this.depth;
		res += " d:" + this.distance;
		//res += " E:" + this.importantEffect;
		res += " P:" + this.previousInteraction;
		//res += " CAP:" + this.state;
		return res;
	}
	
	/**
	 * getter for topProbability of a ConditionVertex
	 * @return a double representing the topProbability
	 */
	protected double getTopProbability() {
		return topProbability;
	}
	
	/**
	 * select the most important effect of a collision
	 * @param simulatedEffectList of the collision
	 * @return max important of the effects, with importance := probability * relevance
	 */
	private Effect getMostImportantEffect(TreeMap<Effect,Double> simulatedEffectList){
		// select most important effect
		// note: likelihood != importance!
		double topImportance = 0.0;
		Effect topEffect = null;
		for(Effect e : simulatedEffectList.keySet())
		{
			double importance = simulatedEffectList.get(e) * getEffectRelevance(e);
//			System.out.println("SD: Effect: " + e.getType() +"evaluated; probability: " + simulatedEffectList.get(e) + ", relevance: " + getEffectRelevance(e));
			
			if (importance > topImportance){
				topEffect = e;
				topImportance = importance;
			}
		}

		
		return topEffect;
	}
}
