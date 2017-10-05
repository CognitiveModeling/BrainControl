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

import marioWorld.engine.GlobalOptions;
import marioWorld.mario.environments.Environment;

/**
 * Encapsulates global continuous coordinates (with even finer resolution than per pixel), the values as they are given by the engine. All conversions cause
 * precision loss.
 * 
 * @author Niels, Eduard
 * 
 */

public class GlobalContinuous {

	public float x, y;

	/**
	 * Default constructor creating 0,0 global continuous point.
	 */
	public GlobalContinuous() {

	}

	/**
	 * Construct new global continuous point.
	 */
	public GlobalContinuous(float x, float y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Conversion to global coarse with grain factor 1.
	 */
	public GlobalCoarse toGlobalCoarse() 
	{
		return new GlobalCoarse((int) Math.floor(this.x / CoordinatesUtil.UNITS_PER_TILE), (int) Math.floor(this.y / CoordinatesUtil.UNITS_PER_TILE));
	}

	/**
	 * Conversion to global discrete fine grain.
	 */
	public GlobalFineGrain toGlobalFineGrain() {
		double grainSize = CoordinatesUtil.UNITS_PER_TILE / CoordinatesUtil.getGrainFactor();

		return new GlobalFineGrain((int) Math.floor(this.x / grainSize), (int) Math.floor(this.y / grainSize));
	}

	/**
	 * Conversion to observation relative discrete coarse (grain factor 1).
	 * 
	 */
	public ObservationRelativeCoarse toObservationRelativeCoarse(GlobalContinuous playerPosition) {
		// First Mario is translated to observation relative continuous.
		GlobalContinuous observationRelativePlayer = globalContinuousPlayerToObservationRelativeContinuousPlayer(playerPosition);
		// The vector to this is the vector of observation relative origin to
		// Mario to this.
		GlobalContinuous thisObservationRelative = this.subtract(playerPosition).add(observationRelativePlayer);

		return new ObservationRelativeCoarse((int) Math.floor(thisObservationRelative.x / CoordinatesUtil.UNITS_PER_TILE),
				(int) Math.floor(thisObservationRelative.y / CoordinatesUtil.UNITS_PER_TILE));
	}

	/**
	 * Conversion to coordinates which are relative to the observation's upper left corner (fine grain).
	 * 
	 * @param playerPosition
	 *            reference point we need to pinpoint the observation's center
	 */
	public ObservationRelativeFineGrain toObservationRelativeFineGrain(GlobalContinuous playerPosition) {

		GlobalContinuous observationRelativePlayer = globalContinuousPlayerToObservationRelativeContinuousPlayer(playerPosition);
		GlobalContinuous thisObservationRelative = this.subtract(playerPosition).add(observationRelativePlayer);
		// Same method as in toObservationRelativeCoarse.
		double grainSize = CoordinatesUtil.UNITS_PER_TILE / CoordinatesUtil.getGrainFactor();
		return new ObservationRelativeFineGrain((int) Math.floor(thisObservationRelative.x / grainSize),
				(int) Math.floor(thisObservationRelative.y / grainSize));
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

	public GlobalContinuous add(GlobalContinuous p) {
		return new GlobalContinuous(this.x + p.x, this.y + p.y);
	}

	public GlobalContinuous subtract(GlobalContinuous p) {
		return new GlobalContinuous(this.x - p.x, this.y - p.y);
	}

	public GlobalContinuous getVectorTo(GlobalContinuous target) {
		return new GlobalContinuous(target.x - this.x, target.y - this.y);
	}

	public double distance(GlobalContinuous p) {
		return Math.sqrt(distanceSq(p));
	}

	public double distanceSq(GlobalContinuous p) {
		double a = p.x - this.x;
		double b = p.y - this.y;
		return a * a + b * b;
	}

	public float getSmallestParallelDistance(GlobalContinuous p) {
		return Math.min(Math.abs(p.x - this.x), Math.abs(p.y - this.y));
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	@Override
	public String toString() {
		GlobalCoarse globalCoarse = this.toGlobalCoarse();
		return "[GlobalContinuous: " + this.x + "," + this.y + "]" + ", [GlobalCoarse: " + globalCoarse.x + "," + globalCoarse.y + "]";
	}

	@Override
	public GlobalContinuous clone() {
		return new GlobalContinuous(this.x, this.y);
	}

	public static GlobalContinuous mean(GlobalContinuous pos1, GlobalContinuous pos2) {
		return new GlobalContinuous(0.5f * (pos1.x + pos2.x), 0.5f * (pos1.y + pos2.y));
	}

	/**
	 * Transform a global continuous Mario position to Mario's continuous coordinates relative to the observation.
	 */
	public static GlobalContinuous globalContinuousPlayerToObservationRelativeContinuousPlayer(GlobalContinuous playerPosition) {

		float xShift = playerPosition.x % CoordinatesUtil.UNITS_PER_TILE;
		float yShift = playerPosition.y % CoordinatesUtil.UNITS_PER_TILE;

		xShift = xShift < 0 ? xShift + CoordinatesUtil.UNITS_PER_TILE : xShift;
		yShift = yShift < 0 ? yShift + CoordinatesUtil.UNITS_PER_TILE : yShift;

		// The first product for each coordinate returns the pixel at the top
		// left of the needed tile, we need to add his offset from the tile's
		// corner.
		return new GlobalContinuous(CoordinatesUtil.PLAYER_OBSERVATION_TILE_POS_X * CoordinatesUtil.UNITS_PER_TILE + xShift,
				CoordinatesUtil.PLAYER_OBSERVATION_TILE_POS_Y * CoordinatesUtil.UNITS_PER_TILE + yShift);
	}
	
	
	}
