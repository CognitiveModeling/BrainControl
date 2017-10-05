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
package marioWorld.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.LinkedHashMap;
import java.util.Set;

//import marioWorld.agents.wox.Easy;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch Date: May 9, 2009 Time: 8:28:06 PM Package: ch.idsia.ai.agents
 */
public class AgentsPool {
	private static Agent currentAgent = null;

	public static void addAgent(Agent agent) {
		agentsHashMap.put(agent.getName(), agent);
	}

	/*public static void addAgent(String agentWOXName) throws IllegalFormatException {
		addAgent(load(agentWOXName));
	}*/

	/*public static Agent load(String name) {
		Agent agent;
		try {
			agent = (Agent) Class.forName(name).newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println(name + " is not a class name; trying to load a wox definition with that name.");
			agent = (Agent) Easy.load(name);
		} catch (Exception e) {
			e.printStackTrace();
			agent = null;
			System.exit(1);
		}
		return agent;
	}*/

	public static Collection<Agent> getAgentsCollection() {
		return agentsHashMap.values();
	}

	public static Set<String> getAgentsNames() {
		return AgentsPool.agentsHashMap.keySet();
	}

	public static Agent getAgentByName(String agentName) {
		// There is only one case possible;
		Agent ret = AgentsPool.agentsHashMap.get(agentName);
		if (ret == null)
			ret = AgentsPool.agentsHashMap.get(agentName.split(":")[0]);
		return ret;
	}

	public static Agent getCurrentAgent() {
		return currentAgent;
	}

	public static void setCurrentAgent(Agent agent) {
		currentAgent = agent;
	}

	static HashMap<String, Agent> agentsHashMap = new LinkedHashMap<String, Agent>();
}
