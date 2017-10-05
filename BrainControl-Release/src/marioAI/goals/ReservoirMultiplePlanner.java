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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import mario.main.Settings;
import marioAI.agents.CAEAgent;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.movement.EffectChainingSupervisor;
import marioAI.reservoirMotivation.AbstractReservoir;
import marioAI.reservoirMotivation.FearReservoir;
import marioAI.reservoirMotivation.HappinessReservoir;
import marioAI.reservoirMotivation.ListenerReservoir;
import marioAI.reservoirMotivation.RandomListenerReservoir;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioUI.speechSynthesis.VoiceResponse;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.Tickable;

/**
 * 
 * @author Fabian, Marcel, et al. (there were other authors, originally?)
 * 
 */
public class ReservoirMultiplePlanner extends GoalPlanner implements Tickable, Observer {
	protected ArrayList<ReservoirPlannerPair> reservoirList = new ArrayList<ReservoirPlannerPair>();
	protected ArrayList<AbstractReservoir> otherReservoirs;
	protected ArrayList<GoalPlanner> failedReservoirList = new ArrayList<GoalPlanner>();

	protected GoalPlanner activePlanner;
	
	
	// protected ArrayList<GoalPlanner> plannerHierarchy = new
	// ArrayList<GoalPlanner>();

	
	/**
	 * Creating Reservoirs(Energy, Health, Movement, Curiosity and Esteem)
	 * and their Planner
	 * plus Reservoirs Happiness and Fear (not really used)
	 * 
	 */
	public ReservoirMultiplePlanner() {
		super();

		// Actual Player now as target for health/energy Change instead of
		// "PLAYER"
		ActionEffectPlanner energyPlanner = new ActionEffectPlanner(ActionEffect.ENERGY_INCREASE);
		ActionEffectPlanner healthPlanner = new ActionEffectPlanner(ActionEffect.OBJECT_HEALTH_INCREASE);
		MovementPlanner movementPlanner = new MovementPlanner();
		CuriosityPlanner curiosityPlanner = new CuriosityPlanner();
//		EsteemPlanner esteemPlanner = new EsteemPlanner();

		// TODO: get values somewhere
		double initial_progress = 0;
		double initial_curiosity = -10;
		double initial_health = 1;

		// arguments: double steepness, double initValue, double avgValue,
		// double minValue, double maxValue, String name, ArrayList
		// <ReservoirEvent> rewardListeners, ArrayList <ReservoirEvent>
		// punishmentListeners
		ListenerReservoir energyReservoir = new ListenerReservoir(-0.05, 100, 100, 0, 400, "Energy", ReservoirType.ENERGY,
				ReservoirEventType.ENERGY_CHANGE);
		RandomListenerReservoir movementReservoir = new RandomListenerReservoir(0.01, -4.0, -0.5, initial_progress, 0,
				-10, 10, "Progress", ReservoirType.PROGRESS, ReservoirEventType.GOAL_REACHED);
		RandomListenerReservoir curiosityReservoir = new RandomListenerReservoir(0.01, -2.0, -0.5, initial_curiosity,
				0, -10, 10, "Curiosity", ReservoirType.CURIOSITY,
				/* ReservoirEventType.OBJECT_INTERACTION, */ ReservoirEventType.KNOWLEDGE_INCREASE);
		ListenerReservoir healthReservoir = new ListenerReservoir(-2, initial_health, 1, 0, 2, "Healthiness",
				ReservoirType.HEALTHINESS, ReservoirEventType.HEALTH_CHANGE);
//		ListenerReservoir esteemReservoir = new ListenerReservoir(-0.1, 20, 50, 0, 100, "Esteem", ReservoirType.ESTEEM,
//				ReservoirEventType.ESTEEM_CHANGE);

		ArrayList<ReservoirPlannerPair> reservoirList = new ArrayList<ReservoirPlannerPair>();
		reservoirList.add(new ReservoirPlannerPair(energyPlanner, energyReservoir));
		reservoirList.add(new ReservoirPlannerPair(movementPlanner, movementReservoir));
		reservoirList.add(new ReservoirPlannerPair(curiosityPlanner, curiosityReservoir));
		reservoirList.add(new ReservoirPlannerPair(healthPlanner, healthReservoir));
//		reservoirList.add(new ReservoirPlannerPair(esteemPlanner, esteemReservoir));

		// TODO: these "emotion" reservoirs don't do anything useful...
		ArrayList<AbstractReservoir> otherReservoirs = new ArrayList<AbstractReservoir>();
		otherReservoirs.add(new HappinessReservoir());
		otherReservoirs.add(new FearReservoir());

		initReservoirs(reservoirList, otherReservoirs);
	}

	
	
	public boolean isReservoirPlanningActive() {
		for (ReservoirPlannerPair rpp : reservoirList) {

			if (rpp.getReservoir().isEnabled())

				return true;
		}
		return false;
	}

	/**
	 * tick the values of the reservoirs are updated
	 */
	@Override
	public void onTick(LevelScene levelScene) {

		for (ReservoirPlannerPair pair : reservoirList) {
			pair.getReservoir().onTick(levelScene);
		}
		
		
		
		if(isSelfMotivated() && isReadyToPlan() && ( System.currentTimeMillis() - getTimeStampOfLastAction()) > getWaitingDuration()) {

			selectAndApproachNewGoal(false);
		}
		
		
	}

	public void init(CAEAgent agent, LevelSceneAdapter levelSceneAdapter) {
		super.init(agent, levelSceneAdapter);

		addObserver(this); // for the PlanningEvents conatining other
							// players (see Goalplanner)
		aStarSimulator.addObserver(this); // for goal events
		agent.addObserver(this); // for goal and reservoir events
		agent.getPlayer().addObserver(this); // for some reservoir events
												// (energys, health)
		agent.getBrain().helpObservable.addObserver(this); // for knowledge
															// reservoir events

		System.out.println("Initializing Reservoirs");
		for (ReservoirPlannerPair pair : reservoirList) {
			pair.getPlanner().init(agent, levelSceneAdapter);
			System.out.println(levelSceneAdapter.getSimulatedLevelScene().getPlanningPlayer());
		}
	}

	/**
	 * random active reservoir sampling!
	 * 
	 * you select the planner by choosing the planner which belongs to the
	 * reservoir
	 */
	public GoalPlanner selectPlanner(ArrayList<GoalPlanner> exclude) {
		// sum up:
		double valueSum = 0.0;
		ArrayList<ReservoirPlannerPair> relevantReservoirs = new ArrayList<ReservoirPlannerPair>();
		
		for (ReservoirPlannerPair pair : reservoirList) {
			if (exclude.contains(pair.getPlanner()))
				continue;
			relevantReservoirs.add(pair);
			valueSum += pair.getReservoir().getValue();

			if (pair.getReservoir().getValue() < 0)
				System.err.println("Error: Reservoir values must be positive!!");
		}

		if (valueSum == 0.0) {
			// all reservoirs are deactivated OR excluded... return null
			return null;
		}
		
		//Create array that holds the normalized value ranges of the reservoirs values
		int index = 0;
		double normalizedSum = 0.0;
		double[] valueRanges = new double[relevantReservoirs.size()];
		for (ReservoirPlannerPair rpp : relevantReservoirs) {

			double normalizedValue = rpp.getReservoir().getValue() / valueSum;
			normalizedSum += normalizedValue;
			valueRanges[index] = normalizedSum;
			index++;
			
		}
		
		
		double random = Math.random();
		for(int i = 0; i<valueRanges.length; i++) {
			if(random <valueRanges[i]) {
				
				this.setChanged();
				this.notifyObservers(relevantReservoirs.get(i).getReservoir());
				return relevantReservoirs.get(i).getPlanner();
			}
		}
		// select reservoir by normalized stochastic sampling.
		// it is very unlikely that is outer for loop runs that often
		// (expectancy is 1), but its better than using a while loop...
		//TODO: use the correct probabilities...
//		for (int i = 0; i < 1000; i++) {
//			for (ReservoirPlannerPair rpp : reservoirList) {
//				if (exclude.contains(rpp.getPlanner()))
//					continue;
//
//				if (Math.random() <= rpp.getReservoir().getValue() / valueSum) {
//					rpp.getReservoir().setBold(); // TODO: doesn't seem to have
//													// an effect...
//					return rpp.getPlanner();
//				}
//			}
//		}
		
		

		System.err.println("Very rare error in ReservoirMultiplePlanner");
		return null;
	}

	@Override
	protected String getGoalSelectionFailureIdentifier() {
		return "PLANNERTYPE(NONE)";
	}

	/**
	 * called by firstPlanner, secondPlanner when new goal has been computed
	 */
	public void update(Observable o, Object arg) {
//		if (arg instanceof PlanningEvent)
			// RESERVOIR EVENT HANDLING
			if (arg instanceof ReservoirEvent) {
			for (ReservoirPlannerPair pair : reservoirList) {
			((ListenerReservoir) (pair.getReservoir())).update((ReservoirEvent) arg);
			}
			for (AbstractReservoir res : otherReservoirs) {
			((ListenerReservoir) (res)).update((ReservoirEvent) arg);
			}
			}

		// PLANNING EVENT HANDLING
		if (arg instanceof PlanningEvent) {
			PlanningEvent event = (PlanningEvent) arg;
			PlanningEventType type = event.type;
			Object object = event.object;
			switch (type) {
			case AGENT_REACHED_GOAL:
				// System.out.println("RESERVOIR PLANNER CATCHED AGENT REACHED
				// GOAL");
				for (ReservoirPlannerPair pair : reservoirList) {
					((ListenerReservoir) (pair.getReservoir()))
							.update(new ReservoirEvent(ReservoirEventType.GOAL_REACHED, 1.0));
				}
				for (AbstractReservoir res : otherReservoirs) {
					((ListenerReservoir) (res)).update(new ReservoirEvent(ReservoirEventType.GOAL_REACHED, 1.0));
				}
				this.failedReservoirList.clear();
				
				
				setTimeStampOfLastAction(System.currentTimeMillis());
//				selectAndApproachNewGoal(false);
				setReadyToPlan(true);
				break;
			case ASTAR_COULD_NOT_REACH_GOAL:
				int i = 0;
				for (ReservoirPlannerPair pair : reservoirList) {
					((ListenerReservoir) (pair.getReservoir())).update(new ReservoirEvent(ReservoirEventType.GOAL_REACHED,-1.0));
					
					// decrease esteem if Astar fails, because the trust of the other players go down
					// send the ESTEEM_CHANGE ReservoirEvent to the reservoirs
					((ListenerReservoir) (pair.getReservoir()))
					.update(new ReservoirEvent(ReservoirEventType.ESTEEM_CHANGE, -3));
					
					//find Esteem Reservoir and print actual esteem value
					if(reservoirList.get(i).getPlanner() instanceof EsteemPlanner){
						System.out.println("esteem-value: "+reservoirList.get(i).getReservoir().getActualValue());
					}
					i++;
				}
				for (AbstractReservoir res : otherReservoirs) {
					((ListenerReservoir)(res)).update(new ReservoirEvent(ReservoirEventType.GOAL_REACHED, -1.0));
					//
					((ListenerReservoir) (res))
					.update(new ReservoirEvent(ReservoirEventType.ESTEEM_CHANGE, -3));
				}
				setTimeStampOfLastAction(System.currentTimeMillis());
				setReadyToPlan(true);
				// selectAndApproachNewGoal(false);

				break;
			case INTERACTION_GOALPLAN_FINISHED:
				System.out.println(object.getClass());

				// An Arraylist<Goal> is created from the received object of the
				// PlanningEvent
				// A workaround is needed for typechecking and conversion
				// Hannah, Pavel
				ArrayList<Goal> goalList = null;
				if(object instanceof ArrayList<?> ){
					//Count the occurences of interactions with other players
					//each new occurence is weighted less and less
					double interactions = 0;
					double weight = 1;
					//check if the actor helps another player to get esteem
					//if the esteem planner is active it is the first interaction with another player
					//Hannah
					if(this.activePlanner instanceof EsteemPlanner){
						interactions++;
						weight /= 2;
					}
					
					if(!((ArrayList<?>) object).isEmpty()){
						if (((ArrayList<?>) object).get(0) instanceof Goal) {
							goalList = (ArrayList<Goal>) object;
							for (Goal g : goalList) {
								PlayerWorldObject target = g.getTarget();
								PlayerWorldObject actor = this.levelSceneAdapter.getSimulatedLevelScene().getPlanningPlayer().getType();
								if (actor.isAnotherPlayer(target)) {
									interactions += weight;
									if (weight > 0.01)
										weight /= 2;
								} else {
									if (g.getEffect() != null) {
										PlayerWorldObject effectTarget = g.getEffect().getEffectTarget();
										if (actor.isAnotherPlayer(effectTarget)) {
											interactions += weight;
											if (weight > 0.01)
												weight /= 2;
										}
									}
								}
							}
							
							//increase esteem, if player helps another player
							// send the ESTEEM_CHANGE ReservoirEvent to the reservoirs
							int j = 0; 
							for (ReservoirPlannerPair pair : reservoirList) {
								((ListenerReservoir) (pair.getReservoir()))
								.update(new ReservoirEvent(ReservoirEventType.ESTEEM_CHANGE, 5 * interactions));
								//find Esteem Reservoir and print actual esteem value
								if(reservoirList.get(j).getPlanner() instanceof EsteemPlanner){
									System.out.println("esteem-value: "+reservoirList.get(j).getReservoir().getActualValue());
								}
								j++;
							}
							for (AbstractReservoir res : otherReservoirs) {
								((ListenerReservoir) (res))
								.update(new ReservoirEvent(ReservoirEventType.ESTEEM_CHANGE, 5 * interactions));
							}
						}
						
					}
					else{
						System.out.println("List is empty");
					}
					
				}


				break;
			
			case EFFECT_CHAINING_FAILED:
				if(getAbort().get()) {
					getAbort().set(false);
					
					this.setChanged();
					this.notifyObservers(new PlanningEvent(PlanningEventType.RESERVOIR_INTERRUPT));
					this.failedReservoirList.clear();

					setTimeStampOfLastAction(System.currentTimeMillis());
					setReadyToPlan(true);
				} else {
					this.setChanged();
					this.notifyObservers(new PlanningEvent(PlanningEventType.RESERVOIR_INTERRUPT));
					selectAndApproachNewGoal(false);
				}
				
				
				// the following events are not handled by this class
			case ASTAR_REACHED_GOAL:
			case GOAL_SELECTION_FAILED:
			case ASTAR_START_SEARCH:
				// case PLAYER_STATE_CHANGED:
			default:
				break;
			}
		}
		super.update(o, arg);
	}


	public void enableReservoirsExceptEsteemAndCuriosity() {
		for (ReservoirPlannerPair res : reservoirList) {
			if (!(
					res.getReservoir().getReservoirType().equals(ReservoirType.ESTEEM)
				 || res.getReservoir().getReservoirType().equals(ReservoirType.CURIOSITY)
				 || res.getReservoir().getReservoirType().equals(ReservoirType.HEALTHINESS)
//				 || res.getReservoir().getReservoirType().equals(ReservoirType.ENERGY)
				 || res.getReservoir().getReservoirType().equals(ReservoirType.PROGRESS)
				 )) {
				res.getReservoir().enable(true);
			}
		}
	}


	//
	// public ArrayList<Goal> selectGoalRegardlessOfActivePlanningBla()
	// {
	//
	// if (! this.resActive()){
	// this.enableReservoirs();
	// }
	//
	// return selectGoalRegardlessOfActivePlanning();
	// }
	//

	/**
	 * Invoked by EsteemPlanner: If no Reservoirs of the other agent are
	 * active, activate them, except Esteem 
	 * and Curiosity (should be possible in the future)
	 * @return "normal" selectGoal on activated Reservoirs
	 */
	public ArrayList<Goal> selectGoalRegardlessOfActivePlanning() {
		
		if (!this.isReservoirPlanningActive()) {
			System.out.println("Activating Reservoirs");
			this.enableReservoirsExceptEsteemAndCuriosity();
		}

//		System.out.println("Active Reservoirs");
//		for (int i = 0; i < reservoirList.size(); i++) {
//			System.out.println(reservoirList.get(i).getReservoir().getName() + "  "
//					+ reservoirList.get(i).getReservoir().isEnabled() + "  "
//					+ reservoirList.get(i).getReservoir().getValue() + "   "
//					+ reservoirList.get(i).getPlanner().getLevelSceneAdapter().getSimulatedLevelScene().getPlanningPlayer());
//		}

		return selectGoal();
	}

	//// >>>>>>> .r117
	// public ArrayList<Goal> selectGoalRegardlessOfActivePlanning()
	// {
	// ArrayList<GoalPlanner> exclude = new ArrayList<GoalPlanner>();
	//
	// ArrayList<Goal> g = new ArrayList<Goal>();
	// GoalPlanner p = null;
	//
	// for(int i=0;i<reservoirList.size();i++)
	// {
	// p = selectPlanner(exclude);
	//
	// if(p == null) //no planner could be selected
	// break;
	//
	// g = p.selectGoal();
	//
	// //System.out.println(g);
	//
	// if(g.size()>0) //the selected planner provided a goal
	// break;
	//
	// exclude.add(p); //ignore this planner
	// }
	//
	// activePlanner = p;
	//
	// if(g.size()==0) //no goal was selected
	// {
	// //try to sample a random planner and give a help request
	// exclude.clear();
	// p=selectPlanner(exclude);
	//
	// System.out.println(p);
	//
	// if(p!=null)
	// update(this,new
	//// PlanningEvent(PlanningEventType.GOAL_SELECTION_FAILED,p.getGoalSelectionFailureIdentifier()));
	// else
	// update(this,new
	//// PlanningEvent(PlanningEventType.GOAL_SELECTION_FAILED,getGoalSelectionFailureIdentifier()));
	// }
	// else
	// {
	// System.out.println("Active Reservoir " + p.getClass().getSimpleName() + "
	//// selected a goal: " + g);
	// //update(this,new PlanningEvent(PlanningEventType.NEW_GOAL_SELECTED, g));
	//// // GoalPlanResponse tested
	// }
	//
	// return g;
	// }

	@Override
	public ArrayList<Goal> selectGoal() {
		if (!this.isReservoirPlanningActive()) {
			activePlanner = null;
			update(this, new PlanningEvent(PlanningEventType.GOAL_SELECTION_FAILED,
					this.getGoalSelectionFailureIdentifier()));
			return new ArrayList<Goal>();
		}
		

//		ArrayList<GoalPlanner> exclude = new ArrayList<GoalPlanner>();

		ArrayList<Goal> goalList = new ArrayList<Goal>();
		GoalPlanner selectedPlanner = null;

		for (int i = 0; i < reservoirList.size(); i++) {
			
			
			selectedPlanner = selectPlanner(failedReservoirList);

			if (selectedPlanner == null) // no planner could be selected
				break;

			goalList = selectedPlanner.selectGoal();

			// System.out.println(g);
			failedReservoirList.add(selectedPlanner); // ignore this planner
			
			if (goalList.size() > 0)  // the selected planner provided a goal
				break;

			
		}

		activePlanner = selectedPlanner;

		if (goalList.size() == 0) // no goal was selected
		{
			// try to sample a random planner and give a help request
			failedReservoirList.clear();
			selectedPlanner = selectPlanner(failedReservoirList);

			System.out.println(selectedPlanner);

			if (selectedPlanner != null)
				update(this, new PlanningEvent(PlanningEventType.GOAL_SELECTION_FAILED,
						selectedPlanner.getGoalSelectionFailureIdentifier()));
			else
				update(this, new PlanningEvent(PlanningEventType.GOAL_SELECTION_FAILED,
						getGoalSelectionFailureIdentifier()));
		} else {
			System.out.println("Active Reservoir " + selectedPlanner.getClass().getSimpleName() + " selected a goal: " + goalList);
			
			update(this, new PlanningEvent(PlanningEventType.NEW_GOAL_SELECTED, goalList)); // GoalPlanResponse
																						// tested
		}

		System.out.println("Active Planner: " + activePlanner);		
		System.out.println("Goals" + goalList);
		
		
		return goalList;
	}

	public LinkedList<AbstractReservoir> getReservoirList() {
		LinkedList<AbstractReservoir> list = new LinkedList<AbstractReservoir>();
		for (ReservoirPlannerPair pair : this.reservoirList) {
			list.add(pair.getReservoir());
		}
		/*
		 * for (AbstractReservoir res : otherReservoirs) { list.add(res); }
		 */
		return list;
	}

	// TODO: the reservoir types are not used consistently
	public AbstractReservoir getReservoir(ReservoirType type) {
		for (ReservoirPlannerPair rpp : reservoirList) {
			if (rpp.getReservoir().getReservoirType() == type) {
				return rpp.getReservoir();
			}
		}

		for (AbstractReservoir res : otherReservoirs) {
			if (res.getReservoirType() == type) {
				return res;
			}
		}

		System.out.println("ReservoirMultiplePlanner.getReservoir: target type not found");
		return null;
	}

	public GoalPlanner getReservoirPlanner(ReservoirType type) {
		for (ReservoirPlannerPair rpp : reservoirList) {
			if (rpp.getReservoir().getReservoirType() == type) {
				return rpp.getPlanner();
			}
		}

		System.out.println("ReservoirMultiplePlanner.getReservoir: target type not found");
		return null;
	}

	public void initReservoirs(ArrayList<ReservoirPlannerPair> reservoirList,
			ArrayList<AbstractReservoir> otherReservoirs) {
		this.reservoirList = reservoirList;
		this.otherReservoirs = otherReservoirs;
	}

	/**
	 * Compute the relevance of effect depending on current reservoir activations 
	 * @param effect
	 * @param defaultWeight for effects that are not associated to a reservoir
	 * @return
	 */
	public double getEffectRelevance(Effect effect, double defaultWeight) {
		final double activation_weight = 5.0; // TODO: find appropriate values
		AbstractReservoir relevantReservoir = null;
		switch (effect.getType()) {
		case CARRY:
		case DROP:
		case MOUNT:
		case UNMOUNT:
//			relevantReservoir = getReservoir(ReservoirType.ESTEEM);
			break;
		case ENERGY_INCREASE:
			relevantReservoir = getReservoir(ReservoirType.ENERGY);
			break;
		case OBJECT_HEALTH_INCREASE:
			relevantReservoir = getReservoir(ReservoirType.HEALTHINESS);
			break;

		default:
			break;
		}
//		System.out.println("SD: Evaluated relevance by reservoir " + relevantReservoir.getName() + " : " + relevantReservoir.getValue());
		if (relevantReservoir != null) {return defaultWeight + relevantReservoir.getValue() * activation_weight;}
		else return defaultWeight;
	}

	
	/**
	 * "Copying" ReservoirMultiplePlanner of other Player with planningAgents 
		LevelSceneAdapter(i.e. Knowledge) 
		and other Player as Agent for correct Effects in the ActionEffectPlanners 
			
	 * @param scene of PlanningPlayer, his knowledge is used during schematic planning
	 * 			
	 * @return ReservoirMultiplePlanner that is used for selecting goals by
	 * 			EsteemPlanner
	 */
	public ReservoirMultiplePlanner softCopy(LevelSceneAdapter scene) {

		ReservoirMultiplePlanner softCopy = new ReservoirMultiplePlanner();

		for (int i = 0; i < softCopy.reservoirList.size(); i++) {
			softCopy.reservoirList.set(i, this.reservoirList.get(i).copy());
//			System.out.println(reservoirList.get(i).getReservoir().getName() + ": "
//					+ reservoirList.get(i).getReservoir().getValue());
//			System.out.println("Copy " + this.reservoirList.get(i).copy().getReservoir().getValue());
//			System.out.println("clone: " + clone.reservoirList.get(i).getReservoir().getName() + ": "
//					+ clone.reservoirList.get(i).getReservoir().getValue() + ": "
//					+ clone.reservoirList.get(i).getReservoir().isEnabled());

		}

		softCopy.init(this.agent, scene);
		
		return softCopy;
	}
	

	


	
	
}
