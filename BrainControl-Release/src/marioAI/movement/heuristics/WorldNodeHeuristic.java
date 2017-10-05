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
package marioAI.movement.heuristics;


import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import marioAI.movement.WorldGoalTester;
import marioAI.movement.WorldNode;
import marioAI.movement.heuristics.FineGrainPotentialField.HeuristicParameters;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.coordinates.ObservationRelativeFineGrain;

/**
 * Interface of WorldNode heuristics
 */

public abstract class WorldNodeHeuristic {

	
	/** constants */
	public static final Double MAX_VALUE = Double.POSITIVE_INFINITY;

	/** fields */
	protected final WorldGoalTester goalTester;
	protected final GlobalContinuous initialPlayerPosition;
	protected PlayerWorldObject[][] observation;
	/**
	 * for each point: distance to the goal in continuous coordinates, along a non-blocking path.
	 */
	protected double[][] distanceMap;
	protected int lengthX;
	protected int lengthY;

	/** abstract methods */
	public abstract double heuristic(WorldNode worldNode);

	public abstract Point cellCoordinates(WorldNode worldNode);
	

	public abstract double[][] getMap();

	protected abstract double[][] generateMap(List<ObservationRelativeFineGrain> goalPositions);
	
	public abstract HeuristicParameters[][][] getHeuristicMap();

	/**
	 * constructor
	 * 
	 * @param observation
	 * @param goalPosition
	 */
	public WorldNodeHeuristic(PlayerWorldObject[][] observation, WorldGoalTester goalTester, GlobalContinuous playerPosition) {
		this.observation = observation;
		this.lengthX = observation[0].length;
		this.lengthY = observation.length;
		this.goalTester = goalTester;
		this.initialPlayerPosition = playerPosition;
		List<ObservationRelativeFineGrain> playerPositionList = new LinkedList<>();
		playerPositionList.add(goalTester.getGoalPos().toObservationRelativeFineGrain(playerPosition));
		this.distanceMap = this.generateMap(playerPositionList);
	}

	protected WorldNodeHeuristic(WorldGoalTester goalTester, GlobalContinuous playerPosition) {
		this.goalTester = goalTester;
		this.initialPlayerPosition = playerPosition;
	}

	protected WorldNodeHeuristic() {
		this.goalTester = null;
		this.initialPlayerPosition = null;
	}
}
