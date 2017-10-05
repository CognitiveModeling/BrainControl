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

import java.awt.Graphics;
import java.awt.Graphics2D;

import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.Sprite;

public class SpriteRendererEnemy extends SpriteRenderer
{
	@Override
	public void render(Graphics2D og, float alpha)
	{
		Enemy enemy = (Enemy) sprite;
		
		if (enemy.winged) 
		{
			int xPixel = (int) (enemy.xOld + (enemy.x - enemy.xOld) * alpha) - renderProperties.renderFORx;
			int yPixel = (int) (enemy.yOld + (enemy.y - enemy.yOld) * alpha) - renderProperties.renderFORy;

		//	if (enemy.type == Enemy.ENEMY_GREEN_KOOPA || enemy.type == Enemy.ENEMY_RED_KOOPA) 
			if (enemy.type == Enemy.ENEMY_GRUMPY || enemy.type == Enemy.ENEMY_RED_VIRUS) 
			{
			} 
			else 
			{
				renderProperties.flipFrameX = !renderProperties.flipFrameX;
				og.drawImage(renderSheet.getImage(enemy.wingTime / 4 % 2,4), xPixel + (renderProperties.flipFrameX ? renderProperties.renderWidth : 0) + (renderProperties.flipFrameX ? 10 : -10), yPixel + (renderProperties.flipFrameY ? renderProperties.renderHeight : 0) - 8,
						renderProperties.flipFrameX ? -renderProperties.renderWidth : renderProperties.renderWidth, renderProperties.flipFrameY ? -renderProperties.renderHeight : renderProperties.renderHeight, null);
				renderProperties.flipFrameX = !renderProperties.flipFrameX;
			}
		}

		super.render(og, alpha);

		if (enemy.winged) 
		{
			int xPixel = (int) (enemy.xOld + (enemy.x - enemy.xOld) * alpha) - renderProperties.renderFORx;
			int yPixel = (int) (enemy.yOld + (enemy.y - enemy.yOld) * alpha) - renderProperties.renderFORy;

			//if (enemy.type == Enemy.ENEMY_GREEN_KOOPA || enemy.type == Enemy.ENEMY_RED_KOOPA) 
			if (enemy.type == Enemy.ENEMY_GRUMPY || enemy.type == Enemy.ENEMY_RED_VIRUS) 
			{
				og.drawImage(renderSheet.getImage(enemy.wingTime / 4 % 2,4), xPixel + (renderProperties.flipFrameX ? renderProperties.renderWidth : 0) + (renderProperties.flipFrameX ? 10 : -10), yPixel + (renderProperties.flipFrameY ? renderProperties.renderHeight : 0) - 10,
						renderProperties.flipFrameX ? -renderProperties.renderWidth : renderProperties.renderWidth, renderProperties.flipFrameY ? -renderProperties.renderHeight : renderProperties.renderHeight, null);
			} 
			else 
			{
				og.drawImage(renderSheet.getImage(enemy.wingTime / 4 % 2,4), xPixel + (renderProperties.flipFrameX ? renderProperties.renderWidth : 0) + (renderProperties.flipFrameX ? 10 : -10), yPixel + (renderProperties.flipFrameY ? renderProperties.renderHeight : 0) - 8,
						renderProperties.flipFrameX ? -renderProperties.renderWidth : renderProperties.renderWidth, renderProperties.flipFrameY ? -renderProperties.renderHeight : renderProperties.renderHeight, null);
			}
		}		
	}
	
	public SpriteRendererEnemy(Sprite setSprite, SpriteSheet setSheet, int defaultXFrame, int defaultYFrame)
	{
		super(setSprite, setSheet, defaultXFrame, defaultYFrame);
	}
}
