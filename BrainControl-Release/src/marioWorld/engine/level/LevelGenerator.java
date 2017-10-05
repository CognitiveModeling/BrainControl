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
package marioWorld.engine.level;

import java.util.List;
import java.util.Random;

import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.Sprite;

public class LevelGenerator {
	public static final int TYPE_EYES = 0;
	public static final int TYPE_MATH = 1;
	public static final int TYPE_EARS = 2;
	public static final int TYPE_ABSTRACT = 3;
	public static final int TYPE_EPILOG = 4;

	public static long lastSeed;
	public static final int LevelLengthMinThreshold = 50;

	public static Level createLevel(int width, int height, long seed, int difficulty, int type, List<Sprite> initialSprites) {
		LevelGenerator levelGenerator = new LevelGenerator(width, height);
		return levelGenerator.createLevel(seed, difficulty, type, initialSprites);
	}

	private int width;
	private int height;
	Level level = new Level(width, height);
	Random random;

	private static final int ODDS_STRAIGHT = 0;
	private static final int ODDS_HILL_STRAIGHT = 1;
	private static final int ODDS_TUBES = 2;
	private static final int ODDS_JUMP = 3;
	private static final int ODDS_CANNONS = 4;
	private int[] odds = new int[5];
	private int totalOdds;
	private int difficulty;
	private int type;

	private LevelGenerator(int width, int height) {
		this.width = width;
		this.height = height;
	}

	private Level createLevel(long seed, int difficulty, int type, List<Sprite> initialSprites) {
		this.type = type;
		this.difficulty = difficulty;
		odds[ODDS_STRAIGHT] = 20;
		odds[ODDS_HILL_STRAIGHT] = 10;
		odds[ODDS_TUBES] = 2 + 1 * difficulty;
		odds[ODDS_JUMP] = 2 * difficulty;
		odds[ODDS_CANNONS] = -10 + 5 * difficulty;

		if (type != LevelGenerator.TYPE_EYES) {
			odds[ODDS_HILL_STRAIGHT] = 0;
		}

		for (int i = 0; i < odds.length; i++) {
			if (odds[i] < 0)
				odds[i] = 0;
			totalOdds += odds[i];
			odds[i] = totalOdds - odds[i];
		}

		lastSeed = seed;
		level = new Level(width, height);
		random = new Random(seed);

		int length = 0;
		length += buildStraight(0, level.width, true, initialSprites);
		while (length < level.width - 64) {
			length += buildZone(length, level.width - length, initialSprites);
		}

		int floor = height - 1 - random.nextInt(4);

		level.xExit = length + 8;
		level.yExit = floor;

		for (int x = length; x < level.width; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					level.setBlock(x, y, (byte) (1 + 9 * 16));
				}
			}
		}

		if (type == LevelGenerator.TYPE_EARS || type == LevelGenerator.TYPE_MATH) {
			int ceiling = 0;
			int run = 0;
			for (int x = 0; x < level.width; x++) {
				if (run-- <= 0 && x > 4) {
					ceiling = random.nextInt(4);
					run = random.nextInt(4) + 4;
				}
				for (int y = 0; y < level.height; y++) {
					if ((x > 4 && y <= ceiling) || x < 1) {
						level.setBlock(x, y, (byte) (1 + 9 * 16));
					}
				}
			}
		}

		fixWalls();
		return level;
	}

	private int buildZone(int x, int maxLength, List<Sprite> initialSprites) {
		int t = random.nextInt(totalOdds);
		int type = 0;
		for (int i = 0; i < odds.length; i++) {
			if (odds[i] <= t) {
				type = i;
			}
		}

		switch (type) {
		case ODDS_STRAIGHT:
			return buildStraight(x, maxLength, false, initialSprites);
		case ODDS_HILL_STRAIGHT:
			return buildHillStraight(x, maxLength, initialSprites);
		case ODDS_TUBES:
			return buildTubes(x, maxLength);
		case ODDS_JUMP:
			return buildJump(x, maxLength);
		case ODDS_CANNONS:
			return buildCannons(x, maxLength);
		}
		return 0;
	}

	private int buildJump(int xo, int maxLength) {
		int js = random.nextInt(4) + 2;
		int jl = random.nextInt(2) + 2;
		int length = js * 2 + jl;

		boolean hasStairs = random.nextInt(3) == 0;

		int floor = height - 1 - random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			if (x < xo + js || x > xo + length - js - 1) {
				for (int y = 0; y < height; y++) {
					if (y >= floor) {
						level.setBlock(x, y, (byte) (1 + 9 * 16));
					} else if (hasStairs) {
						if (x < xo + js) {
							if (y >= floor - (x - xo) + 1) {
								level.setBlock(x, y, (byte) (9 + 0 * 16));
							}
						} else {
							if (y >= floor - ((xo + length) - x) + 2) {
								level.setBlock(x, y, (byte) (9 + 0 * 16));
							}
						}
					}
				}
			}
		}

		return length;
	}

	private int buildCannons(int xo, int maxLength) {
		int length = random.nextInt(10) + 2;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xCannon = xo + 1 + random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			if (x > xCannon) {
				xCannon += 2 + random.nextInt(4);
			}
			if (xCannon == xo + length - 1)
				xCannon += 10;
			int cannonHeight = floor - random.nextInt(4) - 1;

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					level.setBlock(x, y, (byte) (1 + 9 * 16));
				} else {
					if (x == xCannon && y >= cannonHeight) {
						if (y == cannonHeight) {
							level.setBlock(x, y, (byte) (14 + 0 * 16));
						} else if (y == cannonHeight + 1) {
							level.setBlock(x, y, (byte) (14 + 1 * 16));
						} else {
							level.setBlock(x, y, (byte) (14 + 2 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	private int buildHillStraight(int xo, int maxLength, List<Sprite> initialSprites) {
		int length = random.nextInt(10) + 10;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					level.setBlock(x, y, (byte) (1 + 9 * 16));
				}
			}
		}

		addEnemyLine(xo + 1, xo + length - 1, floor - 1, initialSprites);

		int h = floor;

		boolean keepGoing = true;

		boolean[] occupied = new boolean[length];
		while (keepGoing) {
			h = h - 2 - random.nextInt(3);

			if (h <= 0) {
				keepGoing = false;
			} else {
				int l = random.nextInt(5) + 3;
				int xxo = random.nextInt(length - l - 2) + xo + 1;

				if (occupied[xxo - xo] || occupied[xxo - xo + l] || occupied[xxo - xo - 1] || occupied[xxo - xo + l + 1]) {
					keepGoing = false;
				} else {
					occupied[xxo - xo] = true;
					occupied[xxo - xo + l] = true;
					addEnemyLine(xxo, xxo + l, h - 1, initialSprites);
					if (random.nextInt(4) == 0) {
						decorate(xxo - 1, xxo + l + 1, h, initialSprites);
						keepGoing = false;
					}
					for (int x = xxo; x < xxo + l; x++) {
						for (int y = h; y < floor; y++) {
							int xx = 5;
							if (x == xxo)
								xx = 4;
							if (x == xxo + l - 1)
								xx = 6;
							int yy = 9;
							if (y == h)
								yy = 8;

							if (level.getBlock(x, y) == 0) {
								level.setBlock(x, y, (byte) (xx + yy * 16));
							} else {
								if (level.getBlock(x, y) == (byte) (4 + 8 * 16))
									level.setBlock(x, y, (byte) (4 + 11 * 16));
								if (level.getBlock(x, y) == (byte) (6 + 8 * 16))
									level.setBlock(x, y, (byte) (6 + 11 * 16));
							}
						}
					}
				}
			}
		}

		return length;
	}

	private Sprite isLineUsedByInitialSprite(int x, List<Sprite> initialSprites, int accuracy) {
		for (Sprite sprite : initialSprites) {
			if (Math.abs(sprite.x - x) < accuracy) {
				return sprite;
			}
		}
		return null;
	}

	private void addEnemyLine(int x0, int x1, int y, List<Sprite> initialSprites) {
		for (int x = x0; x < x1; x++) {
			if (random.nextInt(35) < difficulty + 1) {
				Sprite spriteAtX = isLineUsedByInitialSprite(x, initialSprites, CoordinatesUtil.PLAYER_OBSERVATION_TILE_POS_X);
				/** randomly */
				if (spriteAtX == null) {
					int type = random.nextInt(4);
					if (difficulty < 1) {
						//type = Enemy.ENEMY_LUKO;
						type = Enemy.ENEMY_GREEN_VIRUS;
					} else if (difficulty < 3) {
						type = random.nextInt(3);
					}
					boolean isWinged = random.nextInt(35) < difficulty;
					level.setSpriteTemplate(new GlobalCoarse(x, y), new SpriteTemplate(type, isWinged));
				}
				/** or via initialSprite */
				// else {
				// if(spriteAtX.x == x){
				// level.setSpriteTemplate(x, y,
				// SpriteTemplate.createSpriteTemplate(spriteAtX));
				// }else {
				// //Do nothing. This blocks lines close to the initialSprite
				// (i.e. no random sprites are created).
				// }
				// }
			}
		}
	}

	private int buildTubes(int xo, int maxLength) {
		int length = random.nextInt(10) + 5;
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		int xTube = xo + 1 + random.nextInt(4);
		int tubeHeight = floor - random.nextInt(2) - 2;
		for (int x = xo; x < xo + length; x++) {
			if (x > xTube + 1) {
				xTube += 3 + random.nextInt(4);
				tubeHeight = floor - random.nextInt(2) - 2;
			}
			if (xTube >= xo + length - 2)
				xTube += 10;

			if (x == xTube && random.nextInt(11) < difficulty + 1) {
				level.setSpriteTemplate(new GlobalCoarse(x, tubeHeight), new SpriteTemplate(Enemy.ENEMY_FLOWER, false));
			}

			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					level.setBlock(x, y, (byte) (1 + 9 * 16));
				} else {
					if ((x == xTube || x == xTube + 1) && y >= tubeHeight) {
						int xPic = 10 + x - xTube;
						if (y == tubeHeight) {
							level.setBlock(x, y, (byte) (xPic + 0 * 16));
						} else {
							level.setBlock(x, y, (byte) (xPic + 1 * 16));
						}
					}
				}
			}
		}

		return length;
	}

	private int buildStraight(int xo, int maxLength, boolean safe, List<Sprite> initialSprites) {
		int length = random.nextInt(10) + 2;
		if (safe)
			length = 10 + random.nextInt(5);
		if (length > maxLength)
			length = maxLength;

		int floor = height - 1 - random.nextInt(4);
		for (int x = xo; x < xo + length; x++) {
			for (int y = 0; y < height; y++) {
				if (y >= floor) {
					level.setBlock(x, y, (byte) (1 + 9 * 16));
				}
			}
		}

		if (!safe) {
			if (length > 5) {
				decorate(xo, xo + length, floor, initialSprites);
			}
		}

		return length;
	}

	private void decorate(int x0, int x1, int floor, List<Sprite> initialSprites) {
		if (floor < 1)
			return;

		// boolean energys = random.nextInt(3) == 0;
		boolean rocks = true;

		addEnemyLine(x0 + 1, x1 - 1, floor - 1, initialSprites);

		int s = random.nextInt(4);
		int e = random.nextInt(4);

		if (floor - 2 > 0) {
			if ((x1 - 1 - e) - (x0 + 1 + s) > 1) {
				for (int x = x0 + 1 + s; x < x1 - 1 - e; x++) {
					level.setBlock(x, floor - 2, (byte) (2 + 2 * 16));
				}
			}
		}

		s = random.nextInt(4);
		e = random.nextInt(4);

		if (floor - 4 > 0) {
			if ((x1 - 1 - e) - (x0 + 1 + s) > 2) {
				for (int x = x0 + 1 + s; x < x1 - 1 - e; x++) {
					if (rocks) {
						if (x != x0 + 1 && x != x1 - 2 && random.nextInt(3) == 0) {
							if (random.nextInt(4) == 0) {
								level.setBlock(x, floor - 4, (byte) (4 + 2 + 1 * 16));
							} else {
								level.setBlock(x, floor - 4, (byte) (4 + 1 + 1 * 16));
							}
						} else if (random.nextInt(4) == 0) {
							if (random.nextInt(4) == 0) {
								level.setBlock(x, floor - 4, (byte) (2 + 1 * 16));
							} else {
								level.setBlock(x, floor - 4, (byte) (1 + 1 * 16));
							}
						} else {
							level.setBlock(x, floor - 4, (byte) (0 + 1 * 16));
						}
					}
				}
			}
		}

		// int length = x1 - x0 - 2;
		//
		//
		// if (length > 5 && rocks) { decorate(x0, x1, floor - 4); }

	}

	private void fixWalls() {
		boolean[][] blockMap = new boolean[width + 1][height + 1];
		for (int x = 0; x < width + 1; x++) {
			for (int y = 0; y < height + 1; y++) {
				int blocks = 0;
				for (int xx = x - 1; xx < x + 1; xx++) {
					for (int yy = y - 1; yy < y + 1; yy++) {
						if (level.getBlockCapped(xx, yy) == (byte) (1 + 9 * 16))
							blocks++;
					}
				}
				blockMap[x][y] = blocks == 4;
			}
		}
		blockify(level, blockMap, width + 1, height + 1);
	}

	private void blockify(Level level, boolean[][] blocks, int width, int height) {
		int to = 0;
		if (type == LevelGenerator.TYPE_EARS) {
			to = 4 * 2;
		} else if (type == LevelGenerator.TYPE_MATH) {
			to = 4 * 3;
		}

		boolean[][] b = new boolean[2][2];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				for (int xx = x; xx <= x + 1; xx++) {
					for (int yy = y; yy <= y + 1; yy++) {
						int _xx = xx;
						int _yy = yy;
						if (_xx < 0)
							_xx = 0;
						if (_yy < 0)
							_yy = 0;
						if (_xx > width - 1)
							_xx = width - 1;
						if (_yy > height - 1)
							_yy = height - 1;
						b[xx - x][yy - y] = blocks[_xx][_yy];
					}
				}

				if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
					if (b[0][0] == b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
						} else {
							// KEEP OLD BLOCK!
						}
					} else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
						}
					}
				} else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
					if (b[0][0]) {
						level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
					} else {
						level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
					}
				} else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
					level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
				} else if (b[0][0] == b[1][0]) {
					if (b[0][0]) {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
						}
					} else {
						if (b[0][1]) {
							level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
						}
					}
				} else if (b[0][1] == b[1][1]) {
					if (b[0][1]) {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
						}
					} else {
						if (b[0][0]) {
							level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
						} else {
							level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
						}
					}
				} else {
					level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
				}
			}
		}
	}
}
