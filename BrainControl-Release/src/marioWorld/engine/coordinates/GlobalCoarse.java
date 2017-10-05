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
 * Encapsulates global discrete coarse (grain Factor 16x16, like a tile) coordinates.
 * 
 * @author Niels, Eduard
 * 
 */

public class GlobalCoarse {

	public int x, y;

	/**
	 * Default constructor creating 0,0 global coarse point.
	 */
	public GlobalCoarse() {

	}

	/**
	 * Construct new global coarse point.
	 */
	public GlobalCoarse(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Convert to global continuous coordinates with the continuous point being at the center of the coarse source tile. Because the center is not unique the
	 * lower right point, (8,8) is used.
	 */
	public GlobalContinuous toGlobalContinuous() {
		return new GlobalContinuous(this.x * CoordinatesUtil.UNITS_PER_TILE + CoordinatesUtil.UNITS_PER_TILE / 2, this.y * CoordinatesUtil.UNITS_PER_TILE
				+ CoordinatesUtil.UNITS_PER_TILE / 2);
	}

	/**
	 * Convert to global discrete fine grain coordinates. If the center is not distinct the lower right fine grain tile is used
	 */
	public GlobalFineGrain toGlobalFineGrain() {
		return this.toGlobalContinuous().toGlobalFineGrain();
	}

	/**
	 * Convert to observation relative coarse coordinates with same resolution, grainFactor 1. Moves the origin for this point from global (0,0) to observation
	 * (0,0).
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public ObservationRelativeCoarse toObservationRelativeCoarse(GlobalContinuous playerPosition) {
		return this.toGlobalContinuous().toObservationRelativeCoarse(playerPosition);
	}

	/**
	 * Convert to observation relative discrete coordinates with higher resolution. The resulting coordinates will be close to the center of the tile pointed to
	 * by this. If the center is not unique, the lower right is used.
	 * 
	 * @param playerPosition
	 *            Reference point needed to pinpoint the observation's center.
	 */
	public ObservationRelativeFineGrain toObservationRelativeFineGrain(GlobalContinuous playerPosition) {
		return this.toObservationRelativeCoarse(playerPosition).toObservationRelativeFineGrain();
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

	public GlobalCoarse add(GlobalCoarse p) {
		return new GlobalCoarse(this.x + p.x, this.y + p.y);
	}

	public GlobalCoarse subtract(GlobalCoarse p) {
		return new GlobalCoarse(this.x - p.x, this.y - p.y);
	}

	public GlobalCoarse getVectorTo(GlobalCoarse target) {
		return new GlobalCoarse(target.x - this.x, target.y - this.y);
	}

	public int getSmallestParallelDistance(GlobalCoarse p) {
		return Math.min(Math.abs(p.x - this.x), Math.abs(p.y - this.y));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		GlobalContinuous globalContinuous = this.toGlobalContinuous();
		return "[GlobalCoarse: " + this.x + "," + this.y + "]" + ", [GlobalContinuous: " + globalContinuous.x + "," + globalContinuous.y + "]";
	}

	@Override
	public GlobalCoarse clone() {
		return new GlobalCoarse(this.x, this.y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GlobalCoarse other = (GlobalCoarse) obj;
		return (this.x == other.x && this.y == other.y);
	}

	public double distanceSq(GlobalCoarse p) {
		double a = p.x - this.x;
		double b = p.y - this.y;
		return a * a + b * b;
	}

	public double distance(GlobalCoarse p) {
		return Math.sqrt(distanceSq(p));
	}
}
