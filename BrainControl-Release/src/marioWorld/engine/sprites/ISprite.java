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
 * 72076 T�bingen 
 * cm-sekretariat -at- inf.uni-tuebingen.de
 ******************************************************************************/
package marioWorld.engine.sprites;

import marioWorld.engine.GenericGetterForAllAttributes;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;

public interface ISprite extends GenericGetterForAllAttributes, Cloneable {

	public GlobalContinuous getPosition(int timeDelay);

	public GlobalContinuous getVelocity(int timeDelay);

	public GlobalCoarse getMapPosition(int timeDelay);

	public PlayerWorldObject getType();

	public boolean isFacingDefined();

	public int getFacing();

	public boolean isOnGroundDefined();

	public boolean isOnGround();

	public boolean isDeadTimeDefined();

	public int getDeadTime();

}
