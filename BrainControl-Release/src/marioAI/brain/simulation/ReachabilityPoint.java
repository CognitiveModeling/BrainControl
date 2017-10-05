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
package marioAI.brain.simulation;



/**
 * Basic class for determining reachability of a field in the static map
 *
 */
public class ReachabilityPoint 
{
	private int age = 0;

	private boolean reachable = false;
	
	//this is redundant stuff, SHOULD be replaced by checking neighboring reachability
//	private boolean reachable_from_above = false;
//	private boolean reachable_from_below = false;
//	private boolean reachable_from_left = false;
//	private boolean reachable_from_right = false;
		
	public ReachabilityPoint(boolean reachable) 
	{
		this.reachable = reachable;
	}
	
	public ReachabilityPoint() 
	{
		this.reachable = false;
	}

	public void increment()
	{
		age++;
		
		if(Math.random() < age/10000.0)	//TODO: this is a parameter!
		{
			reachable=false;
//			reachable_from_above=false;
//			reachable_from_below=false;
//			reachable_from_left=false;
//			reachable_from_right=false;
			
			age=0;
		}		
	}
	
	public boolean isReachable() 
	{
		return reachable;
	}
//	public boolean isReachableFromLeft() 
//	{
//		return reachable_from_left;
//	}	
//	public boolean isReachableFromRight() 
//	{
//		return reachable_from_right;
//	}	
//	public boolean isReachableFromBelow() 
//	{
//		return reachable_from_below;
//	}	
//	public boolean isReachableFromTop() 
//	{
//		return reachable_from_above;
//	}		
	
	public void setReachable(boolean r) 
	{
		if(r)
			age=0;
			
		this.reachable = r;		
	}
//	public void setReachableFromLeft(boolean r) 
//	{
//		this.reachable_from_left = r;
//	}
//	public void setReachableFromRight(boolean r) 
//	{
//		this.reachable_from_right = r;
//	}
//	public void setReachableFromBelow(boolean r) 
//	{
//		this.reachable_from_below = r;
//	}
//	public void setReachableFromTop(boolean r) 
//	{
//		this.reachable_from_above = r;
//	}
	
	public String toString() 
	{
		return ""+reachable;
	}
}
