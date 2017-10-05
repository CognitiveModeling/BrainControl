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
package marioUI.speechSynthesis;

import java.util.Observable;
import java.util.Observer;

import marioUI.voiceControl.VoiceControl;
import marioUI.voiceControl.interpretation.VoiceKnowledgeManager;

/**
 * @author fabian
 * */
public class VoiceResponseEventBus implements Observer
{
	//TODO: this class should be used instead of the "while(true)" loop in voice control...
	//TODO: this class should user grammars to handle all speech output events!
		
	private VoiceControl voiceControl; 
	
	private void createResponse(VoiceResponse response)
	{
		String tagset=response.tagset;
		if(!tagset.equals(""))
			tagset+=" ";
		
		/*if (response.type.equals(VoiceResponseType.GOAL_CHAIN)){
			System.out.println("Creating goal plan response for tagset " + tagset);
			String reply = //Woher Goals?
		}*/
		tagset+="RESPONSETYPE("+response.type.name() + ")";
		
		System.out.println("Creating response for tagset " + tagset);
		
		String reply = VoiceKnowledgeManager.sentenceGenerator.getSentence(tagset);
				
		if(!reply.equals(""))
		{
			voiceControl.display(reply, response.playerIndex);
		}
		else
		{
			voiceControl.display("Please extend my response grammars by the tagset " + tagset, response.playerIndex);
		}
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		if(arg1 instanceof VoiceResponse)			
			createResponse((VoiceResponse) arg1);
	}
	
	public VoiceResponseEventBus(VoiceControl voiceControl)
	{
		this.voiceControl=voiceControl;
		voiceControl.addObserver(this);
	}

	public VoiceControl getVoiceControl() {
		// TODO Auto-generated method stub
		return this.voiceControl;
	}
}
