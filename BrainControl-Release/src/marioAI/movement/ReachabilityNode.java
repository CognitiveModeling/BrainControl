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

import java.nio.file.DirectoryIteratorException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import marioAI.brain.Brain;
import marioAI.brain.Generalizer;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.Condition;
import marioAI.collision.ConditionDirectionPair;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.util.GeneralUtil;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Player;
/**
 * This class implements the reachability map, it is calculated through simulating the movement of an agent in 9 directions
 * stoping at movement hindering collisions and recording all the collision that happened sofar
 * 
 * @author Yves
 *
 */
public class ReachabilityNode {
	

	private int playerIndex;
	private SimulatedLevelScene simulatedLevelScene;
	//private SimulatedLevelScene originalSimulatedLevelScene;
	//private LevelSceneAdapter levelSceneAdapter;
	
	
	private float xVelocity;
	private float yVelocity;
	
	//private Set<CollisionAtPosition> destructionRelevantCollisions;
	
	//private boolean isOriginalNode = false;
	//private boolean isSecondNode;
	
	//private List<ReachabilityNode> afterDestructionNodes;
	private TreeMap<Integer,Set<CollisionAtPosition>> combinedCollisionsAtPositions;
	private TreeMap<Integer,float[][]> combinedReachabilityCounter;
	
	
	public boolean[][] visited;
	//public Set<CollisionDirection>[][] collisionDirections;
	public Set<CollisionAtPosition> uniqueFoundCollisions;
	private Set<CollisionAtPosition> learningRelevantCollisions;
	private Set<CollisionAtPosition> learningNonRelevantCollisions;
	
	
	public float[][] reachabilityCounter;
	public Brain brain;
	private GlobalCoarse planningPlayerPosition;
	private double distanceLimit = 11;
	
	
	
	//public List<ReachabilityNode> getAfterDestructionNodes() {
	//	return afterDestructionNodes;
	//}
	public Set<CollisionAtPosition> getEffectTargetRelevantCollisions() {
		return learningRelevantCollisions;
	}
	public TreeMap<Integer, Set<CollisionAtPosition>> getCombinedCollisionsAtPositions() {
		return combinedCollisionsAtPositions;
	}
	public TreeMap<Integer, float[][]> getCombinedReachabilityCounter() {
		return combinedReachabilityCounter;
	}
	
	public Set<CollisionAtPosition> getPositionTargetRelevantCollisions() {
		return learningNonRelevantCollisions;
	}
	/**
	 * Constructor for the internal recursive approach. Is created every time a collision is found and the movement is spread from this point.
	 *  
	 * @param copyToSimulateOn
	 * @param xVelocity
	 * @param yVelocity
	 * @param reachabilityCounter
	 */
	public ReachabilityNode(SimulatedLevelScene copyToSimulateOn, float xVelocity, float yVelocity,
			float[][]reachabilityCounter,boolean[][] visited,Set<CollisionAtPosition> collisionDirections, Brain brain,int playerIndex,GlobalCoarse planningPlayerPosition) {
		this.simulatedLevelScene = copyToSimulateOn;
		//this.playerIndex = simulatedLevelScene.calculateMovingPlayer();
		this.playerIndex = playerIndex;
		this.xVelocity = xVelocity;
		this.yVelocity = yVelocity;
		this.reachabilityCounter = reachabilityCounter;
		this.visited = visited;
		this.uniqueFoundCollisions = collisionDirections;
		this.brain = brain;
		this.planningPlayerPosition = planningPlayerPosition;
		//this.destructionRelevantCollisions = destructionRelevantCollisions;
		
		
	}
	/**
	 * Use this Constructor for the initial Node.
	 * @param brain 
	 * @param brain
	 */
	public ReachabilityNode(Brain brain) {
		
		//this.levelSceneAdapter = brain.getLevelSceneAdapter();
		this.simulatedLevelScene = brain.getLevelSceneAdapter().getSimulatedLevelScene();
		this.playerIndex = simulatedLevelScene.calculateMovingPlayer();
		//this.playerIndex = simulatedLevelScene.getPlanningPlayerIndex();
//		this.xVelocity = levelSceneAdapter.getPlayers().get(playerIndex).yVelocity;
//		this.yVelocity = levelSceneAdapter.getPlayers().get(playerIndex).xVelocity;
//		this.xVelocity = 1.2f;
//		this.xVelocity = 0.6f + (0.6f * (simulatedLevelScene.getSimulatedPlayers().get(playerIndex).getNumEnergys() /100.0f));
		this.xVelocity = calculateSpeed();
		this.yVelocity = 1.9f * simulatedLevelScene.getSimulatedPlayers().get(playerIndex).getIndividualYJumpSpeedMultiplier();

		this.visited = new boolean[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		this.reachabilityCounter = new float[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		//this.collisionDirections = new Set[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		this.uniqueFoundCollisions = new HashSet<CollisionAtPosition>();
		//this.destructionRelevantCollisions = new HashSet<CollisionAtPosition>();
		this.brain = brain;
		planningPlayerPosition = simulatedLevelScene.getPlanningPlayerPos().toGlobalCoarse();
		//this.isOriginalNode = true;
		//this.afterDestructionNodes = new ArrayList<ReachabilityNode>();
		simulateNode();/*
		combineCollisionSets();
		combineReachabilityMaps();*/
		addDismountCollision();
		learningRelevantCollisions = createLearningRelevantCollisionSet();
		learningNonRelevantCollisions = createLearningNonRelevantCollisionSet();
		
		checkMountCollisionProperties();
		addDropCollision();
	}
	


	public ReachabilityNode(SimulatedLevelScene simulatedLevelScene, int playerIndex) {

		this.simulatedLevelScene = simulatedLevelScene;
		
		this.playerIndex = simulatedLevelScene.calculateMovingPlayer(playerIndex);
		//this.playerIndex = playerIndex;
//		this.xVelocity = 0.6f + (0.6f * (simulatedLevelScene.getSimulatedPlayers().get(playerIndex).getNumEnergys() /100.0f));
		this.xVelocity = calculateSpeed();
		this.yVelocity = 1.9f * simulatedLevelScene.getSimulatedPlayers().get(this.playerIndex).getIndividualYJumpSpeedMultiplier();


		this.visited = new boolean[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		this.reachabilityCounter = new float[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		//this.collisionDirections = new Set[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		this.uniqueFoundCollisions = new HashSet<CollisionAtPosition>();
		//this.destructionRelevantCollisions = new HashSet<CollisionAtPosition>();
		//this.afterDestructionNodes = new ArrayList<ReachabilityNode>();
		planningPlayerPosition = simulatedLevelScene.getPlanningPlayerPos().toGlobalCoarse();
		simulateNode();
		addDismountCollision();
		
		learningRelevantCollisions = createLearningRelevantCollisionSet();
		learningNonRelevantCollisions = createLearningNonRelevantCollisionSet();
		
		//addDismountCollision(this.simulatedLevelScene);
		checkMountCollisionProperties();
		addDropCollision();
	}
	
	private float calculateSpeed() {
		int normalizedEnergy = simulatedLevelScene.getSimulatedPlayers().get(playerIndex).getNumEnergys() > Player.maxEnergySpeed? Player.maxEnergySpeed : simulatedLevelScene.getSimulatedPlayers().get(playerIndex).getNumEnergys();
		
		float sideWaysSpeed = 0.6f + (0.6f * (normalizedEnergy /100.0f));
		
		
		return sideWaysSpeed;
	}
	
	public ReachabilityNode updateReachabilityMap(SimulatedLevelScene simulatedLevelScene, int playerIndex){
		return new ReachabilityNode(simulatedLevelScene, playerIndex);
	}
	

	/**
	 * Simulate the movement of this node in the 7 direction, low right, low left, middle right, middle left, high right, high left and vertical jump 
	 */
	public void simulateNode() {
		
		
		boolean[] inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		
		//right
			
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_RIGHT] = true;
		simulateMove(inputKeys,16f,1,true);	
		
		//left
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_LEFT] = true;
		simulateMove(inputKeys,16f,1,true);
		
		//jump central
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		simulateMove(inputKeys,xVelocity,8,false); 
		//jump left
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_LEFT] = true;
		simulateMove(inputKeys,xVelocity,5,false); 
		//jump right
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_RIGHT] = true;
		simulateMove(inputKeys,xVelocity,5,false);
		
		//high jump right
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_RIGHT] = true;
		simulateMove(inputKeys,xVelocity,11,false); 
		//high jump left
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_LEFT] = true;
		simulateMove(inputKeys,xVelocity,11,false);
		//high jump right
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_RIGHT] = true;
		simulateMove(inputKeys,xVelocity/2,11,false); 
		//high jump left
		inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		inputKeys[Player.KEY_JUMP] = true;
		inputKeys[Player.KEY_LEFT] = true;
		simulateMove(inputKeys,xVelocity/2,11,false); 
		
		
	}
	/**
	 * Simulate a Move in the direction given through inputkeys and jumplength, simulatedXVelocity is only used to force a 1-tile sideways movement.
	 * 
	 * @param inputKeys
	 * @param simulatedXVelocity
	 * @param jumpLength
	 */
	private void simulateMove(boolean[] inputKeys, float simulatedXVelocity, int jumpLength, boolean isHorizontalMovement) {
		SimulatedLevelScene copyToSimulateOn = null;
		try {
			copyToSimulateOn = (SimulatedLevelScene) simulatedLevelScene.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//copyToSimulateOn.synchronizeCarries();
		
		/*
		List<CollisionAtPosition> result = simulateUntilCollision(simulatedXVelocity, yVelocity,jumpLength,new ArrayList<CollisionAtPosition>(), inputKeys, copyToSimulateOn,0);
		//needs further splitting
		
		
		if(!updateVisited(result)) {
			
			ReachabilityNode nextNode = new ReachabilityNode(copyToSimulateOn, playerIndex, xVelocity, yVelocity,reachabilityCounter,visited,collisionDirections,brain);
			nextNode.simulateNode();

		} */
		List<ReachabilityWrapper> result = simulateUntilCollision(simulatedXVelocity, yVelocity,jumpLength, inputKeys, copyToSimulateOn,isHorizontalMovement);
		//needs further splitting
		

		
		for (ReachabilityWrapper reachabilityWrapper : result) {
			if(!updateVisited(reachabilityWrapper.getCapList())) {
				
				ReachabilityNode nextNode = new ReachabilityNode(reachabilityWrapper.getSimulatedLevelScene(), xVelocity, yVelocity,reachabilityCounter,visited,
						uniqueFoundCollisions,brain,playerIndex,planningPlayerPosition);
				nextNode.simulateNode();

			}
		}
		
	}
	/**
	 * Recursive Function that will simulate a given input in the given levelscene until the input yields a collision with the environment.
	 * 
	 * 
	 * @param xVelocity 
	 * @param yVelocity
	 * @param jumpLength
	 * @param recCollisions
	 * @param inputKeys
	 * @param simulatedLevelScene
	 * @param isHorizontalMovement 
	 * @return
	 */
	private List<ReachabilityWrapper> simulateUntilCollision(float xVelocity, float yVelocity, int jumpLength, boolean[] inputKeys, 
			SimulatedLevelScene simulatedLevelScene, boolean isHorizontalMovement) {
		
		
		//boolean[] keys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		HashSet<CollisionAtPosition> recCollisions = new HashSet<CollisionAtPosition>();
		List<ReachabilityWrapper> recResult = new ArrayList<ReachabilityWrapper>();
		List<CollisionAtPosition> collisions = new ArrayList<CollisionAtPosition>();
		
		while(!isListTerminating(recCollisions)){
			
			if(jumpLength <=0) {		
				inputKeys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
			}
			jumpLength--;
			
			collisions = simulatedLevelScene.simulateReachabilityMovement(inputKeys,xVelocity, yVelocity,reachabilityCounter,playerIndex);
			
			
			
			recCollisions.addAll(collisions);
			/*
			if(simulatedLevelScene.isDestructionOfObjectOccured()){
				destructionRelevantCollisions.addAll(collisions);
			}*/
			
			//if it was a horizontal movement, then we only want to travel one tile at a time
			if(isHorizontalMovement) {
				simulatedLevelScene.getSimulatedPlayers().get(playerIndex).setXa(0);
			}
			
		}
		
		recResult.add(new ReachabilityWrapper(simulatedLevelScene, recCollisions));
		return recResult;
		
		/*
		boolean[] keys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		if(jumpLength >0) {		
			keys = inputKeys;
		}
		int bevor = recCollisions.size();
		
 		recCollisions.addAll(simulatedLevelScene.tick(keys,xVelocity, yVelocity,reachabilityCounter));
 		
		boolean newCollisions = (bevor-recCollisions.size() <0);
		
		if(newCollisions){
			collisionCounter++;
		}
		/**
		 * Movement only in x-Direction
		 *
		if(!inputKeys[Player.KEY_JUMP]) {
			simulatedLevelScene.getSimulatedPlayers().get(playerIndex).setXa(0);
		}
		/**
		 * if no collision is found yet, simulate one step further or if collision was a coin
		 *
		if(isListNonTerminating(recCollisions)) {
			return simulateUntilCollision(xVelocity, yVelocity,jumpLength-1,recCollisions, inputKeys, simulatedLevelScene,recResult,collisionCounter);
		}
		
		
		SimulatedLevelScene copyToSimulateFurtherOn = null;
		try {
			copyToSimulateFurtherOn = (SimulatedLevelScene) simulatedLevelScene.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recResult.add(new ReachabilityWrapper(copyToSimulateFurtherOn, recCollisions));
		if(collisionCounter < 2){
			return simulateUntilCollision(xVelocity, yVelocity,jumpLength-1,recCollisions, inputKeys, simulatedLevelScene,recResult,collisionCounter);
		}
		return recResult;*/
	}
	/*
	private List<CollisionAtPosition> simulateUntilCollision(float xVelocity, float yVelocity, int jumpLength, List<CollisionAtPosition> recResult, boolean[] inputKeys, SimulatedLevelScene simulatedLevelScene,int collisionCounter) {
		
		boolean[] keys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];
		if(jumpLength >0) {		
			keys = inputKeys;
		}
		int bevor = recResult.size();
 		recResult.addAll(simulatedLevelScene.tick(keys,xVelocity, yVelocity,reachabilityCounter));
		boolean newCollisions = (bevor-recResult.size() <0);
		if(newCollisions){
			collisionCounter++;
		}
		/**
		 * Movement only in x-Direction
		 *
		if(!inputKeys[Player.KEY_JUMP]) {
			simulatedLevelScene.getSimulatedPlayers().get(playerIndex).setXa(0);
		}
		/**
		 * if no collision is found yet, simulate one step further or if collision was a coin
		 *
		if(isListNonTerminating(recResult)) {
			return simulateUntilCollision(xVelocity, yVelocity,jumpLength-1,recResult, inputKeys, simulatedLevelScene,collisionCounter);
		}
		if(collisionCounter < 1000) {
			//return simulateUntilCollision(xVelocity, yVelocity,jumpLength-1,recResult, inputKeys, simulatedLevelScene,collisionCounter);
		}
		
		return recResult;
	} */
	/**
	 * checks if the input list recResult is empty or contains only collision that will not terminate a movement, e.g. a coin.
	 * 
	 * @param recResult
	 * @return
	 */
	private boolean isListTerminating(HashSet<CollisionAtPosition> recResult) {
		
		if(recResult.isEmpty()) {
			return false;
		}
		for (CollisionAtPosition collisionAtPosition : recResult) {
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(collisionAtPosition.conditionDirectionPair.condition.getTarget());
			
			
			if(PlayerWorldObject.PLAYER != Generalizer.generalizePlayer(type) && type != PlayerWorldObject.ENERGY_FRONT ){
				return true;
				
			}
			if(PlayerWorldObject.PLAYER == Generalizer.generalizePlayer(type) /*type ==  PlayerWorldObject.MARIO || type == PlayerWorldObject.TOAD */){
				if(!collisionAtPosition.conditionDirectionPair.collisionDirection.equals(CollisionDirection.ABOVE) ){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Updates the map of all visited positions to keep track of when to stop the recursion
	 * 
	 * @param collisionAtPositionList
	 * @return
	 */
	private boolean updateVisited(HashSet<CollisionAtPosition> collisionAtPositionSet) {
		
		
		boolean wasAlreadyVisited = false;
		
		
		for (CollisionAtPosition collisionAtPosition : collisionAtPositionSet) {
			int x = 0;
			int y = 0;
			PlayerWorldObject type = Generalizer.generalizePlayerWorldObject(collisionAtPosition.conditionDirectionPair.condition.getTarget());
			x = collisionAtPosition.getPosition().x;
			y = collisionAtPosition.getPosition().y;
			

			//CollisionDirection direction = collisionAtPosition.getDirection();
			
			
			/*check if out of bound*/
			if(y >= visited.length || y < 0 || x >= visited[0].length || x <0){
				return true;
			}
			//if(collisionDirections[y][x] == null) {
				//collisionDirections[y][x] = new HashSet<CollisionDirection>();
			//} 
			if(visited[y][x]) {
				if(uniqueFoundCollisions.contains(collisionAtPosition)){
					wasAlreadyVisited = true;
				//} else{
					//return false;
				}
				
			} else {
				if(type != PlayerWorldObject.ENERGY_FRONT ){
					visited[y][x] = true;
				}
			}
			
			
			//collisionDirections[y][x].add(direction);
			uniqueFoundCollisions.add(collisionAtPosition);
			//keep reachability locally constrained
			simulatedLevelScene.getKnowledge();
			if(GeneralUtil.getDistance(planningPlayerPosition,collisionAtPosition.getPosition())>distanceLimit){
				return true;
			}
		}
		
			
		return wasAlreadyVisited;
	}
	/*
	private void combineCollisionSets() {
		combinedCollisionsAtPositions = new TreeMap<Integer,Set<CollisionAtPosition>>();
		Set<CollisionAtPosition> collisionsInSecondIterations = new HashSet<CollisionAtPosition>();
		Set<CollisionAtPosition> collisionsInThirdIterations = new HashSet<CollisionAtPosition>();
		
		for(ReachabilityNode current: this.getAfterDestructionNodes()) {
			collisionsInSecondIterations.addAll(current.collisionDirections);
			for(ReachabilityNode next : current.getAfterDestructionNodes()){
				collisionsInThirdIterations.addAll(next.collisionDirections);
			}
		}
		
		collisionsInSecondIterations.removeAll(collisionDirections);
		collisionsInThirdIterations.removeAll(collisionDirections);
		collisionsInThirdIterations.removeAll(collisionsInSecondIterations);
		
		combinedCollisionsAtPositions.put(0,collisionDirections);
		combinedCollisionsAtPositions.put(1, collisionsInSecondIterations);
		combinedCollisionsAtPositions.put(2, collisionsInThirdIterations);
		
	}
	private void combineReachabilityMaps() {
		combinedReachabilityCounter = new TreeMap<Integer,float[][]>();
		float[][]reachabilityInSecondIterations = new float[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		float[][]reachabilityInThirdIterations = new float[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
		for(ReachabilityNode current: this.getAfterDestructionNodes()) {
			union(reachabilityInSecondIterations,current.reachabilityCounter);
			for(ReachabilityNode next : current.getAfterDestructionNodes()){
				union(reachabilityInThirdIterations,next.reachabilityCounter);
			}
		}
		difference(reachabilityCounter, reachabilityInSecondIterations);
		
		difference(reachabilityCounter, reachabilityInThirdIterations);
		difference(reachabilityInSecondIterations, reachabilityInThirdIterations);
		
		combinedReachabilityCounter.put(0, reachabilityCounter);
		combinedReachabilityCounter.put(1,reachabilityInSecondIterations);
		combinedReachabilityCounter.put(2,reachabilityInThirdIterations);
		
	} */
	/**
	 * calculates difference A-B, so that only entries that are not in A are still in b
	 * 
	 * @param inputA
	 * @param inputB
	 */
	private void difference(float[][] inputA, float[][]inputB){
		//float[][]tmp = new float[simulatedLevelScene.getMapHeight()][simulatedLevelScene.getMapWidth()];
//		for (int i = 0; i < reachabilityCounter.length; i++) {
//			for(int j = 0; j<reachabilityCounter[i].length; j++) {
//				if(inputA[i][j] != 0){
//					inputB[i][j] = 0;
//				}
//			}
//		}
		
	}
	/**
	 * joins the two float matrix so that the higher values will all be stored in inputA
	 * 
	 * @param inputA
	 * @param inputB
	 */
	private void union(float[][] inputA, float[][]inputB){
//		
//		for (int i = 0; i < reachabilityCounter.length; i++) {
//			for(int j = 0; j<reachabilityCounter[i].length; j++) {
//				if(inputA[i][j] < inputB[i][j]){
//					inputA[i][j] = inputB[i][j];
//				}
//			}
//		}
		
	}
	/**
	 * adds collision with carrier so that a dismount collision can be found.
	 */
	public void addDismountCollision() {

		SimulatedPlayer carrier = simulatedLevelScene.getSimulatedPlayers().get(playerIndex);
		if(carrier.getCarries() == null) {
			return;
		}
		SimulatedPlayer carried = (SimulatedPlayer)carrier.getCarries();
		Condition condition = new Condition(carried.type, carrier.type, carried.getHealth(), carried.wasInvulnerable(), true);
		//uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.LEFT), carrier.getMapPosition(0), carrier, carried));
		uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), carrier.getMapPosition(0), carrier, carried));
	}
	
	public void addDismountCollision(SimulatedLevelScene simulatedLevelScene ) {
		if(learningRelevantCollisions == null) {
			return;
		}
		HashSet<CollisionAtPosition> learningRelevantCollisionSet = new HashSet<CollisionAtPosition>();
		SimulatedPlayer carrier = simulatedLevelScene.getSimulatedPlayers().get(playerIndex);
		
		for(CollisionAtPosition cap : learningRelevantCollisions) {
			if(!(cap.getActorSprite() != null && (cap.getDirection() == CollisionDirection.LEFT || cap.getDirection() == CollisionDirection.RIGHT))){
				learningRelevantCollisionSet.add(cap);
			}
		}
		if(carrier.getCarries() == null) {
			return;
		}
		SimulatedPlayer carried = (SimulatedPlayer)carrier.getCarries();
		Condition condition = new Condition(carried.type, carrier.type, carried.getHealth(), carried.wasInvulnerable(), true);
		learningRelevantCollisionSet.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), carrier.getMapPosition(0), carrier, carried));
		uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), carrier.getMapPosition(0), carrier, carried));
		
		learningRelevantCollisions = learningRelevantCollisionSet;
	}
	/*
	public void addDismountCollision(SimulatedLevelScene simulatedLevelScene ) {
		if(learningRelevantCollisions == null) {
			return;
		}
		HashSet<CollisionAtPosition> learningRelevantCollisionSet = new HashSet<CollisionAtPosition>();
		SimulatedPlayer carrier = simulatedLevelScene.getSimulatedPlayers().get(playerIndex);
		
		for(CollisionAtPosition cap : learningRelevantCollisions) {
			if(!(cap.getSprite() != null && (cap.getDirection() == CollisionDirection.LEFT || cap.getDirection() == CollisionDirection.RIGHT))){
				learningRelevantCollisionSet.add(cap);
			}
		}
		if(carrier.carries == null) {
			return;
		}
		SimulatedPlayer carried = (SimulatedPlayer)carrier.carries;
		Condition condition = new Condition(carried.type, carrier.type, carried.getHealth(), carried.wasInvulnerable(), true);
		learningRelevantCollisionSet.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.LEFT), carrier.getMapPosition(0), carrier));
		learningRelevantCollisionSet.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), carrier.getMapPosition(0), carrier));
		uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.LEFT), carrier.getMapPosition(0), carrier));
		uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.RIGHT), carrier.getMapPosition(0), carrier));
		
		learningRelevantCollisions = learningRelevantCollisionSet;
	}*/
	
	private void addDropCollision() {
		if(learningRelevantCollisions == null) {
			return;
		}
		
		
		SimulatedPlayer carrier = simulatedLevelScene.getPlanningPlayer();
		if(carrier.getCarries() != null) {
			SimulatedPlayer carried = (SimulatedPlayer) carrier.getCarries();
			Condition condition = new Condition(carrier.type, carried.type, carried.getHealth(), carried.wasInvulnerable(), true);
			learningRelevantCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.BELOW), carried.getMapPosition(0), carrier, carried));
			uniqueFoundCollisions.add(new CollisionAtPosition(new ConditionDirectionPair(condition, CollisionDirection.BELOW), carried.getMapPosition(0), carrier, carried));
			
		}
	}
	/**
	 * filters all non leanring relevant collision out of the uniqueFoundCollision set.
	 * @return
	 */
	private Set<CollisionAtPosition> createLearningRelevantCollisionSet() {
		HashSet<CollisionAtPosition> learningRelevantCollisionSet = new HashSet<CollisionAtPosition>();
		
		for(CollisionAtPosition tmp : uniqueFoundCollisions) {
			if(Generalizer.generalizePlayer(tmp.conditionDirectionPair.condition.getTarget()).isRelevantToLearning()) {
				learningRelevantCollisionSet.add(tmp);
			}
				
		}
		
		return learningRelevantCollisionSet;
	}
	

	private Set<CollisionAtPosition> createLearningNonRelevantCollisionSet() {
		HashSet<CollisionAtPosition> learningNonRelevantCollisionSet = new HashSet<CollisionAtPosition>();
		
		for(CollisionAtPosition tmp : uniqueFoundCollisions) {
			if(!tmp.conditionDirectionPair.condition.getTarget().isRelevantToLearning()) {
				learningNonRelevantCollisionSet.add(tmp);
			}
				
		}
		
		return learningNonRelevantCollisionSet;
	}
	
	private void checkMountCollisionProperties() {
		ArrayList<CollisionAtPosition> toBeRemoved = new ArrayList<CollisionAtPosition>();
		for(CollisionAtPosition tmp : learningRelevantCollisions) {
			if(tmp.getDirection().equals(CollisionDirection.ABOVE) && tmp.getTargetSprite()!= null && tmp.getTargetSprite() instanceof SimulatedPlayer) {
				SimulatedPlayer toBeMounted  =  ((SimulatedPlayer) tmp.getTargetSprite());
				GlobalCoarse pos = toBeMounted.getPosition(0).toGlobalCoarse();
				
				if(PlayerWorldObject.isPositionBlocking(pos, simulatedLevelScene, toBeMounted)){
					toBeRemoved.add(tmp);
				}
				
			}
		}
		
		for(CollisionAtPosition tmp : toBeRemoved) {
			uniqueFoundCollisions.remove(tmp);
			learningRelevantCollisions.remove(tmp);
		}
	}
	
	/*
	
	*used to draw continous positions
	public List<GlobalContinuous> simulateStep(float xVelocity, float yVelocity) {
		
		List<GlobalContinuous> result = new ArrayList<GlobalContinuous>();
		result = simulateStep(true,xVelocity, yVelocity,8,result);
		
		return result;
	}
	
	private List<GlobalContinuous> simulateStep(boolean initial, float xVelocity, float yVelocity, int jumpLength, List<GlobalContinuous> recResult) {
		
		boolean[] keys = new boolean[Player.NUMBER_OF_NON_CHEATING_KEYS];

		if(initial || jumpLength >0) {		
			keys[Player.KEY_JUMP]= true;
			keys[Player.KEY_RIGHT] = true;
		}
		recResult.addAll(simulatedLevelScene.tick(keys,xVelocity, yVelocity));
		System.out.println("reachabilityNode test");	
		//if(rec.isEmpty()) {
		if(jumpLength >0) {
			return simulateStep(false, xVelocity, yVelocity,jumpLength-1,recResult);
		}
		
		return recResult;
	} */
	
}
