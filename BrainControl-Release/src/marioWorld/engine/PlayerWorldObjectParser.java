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
package marioWorld.engine;

import static marioWorld.engine.PlayerWorldObject.CENTER_DIRT;
import static marioWorld.engine.PlayerWorldObject.ENERGY_FRONT;
//import static marioWorld.engine.PlayerWorldObject.LUKO;
import static marioWorld.engine.PlayerWorldObject.GREEN_VIRUS;
//import static marioWorld.engine.PlayerWorldObject.GREEN_KOOPA;
//import static marioWorld.engine.PlayerWorldObject.GREEN_VIRUS;
import static marioWorld.engine.PlayerWorldObject.GRUMPY;
import static marioWorld.engine.PlayerWorldObject.NONE;
import static marioWorld.engine.PlayerWorldObject.PLAYER;
//import static marioWorld.engine.PlayerWorldObject.RED_KOOPA;
import static marioWorld.engine.PlayerWorldObject.RED_VIRUS;
import static marioWorld.engine.PlayerWorldObject.GRUMPY_STILL;
import static marioWorld.engine.PlayerWorldObject.STONE_BLOCK;

import java.util.HashMap;

import marioAI.util.GeneralUtil;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.utils.ResetStaticInterface;

/**
 * Helper class which parses a description of an array of MarioWorldObjects (and observation). It also provides functions to convert an array of
 * MarioWorldObjects into a string representation. Note that it will not convert all MarioWorldObjects because only a subset of them is actually relevant to the
 * purpose of the class. Also note that the mapping is not bijective. Feel free to add new entries.
 * 
 * @author sebastian
 * 
 */
public class PlayerWorldObjectParser implements ResetStaticInterface {
	private PlayerWorldObjectParser() {
		classesWithStaticStuff.add(this.getClass());

	}

	/**
	 * Bindings for static mario world objects.
	 */
	private static HashMap<Character, PlayerWorldObject> staticBindings = new HashMap<Character, PlayerWorldObject>();
	private static HashMap<PlayerWorldObject, Character> staticReverse = new HashMap<PlayerWorldObject, Character>();

	/**
	 * Bindings for dynamic (i.e., non static) mario world objects.
	 */
	private static HashMap<Character, PlayerWorldObject> dynamicBindings = new HashMap<Character, PlayerWorldObject>();
	private static HashMap<PlayerWorldObject, Character> dynamicReverse = new HashMap<PlayerWorldObject, Character>();

	static {
		addStatic(' ', NONE);
		addStatic('#', STONE_BLOCK);
		addStatic('C', ENERGY_FRONT);
		addStatic('|', CENTER_DIRT);

		addDynamic(' ', NONE);

		// to model the environment:
		addDynamic('#', NONE);

		// proxy for mario
		addDynamic('M', PLAYER);

		//addDynamic('G', LUKO);
		addDynamic('G', GREEN_VIRUS);
		//addDynamic('R', RED_KOOPA);
		//addDynamic('K', GREEN_KOOPA);
		addDynamic('R', RED_VIRUS);
		//addDynamic('K', GREEN_VIRUS);
		addDynamic('K', GRUMPY);
		addDynamic('S', GRUMPY_STILL);
	}

	private static void addStatic(char c, PlayerWorldObject o) {
		staticBindings.put(c, o);
		if (!staticReverse.containsKey(o))
			staticReverse.put(o, c);
	}

	private static void addDynamic(char c, PlayerWorldObject o) {
		dynamicBindings.put(c, o);
		if (!dynamicReverse.containsKey(o))
			dynamicReverse.put(o, c);
	}

	/**
	 * Dumps a given array of MarioWorldObjects to a string array. It will treat the observation as dynamic but will try to resolve those it did not recognize
	 * as statics.
	 * 
	 * @param observation
	 * @return
	 */
	public static String[] dumpToString(PlayerWorldObject[][] observation) {
		String[] dump = new String[observation.length];

		generalize(GeneralUtil.clone2DPlayerWorldObjectArray(observation));

		for (int i = 0; i < observation.length; i++) {
			dump[i] = dumpLine(observation[i]);
		}

		return dump;
	}

	/**
	 * Dumps a line, treating everything as dynamic and only resorting to static if there is no fitting dynamic binding available.
	 * 
	 * @param obs
	 * @return
	 */
	private static String dumpLine(PlayerWorldObject[] obs) {
		StringBuffer sb = new StringBuffer(obs.length);

		for (int i = 0; i < obs.length; i++) {
			PlayerWorldObject mwo = obs[i];
			Character c = dynamicReverse.get(mwo);
			if (c == null) {
				c = staticReverse.get(mwo);
				if (c == null) {
					c = staticReverse.get(PlayerWorldObject.NONE);
				}
			}
			sb.append(c);
		}

		return sb.toString();
	}

	/**
	 * Will dump the two arrays to a string array. The dynamic part of the observation is considered to be the 'foreground', the static part is the
	 * 'background'. If either of them is null, it will be ignored
	 * 
	 * @param dynamicObs
	 * @param staticObs
	 * @return
	 */
	public static String[] dumpToString(PlayerWorldObject[][] dynamicObs, PlayerWorldObject[][] staticObs) {
		int dynLen = dynamicObs == null ? 0 : dynamicObs.length;
		int staLen = staticObs == null ? 0 : staticObs.length;

		int len = Math.max(staLen, dynLen);
		String[] dump = new String[len];

		generalize(GeneralUtil.clone2DPlayerWorldObjectArray(dynamicObs));
		generalize(GeneralUtil.clone2DPlayerWorldObjectArray(staticObs));

		for (int i = 0; i < len; i++) {
			dump[i] = dumpLine(dynamicObs[i], staticObs[i]);
		}

		return dump;
	}

	private static String dumpLine(PlayerWorldObject[] dynamicObs, PlayerWorldObject[] staticObs) {
		int dynLen = dynamicObs == null ? 0 : dynamicObs.length;
		int staLen = staticObs == null ? 0 : staticObs.length;

		int len = Math.max(staLen, dynLen);

		StringBuffer sb = new StringBuffer(len);
		for (int i = 0; i < len; i++) {
			if (dynamicObs != null && dynamicObs[i] != PlayerWorldObject.NONE) {
				PlayerWorldObject mwo = dynamicObs[i];
				Character c = dynamicReverse.get(mwo);
				if (c != null) {
					sb.append(c);
					continue;
				}
			}

			if (staticObs != null) {
				PlayerWorldObject mwo = staticObs[i];
				Character c = staticReverse.get(mwo);
				if (c != null) {
					sb.append(c);
					continue;
				}
			}

			sb.append(staticReverse.get(PlayerWorldObject.NONE));
		}

		return sb.toString();
	}

	/**
	 * Parses an observation.
	 * 
	 * @param obs
	 * @param ot
	 * @return
	 */
	public static PlayerWorldObject[][] parseObservation(String[] obs, ObjectType ot) {
		PlayerWorldObject[][] world = new PlayerWorldObject[obs.length][];

		HashMap<Character, PlayerWorldObject> lookUp;
		if (ot == ObjectType.STATIC) {
			lookUp = staticBindings;
		} else {
			lookUp = dynamicBindings;
		}

		for (int i = 0; i < obs.length; i++) {
			world[i] = parseLine(obs[i], lookUp);
		}
		return world;
	}

	/**
	 * Parses a single line of an observation.
	 * 
	 * @param obs
	 * @param ot
	 * @return
	 */
	private static PlayerWorldObject[] parseLine(String obs, HashMap<Character, PlayerWorldObject> lookUp) {
		PlayerWorldObject[] world = new PlayerWorldObject[obs.length()];
		for (int i = 0; i < obs.length(); i++) {
			char c = obs.charAt(i);
			c = Character.toUpperCase(c);
			PlayerWorldObject o = lookUp.get(c);
			if (!o.isPlayer())
				world[i] = o;
		}
		return world;
	}

	private static void generalize(PlayerWorldObject[][] obs) {
		if (obs == null)
			return;
		for (int y = 0; y < obs.length; y++) {
			for (int x = 0; x < obs[0].length; x++) {
				obs[y][x] = generalize(obs[y][x]);
			}
		}
	}

	private static PlayerWorldObject generalize(PlayerWorldObject o) {
		if (staticReverse.containsKey(o) || dynamicReverse.containsKey(o))
			return o;

		switch (o) {
		case ENERGY_ANIM:
		case SIMPLE_BLOCK2:
		case ENERGY_FRONT:
		case ENERGY_SIDE1:
		case ENERGY_SIDE2:
		case ENERGY_SIDE3:
			return ENERGY_FRONT;
		default:
			break;
		}
		if (o.isStatic) {
			if (o.blocksPath == PlayerWorldObject.BlockingType.FULL || o.blocksPath == PlayerWorldObject.BlockingType.TOP
					|| o.blocksPath == PlayerWorldObject.BlockingType.BOTTOM)
				return STONE_BLOCK;
			return CENTER_DIRT;
		}
		return o;
	}

	public static void deleteStaticAttributes() {
		staticBindings = null;
		staticReverse = null;
		dynamicBindings = null;
		dynamicReverse = null;	
	}

	public static void resetStaticAttributes() {
		staticBindings = new HashMap<Character, PlayerWorldObject>();
		staticReverse = new HashMap<PlayerWorldObject, Character>();
		dynamicBindings = new HashMap<Character, PlayerWorldObject>();
		dynamicReverse = new HashMap<PlayerWorldObject, Character>();	
	}
}
