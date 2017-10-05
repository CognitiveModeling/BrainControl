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

import marioWorld.mario.environments.Environment;

/**
 * Encapsulates global discrete fine grain coordinates (rarely used).
 * 
 * @author Niels, Eduard
 * 
 */

public class GlobalFineGrain {

	public int x, y;

	/**
	 * Default constructor creating 0,0 global discrete fine grain point.
	 */
	public GlobalFineGrain() {

	}

	/**
	 * Construct new global fine grain point.
	 */
	public GlobalFineGrain(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Convert to global continuous coordinates with the continuous point being at the center of the source tile. If the center is not unique the lower right
	 * one is used (grainFactor 1: +8,+8, 2: +4,+4 etc...)
	 */
	public GlobalContinuous toGlobalContinuous() {
		int grainSize = CoordinatesUtil.UNITS_PER_TILE / CoordinatesUtil.getGrainFactor();

		return new GlobalContinuous(this.x * grainSize + grainSize / 2.0f, this.y * grainSize + grainSize / 2.0f);
	}

	/**
	 * Convert to global discrete coarse (grain factor 1).
	 */
	public GlobalCoarse toGlobalCoarse() {
		return new GlobalCoarse((int) Math.floor(this.x / (float) CoordinatesUtil.getGrainFactor()), (int) Math.floor(this.y
				/ (float) CoordinatesUtil.getGrainFactor()));
	}

	/**
	 * Conversion to observation relative discrete coarse (grain factor 1).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public ObservationRelativeCoarse toObservationRelativeCoarse(GlobalContinuous playerPosition) {
		return this.toObservationRelativeFineGrain(playerPosition).toObservationRelativeCoarse();
	}

	/**
	 * Convert to observation relative discrete coordinates (with same resolution).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public ObservationRelativeFineGrain toObservationRelativeFineGrain(GlobalContinuous playerPosition) {

		GlobalFineGrain discretePlayerPosition = playerPosition.toGlobalFineGrain();

		GlobalFineGrain observationRelativeDiscretePlayer = GlobalContinuous.globalContinuousPlayerToObservationRelativeContinuousPlayer(playerPosition)
				.toGlobalFineGrain();

		GlobalFineGrain thisObservationRelative = this.subtract(discretePlayerPosition).add(observationRelativeDiscretePlayer);

		return new ObservationRelativeFineGrain(thisObservationRelative.x, thisObservationRelative.y);
	}

	/**
	 * Check if this is within the observation.
	 * 
	 * @param playerPosition
	 *            Used to pinpoint the obervation's center,
	 * @return True if the local coarse coordinates of this are within the limits provided by {@link Environment}.
	 */
	public boolean isWithinObservation(GlobalContinuous playerPosition) 
	{
		int x = this.toObservationRelativeCoarse(playerPosition).x;
		int y = this.toObservationRelativeCoarse(playerPosition).y;
		int xLimit = Environment.HalfObsWidth * 2 + 1;
		int yLimit = Environment.HalfObsHeight * 2 + 1;
		return (x < xLimit && x >= 0) && (y < yLimit && y >= 0);
	}

	public GlobalFineGrain add(GlobalFineGrain p) {
		return new GlobalFineGrain(this.x + p.x, this.y + p.y);
	}

	public GlobalFineGrain subtract(GlobalFineGrain p) {
		return new GlobalFineGrain(this.x - p.x, this.y - p.y);
	}

	public GlobalFineGrain getVectorTo(GlobalFineGrain target) {
		return new GlobalFineGrain(target.x - this.x, target.y - this.y);
	}

	public int getSmallestParallelDistance(GlobalFineGrain p) {
		return Math.min(Math.abs(p.x - this.x), Math.abs(p.y - this.y));
	}

	@Override
	public String toString() {
		return "[GlobalFineGrain: " + this.x + "," + this.y + "]";
	}

	@Override
	public GlobalFineGrain clone() {
		return new GlobalFineGrain(this.x, this.y);
	}
}
