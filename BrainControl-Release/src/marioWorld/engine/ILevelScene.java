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
package marioWorld.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import marioAI.collision.CollisionAtPosition;
import marioAI.movement.PlayerActions;
import marioWorld.engine.coordinates.GlobalContinuous;

/**
 * defines common functionality of brain.simulation.SimulatedLevelScene and ...robinbaumgarten.astar.LevelScene. Enables easy switching between which of those
 * two simulations is used. Only needed as temporary solution, until robin baumgarten's code is dropped.
 * 
 * @author Stephan
 * 
 */
public interface ILevelScene extends Cloneable {

	/**
	 * With which static object mario collides. TODO: do this also for moving objects. Return value is only implemented in the simulation, not in the game
	 * engine.
	 */
	public List<CollisionAtPosition> tick(PlayerActions action);

	public Object clone() throws CloneNotSupportedException;

	// unfortunately, Cloneable does not define clone(), rather it INDICATES,
	// that a field-for-field copy is ok in Object.clone(). Bad java software
	// concept.

	public GlobalContinuous getPlanningPlayerPos();
	
	public void init();

	/** used to synchronize engine world and simulated world */
	public boolean setLevelScene(byte[][] data);

	public boolean setEnemies(float[] enemies);

	public void setPlayerPosition(float x, float y);

	public void setPlayerVelocity(float xa, float ya);
}
