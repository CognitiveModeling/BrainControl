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

import marioAI.goals.ReservoirEventType;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.LevelScene;

/**
 * @author Fabian
 */

public class RandomListenerReservoir extends ListenerReservoir 
{
	private double probability;
	
	private double addValue;
		
	public RandomListenerReservoir(double probability, double addValue, double steepness, double initValue, double avgValue, double minValue, double maxValue, String name, ReservoirType reservoirType, ReservoirEventType... listeners) 
	{		
		super(steepness, initValue, avgValue, minValue, maxValue, name, reservoirType, listeners);
		
		this.probability=probability;
		this.addValue=addValue;
	}
		
	@Override
	public void onTick(LevelScene levelScene) 
	{
		super.onTick(levelScene);
		if(Math.random() < probability) 
		{
			add(addValue);
		}		
		
		//System.out.println("VALUE IS " + this.value);
	}

	
	/**
	 * Copying a RandomListenerReservoir (new Instance)
	 */
	@Override
	public AbstractReservoir copy(){
		super.copy();
		ReservoirEventType[] rt = new ReservoirEventType[listeners.size()];
		for (int i = 0; i < rt.length; i++) {
			rt[i] = listeners.get(i);
		}
		RandomListenerReservoir copy = new RandomListenerReservoir(probability, addValue, steepness, value, avgValue, minValue, maxValue, name, reservoirType, rt);
		copy.enable(this.isEnabled);
		return copy;
	
	}
}
