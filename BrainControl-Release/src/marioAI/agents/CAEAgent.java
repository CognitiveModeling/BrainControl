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
package marioAI.agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import marioAI.brain.Effect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.collision.ConditionDirectionPair;
import marioAI.goals.Goal;
import marioAI.goals.GoalPlanner;
import marioAI.goals.PlanningEvent;
import marioAI.goals.PlanningEventType;
import marioAI.movement.AStarSimulator;
import marioAI.movement.EffectChainingSupervisor;
import marioAI.movement.PlayerActions;
import marioAI.movement.tgng.Tgng;
import marioAI.run.PlayHook;
import marioUI.gui.MouseInputController;
import marioUI.gui.TutorialFunction;
import marioUI.voiceControl.VoiceControl;
import marioWorld.agents.Agent;
import marioWorld.engine.EngineHook;
import marioWorld.engine.Tickable;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Player;
import marioWorld.mario.environments.Environment;

/**
 * Condition Action Effect Agent
 * 
 * The main class of the marioAI challenge by extending an Agent. It is
 * instantiated in the global class Play. Holds the Brain and the mission Mario
 * pursues and is therefore observed by a view. It is also capable of being
 * controlled by a human player either with mouse, keyboard or voice.
 * 
 * Offers access to speak-method.
 * 
 * @author Ylva, Stefan, Volker, Jonas, Mihael
 * 
 */
public class CAEAgent extends BrainAgent// implements Observer 
{
	private boolean disableEmotions;
	
	/**
	 * Actions of the human player
	 */
	protected boolean[] primitiveAction = null;
	
	
	/**
	 * Microphone hack
	 */
	public String microphone;

	/**
	 * Emotional action are going to be proceeded, when Mario feels any emotion
	 */
	// marks, if Mario did an emotional action - if true: goals need to be
	// recomputed
	private boolean emotionActionPerformed = false;
	// stores Marios emotional action
	private List<boolean[]> emotionActions;

	int lastPayment=0;
	
	// all the emotional actions, Mario can perform
	public enum EmotionActionType {
		FEAR_ACTION, HAPPY_ACTION, COURAGEOUS_ACTION, NONE
	}

    //private EvolveActionPlan actionplanEvolver;

	private Tgng tgng;

	/**
	 * Movement planner
	 */
	public AStarSimulator aStarSimulator;
	
	private EffectChainingSupervisor effectChainingSupervisor;



	/**
	 * Current player position.
	 */
	private GlobalContinuous playerPosition;

	/**
	 * Goal planner used by the agent.
	 */
	private final GoalPlanner planner;

	/**
	 * The agent's current goal node.
	 */
	//public WorldGoalTester goalNode = null;
	public boolean waitingToSlowDownBeforeNextGoalSearch = false;

	public final Lock lock = new ReentrantLock();
	public int actionCountdown = -1;
	
	private MouseInputController mouseInput = new MouseInputController();



	/**
	 * Central Voice Class needed to get access to other voice components
	 */
	//private VoiceControl voiceControl;

	public CAEAgent(
			String name,
			int playerIndex,
			GoalPlanner planner,
			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
			String[] initiallyActiveGuiOverlays, Tickable... tickables) 
	{
		super(name, playerIndex, initialKnowledge);
		//classesWithStaticStuff.add(this.getClass());
		this.primitiveAction = new boolean[Environment.numberOfButtons];
		this.planner = planner;
		//planner.addObserver(this);
				
		//addObserver(PlayHook.voiceResponseEventBus);

		this.aStarSimulator = new AStarSimulator(brain.getLevelSceneAdapter(), brain);
		//aStarSimulator.addObserver(this);

		EngineHook.installHook(new CAEAgentHook(this,
				initiallyActiveGuiOverlays, tickables));

		emotionActions = new ArrayList<boolean[]>();
//		if (this.getPlanner() instanceof CuriosityPlanner) {
//			this.tgng = new Tgng("", (CuriosityPlanner) this.getPlanner());
//		} else {
//			this.tgng = new Tgng("", null);
//		}
		this.tgng = new Tgng("");
		//this.actionplanEvolver = new EvolveActionPlan(); // TODO
	}

//	public CAEAgent(
//			String name,
//			int playerIndex,
//			GoalPlanner planner,
//			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
//			String[] initiallyActiveGuiOverlays, Tickable... tickables) 
//	{
//		this(name, playerIndex, planner, initialKnowledge, initiallyActiveGuiOverlays, tickables);
//	
//		if (this.getPlanner() instanceof CuriosityPlanner) 
//		{
//			this.tgng = new Tgng(name,
//					(CuriosityPlanner) this.getPlanner());
//		} 
//		else 
//		{
//			this.tgng = new Tgng(name, null);
//		}
//	}

//	public CAEAgent(
//			int playerIndex,
//			GoalPlanner planner,
//			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
//			Tickable... tickables) {
//		this(playerIndex, name, planner, initialKnowledge, new String[] { "overlayAStar",
//				"overlayGoal"}, tickables);
//
//		emotionActions = new ArrayList<boolean[]>();
//	}

	/**
	 * Former VoiceAgent Constructor
	 * 
	 * @param planner
	 * @param initialKnowledge
	 * @param voiceControl
	 * @param tickables
	 */
//	public CAEAgent(
//			String name,
//			GoalPlanner planner,
//			int playerIndex,
//			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
//			VoiceControl voiceControl, Tickable... tickables) {
//		this(planner, initialKnowledge, new String[] { "overlayAStar",
//				"overlayGoal"},
//				tickables);
//		//this.voiceControl = voiceControl;
//	}

	/**
	 * CAEAgent that can process voice commands and manage knowledge over voice
	 * (without any initialKnowledge, using local observation by default, and
	 * current goal as overlay)
	 * 
	 * @param name
	 * @param planner
	 * @param voiceControl
	 * @param tickables
	 */
	public CAEAgent(
			String name,
			int playerIndex,
			GoalPlanner planner,
			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
			VoiceControl voiceControl, Tickable... tickables) 
	{
		this(name, playerIndex, planner, initialKnowledge, new String[] { "overlayAStar", "overlayGoal", "overlaySpokenText","overlayMotivations"},
				tickables);
		
		//this.voiceControl = voiceControl;	
		
//		if (this.getPlanner() instanceof CuriosityPlanner) 
//		{
//			this.tgng = new Tgng(name,(CuriosityPlanner) this.getPlanner());
//		} 
//		else 
//		{
//			this.tgng = new Tgng(name, null);
//		}
		this.tgng = new Tgng(name);
	}

	//same as above + microphone
	public CAEAgent(
			String name,
			int playerIndex,
			GoalPlanner planner,
			Map<ConditionDirectionPair, TreeMap<Effect, Double>> initialKnowledge,
			VoiceControl voiceControl,
			String microphone,
			Tickable... tickables) 
	{
		this(name,playerIndex,planner,initialKnowledge,voiceControl,tickables);
		this.microphone = microphone;
	}

//	NOT USED
//	public CAEAgent(String name, int playerIndex, GoalPlanner planner,
//			/*VoiceControl voiceControl,*/ Tickable... tickables) 
//	{
//		this(name, playerIndex, planner, null, new String[] { "overlayAStar",
//				"overlayGoal"}, tickables);
//		
//		//this.voiceControl = voiceControl;
//		
////		if (this.getPlanner() instanceof CuriosityPlanner) 
////		{
////			this.tgng = new Tgng(name,
////					(CuriosityPlanner) this.getPlanner());
////		} else {
////			this.tgng = new Tgng(name, null);
////		}
//		this.tgng = new Tgng(name);
//	}

	public void init() {
		planner.init(this, getBrain().getLevelSceneAdapter());
		tgng.init();
		this.aStarSimulator.setLevelSceneAdapter(getBrain()
				.getLevelSceneAdapter());		
	}

	/**
	 * Needed to grant access to other voice components
	 */
	@Deprecated
	public VoiceControl getVoiceControl() {
		return PlayHook.voiceControl;
	}

	/**
	 * Methods to override from Agent interface
	 */
	@Override
	public void reset() {
		// firstStep = true;
	}

	/*
	 * inserts a line of commands into the emotionActions list. These commands
	 * correspond to Players emotion
	 */
	public void setEmotionActionType(EmotionActionType emotionActionType) {
		// motions Player can perform. One motion is a singular movement in
		// scene
		boolean[] standStill = { false, false, false, false, false };
		boolean[] left = { true, false, false, false, false };
		boolean[] right = { false, true, false, false, false };
		boolean[] jump = { false, false, false, true, false };
		boolean[] jumpLeft = { true, false, false, true, false };
		boolean[] jumpRight = { false, true, false, true, false };
		boolean[] cowerToFloor = { false, false, true, false, false };

		switch (emotionActionType) {
		case FEAR_ACTION:
			// if Player has fear, he will move left/right/left/right for
			// several
			// times (this shall look like trembling)

			// first of all clear the list to ensure an empty stack
			emotionActions.clear();

			// fills emotionActions list with left/right input
			for (int i = 0; i < 10; i++) {
				emotionActions.add(left);
				emotionActions.add(right);
			}

			// to stop his movement, standStill needs to be added at the end of
			// emotionActions - otherwise, he would just keep moving into the
			// last commands direction (in this case: right)
			emotionActions.add(standStill);
			break;
		case HAPPY_ACTION:
			// if Player is happy, he will perform a jump

			emotionActions.clear();

			// adds the jump to the emotionAction list so that Player can
			// execute
			// it
			emotionActions.add(jump);

			emotionActions.add(standStill); // just to be save
			break;
		case COURAGEOUS_ACTION:
			// if Player is courageous (brave), he will jump twice: once left,
			// and once to the right

			emotionActions.clear();

			for (int i = 0; i < 6; i++) {
				// semi high jump to the left (only 6 times added)
				emotionActions.add(jumpLeft);
			}

			for (int i = 0; i < 12; i++) {
				// short cowering (looks funny)
				emotionActions.add(cowerToFloor);
			}

			for (int i = 0; i < 10; i++) {
				// big jump to the right
				emotionActions.add(jumpRight);
			}

			// make Player stop after his jump
			emotionActions.add(standStill);
			break;
		default:
			break;
		}
		;
	}

	/*
	 * returns the first entry of emotionActions to be executed by Player. At
	 * the same time, this method removes that command from the emotionActions
	 * list
	 */
	private boolean[] performEmotion() {

		// store in tempAction the first command of Players emotional action
		boolean[] tempAction = emotionActions.get(0);

		// remove the command which currently will be performed
		emotionActions.remove(0);

		return tempAction;
	}

	public void disableEmotions(boolean disableEmotions) {
		this.disableEmotions = disableEmotions;
	}

	//private int counter = 0;

	// Agent
	@Override
	public boolean[] getAction(Environment observation) 
	{
		//why!?
		/*this.counter++;
		if (this.counter == 2) 
		{
			this.planner.computeAndSetNewGoal();
		}*/

		//TEST!!!!! DELETEME!!!		
		/*counter++;
		if(counter%150==0)
			PlayHook.addRespawningSprite(GlobalOptions.getGameWorld(), PlayerWorldObject.LUKO, new GlobalCoarse(11, 0));*/
		
		/*
		 * if Player is aware of any emotions, he will first of all satisfy them
		 * before he moves to his next goal. So, if emotionAction array contains
		 * a value, Player will perform an emotion (jump, cower on floor, ...)
		 * 
		 * Works as the following: as long as emotionActions contains a motion,
		 * this loop will be entered and Player will complete his
		 * emotionalAction
		 */
		if (!disableEmotions && emotionActions != null
				&& emotionActions.size() > 0) {

			// after this body, an emotionAction will be performed, which might
			// have changed Players position. To recompute the goals, set the
			// boolean on true to enter the if-loop below
			emotionActionPerformed = true;

			// his current part of the emotional action will be returned
			return performEmotion();
		}

		if (emotionActionPerformed) {
			// Goals have to be recomputed, because the emotional action could
			// have changed Players position
			getAStarSimulator().recomputeAllGoals();
			emotionActionPerformed = false;
		}

		if (tgng.isActive() || enemyNearby()) 
		{
			aStarSimulator.stopEverythingInAStarComputation();
			boolean[] tgngMove = tgng.getTgngMove(brain.getLevelSceneAdapter()
					.createNewSimulatedLevelScene(null));
			// move can be null if tgng isnt active anymore -> use AStar for
			// planning
			if (tgngMove != null) 
			{
				//System.out.println("return tgngMove;");
				return tgngMove;
			} 
			else 
			{
				// tgng stopped, use AStar for planning
				aStarSimulator.recomputeAllGoals();
				this.waitingToSlowDownBeforeNextGoalSearch = true;
			}
		}

		//planner.update(observation); //!??!?!?!?!

		// update position of player in the map
		float[] arrayPlayerPos = observation.getPlayerFloatPos(getPlayer());
		this.playerPosition = new GlobalContinuous(arrayPlayerPos[0],arrayPlayerPos[1]);
		TutorialFunction.getTutor().playerIsAtPosition(this.playerPosition.toGlobalCoarse().x,this.playerPosition.toGlobalCoarse().y);

		// let the view know the position and goal
//		this.setChanged();
//		this.notifyObservers(new PlanningEvent(PlanningEventType.PLAYER_STATE_CHANGED));
		
		// as soon as player stands still after the goal has been reached - this
		// may or may not be the same time frame - set new goal in planner.
		if (waitingToSlowDownBeforeNextGoalSearch && !brain.getLevelSceneAdapter().getSimulatedLevelScene().getMovingPlayer().isMoving()) 
		{
			this.waitingToSlowDownBeforeNextGoalSearch = false;
			//this.planner.computeAndSetNewGoal();
			
			System.out.println("AGENT "+getName()+" NOTIFIES ABOUT AGENT_REACHED_GOAL");
			
			setChanged();
			notifyObservers(new PlanningEvent(PlanningEventType.AGENT_REACHED_GOAL));
		}
		
		if(!waitingToSlowDownBeforeNextGoalSearch && aStarSimulator.actionsArePending())
		{
			PlayerActions aStarAction = aStarSimulator.getNextAction(brain.getLevelSceneAdapter());
			//System.out.println("aStarAction: "+aStarAction+"\n");
			
			if(!aStarSimulator.actionsArePending())
			{
				waitingToSlowDownBeforeNextGoalSearch=true;
				
				/*setChanged();
				notifyObservers(new PlanningEvent(PlanningEventType.AGENT_REACHED_GOAL));*/
			}
			
			return aStarAction.getKeyStates().get(getPlayerIndex()).getKeyState();
		}
		
//		//TEST: empty and return
//		boolean ret[]=primitiveAction.clone();		
//		primitiveAction=new boolean[Environment.numberOfButtons];
//		
//		return ret;
		
		return primitiveAction;		
	}

	boolean enemyNearby() {
		ArrayList<SimulatedSprite> enemies = new ArrayList<SimulatedSprite>();
		SimulatedLevelScene level = brain.getLevelSceneAdapter()
				.getSimulatedLevelScene();

		for (SimulatedSprite sprite : level.getSimulatedSprites()) {
			if (sprite.isHostile()) {
				enemies.add(sprite);
			}
		}

		for (SimulatedSprite enemy : enemies) {
			if (Tgng.isInTgngThresholds(new SimulatedPlayer(this.getPlayer()),
					enemy)) {
				return true;
			}
		}

		return false;
	}

	// Agent
	@Override
	public AGENT_TYPE getType() {
		return Agent.AGENT_TYPE.AI;
	}

	// /**
	// * Methods for human control, get called when a key is pressed.
	// */
	// public void keyPressed(KeyEvent e) {
	// int keyCode = e.getKeyCode();
	// toggleKey(keyCode, true);
	// planner.keyPressed(e);
	// }
	//
	// public void keyReleased(KeyEvent e) {
	// toggleKey(e.getKeyCode(), false);
	// planner.keyReleased(e);
	// }
	//
	//
	// /**
	// * Fills the action according to the key presses
	// *
	// * @param keyCode
	// * @param isPressed
	// */
	// protected void toggleKey(int keyCode, boolean isPressed) {
	// switch (keyCode) {
	// case KeyEvent.VK_LEFT:
	// humanAction[Player.KEY_LEFT] = isPressed;
	// break;
	// case KeyEvent.VK_RIGHT:
	// humanAction[Player.KEY_RIGHT] = isPressed;
	// break;
	// case KeyEvent.VK_DOWN:
	// humanAction[Player.KEY_DOWN] = isPressed;
	// break;
	//
	// case KeyEvent.VK_S:
	// humanAction[Player.KEY_JUMP] = isPressed;
	// break;
	// case KeyEvent.VK_A:
	// humanAction[Player.KEY_SPEED] = isPressed;
	// break;
	// }
	// }

	/**
	 * Executes a direction command
	 * 
	 * @param actionCode
	 *            corresponding to arrow keys
	 */
	public void directionCommand(String actionCode) 
	{
		toggle(actionCode, true);
	}

	/**
	 *
	 * @param actionCode
	 *            corresponding to arrow keys
	 * @param isActive
	 */
	public void toggle(String actionCode, boolean isActive) 
	{
		actionCountdown = 10;

		aStarSimulator.stopEverythingInAStarComputation();
		
		switch (actionCode) 
		{
		case "LEFT":
			primitiveAction[Player.KEY_LEFT] = isActive;
			break;
		case "RIGHT":
			primitiveAction[Player.KEY_RIGHT] = isActive;
			break;
		case "DOWN":
			primitiveAction[Player.KEY_DOWN] = isActive;
			break;
		case "JUMP":
			primitiveAction[Player.KEY_JUMP] = isActive;
			break;
		case "SPEED_UP":
			primitiveAction[Player.KEY_SPEED] = isActive;
			break;
		case "SPEED_DOWN":
			primitiveAction[Player.KEY_SPEED] = !isActive;
			break;
		case "JUMP_RIGHT":
			primitiveAction[Player.KEY_RIGHT] = isActive;
			primitiveAction[Player.KEY_JUMP] = isActive;
			break;
		case "JUMP_LEFT":
			primitiveAction[Player.KEY_LEFT] = isActive;
			primitiveAction[Player.KEY_JUMP] = isActive;
			break;
		case "CARRYDROP":
			primitiveAction[Player.KEY_CARRYDROP] = isActive;
			actionCountdown = 0;
			break;		
		/*case "WAIT":
			Arrays.fill(humanAction, false);
			aStarSimulator.stopEverythingInAStarComputation();
			if (CAEAgentHook.view.getVoicePanel().getVoiceTab().activateReservoirs.isSelected()) 
			{
				CAEAgentHook.view.getVoicePanel().getVoiceTab().activateReservoirs.doClick();
			}
			break;*/
		case "RESET":
			Arrays.fill(primitiveAction, isActive);
			break;
		}

	}

	public GoalPlanner getPlanner() {
		return planner;
	}

	public AStarSimulator getAStarSimulator() {
		return aStarSimulator;
	}

	public GlobalContinuous getPlayerPosition() {
		return this.playerPosition;
	}

	/**
	 * @return the current goal
	 */
	public Goal getCurrentGoal() {
		return this.aStarSimulator.getCurrentGoalPlayerIsMovingTo();
	}

//	@Override
//	public void update(Observable o, Object arg) 
//	{
//		if (o == aStarSimulator) 
//		{
////			switch ((PlanningEventType) arg) 
////			{
////			//case GOAL_CHANGED:
////				/*if(aStarSimulator.getLastGoalAdded() == null || aStarSimulator.getLastGoalAdded().getGoal().actionEffect == null) 
////				{
////					this.goalNode = aStarSimulator.getLastGoalAdded();
////				}*/
////				//break;
////			case ASTAR_REACHED_GOAL:
////				//this.goalNode = aStarSimulator.getCurrentGoal();
////				break;
////			case ASTAR_COULD_NOT_REACH_GOAL:
////				break;
////			case EFFECT_CHAINING_FAILED:
////				break;
////			default:
////				break;
////			}
//			
//			setChanged();
//			notifyObservers(arg);		//notify specifically missioncontrol panel	
//		}
//	}

	public boolean actionPlanIsEmpty() {
		boolean actionPlanIsEmpty = this.getAStarSimulator()
				.actionsArePending();
		return actionPlanIsEmpty;
	}

//	/**
//	 * Tests for enemys in close surrounding(5 ticks) and returns if near
//	 * 
//	 * @return enemyNear
//	 */
//	public boolean testForEvasivAction() {
//		boolean enemyNear = false;
//		// creats Simulation and ticks forward 5 times
//		SimulatedLevelScene tickForCollision = brain.getLevelSceneAdapter()
//				.createNewSimulatedLevelScene(brain);
//		boolean[] noMovement = { false, false, false, false, false };
//		List<CollisionAtPosition> collisions = new ArrayList<CollisionAtPosition>();
//		for (int i = 0; i < 5; i++) {
//			collisions.addAll(tickForCollision.tick(noMovement));
//		}
//		// tests for null pointer and collisions with enemies
//		if (!collisions.isEmpty()) {
//			for (int i = 0; i < collisions.size(); i++) {
//				if (collisions.get(i) != null) {
//					if (collisions.get(i).getSprite() != null) {
//						if (collisions.get(i).getSprite().type.isHostile()) {
//							enemyNear = true;
//						}
//					}
//				}
//			}
//		}
//		return enemyNear;
//	}

	public void changed() {
		this.setChanged();

	}

	public void refundLastPayment()
	{
		PlayHook.players.get(this.brain.getPlayerIndex()).increaseEnergy(lastPayment);
		lastPayment=0;
	}
	
	public boolean payEnergys(int prize)
	{
		boolean couldPay = PlayHook.players.get(this.brain.getPlayerIndex()).decreaseEnergys(prize);
		
		if (PlayHook.outOfEnergys())
		{
//			PlayHook.players.get(this.brain.getPlayerIndex()).getGEH().evalGameFailed();
		}
		
		if(couldPay)
			lastPayment=prize;
		
//		return couldPay; //auskommentiert da wir auch mit weniger als 0 energy weiterspielen wollen
		return true;
	}

//	public static void deleteStaticAttributes() {
//		//BrainAgent.deleteStaticAttributes();
//		emotionActionPerformed = false;
//		emotionActions = null;
//	}

	public void increaseEnergys(int energys) {
		PlayHook.players.get(this.brain.getPlayerIndex()).increaseEnergy(energys);
	}
	
	public MouseInputController getMouseInput() {
		return mouseInput;
	}

	public void setMouseInput(MouseInputController mouseInput) {
		this.mouseInput = mouseInput;
	}
	public EffectChainingSupervisor getEffectChainingSupervisor() {
		return effectChainingSupervisor;
	}

	public void setEffectChainingSupervisor(EffectChainingSupervisor effectChainingSupervisor) {
		this.effectChainingSupervisor = effectChainingSupervisor;
	}
//	public CAEAgent clone(){
//		CAEAgent clone = new CAEAgent("Clone" + this.getName(), actionCountdown, planner, initialKnowledge, initiallyActiveGuiOverlays, tickables)
//				
//				return clone;
//	}
//	
	
//	public static void resetStaticAttributes() {
//		//BrainAgent.resetStaticAttributes();
//		emotionActions = new ArrayList<boolean[]>();
//	}
}
