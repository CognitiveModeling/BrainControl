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
package marioAI.reservoirMotivation;

import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.LevelScene;

/**
 * A class for a random reservoir, that can be used to describe the level for a need of a certain behavior
 * (curiosity). Increases its value randomly.
 * 
 * @author Marcel, Jannine, Matthias, Leona
 */

public class RandomReservoir extends AbstractReservoir {

	// probability of a value increase
	final double probabilityFactor = 0.005;
	
	// value by which reservoir is increased
	final double add = 0.4;
	

	public RandomReservoir(double value, String name) {
		super(name,ReservoirType.UNDEFINED);
		this.value = value;
	}
	
	/**
	 * Called each time step. Increases the value of the reservoir with a certain probability.
	 */
	@Override
	public void onTick(LevelScene levelScene) {
		if(Math.random() < probabilityFactor) {
			add(add);
		}
		
	}

	/**
	 * Returns the name of the reservoir.
	 */
	@Override
	public String getName() {
		return name;
	}
	
	
	/**
	 * Updates the value of the reservoir when a Change object is sent.
	 * @param change
	 */
	/*@Override
	public void updateReservoir(Object change) 
	{
		//TODO this is not generic
		
		if(change instanceof ReservoirEvent) 
		{
			ReservoirEvent event = (ReservoirEvent) change;
			
			if(event.type == ReservoirEventType.PLAYER_HEALTH_CHANGE) 
			{
				// increase the value of the reservoir, if health is increased
				add(event.parameter*0.2);
			}
		} 
		
		//OMG
//		else if(change instanceof Double) 
//		{
//			// increase the value of the reservoir by the given value
//			double decrease = ((Double) change).doubleValue();
//			add(-decrease);
//		}
	}*/
	
	@Override
	public AbstractReservoir copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void increaseUrge() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decreaseUrge() {
		// TODO Auto-generated method stub
		
	}
}
