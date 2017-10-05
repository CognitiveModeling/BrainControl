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

import marioAI.agents.CAEAgent;
import marioAI.brain.Brain;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;
import marioWorld.engine.Tickable;
import marioWorld.utils.ResetStaticInterface;

/*
 * @author Fabian et al.
 * */
public abstract class AbstractReservoir implements Tickable, Comparable<AbstractReservoir>, ResetStaticInterface
{	
	public static int reservoirCounter = 0;
	
	// minimal value, a reservoir can adopt
	public double minValue = 0;

	// maximal value, a reservoir can adopt
	public double maxValue = 1;

	// how much of the reservoir will be cleared when calling the reset method
	protected double avgValue = 0.5;
	
	// steepness of the output function
	protected double steepness = 0.5;

	// the reservoirs name
	protected String name;

	// contains the type of the reservoir:
	// CURIOSITY, HUNGER, HAPPY, FEAR or UNDEFINED
	protected ReservoirType reservoirType;
	
	// reservoir name is written bold when it is true
	public boolean bold = false;

	// current value of a reservoir
	protected double value = avgValue;
	
	protected boolean isEnabled = true;
	protected double timeDecayRate = 0;

	public AbstractReservoir(String name, ReservoirType reservoirType) 
	{
		classesWithStaticStuff.add(this.getClass());
		
		this.reservoirType=reservoirType;
		AbstractReservoir.reservoirCounter++;
		this.name = name;	
	}
	
	public void setValue(double value){
		this.value = value;
	}
	/*public void multiply(double mult) {
		value *= mult;
		// watch out, not to overfill a reservoir
		if (value > maxValue)
			value = maxValue;
		if (value < minValue)
			value = minValue;
	}*/
	
	/*
	 * adds a given value to the reservoir
	 */
	public void add(double add) {
		value += add;

		// watch out, not to overfill a reservoir
		if (value > maxValue)
			value = maxValue;
		if (value < minValue)
			value = minValue;
	}

	/*
	 * clears a constant value (= diminishingValue) from a reservoir
	 */
	/*public void reset() 
	{
		value=avgValue;
		if (value < minValue)
			value = minValue;
	}*/

	public double getValue() 
	{
		//System.out.println(name + " reservoir state: " + value + " => urge: " + 1.0 / (1.0 + Math.exp(- steepness * (value - avgValue))));
		
		if(!isEnabled)
			return 0.0;
		
		return 1.0 / (1.0 + Math.exp(- steepness * (value - avgValue)));
	}

	public double getActualValue(){
		return value;
	}
	
	public String getName() {
		return name;
	}

	public double minValue() {
		return minValue;
	}

	public double maxValue() {
		return maxValue;
	}

	/**
	 * resets to minValue
	 */
	/*public void setToMin() 
	{
		if(steepness>0)
			value = minValue;
		else
			value = maxValue;
	}

	public void setToMax() 
	{
		if(steepness>0)
			value = maxValue;
		else
			value = minValue;
	}*/

	public void enable(boolean set_enabled)
	{
		//System.out.println(this + " enabled: " + set_enabled);
		this.isEnabled=set_enabled;
	}
	
	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	/**
	 * is called when the concrete reservoir is active: the name of the
	 * reservoir is drawed bold in GraphPanel
	 */
	public void setBold() {
		this.bold = true;
	}

	/**
	 * is called when a different reservoir becomes active
	 */
	public void undoBold() {
		this.bold = false;
	}
	
	/**
	 * when Mario interacts with his environment, he might interact with an
	 * object, which influences his emotions (reservoir): the corresponding
	 * effect will be sent to the reservoirs which have to be updated
	 * 
	 * example: Mario "interacts" with a Luko. This effects will be sent to
	 * fear reservoir - his fear increases, and to happy reservoir - his
	 * happiness decreases
	 * 
	 * @param change
	 */
	//public abstract void updateReservoir(Object change);

	/* only for FearReservoir - sends the brain to the FearReservoir */
	public void setKnowledge(Brain brain) {
	}

	/* only for FearReservoir - sends the agent to the FearReservoir */
	public void setAgent(CAEAgent agent) {
	}

	public ReservoirType getReservoirType() {
		return reservoirType;
	}

	public static void deleteStaticAttributes() {
		reservoirCounter = 0;
	}
	
	public static void resetStaticAttributes() {
		// TODO Auto-generated method stub
		
	}	
	
	@Override
	public int compareTo(AbstractReservoir o) {
		 //two different types of reservoirs
		if(this.getReservoirType()!=o.reservoirType)
			return this.reservoirType.ordinal() - o.reservoirType.ordinal();
		 //the following should never happen -- unless you write to classes which share the same reservoirType
		if(this.getClass()!=o.getClass())
			return this.hashCode()-o.hashCode();
		//two different instances of the same class
		if(this!=o)
			return this.hashCode()-o.hashCode();
		//same instance
		return 0;
	}

	
	public abstract AbstractReservoir copy();
	
	//Create reservoir specific function to calculate the value of an reservoir for a single spike input i.e. collect energy
	public abstract void increaseUrge();
	
	public abstract void decreaseUrge();

	/*public void setValue(double d) {
		this.value = d;		
	}*/
	

}
