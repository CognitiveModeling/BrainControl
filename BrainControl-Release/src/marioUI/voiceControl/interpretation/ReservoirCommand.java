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
package marioUI.voiceControl.interpretation;

import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirAction;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirParameter;
import marioAI.reservoirMotivation.ReservoirUtilities.ReservoirType;

/**
 * VoiceCommand representing a Command to change reservoirs
 * 
 * @author chantal
 * 
 */

public class ReservoirCommand extends VoiceCommand {

	/*
	 * stores the type of reservoir that is going to be changed
	 */
	private ReservoirType reservoirType;

	/*
	 * stores the type of action executed on a reservoir
	 */
	private ReservoirAction reservoirAction;

	/*
	 * stores the type of action executed on a reservoir
	 */
	private ReservoirParameter reservoirParameter;
	/*
	 * stores if the reservoir should be the only one active
	 */
	private boolean exlusiveReservoir;
	
	/*
	 * constructor that sets reservoirType and reservoirAction
	 */
	public ReservoirCommand(ReservoirType reservoirType, ReservoirAction reservoirAction, ReservoirParameter reservoirParameter) 
	{
		super(CommandType.RESERVOIR);
		this.reservoirType = reservoirType;
		this.reservoirAction = reservoirAction;
		this.reservoirParameter = reservoirParameter;
		this.exlusiveReservoir = false;
	}
	public ReservoirCommand(ReservoirType reservoirType, ReservoirAction reservoirAction, ReservoirParameter reservoirParameter, boolean exclusiveReservoir) 
	{
		super(CommandType.RESERVOIR);
		this.reservoirType = reservoirType;
		this.reservoirAction = reservoirAction;
		this.reservoirParameter = reservoirParameter;
		this.exlusiveReservoir = exclusiveReservoir;
	}
	
	public boolean isExlusiveReservoir() {
		return exlusiveReservoir;
	}
	/**
	 * get method
	 * @return
	 */
	public ReservoirType getReservoirType() {
		return this.reservoirType;
	}

	public ReservoirParameter getReservoirParameter() {
		return this.reservoirParameter;
	}	
	
	/**
	 * get method
	 * @return
	 */
	public ReservoirAction getReservoirAction() {
		return this.reservoirAction;
	}

	
	public String toString() {
		return "Command Type: " + this.getType().toString() + ", "
				+ this.reservoirAction.toString() + " "
				+ this.reservoirType.toString();
	}

}
