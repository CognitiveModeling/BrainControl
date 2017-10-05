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
package marioAI.brain.simulation;

import java.util.ArrayList;

import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.movement.PlayerAction;
import marioAI.movement.PlayerActions;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.mario.environments.Environment;

/**
 * This class documents the reachability of static objects in the environment. This is done by simulating
 * Players in cloned scenes which are moved systematically, while recording their results. The results are 
 * saved in a matrix parallel to the staticGlobalMap of the scene.
 * 
 */

public class ReachabilityMap {

	//a matrix of ReachabilityPoints, which contain booleans for general and directional reachability
	public ReachabilityPoint[][] map;
	//used to ensure parallelism with the real scene
	private PlayerWorldObject[][] staticGlobalMap;

	//stochastically selects scenes which need to be further simulated, this is done to save resources
	public ArrayList <SimulatedLevelScene> sceneList = new ArrayList <SimulatedLevelScene>();
	
	//determines the depth of the simulation
	private int recursions = 1;
	private double keepScenes = 10;
	private int maxConsecutiveActionSteps = 10;
	private int maxConsecutiveNoActionSteps = 30;

	public ReachabilityMap(SimulatedLevelScene scene, PlayerWorldObject[][] staticGlobalMap, int recursions)
	{
		this.staticGlobalMap = staticGlobalMap;

		this.map = new ReachabilityPoint[staticGlobalMap.length][staticGlobalMap[0].length]; 

		for(int i = 0; i<this.map.length; i++){
			for (int j = 0; j<this.map[i].length; j++){
				this.map[i][j] = new ReachabilityPoint(false);
			}
		}

//		sceneList = new SimulatedLevelScene[8];
//		if(scene.getPlanningPlayer().getCarriedBy()!= null){
//			for (int i = 0; i< scene.getSimulatedPlayers().size(); i++){
//				if (scene.getSimulatedPlayers().get(i)== scene.getPlanningPlayer().getCarriedBy()){
//					updateReachabilityMap(scene, i, recursions);
//					updateReachabilityMap(scene,scene.getPlanningPlayerIndex(),recursions);
//					break;
//				}
//			}
//		}
//		else updateReachabilityMap(scene, scene.getPlanningPlayerIndex(), recursions);
		
		updateReachabilityMap(scene, scene.getPlanningPlayerIndex(), recursions);
		deleteScenes();
		increment();
	}
	
	public boolean isReachable(int y, int x)
	{
		if(x<0 || x >= map[0].length || y < 0 || y >= map.length)
			return false;
		
		return map[y][x].isReachable();
	}
	
	private void deleteScenes()
	{
		while(sceneList.size()>keepScenes)
		{
			int n=(int) (Math.random()*sceneList.size());
			sceneList.remove(n);
		}
	}

	public void updateReachabilityMap(SimulatedLevelScene scene, int playerindex, int recursion)
	{		
		if (recursion == 0)
			return;
		
		for (int i = 0; i<5; i++)
		{
			SimulatedLevelScene SLSClone = new SimulatedLevelScene(scene, true);
			SimulatedPlayer newplayer = SLSClone.getSimulatedPlayers().get(playerindex);
			PlayerActions actions = new PlayerActions();
			
			//left:
			if(i == 0){
//				newplayer.xa = 0;
				actions = new PlayerActions(playerindex, new PlayerAction (true, false, false, false, false, false, 0));
				}
			//right:
			if(i == 1) {
//				newplayer.xa = 0;
				actions = new PlayerActions(playerindex, new PlayerAction (false, true, false, false, false, false, 0));
				} 
			//short jump left:
			if(i == 2 && newplayer.x !=0) {
//				newplayer.xa = 0;
				actions = new PlayerActions(playerindex, new PlayerAction (true, false, false, true, false, false, 0));
				} 
			//short jump right:
			if(i == 3) {
//				newplayer.xa = 0;
				actions = new PlayerActions(playerindex, new PlayerAction (false, true, false, true, false, false, 0));
				} 
//			//long jump left:
//			if(i == 4 && newplayer.x != 0){
//				newplayer.xa = -8;
//				actions = new PlayerActions(playerindex, new PlayerAction (true, false, false, true, false, false, 0));
//				} 
//			//long jump right:
//			if(i == 5) {
//				newplayer.xa = 8;
//				actions = new PlayerActions(playerindex, new PlayerAction (false, true, false, true , false, false, 0));
//				}
			//jump straight up:
			if(i == 4) {
//				newplayer.xa = 0;
				actions = new PlayerActions(playerindex, new PlayerAction (false, false, false, true, false, false, 0));
				}			
			
			int steps = (int) (Math.random()*maxConsecutiveActionSteps);
			for(int j=0;j<steps;j++)
			{	
				SLSClone.tick(actions);
			
				setMap(SLSClone);
								
				//off screen
				if(Math.abs(scene.getSimulatedPlayers().get(playerindex).getOriginalSprite().getPosition().toGlobalCoarse().x - newplayer.getPosition(0).toGlobalCoarse().x) > Environment.HalfObsWidth-1)
					break;
				if(newplayer.getPosition(0).toGlobalCoarse().y < 0 || newplayer.getPosition(0).toGlobalCoarse().y > 15)
					break;
			}

			steps = (int) (Math.random()*maxConsecutiveNoActionSteps);
			for(int j=0;j<steps;j++)
			{	
				SLSClone.tick(new PlayerActions());
			
				setMap(SLSClone);
								
				//off screen
				if(Math.abs(scene.getSimulatedPlayers().get(playerindex).getOriginalSprite().getPosition().toGlobalCoarse().x - newplayer.getPosition(0).toGlobalCoarse().x) > Environment.HalfObsWidth-3)
					break;
				if(newplayer.getPosition(0).toGlobalCoarse().y < 0 || newplayer.getPosition(0).toGlobalCoarse().y > 15)
					break;
			}			
			
			sceneList.add(SLSClone);
			
//				if (recursion == recursions)
//				{
//					sceneList[i] = SLSClone;
//				}
			
//				if (SLSClone.getPlanningPlayerIndex() != playerindex && SLSClone.getPlanningPlayer().getCarriedBy()==null)
//					recursion=0;	
						
			updateReachabilityMap(SLSClone, playerindex ,recursion -1);
		}
	}


	private void increment()
	{
		for(int i=0;i<map.length;i++)
			for(int j=0;j<map[0].length;j++)
				map[i][j].increment();		
	}
	
	public void setMap(SimulatedLevelScene SLSClone)
	{ 		
		SimulatedPlayer player = SLSClone.getPlanningPlayer();
		float x = player.x;
		float y = player.y;
		float height = player.getHeight();
		float width = player.getWidth();
		int tileix = (int) (x/CoordinatesUtil.UNITS_PER_TILE);
		int tileiy = (int) (y/CoordinatesUtil.UNITS_PER_TILE);
		
		if(x <= 0 || y>223 || y - height <=0) return;
		
		map[tileiy][tileix].setReachable(true);
		
		if(tileiy-1>=0)
			map[tileiy-1][tileix].setReachable(true);
		
//		for(int iy = (int) y; iy > y - height; iy -= height/2){ 
//			for (int ix = (int) x; ix < x + width; ix += width){ 
//				tileix = ix/CoordinatesUtil.UNITS_PER_TILE;
//				tileiy = iy/CoordinatesUtil.UNITS_PER_TILE;
//				if(tileix >= 0 && tileix < map[0].length && tileiy >= 0 && tileiy < map.length){
//					if(!staticGlobalMap[tileiy][tileix].isBlocking(1)){
//						if (map[tileiy][tileix] == null) map[tileiy][tileix] = new ReachabilityPoint(true);
//						map[tileiy][tileix].setReachable(true);
//
//						if (tileix+1 < map[0].length && staticGlobalMap[tileiy][tileix+1].isBlocking(1)){
//							if (map[tileiy][tileix+1] == null) map[tileiy][tileix+1] = new ReachabilityPoint(false);
//							map[tileiy][tileix+1].setReachableFromLeft(true);
//						}
//						if (tileix>0 && staticGlobalMap[tileiy][tileix-1].isBlocking(1)){
//							if (map[tileiy][tileix-1] == null) map[tileiy][tileix-1] = new ReachabilityPoint(false);
//							map[tileiy][tileix-1].setReachableFromRight(true);
//						}
//						if (tileiy+1 < map.length && staticGlobalMap[tileiy+1][tileix].isBlocking(1)) {
//							if (map[tileiy+1][tileix] == null) map[tileiy+1][tileix] = new ReachabilityPoint(false);
//							map[tileiy+1][tileix].setReachableFromTop(true);
//						}
//						if (tileiy>0 && staticGlobalMap[tileiy-1][tileix].isBlocking(1)) {
//							if (map[tileiy-1][tileix] == null) map[tileiy-1][tileix] = new ReachabilityPoint(false);
//							map[tileiy-1][tileix].setReachableFromBelow(true);
//						}
//					}
//					else return;
//				}
//			}
//		}		
	}

	
	public void showMap (String s){	
		System.out.println("");
		System.out.println(s);
		for(int i = 0; i<map.length; i++)
		{
			System.out.println("");
			for (int j = 0; j < 50; j++)
			{
//				if(map[i][j].isReachableFromTop() ||
//				   map[i][j].isReachableFromLeft() ||
//				   map[i][j].isReachableFromRight() ||
//				   map[i][j].isReachableFromBelow())
//				{
//					System.out.print("B");
//				}
//				else
				{
					if (map[i][j].isReachable())
					{
						System.out.print("R");
					}
					else
					{
						System.out.print(" ");
					}
				}
			}
		}
	}

	public void updateReachabilityMap(SimulatedLevelScene scene) 
	{
		//Fabian: what!?
		/*if (sceneList == null){
			for (int i = 0; i<sceneList.length; i++){
				sceneList[i] = scene;
			}
		}
		else {*/
//			for (int i = 0; i<sceneList.size(); i++)
//			{
//				if (sceneList.get(i) == null)
//				{
//					sceneList.set(i,scene);
//				}
//			}
		//}

		ArrayList<SimulatedLevelScene> sceneListCopy = (ArrayList<SimulatedLevelScene>) sceneList.clone();
		
		sceneList.clear();
		
		updateReachabilityMap(scene, scene.getPlanningPlayerIndex(), recursions);		
		
		for (int i = 0; i<sceneListCopy.size(); i++)
		{
//			int random = (int) (Math.random()*(sceneList.size()));
//			
//			if (random==2||random==4||random==6)
//			{
//				random = random-1;
//			}
//			
//			if (random==0)
//			{
//				random = random+1;
//			}
			
			//Fabian: what!??!
//			if((sceneList[i]).getPlanningPlayer().getCarriedBy()!= null){
//				for (int j =0; j< (sceneList[i]).getSimulatedPlayers().size(); j++){
//					if ((sceneList[i]).getSimulatedPlayers().get(j)== (sceneList[i]).getPlanningPlayer().getCarriedBy()){
//						int random2 = (int) (Math.random()*2);
//						updateReachabilityMap(sceneList[i], j, recursions);
//						if (random2 == 0){
//							newSceneList[i] = sceneList[random];
//						}
//						updateReachabilityMap(sceneList[i],(sceneList[i]).getPlanningPlayerIndex(),recursions);
//						if (random2 == 1){
//							newSceneList[i] = sceneList[random];
//						}
//						break;
//					}
//				}
//			}
//			else 
			{
				updateReachabilityMap(sceneListCopy.get(i), sceneListCopy.get(i).getPlanningPlayerIndex(), recursions);
				//newSceneList[i] = sceneList[random];
			}	
		}
		//sceneList = newSceneList;
		
		deleteScenes();
		increment();
	}

//	// Functions for finishing the recursions at different times
//	public void resetfirsthalfofSceneList(SimulatedLevelScene scene) 
//	{
//		for (int i = 0; i<sceneList.length/2; i++)
//		{
//			sceneList[i] = scene;
//		}
//	}
//	public void resetsecondhalfofSceneList(SimulatedLevelScene scene) 
//	{
//		for (int i = sceneList.length/2; i<sceneList.length; i++)
//		{
//			sceneList[i] = scene;
//		}
//	}
}
