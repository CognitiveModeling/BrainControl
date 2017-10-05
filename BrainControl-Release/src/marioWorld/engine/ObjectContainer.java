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

import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.sprites.Sprite;

/**
 * Container to combine PlayerWorldObject, Sprite and GlobalCoarse
 * Intended usage e.g. in goals (target, effect target(?), actor)
 * 
 * @author Johannes
 *
 */

public class ObjectContainer {
	
	private PlayerWorldObject type;
	private Sprite sprite;
	private GlobalCoarse coordinates;
	
	public ObjectContainer(PlayerWorldObject type, Sprite sprite,
			GlobalCoarse coordinates) {
		this.type = type;
		this.sprite = sprite;
		this.coordinates = coordinates;
	}

	public PlayerWorldObject getType() {
		return type;
	}

	public void setType(PlayerWorldObject type) {
		this.type = type;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public GlobalCoarse getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(GlobalCoarse coordinates) {
		this.coordinates = coordinates;
	}

}
