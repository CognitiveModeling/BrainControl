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

import java.util.ArrayList;

import marioAI.goals.ReservoirEvent;
import marioAI.goals.ReservoirEventType;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.LevelScene;

/**
 * @author Fabian
 */

public class ListenerReservoir extends AbstractReservoir 
{	
	protected ArrayList <ReservoirEventType> listeners = new ArrayList <ReservoirEventType>();
		
	public ListenerReservoir(double steepness, double initValue, double avgValue, double minValue, double maxValue, String name, ReservoirType reservoirType, ReservoirEventType... listeners) 
	{
		super(name,reservoirType);
		this.steepness=steepness;
		this.value=initValue;
		this.avgValue=avgValue;
		this.minValue=minValue;
		this.maxValue=maxValue;
		for(int i=0;i<listeners.length;i++)
			this.listeners.add(listeners[i]);
	}
	
	/**
	 * Slowly move reservoir to homestasis over time after it was peaked
	 */
	@Override
	public void onTick(LevelScene levelScene)
	{
		if(value < avgValue) {
			this.add(timeDecayRate);
		} else {
			timeDecayRate = 0;
		}
	}

	/**
	 * Updates the value of the reservoir when a Change object is sent.
	 * TODO: this should be an observer
	 * @param change
	 */
	public void update(ReservoirEvent arg) 
	{			
		//if(arg instanceof ReservoirEvent) 
		{				
			ReservoirEvent event = (ReservoirEvent) arg;			
			
			if(listeners.contains(event.type))		
			{				
				System.out.println("Reservoir " + name + " got event!: " + event.type + " " + event.parameter);
				
				this.add(event.parameter);
			}
		}
	}
	
	/**
	 * Copying a ListenerReservoir (new Instance)
	 */
	@Override
	public AbstractReservoir copy(){
		ReservoirEventType[] rt = new ReservoirEventType[listeners.size()];
		for (int i = 0; i < rt.length; i++) {
			rt[i] = listeners.get(i);
		}

		ListenerReservoir copy = new ListenerReservoir(this.steepness, value, avgValue, minValue, maxValue, name, reservoirType, rt);
		copy.enable(this.isEnabled);
		
		return copy;
	}
	
	
	/**
	 * Move value in direction of minValue 
	 */
	@Override
	public void increaseUrge() {
		
		this.add((minValue-value)*0.7);
	
		this.timeDecayRate = (avgValue - value)*0.01;
	
	}

	@Override
	public void decreaseUrge() {
		this.value = value - ((minValue-value)*0.35);
		if(value < minValue) {
			value = minValue;
		}
		if(value > maxValue) {
			value = maxValue;
		}
		
		
	}
}
