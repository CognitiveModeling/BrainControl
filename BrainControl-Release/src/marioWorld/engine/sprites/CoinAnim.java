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
package marioWorld.engine.sprites;

import marioWorld.engine.Art;
import marioWorld.engine.LevelScene;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;
import marioWorld.utils.Sounds;
import marioWorld.utils.Sounds.Type;

public class CoinAnim extends Sprite {
	private int life = 16;

	public CoinAnim(int xTile, int yTile) {
		kind = KIND_ENERGY_ANIM;
		
		//wPic = hPic = 16;
		
		x = xTile * 16;
		y = yTile * 16 - 16;
		xa = 0;
		ya = -6f;
		
		renderer = new SpriteRenderer(this, Art.level, 0, 2);		
	}

	@Override
	public void move() {
		if (life-- < 0) {
			Sprite.spriteContext.removeSprite(this);
			for (int xx = 0; xx < 2; xx++)
				for (int yy = 0; yy < 2; yy++)
					Sprite.spriteContext.addSprite(new Sparkle((int) x + xx * 8 + (int) (Math.random() * 8), (int) y + yy * 8 + (int) (Math.random() * 8), 0,
							0, 0, 2, 5));
		}

		renderer.properties().selectedFrameX = life & 3;

		x += xa;
		y += ya;
		ya += 1;
	}

	@Override
	public CoinAnim cloneWithNewLevelScene(LevelScene ls) {
		CoinAnim ret = (CoinAnim) super.clone();
		ret.life = life;
		return ret;
	}
}
