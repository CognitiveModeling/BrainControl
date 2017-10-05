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
package marioWorld.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import marioWorld.agents.Agent;
//import marioWorld.agents.AgentsPool;
import marioWorld.agents.AgentsPool;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstName_at_idsia_dot_ch Date: May 5, 2009 Time: 9:34:33 PM Package: ch.idsia.utils
 */
public class ParameterContainer {
	protected HashMap<String, String> optionsHashMap = new HashMap<String, String>();
	private static final List<String> allowedOptions = new ArrayList<String>();
	protected static final HashMap<String, String> defaultOptionsHashMap = new HashMap<String, String>() ;
	private String[] allowed = null;

	public ParameterContainer() {

		init();

		InitDefaults();
	}

	private void init() {
		if (allowed == null)
			allowed = new String[] { "-ag",
					// "-agentName",
					// "-attemptsNumber",
					// "-e",
					"-echo", "-ewf", "-fastTCP",
					// "-exitWhenFinished",
					// "-gameViewer",
					// "-gameViewerContinuousUpdates",
					// "-gui",
					"-gv", "-gvc", "-i", "-ld",
					// "-levelDifficulty",
					// "-levelLength",
					// "-levelRandSeed",
					// "-levelType",
					"-ll", "-ls", "-lt", "-m", "-mm", "-maxFPS", "-not",
					// "-matLabFile",
					// "-pauseWorld",
					"-port",
					// "-powerRestoration",
					"-pr", "-pw", "-server", "-ssiw",
					// "-stopSimulationIfWin",
					"-t", "-tc", "-tl",
					// "-toolsConfigurator",
					"-vaot",
					// "-viewAlwaysOnTop",
					// "-viewLocationX",
					// "-viewLocationY",
					"-vis",
					// "-visual",
					"-vlx", "-vly", "-ze", "-zm", "-initialPlayerPositionX", "-initialPlayerPositionY", "-initialGoalX", "-initialGoalY", "-ownLevelName" };
			for(int i = 0; i < allowed.length; i++){
				if(!allowedOptions.contains(allowed[i])){
					allowedOptions.add(allowed[i]);
				}
			}
	}

	public void addParameterValue(String param, String value) {
		if (allowedOptions.contains(param)) {
			assert (optionsHashMap.get(param) == null);
			optionsHashMap.put(param, value);
		} else
			System.err.println("Parameter " + param + " is not valid. Typo?");
	}

	public void setParameterValue(String param, String value) {
		try {
			if (allowedOptions.contains(param)) {
				optionsHashMap.put(param, value);
			} else {
				throw new IllegalArgumentException("Parameter " + param + " is not valid. Typo?");
			}
		} catch (IllegalArgumentException e) {

			System.err.println("Error: Undefined parameter '" + param + " " + value + "'");
			System.err.println(e.getMessage());
			System.err.println("Some defaults might be used instead");
		}
	}

	public String getParameterValue(String param) {
		if (allowedOptions.contains(param)) {
			if (optionsHashMap.get(param) == null) {
				// System.err.println("InfoWarning: Default value '" +
				// defaultOptionsHashMap.get(param) + "' for " + param +
				// " used");
				optionsHashMap.put(param, defaultOptionsHashMap.get(param));
			}
			return optionsHashMap.get(param);
		} else {
			System.err.println("Parameter " + param + " is not valid. Typo?");
			return "";
		}
	}

	public int i(String s) {
		return Integer.parseInt(s);
	}

	public float f(String s) {
		return Float.parseFloat(s);
	}

	public String s(Object i) {
		return String.valueOf(i);
	}

	public String s(Agent a) {
		try {
			if (AgentsPool.getAgentByName(a.getName()) == null)
				AgentsPool.addAgent(a);
			return a.getName();
		} catch (NullPointerException e) {
			System.err.println("ERROR: Agent Not Found");
			return "";
		}
	}

	public Agent a(String s) {
		return AgentsPool.getAgentByName(s);
	}

	public boolean b(String s) {
		return "on".equals(s) || Boolean.valueOf(s);
	}

	public static void InitDefaults() {
		System.out.println("CMDLINEOPT init defaults");
			defaultOptionsHashMap.put("-ag", "HumanKeyboardAgent"); // defaultOptionsHashMap.put("-agentName","NoAgent");
			defaultOptionsHashMap.put("-echo", "off"); // defaultOptionsHashMap.put("-echo","off");
			defaultOptionsHashMap.put("-ewf", "on"); // defaultOptionsHashMap.put("-exitWhenFinished","off");
			defaultOptionsHashMap.put("-fastTCP", "off"); //
			defaultOptionsHashMap.put("-gv", "off"); // defaultOptionsHashMap.put("-gameViewer","off");
			defaultOptionsHashMap.put("-gvc", "off"); // defaultOptionsHashMap.put("-gameViewerContinuousUpdates","off");
			defaultOptionsHashMap.put("-i", "off"); // Invulnerability
			defaultOptionsHashMap.put("-ld", "0"); // defaultOptionsHashMap.put("-levelDifficulty","0");
			defaultOptionsHashMap.put("-ll", "320"); // defaultOptionsHashMap.put("-levelLength","320");
			defaultOptionsHashMap.put("-ls", "0"); // defaultOptionsHashMap.put("-levelRandSeed","1");
			defaultOptionsHashMap.put("-lt", "0"); // defaultOptionsHashMap.put("-levelType","1");
			defaultOptionsHashMap.put("-maxFPS", "off"); // defaultOptionsHashMap.put("-maxFPS","off");
			defaultOptionsHashMap.put("-m", ""); // defaultOptionsHashMap.put("-matLabFile","DefaultMatlabFile");
			defaultOptionsHashMap.put("-mm", "2");
			defaultOptionsHashMap.put("-not", "1"); // defaultOptionsHashMap.put("-attemptsNumber","5");
			defaultOptionsHashMap.put("-pw", "off"); // defaultOptionsHashMap.put("-pauseWorld","off");
			defaultOptionsHashMap.put("-port", "4242"); // defaultOptionsHashMap.put("-port","4242");
			defaultOptionsHashMap.put("-pr", "off"); // defaultOptionsHashMap.put("-powerRestoration","off");
			defaultOptionsHashMap.put("-ssiw", "off"); // defaultOptionsHashMap.put("-stopSimulationIfWin","off");
			defaultOptionsHashMap.put("-server", "off");
			defaultOptionsHashMap.put("-t", "on"); // defaultOptionsHashMap.put("-timer","on");
			defaultOptionsHashMap.put("-tl", "200"); // defaultOptionsHashMap.put("-timer","on");
			defaultOptionsHashMap.put("-tc", "off"); // defaultOptionsHashMap.put("-toolsConfigurator","off");
			defaultOptionsHashMap.put("-vaot", "off"); // defaultOptionsHashMap.put("-viewAlwaysOnTop","off");
			defaultOptionsHashMap.put("-vlx", "0"); // defaultOptionsHashMap.put("-viewLocationX","0");
			defaultOptionsHashMap.put("-vly", "0"); // defaultOptionsHashMap.put("-viewLocationY","0");
			defaultOptionsHashMap.put("-vis", "on"); // defaultOptionsHashMap.put("-visual","on");
			defaultOptionsHashMap.put("-zm", "1");
			defaultOptionsHashMap.put("-ze", "0");
	}

	public static String getDefaultParameterValue(String param) {
		if (allowedOptions.contains(param)) {
			assert (defaultOptionsHashMap.get(param) != null);
			return defaultOptionsHashMap.get(param);
		} else {
			System.err.println("Reques for Default Parameter " + param + " Failed. Typo?");
			return "";
		}
	}
}
