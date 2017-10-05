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

import mario.main.Settings;
import marioAI.brain.Effect;
import marioAI.brain.Effect.ActionEffect;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.Condition.PlayerHealthCondition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.heuristics.FineGrainPotentialField;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.BlockingType;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeCoarse;

/**
 * this class makes sure that the player will always move to the right.
 * 
 * @author Jonas Einig, Marcel
 *
 * @param <T>
 */
public class MovementPlanner extends GoalPlanner
{
	//private PlayerWorldObject[][] map;

	public boolean toTheRight = true;
	
	/**
	 * computes goal which is on the left or right border of the player's observation
	 */
	/*	@Override
	public ArrayList<Goal> selectGoalOld() 
	{
		ArrayList<Goal> ret = new ArrayList<Goal>();		
		
		
//		//TODO: JUST A TEST!!!!!!!!!!
//		SimulatedLevelScene scene = levelSceneAdapter.getSimulatedLevelScene();
//		
//		//clark mounts jay 
//		Goal g1 = new Goal
//		(
//				scene.getSimulatedPlayers().get(1).getMapPosition(0),
//				PlayerWorldObject.CLARK,
//				scene.getSimulatedPlayers().get(0),
//				PlayerWorldObject.JAY,
//				scene.getSimulatedPlayers().get(1),
//				CollisionDirection.ABOVE,
//				PlayerHealthCondition.IRRELEVANT
//		);
//	
//		//TODO: unmounting a player by keypresses does not seem to be simulated correctly!!!
//		//clark unmounts jay
//		Goal g2 = new Goal
//		(
//				scene.getSimulatedPlayers().get(1).getMapPosition(0),
//				PlayerWorldObject.CLARK,
//				scene.getSimulatedPlayers().get(0),
//				PlayerWorldObject.JAY,
//				scene.getSimulatedPlayers().get(1),
//				CollisionDirection.RIGHT,
//				PlayerHealthCondition.IRRELEVANT
//		);
//		//clark unmounts jay by going to another position
//		GlobalCoarse someposition=scene.getSimulatedPlayers().get(1).getMapPosition(0);
//		someposition.x+=3;
//		Goal g2 = new Goal
//		(
//				someposition,
//				PlayerWorldObject.CLARK,
//				scene.getSimulatedPlayers().get(0),
//				PlayerWorldObject.NONE,
//				scene.getSimulatedPlayers().get(1),
//				CollisionDirection.IRRELEVANT,
//				PlayerHealthCondition.IRRELEVANT
//		);
//				
//		ret.add(g1);
//		ret.add(g2);
		

		SimulatedLevelScene scene = levelSceneAdapter.getSimulatedLevelScene();
		double highestDistance=-10000;
		boolean goalFound=false;		
		GlobalCoarse bestGoalPos = new GlobalCoarse(0,0);
		
		for(int x=Math.max(0,scene.getPlanningPlayerPos().toGlobalCoarse().x-10);x<Math.min(scene.getPlanningPlayerPos().toGlobalCoarse().x+10,scene.Rmap.map[0].length);x++)		//TODO: reachability seems in global coordinates... not very effective!
		{
			for(int y=0;y<scene.Rmap.map.length;y++)
			{
				double xDistance = (x - scene.getPlanningPlayerPos().toGlobalCoarse().x);
				double yDistance = (y - scene.getPlanningPlayerPos().toGlobalCoarse().y);
				
				//prefer x-changes to the desired direction, privilege negative y changes (upwards)
				double distanceRating = (toTheRight ? 1 : -1) * xDistance - yDistance * 0.1;
				
				if
				(
						Math.abs(xDistance)+Math.abs(yDistance)>1 && 	//its clearly a different position
						scene.Rmap.isReachable(y-1,x) && scene.isBlockingAgainstDirection(new GlobalCoarse(x, y),CollisionDirection.ABOVE) && 	//it is a position on ground
						distanceRating >= highestDistance				//best position checked so far
				)
				{
					highestDistance = xDistance;
					bestGoalPos = new GlobalCoarse(x,y-1);
					goalFound=true;
					
					System.out.println("goal position " + x + " "+ y + " seems OK. xDistance " + xDistance);
				}
			}
		}		
		
		if(goalFound)
		{		
			Goal newGoal = new Goal
					(
							bestGoalPos,
							this.agent.getPlayer().getType(),
							scene.getPlanningPlayer(),
							PlayerWorldObject.NONE,
							null,
							CollisionDirection.IRRELEVANT,
							PlayerHealthCondition.IRRELEVANT
					);

			//TODO: call effect chaining for all goals, not just for separate planners
			//translate effects to sequences of goals:						
			return newGoal.translateEffectGoal(this.levelSceneAdapter.getSimulatedLevelScene());
		}
		
		return ret;
	} */
	public ArrayList<Goal> selectGoal() 
	{
		
		
		ArrayList<Goal> ret = new ArrayList<Goal>();
		
		SimulatedLevelScene scene = null;
		try {
			scene = (SimulatedLevelScene) levelSceneAdapter.getSimulatedLevelScene().clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scene.reachabilityNodes = scene.updateReachabilityMaps(scene);
		int[][] exploredMap = levelSceneAdapter.getPlayers().get(scene.getPlanningPlayerIndex()).getExploredMap(); //keep in mind where the player has already been.
		double highestDistance=0;
		boolean goalFound=false;		
		GlobalCoarse bestGoalPos = new GlobalCoarse(0,0);
		
		
//		if(this.agent.getMouseInput().isOverlayActive()) {
//			bestGoalPos = this.agent.getMouseInput().getSelectedTilePosition(agent.getBrain().levelSceneAdapter.getClonedLevelScene(), agent.getPlayerIndex()).toGlobalCoarse();
//			goalFound = true;
//			agent.getMouseInput().setOverlayActive(false);
//		} else {
			for(int x=Math.max(0,scene.getPlanningPlayerPos().toGlobalCoarse().x-10);x<Math.min(scene.getPlanningPlayerPos().toGlobalCoarse().x+10,scene.getMapWidth());x++)		//TODO: reachability seems in global coordinates... not very effective!
			{
				for(int y=1;y<scene.getMapHeight();y++)
				{
					double xDistance = (x - scene.getPlanningPlayerPos().toGlobalCoarse().x);
					double yDistance = (y - scene.getPlanningPlayerPos().toGlobalCoarse().y);
					
					//prefer x-changes to the desired direction, privilege negative y changes (upwards)
					double distanceRating = (toTheRight ? 1 : -1) * xDistance - yDistance * 0.1;
					
					if
					(
							Math.abs(xDistance)+Math.abs(yDistance)>1 && 	//its clearly a different position	
							scene.isEasilyReachable(new GlobalCoarse(x,y), CollisionDirection.ABOVE) &&
							scene.isBlockingAgainstDirection(new GlobalCoarse(x, y),CollisionDirection.ABOVE)&& //it is a position on ground
							distanceRating >= highestDistance 
	//						&& exploredMap[y-1][x] == 0
											//best position checked so far
					)
					{
//						if(toTheRight) {
//							if(distanceRating >= highestDistance) {
								highestDistance = distanceRating;
								bestGoalPos = new GlobalCoarse(x,y-1);
								goalFound=true;
								
								System.out.println("goal position " + x + " "+ y + " seems OK. xDistance " + xDistance);
//							}
//						} else {
//							if(distanceRating >= highestDistance) {
//								highestDistance = distanceRating;
//								bestGoalPos = new GlobalCoarse(x,y-1);
//								goalFound=true;
//								
//								System.out.println("goal position " + x + " "+ y + " seems OK. xDistance " + xDistance);
//							}
//						}
						
					}
				}
			}
//		}		
		
		if(goalFound)
		{		
			Goal newGoal = new Goal
					(
							bestGoalPos,
							this.agent.getPlayer().getType(),
							scene.getPlanningPlayer(),
							PlayerWorldObject.NONE,
							null,
							CollisionDirection.IRRELEVANT,
							PlayerHealthCondition.IRRELEVANT
					);
			newGoal.setEffect(new Effect(ActionEffect.PROGRESS, this.agent.getPlayer().getType()));

			//TODO: call effect chaining for all goals, not just for separate planners
			//translate effects to sequences of goals:	
			ret.add(newGoal);
//			return newGoal.translateEffectGoal(this.levelSceneAdapter.getSimulatedLevelScene());
		}
		
		
		return ret;
	}

	
	
//	private PlayerWorldObject getMapObject(int x, int y, PlayerWorldObject[][] map, GlobalContinuous playerPos) 
//	{
//		GlobalCoarse globalCoarse = new GlobalCoarse(x, y);
//		ObservationRelativeCoarse localCoarse = globalCoarse.toObservationRelativeCoarse(playerPos);
//		return (localCoarse.y >= 0) && (localCoarse.y < map.length) && (localCoarse.x >= 0) && (localCoarse.x < map[0].length) ? map[localCoarse.y][localCoarse.x] : null;
//	}

	@Override
	public void update(Observable o, Object arg) 
	{
	}
	
	@Override
	protected String getGoalSelectionFailureIdentifier() 
	{
		return "PLANNERTYPE(MOVEMENT)";
	}	
}
