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
 * Encapsulates observation relative discrete coordinates as they are used in the observation, grain factor = 1.
 * 
 * @author Niels, Eduard
 * 
 */

public class ObservationRelativeCoarse {

	public int x, y;

	/**
	 * Default constructor creating the observation origin.
	 */
	public ObservationRelativeCoarse() {

	}

	/**
	 * Construct new observation relative discrete point.
	 */
	public ObservationRelativeCoarse(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Convert to global continuous coordinates.
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalContinuous toGlobalContinuous(GlobalContinuous playerPosition) {
		return this.toGlobalCoarse(playerPosition).toGlobalContinuous();
	}

	/**
	 * Convert to global discrete coordinates (with same resolution).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalCoarse toGlobalCoarse(GlobalContinuous playerPosition) {

		ObservationRelativeCoarse discreteVectorFromPlayerToThis = playerPosition.toObservationRelativeCoarse(playerPosition).getVectorTo(this);

		// Difference vectors are the same in global or observation relative
		// discrete.
		GlobalCoarse vector = new GlobalCoarse(discreteVectorFromPlayerToThis.x, discreteVectorFromPlayerToThis.y);
		return playerPosition.toGlobalCoarse().add(vector);
	}

	/**
	 * Convert to global discrete fine grain.
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public GlobalFineGrain toGlobalFineGrain(GlobalContinuous playerPosition) {
		return this.toObservationRelativeFineGrain().toGlobalFineGrain(playerPosition);
	}

	/**
	 * Convert to observation relative discrete fine grain.
	 */
	public ObservationRelativeFineGrain toObservationRelativeFineGrain() {
		return this.toGlobalContinuous(new GlobalContinuous(0, 0)).toObservationRelativeFineGrain(new GlobalContinuous(0, 0));
	}

	/**
	 * Check if this is within the observation.
	 * 
	 * @param marioPosition
	 *            Used to pinpoint the obervation's center,
	 * @return True if the local coarse coordinates of this are within the limits provided by {@link Environment}.
	 */
	public boolean isWithinObservation() 
	{
		int xLimit = Environment.HalfObsWidth * 2 + 1;
		int yLimit = Environment.HalfObsHeight * 2 + 1;
		return (x < xLimit && x >= 0) && (y < yLimit && y >= 0);
	}

	public ObservationRelativeCoarse add(ObservationRelativeCoarse p) {
		return new ObservationRelativeCoarse(this.x + p.x, this.y + p.y);
	}

	public ObservationRelativeCoarse subtract(ObservationRelativeCoarse p) {
		return new ObservationRelativeCoarse(this.x - p.x, this.y - p.y);
	}

	public ObservationRelativeCoarse getVectorTo(ObservationRelativeCoarse target) {
		return new ObservationRelativeCoarse(target.x - this.x, target.y - this.y);
	}

	public int getSmallestParallelDistance(ObservationRelativeCoarse p) {
		return Math.min(Math.abs(p.x - this.x), Math.abs(p.y - this.y));
	}

	@Override
	public String toString() {
		return "[ObsRelCoarse: " + this.x + "," + this.y + "]";
	}

	@Override
	public ObservationRelativeCoarse clone() {
		return new ObservationRelativeCoarse(this.x, this.y);
	}
	public double distanceSq(ObservationRelativeCoarse p) {
		double a = p.x - this.x;
		double b = p.y - this.y;
		return a * a + b * b;
	}

	public double distance(ObservationRelativeCoarse p) {
		return Math.sqrt(distanceSq(p));
	}
}

