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

import java.util.ArrayList;

import marioWorld.utils.ResetStaticInterface;

/**
 * Interface which defines a set of hooks which will be called by the engine. This is mainly used for debugging purposes. If you plan to add any callbacks from
 * outside packages to the engine, edit this class and create a subclass!
 * 
 * @author sebastian
 * 
 */
public abstract class EngineHook implements ResetStaticInterface 
{

	private static ArrayList<EngineHook> hooks = new ArrayList<EngineHook>();

	/**
	 * Installs a hook into the engine.
	 * 
	 * @param hook
	 */
	public static void installHook(EngineHook hook) 
	{
		System.out.println("EngineHook::installHook");
		hooks.add(hook);
	}

	/**
	 * Returns all installed hooks by using an iterator.
	 * 
	 * @return
	 */
	public static Iterable<EngineHook> getHooks() {
		return hooks;
	}

	/**
	 * Called when the game world has been prepared.
	 * 
	 * @param component
	 */
	public abstract void OnGameWorldPrepared(GameWorld component);
	
	public static void deleteStaticAttributes()
	{
		hooks = null;
	}
	
	public static void resetStaticAttributes(){
		hooks = new ArrayList<EngineHook>();
	}
}
