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
package marioWorld.engine.level;

import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.PlayerWorldObject.ObjectType;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.FlowerEnemy;
import marioWorld.engine.sprites.Sprite;

/**
 * Comment by Stephan: This class defines for a single cell in the map the type of sprite which can be spawned there. This type is final. Typically, a sprite
 * will be spawned there as soon as mario gets close enough. Also provides access to isDead and isWinged.
 * 
 */
public class SpriteTemplate {
	public int lastVisibleTick = -1;
	public Sprite sprite;
	public boolean isDead = false;
	private boolean winged;

	public int getType() {
		return type;
	}

	public boolean isWinged() {
		return winged;
	}


	// 0 = Red Koopa (does not walk off ledges)
	// 1 = Green Koopa (walks off ledges)
	// 2 = Luko
	// 3 = Spikey
	// 4 = Piranha Plant up/down
	// 5 = Bullet Willy
	// 6 = Pirhana Plant left
	private final int type;

	/**
	 * 
	 * @param type
	 * @param winged
	 */
	public SpriteTemplate(int type, boolean winged) {
		this.type = type;
		this.winged = winged;
	}

	public void spawn(LevelScene world, GlobalCoarse mapPosition, int dir) {
		if (isDead)
			return;

		if (type == Enemy.ENEMY_FLOWER) {
			sprite = new FlowerEnemy(world, new GlobalContinuous(mapPosition.x * 16 + 15, mapPosition.y * 16 + 24), mapPosition);
		} else {
			// sprite = new Enemy(world, x*16+8, y*16+15, dir, type, winged);
			sprite = new Enemy(world, new GlobalContinuous(mapPosition.x * 16 + 8, mapPosition.y * 16 + 15), mapPosition, dir, type, winged);
		}
		sprite.spriteTemplate = this;
		world.addSprite(sprite);
	}

	/**
	 * This function clones a sprite template while setting the sprite field to null.
	 * 
	 * @return A clone of this with sprite == null
	 */
	public SpriteTemplate cloneNonRecursive() {
		SpriteTemplate ret = new SpriteTemplate(type, winged);
		ret.isDead = isDead;
		ret.lastVisibleTick = lastVisibleTick;
		ret.sprite = null;
		return ret;
	}

	public static SpriteTemplate createSpriteTemplate(Sprite sprite) {
		PlayerWorldObject helperObject = PlayerWorldObject.getElement(sprite.kind, ObjectType.NON_STATIC, 0);
		int type = SpriteTemplate.translatePlayerWorldObjectToTemplateType(helperObject);
		boolean isWinged = PlayerWorldObject.getElement(sprite).isWinged();
		return new SpriteTemplate(type, isWinged);
	}

	/**
	 * @author Stephan Translates the id used in sprite to the (differently encoded) id used in SpriteTemplate
	 * @param spriteKind
	 *            the id in Sprite
	 * @return the id in SpriteTemplate
	 */
	public static int translatePlayerWorldObjectToTemplateType(PlayerWorldObject playerWorldObject) {
		switch (playerWorldObject) {
		/*case RED_KOOPA:
		case RED_KOOPA_WINGED:
			return 0;
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
			return 1;
			*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
			return 0;
		//case GREEN_VIRUS:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY:
		case GRUMPY_WINGED:
			return 1;
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
			return 2;
		case SHALLY:
		case SHALLY_WINGED:
			return 3;
		case PIRHANA_PLANT:
			return 4;
		case BULLET_WILLY:
			return 5;
		default:
			new Throwable("Not implemented yet").printStackTrace();
			return -1;
		}
	}
}
