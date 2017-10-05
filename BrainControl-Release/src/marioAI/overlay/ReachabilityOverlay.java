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
package marioAI.overlay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Set;

import marioAI.brain.Brain;
import marioAI.collision.CollisionAtPosition;
import marioAI.collision.ConditionDirectionPair.CollisionDirection;
import marioAI.movement.ReachabilityNode;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * Shows the reachability map as an overlay
 * @author Yves
 */
public class ReachabilityOverlay extends ToggleableOverlay {
	private static final int halfOfSize = 3*GlobalOptions.resolution_factor;
	private Brain brain;
	private static final int PRIMARY = 0;
	private static final int SECONDARY = 1;
	private static final int TERTIARY = 2;
	
	public ReachabilityOverlay(Brain brain) {
		super("reachbility overlay");
		this.brain = brain;
	}

	/**
	 * Draws the points at the position to which they belong.
	 * Creates a ReachabilityNode and starts the simulation from that point on out.
	 */
	public void drawOverlay(Graphics g, LevelScene levelScene, int playerIndex) 
	{
		//calculateReachability(levelScene, playerIndex);
		if(playerIndex == brain.getPlayerIndex()) {
			ReachabilityNode start = brain.reachabilityNode;
			drawPossibleCollisions(start.reachabilityCounter,start.uniqueFoundCollisions, g, levelScene, playerIndex,PRIMARY);
		
		}
	}

	private void drawPossibleCollisions(float[][] reachabilityMap,Set<CollisionAtPosition> collisionDirections,Graphics g, LevelScene levelScene, int playerIndex,int color) {
		Graphics2D g2 = (Graphics2D) g;
		

		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];
		float maximum = 0f;
		for (int i = 0; i < reachabilityMap.length; i++) {
			for(int j = 0; j<reachabilityMap[i].length; j++) {
				if(reachabilityMap[i][j] > maximum) {
					maximum = reachabilityMap[i][j];
				}
			}
		}
	
		for (int i = 0; i < reachabilityMap.length; i++) {
			for(int j = 0; j<reachabilityMap[i].length; j++) {
				
				GlobalCoarse gc = new GlobalCoarse(j,i);
				GlobalContinuous pos = gc.toGlobalContinuous();
				//offset the position -8 in both directions to get top left corner of a block, not the center.	
				int x = (int) (pos.x -8 - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y -8 - yCam) * GlobalOptions.resolution_factor;
				
				if(reachabilityMap[i][j] > 0) {
					
					
					
					float b = 1.0f-(reachabilityMap[i][j]/maximum);
					
					switch(color){
						case 0:
							g2.setColor(new Color(1.0f, b, b, 0.5f));
							break;
						case 1:
							g2.setColor(new Color(b, 1.0f, b, 0.5f));
							break;
						case 2:
							g2.setColor(new Color(b, b, 1.0f, 0.5f));
							break;
						default:
							g2.setColor(new Color(1.0f, b, b, 0.5f));
					}
					
					
					g2.fillRect((int) x,
							(int) y, 16*GlobalOptions.resolution_factor, 16*GlobalOptions.resolution_factor);
					}
				
				
				/*if(collisionDirections[i][j] != null) {
					for(CollisionDirection cd : collisionDirections[i][j] ) {
						 x = (int) (pos.x  - xCam) * GlobalOptions.resolution_factor;
						 y = (int) (pos.y  - yCam) * GlobalOptions.resolution_factor;
						drawArrow(g, new Point(x, y), cd, new Color(0, 0, 160));
					}
					
				}*/
				
				}
			}
		
			for (CollisionAtPosition collisionAtPosition : collisionDirections) {
				CollisionDirection cd = collisionAtPosition.getDirection();
				GlobalCoarse gc = collisionAtPosition.getPosition();
				
				
				GlobalContinuous pos = gc.toGlobalContinuous();
				int x = (int) (pos.x  - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y  - yCam) * GlobalOptions.resolution_factor;
				drawArrow(g, new Point(x, y), cd, new Color(0, 0, 160));
			}
					
	} 
	/**
	 * Uses the visited float matrix created in the reachability node, colors depend on the frequency of the visits of  a point.
	 * 
	 * @param reachabilityMap
	 * @param g
	 * @param levelScene
	 * @param playerIndex
	 *//*
	private void drawPossibleCollisions(float[][] reachabilityMap,Set<CollisionAtPosition> collisionDirections,Graphics g, LevelScene levelScene, int playerIndex,int color) {
		Graphics2D g2 = (Graphics2D) g;
		

		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];
		float maximum = 0f;
		for (int i = 0; i < reachabilityMap.length; i++) {
			for(int j = 0; j<reachabilityMap[i].length; j++) {
				if(reachabilityMap[i][j] > maximum) {
					maximum = reachabilityMap[i][j];
				}
			}
		}
	
		for (int i = 0; i < reachabilityMap.length; i++) {
			for(int j = 0; j<reachabilityMap[i].length; j++) {
				
				GlobalCoarse gc = new GlobalCoarse(j,i);
				GlobalContinuous pos = gc.toGlobalContinuous();
				//offset the position -8 in both directions to get top left corner of a block, not the center.	
				int x = (int) (pos.x -8 - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y -8 - yCam) * GlobalOptions.resolution_factor;
				
				if(reachabilityMap[i][j] > 0) {
					
					
					
					float b = 1.0f-(reachabilityMap[i][j]/maximum);
					
					switch(color){
						case 1:
							g2.setColor(new Color(1.0f, b, b, 0.5f));
							break;
						case 2:
							g2.setColor(new Color(b, 1.0f, b, 0.5f));
							break;
						case 3:
							g2.setColor(new Color(b, b, 1.0f, 0.5f));
							break;
						default:
							g2.setColor(new Color(1.0f, b, b, 0.5f));
					}
					
					
					g2.fillRect((int) x,
							(int) y, 32, 32);
					}
				
				
				/*if(collisionDirections[i][j] != null) {
					for(CollisionDirection cd : collisionDirections[i][j] ) {
						 x = (int) (pos.x  - xCam) * GlobalOptions.resolution_factor;
						 y = (int) (pos.y  - yCam) * GlobalOptions.resolution_factor;
						drawArrow(g, new Point(x, y), cd, new Color(0, 0, 160));
					}
					
				}*//*
				
				}
			}
		
			for (CollisionAtPosition collisionAtPosition : collisionDirections) {
				CollisionDirection cd = collisionAtPosition.getDirection();
				GlobalCoarse gc = collisionAtPosition.getPosition();
				
				
				GlobalContinuous pos = gc.toGlobalContinuous();
				int x = (int) (pos.x  - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y  - yCam) * GlobalOptions.resolution_factor;
				drawArrow(g, new Point(x, y), cd, new Color(0, 0, 160));
			}
					
	} */
	private void drawArrow(Graphics g, Point p, CollisionDirection direction,
			Color c) {
		if (direction == null)
			return;
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(c);
		g2.setStroke(new BasicStroke(2));
		Point tip = null, shift, tail = null, arm1 = null, arm2 = null;
		switch (direction) {
		case ABOVE:
			shift = new Point(0, -(LevelScene.DISCRETE_CELL_SIZE)/2 * GlobalOptions.resolution_factor);
			tip = add(p, shift);
			tail = add(tip, shift);
			arm1 = add(tip, new Point(-halfOfSize, -halfOfSize));
			arm2 = add(tip, new Point(halfOfSize, -halfOfSize));
			break;
		case LEFT:
			shift = new Point(-(LevelScene.DISCRETE_CELL_SIZE)/2 * GlobalOptions.resolution_factor, 0);
			tip = add(p, shift);
			tail = add(tip, shift);
			arm1 = add(tip, new Point(-halfOfSize, -halfOfSize));
			arm2 = add(tip, new Point(-halfOfSize, halfOfSize));
			break;
		case BELOW:
			shift = new Point(0, (LevelScene.DISCRETE_CELL_SIZE)/2 * GlobalOptions.resolution_factor);
			tip = add(p, shift);
			tail = add(tip, shift);
			arm1 = add(tip, new Point(-halfOfSize, halfOfSize));
			arm2 = add(tip, new Point(halfOfSize, halfOfSize));
			break;
		case RIGHT:
			shift = new Point((LevelScene.DISCRETE_CELL_SIZE)/2 * GlobalOptions.resolution_factor, 0);
			tip = add(p, shift);
			tail = add(tip, shift);
			arm1 = add(tip, new Point(halfOfSize, -halfOfSize));
			arm2 = add(tip, new Point(halfOfSize, halfOfSize));
			break;
		case IRRELEVANT:
			break;
		default:
			throw new IllegalArgumentException(
					"In GoalOverlay.drawArrow: Direction was " + direction);
		}
		if (tip != null && tail != null && arm1 != null && arm2 != null) {
			g2.drawLine(tip.x, tip.y, tail.x, tail.y);

			g2.drawLine(tip.x, tip.y, arm1.x, arm1.y);
			g2.drawLine(tip.x, tip.y, arm2.x, arm2.y);
		}
	}
	
	private static Point add(Point p1, Point p2) {
		return new Point(p1.x + p2.x, p1.y + p2.y);
	}
	/*
	private void drawPossibleCollisions(ArrayList<CollisionAtPosition> collisionAtPositionList,Graphics g, LevelScene levelScene, int playerIndex) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);

		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];

		
		for (CollisionAtPosition cap : collisionAtPositionList) {
			
			if(cap != null) {				
				GlobalContinuous pos = cap.getStaticPosition().toGlobalContinuous();

				//offset the position -8 in both directions to get top left corner of a block, not the center.	
				
				int x = (int) (pos.x -8 - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y -8 - yCam) * GlobalOptions.resolution_factor;
				
				g2.drawRect((int) x,
						(int) y, 32, 32);
			}	
		}
	}*/
	/*
	private boolean[][] calculateReachability(LevelScene levelScene, int playerIndex) {
		ReachabilityNode start = new ReachabilityNode(levelScene, playerIndex,brain,1.2f,1.9f);
		start.simulateNode();
	
		return ReachabilityNode.visited;
	}
	*/
	
	
	
	/*
	 * Draws continous positions
	 * 
	 
	private ArrayList<GlobalContinuous> calculateReachability(LevelScene levelScene, int playerIndex) {
		ArrayList<GlobalContinuous> cap = new ArrayList<GlobalContinuous>();
		ReachabilityNode start = new ReachabilityNode(levelScene, playerIndex,brain);
		cap.addAll((start.simulateStep(1.2f, 1.9f)));
		//cap.addAll((start.simulateStep(12.0f, 3.5f)));
		

	
		return cap;
	} 
	
		/*
	private ArrayList<CollisionAtPosition> calculateReachability(LevelScene levelScene, int playerIndex) {
		ArrayList<CollisionAtPosition> cap = new ArrayList<CollisionAtPosition>();
		ReachabilityNode start = new ReachabilityNode(levelScene, playerIndex,brain);
		cap.addAll((start.simulateStep(1.2f, 1.9f)));
		
		
		*
		 * debug test
		cap.add(new CollisionAtPosition(new ConditionDirectionPair(null, null), new GlobalCoarse(3, 13), null));
		cap.add(new CollisionAtPosition(new ConditionDirectionPair(null, null), new GlobalCoarse(4, 13), null));
		cap.add(new CollisionAtPosition(new ConditionDirectionPair(null, null), new GlobalCoarse(5, 13), null));
		 *
	
		return cap;
	} */
	/*
	private void drawPossibleCollisions(ArrayList<GlobalContinuous> collisionAtPositionList,Graphics g, LevelScene levelScene, int playerIndex) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.RED);

		float xCam = levelScene.cameras.get(playerIndex)[0];
		float yCam = levelScene.cameras.get(playerIndex)[1];

		
		for (GlobalContinuous cap : collisionAtPositionList) {
			
			if(cap != null) {				
				//GlobalContinuous pos = cap.getStaticPosition().toGlobalContinuous();
				GlobalContinuous pos=cap;
				//offset the position -8 in both directions to get top left corner of a block, not the center.
				
				Player currentPlayer = levelScene.getPlayers().get(playerIndex);
				int xOffset =currentPlayer.width* GlobalOptions.resolution_factor;
				int yOffset = currentPlayer.getHeight();
				
				int x = (int) (pos.x -xOffset - xCam) * GlobalOptions.resolution_factor;
				int y = (int) (pos.y -yOffset - yCam) * GlobalOptions.resolution_factor;
				
				g2.drawRect((int) x,
						(int) y, 32, 32);
			}	
		}
	}*/
}

