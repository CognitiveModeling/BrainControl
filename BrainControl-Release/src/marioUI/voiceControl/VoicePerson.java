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
package marioUI.voiceControl;

import java.util.ArrayList;

import marioWorld.agents.Agent;
import marioWorld.engine.Logging;

/**
 * Class representing participant of voice communication
 * 
 * TODO: This is at the basis for individual speech capabilities. Should be extended somewhen.
 * Actually, this should implement individual speech capabilities per individual grammar files rather than hard coded sentences.
 * 
 * @author smihael, fabian
 *
 */
public class VoicePerson 
{
	public String voice;
	/*public String introductionSentence;
	public String noRecgResultSentence;
	public String whatDoYouMeanSentence;*/
	
	public VoicePerson(String setVoice/*, String setIntroductionSentence, String setNoRecgResultSentence, String setWhatDoYouMeanSentence*/) 
	{
		voice = setVoice;
		/*introductionSentence = setIntroductionSentence;
		noRecgResultSentence = setNoRecgResultSentence;
		whatDoYouMeanSentence = setWhatDoYouMeanSentence;*/
	}
	

	/**
	 * Decides between Players, based on reference to whom appears first in tags
	 *  (for CLARK JAY KNOWLEDGE_TRANSFER will select Clark as an agent because Jay is grammatical object)
	 * 
	 * @param tags
	 *            input text
	 * @param agents
	 *            list of agents
	 * @return pointer to appropriate agent
	 */
	public static Agent agentSelector(String tags, ArrayList<Agent> agents) 
	{	
		Agent agent;

		if ( agents.size() > 1 ) 
		{
			for (String tag : tags.split(" ")) 
			{
				for(int i=0;i<agents.size();i++)
				{
					if(tag.toUpperCase().contains(agents.get(i).getName().toUpperCase()))
					{
						Logging.logInfo("VoicePersons", "\""+tags+"\" passed to "+agents.get(i).getName());
						return agents.get(i);						
					}
				}
			}
		}

		agent = agents.get(0);
		Logging.logInfo("VoicePersons", "No information about agent given in "+"\""+tags+"\". Default is " + agents.get(0).getName() + "'.");

		return agent;
	}
	
}
