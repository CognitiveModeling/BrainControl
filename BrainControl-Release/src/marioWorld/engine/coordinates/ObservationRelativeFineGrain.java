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
package marioWorld.engine.coordinates;

import marioAI.brain.simulation.LevelSceneAdapter;
import marioWorld.mario.environments.Environment;

/**
 * Encapsulates observation relative discrete coordinates as they are used in heuristics.
 * 
 * @author Niels, Eduard
 * 
 */

public class ObservationRelativeFineGrain {

	public int x, y;

	/**
	 * Default constructor creating the observation's origin.
	 */
	public ObservationRelativeFineGrain() {

	}

	/**
	 * Construct new observation relative fine grain point.
	 */
	public ObservationRelativeFineGrain(int x, int y) 
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * Convert to global continuous coordinates.
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalContinuous toGlobalContinuous(GlobalContinuous playerPosition) 
	{
		return this.toGlobalFineGrain(playerPosition).toGlobalContinuous();
	}

	/**
	 * Conversion to global discrete coarse (grain factor 1).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalCoarse toGlobalCoarse(GlobalContinuous playerPosition) {
		return this.toGlobalContinuous(playerPosition).toGlobalCoarse();
	}

	/**
	 * Convert to global discrete coordinates (with same resolution).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalFineGrain toGlobalFineGrain(GlobalContinuous playerPosition) {

		ObservationRelativeFineGrain discreteVectorFromPlayerToThis = playerPosition.toObservationRelativeFineGrain(playerPosition).getVectorTo(this);

		// Difference vectors are the same in global or observation relative
		// discrete.
		GlobalFineGrain vector = new GlobalFineGrain(discreteVectorFromPlayerToThis.x, discreteVectorFromPlayerToThis.y);
		return playerPosition.toGlobalFineGrain().add(vector);
	}

	/**
	 * Conversion to a lower resolution within the observation.
	 */
	public ObservationRelativeCoarse toObservationRelativeCoarse() 
	{
		return new ObservationRelativeCoarse((int) Math.floor(this.x / (float) CoordinatesUtil.getGrainFactor()), (int) Math.floor(this.y / (float) CoordinatesUtil.getGrainFactor()));
	}

	/**
	 * Check if this is within the observation.
	 * 
	 * @param position
	 *            Used to pinpoint the obervation's center,
	 * @return True if the local coarse coordinates of this are within the limits provided by {@link Environment}.
	 */
	public boolean isWithinObservation(GlobalContinuous position) 
	{
		final int maxX = (Environment.HalfObsWidth * 2 + 1) / CoordinatesUtil.getGrainFactor();
		final int maxY = (Environment.HalfObsHeight * 2 + 1) / CoordinatesUtil.getGrainFactor();

		//test map constraints:
		GlobalCoarse toTest = this.toGlobalCoarse(position);
		
		if(toTest.x < 0 || toTest.x >= LevelSceneAdapter.getTotalLevelWidth() || toTest.y < 0 || toTest.y >= LevelSceneAdapter.getTotalLevelHeight()) 
		{
			return false;
		}
		
		//test observation constraints
		return (this.x >= 0 && this.x < maxX && this.y >=0 && this.y < maxY);
	}

	public ObservationRelativeFineGrain add(ObservationRelativeFineGrain p) 
	{
		return new ObservationRelativeFineGrain(this.x + p.x, this.y + p.y);
	}

	public ObservationRelativeFineGrain subtract(ObservationRelativeFineGrain p) {
		return new ObservationRelativeFineGrain(this.x - p.x, this.y - p.y);
	}

	public ObservationRelativeFineGrain getVectorTo(ObservationRelativeFineGrain target) {
		return new ObservationRelativeFineGrain(target.x - this.x, target.y - this.y);
	}

	public int getSmallestParallelDistance(ObservationRelativeFineGrain p) {
		return Math.min(Math.abs(p.x - this.x), Math.abs(p.y - this.y));
	}

	@Override
	public String toString() {
		return "[ObsRelFineGrain: " + this.x + "," + this.y + "]";
	}

	@Override
	public ObservationRelativeFineGrain clone() {
		return new ObservationRelativeFineGrain(this.x, this.y);
	}
}
