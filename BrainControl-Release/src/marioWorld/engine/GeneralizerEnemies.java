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

import marioWorld.engine.sprites.Sprite;

/**
 * Created by IntelliJ IDEA. User: Sergey Karakovskiy, firstname_at_idsia_dot_ch Date: Aug 5, 2009 Time: 7:04:19 PM Package: ch.idsia.mario.engine
 */
public class GeneralizerEnemies implements Generalizer {

	public byte ZLevelGeneralization(byte el, int ZLevel) {
		switch (ZLevel) {
		case (0):
			switch (el) {
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
				return Sprite.KIND_NONE;
			}
			return el;
		case (1):
			switch (el) {
			case (Sprite.KIND_ENERGY_ANIM):
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
				return Sprite.KIND_NONE;
			case (Sprite.KIND_FIREBALL):
				return Sprite.KIND_FIREBALL;
			case (Sprite.KIND_BULLET_WILLY):
			case (Sprite.KIND_LUKO):
			case (Sprite.KIND_LUKO_WINGED):
				/*
				case (Sprite.KIND_GREEN_KOOPA):
				case (Sprite.KIND_GREEN_KOOPA_WINGED):
				case (Sprite.KIND_RED_KOOPA):
				case (Sprite.KIND_RED_KOOPA_WINGED):
				*/
				case (Sprite.KIND_GREEN_VIRUS):
				case (Sprite.KIND_GREEN_VIRUS_WINGED):
				case (Sprite.KIND_RED_VIRUS):
				case (Sprite.KIND_RED_VIRUS_WINGED):
				return Sprite.KIND_LUKO;
			case (Sprite.KIND_SHALLY):
			case (Sprite.KIND_ENEMY_FLOWER):
				return Sprite.KIND_SHALLY;
			}
			return el;
		case (2):
			switch (el) {
			case (Sprite.KIND_ENERGY_ANIM):
			case (Sprite.KIND_PARTICLE):
			case (Sprite.KIND_SPARCLE):
			case (Sprite.KIND_FIREBALL):
				return Sprite.KIND_NONE;
			case (Sprite.KIND_BULLET_WILLY):
			case (Sprite.KIND_LUKO):
			case (Sprite.KIND_LUKO_WINGED):
				/*
			case (Sprite.KIND_GREEN_KOOPA):
			case (Sprite.KIND_GREEN_KOOPA_WINGED):
			case (Sprite.KIND_RED_KOOPA):
			case (Sprite.KIND_RED_KOOPA_WINGED):
			*/
			case (Sprite.KIND_GREEN_VIRUS):
			case (Sprite.KIND_GREEN_VIRUS_WINGED):
			case (Sprite.KIND_RED_VIRUS):
			case (Sprite.KIND_RED_VIRUS_WINGED):
			case (Sprite.KIND_SHALLY):
			case (Sprite.KIND_ENEMY_FLOWER):
				return Sprite.KIND_SHALLY;
			}
			return (el == 0 || el == Sprite.KIND_FIREBALL) ? el : -2;
		}
		return el; // TODO: Throw unknown ZLevel exception
	}
}
