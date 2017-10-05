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

import java.util.Random;

public class BgLevelGenerator 
{
	//since we might have multiple players, use the same seed for all generated backgrounds.
	public static long backgroundGraphicsRandomSeed = new Random().nextLong();

	public static Level createLevel(int width, int height, int layer, int type) 
	{
		BgLevelGenerator levelGenerator = new BgLevelGenerator(width, height, layer, type);
		return levelGenerator.createLevel();
	}

	private int width;
	private int height;
	private int layer;
	private int type;

	private BgLevelGenerator(int width, int height, int layer, int type) {
		//TODO: distant is no more used in this class
		this.width = width;
		this.height = height;
		this.layer = layer;
		this.type = type;
	}

	private Level createLevel() {
		
		Level level = null;
		//slightly redundant, but for easier reading...	
		//index shift for first two layers
		switch (type) {
		case LevelGenerator.TYPE_EYES: {		
			level = setLevel(1);	
			break;
		}
		case LevelGenerator.TYPE_MATH: {
			level = setLevel(2);			
			break;
		}
		case LevelGenerator.TYPE_EARS: {
			level = setLevel(3);
			break;
		}
		case LevelGenerator.TYPE_ABSTRACT: {
			level = setLevel(4);
			break;
		}
		case LevelGenerator.TYPE_EPILOG: {
			level = setLevel(5);
			break;
		}
		}
		return level;
	}
	
	private Level setLevel(int specificType) {
		Level level = new Level(width, height);
		
		if(layer <= 1) { // for the two most away layers
			for (int x = 0; x < width; x++) {
				level.setBlock(x, 0, (byte) (layer));
			}
		} else {	
			for (int x = 0; x < width; x++) {
				level.setBlock(x, 0, (byte) (specificType * 8 + layer - 2));
			}
		}
		
		return(level);
	}
	
}
