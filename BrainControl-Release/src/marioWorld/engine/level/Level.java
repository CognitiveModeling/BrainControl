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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import marioWorld.engine.coordinates.GlobalCoarse;

public class Level {
	public static final String[] BIT_DESCRIPTIONS = {//
	"BLOCK UPPER", //
			"BLOCK ALL", //
			"BLOCK LOWER", //
			"SPECIAL", //
			"BUMPABLE", //
			"BREAKABLE", //
			"PICKUPABLE", //
			"ANIMATED",//
	};

	public static byte[] TILE_BEHAVIORS = new byte[256];

	public static final int BIT_BLOCK_UPPER = 1 << 0;
	public static final int BIT_BLOCK_ALL = 1 << 1;
	public static final int BIT_BLOCK_LOWER = 1 << 2;
	public static final int BIT_SPECIAL = 1 << 3;
	public static final int BIT_BUMPABLE = 1 << 4;
	public static final int BIT_BREAKABLE = 1 << 5;
	public static final int BIT_PICKUPABLE = 1 << 6;
	public static final int BIT_ANIMATED = 1 << 7;

	private static final int FILE_HEADER = 0x271c4178;
	public int width;
	public int height;

	public byte[][] map;		//ATTENTION: MATRIX ENCODING IS [x][y]
	public byte[][] data;
	public byte[][] observation;

	public SpriteTemplate[][] spriteTemplates;

	public int xExit;
	public int yExit;

	public Level(int width, int height) {
		this.width = width;
		this.height = height;

		xExit = 10;
		yExit = 10;
		map = new byte[width][height];
		data = new byte[width][height];
		spriteTemplates = new SpriteTemplate[width][height];
		observation = new byte[width][height];
	}

	// Level klonen
	// geklont werden die Variablen, die von tick() verÃ¤ndert werden: width,
	// height, data
	public Level clone() {
		Level ret = new Level(width, height);
		ret.width = this.width;
		ret.height = this.height;
		ret.data = this.cloneData();
		ret.map = this.cloneMap();
		ret.xExit = this.xExit;
		ret.yExit = this.yExit;
		ret.observation = cloneByteField(observation);
		// TODO sprite templates should be cloned as well, this is probably
		// going to cause trouble
		return ret;
	}

	// data klonen
	public byte[][] cloneData() {
		return cloneByteField(data);
	}

	// nur map klonen
	public byte[][] cloneMap() {
		return cloneByteField(map);
	}

	private byte[][] cloneByteField(byte[][] toClone) {
		if (toClone == null) {
			return null;
		}
		if (toClone.length == 0) {
			return new byte[0][];
		}
		byte[][] ret = new byte[toClone.length][toClone[1].length];
		for (int i = 0; i < ret.length; i++) {
			for (int j = 0; j < ret[i].length; j++) {
				ret[i][j] = toClone[i][j];
			}
		}
		return ret;
	}

	// public void ASCIIToOutputStream(OutputStream os) throws IOException {
	// BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
	// bw.write("\nwidth = " + width);
	// bw.write("\nheight = " + height);
	// bw.write("\nMap:\n");
	// for (int y = 0; y < height; y++)
	//
	// {
	// for (int x = 0; x < width; x++)
	//
	// {
	// bw.write(map[x][y] + "\t");
	// }
	// bw.newLine();
	// }
	// bw.write("\nData: \n");
	//
	// for (int y = 0; y < height; y++)
	//
	// {
	// for (int x = 0; x < width; x++)
	//
	// {
	// bw.write(data[x][y] + "\t");
	// }
	// bw.newLine();
	// }
	//
	// bw.write("\nspriteTemplates: \n");
	// for (int y = 0; y < height; y++)
	//
	// {
	// for (int x = 0; x < width; x++)
	//
	// {
	// if (spriteTemplates[x][y] != null)
	// bw.write(spriteTemplates[x][y].getType() + "\t");
	// else
	// bw.write("_\t");
	//
	// }
	// bw.newLine();
	// }
	//
	// bw.write("\n==================\nAll objects: (Map[x,y], Data[x,y], Sprite[x,y])\n");
	// for (int y = 0; y < height; y++)
	// {
	// for (int x = 0; x < width; x++)
	//
	// {
	// bw.write("(" + map[x][y] + "," + data[x][y] + ", " +
	// ((spriteTemplates[x][y] == null) ? "_" : spriteTemplates[x][y].getType())
	// + ")\t");
	// }
	// bw.newLine();
	// }
	//
	// // bw.close();
	// }

	public static void loadBehaviors(DataInputStream dis) throws IOException {
		dis.readFully(Level.TILE_BEHAVIORS);
	}

	public static void saveBehaviors(DataOutputStream dos) throws IOException {
		dos.write(Level.TILE_BEHAVIORS);
	}

	public static Level load(DataInputStream dis) throws IOException {
		long header = dis.readLong();
		if (header != Level.FILE_HEADER)
			throw new IOException("Bad level header");
		// unused, but the call to dis.read() needs to stay
		@SuppressWarnings("unused")
		int version = dis.read() & 0xff;

		int width = dis.readShort() & 0xffff;
		int height = dis.readShort() & 0xffff;
		Level level = new Level(width, height);
		level.map = new byte[width][height];
		level.data = new byte[width][height];
		for (int i = 0; i < width; i++) {
			dis.readFully(level.map[i]);
			dis.readFully(level.data[i]);
		}
		return level;
	}

	public void save(DataOutputStream dos) throws IOException {
		dos.writeLong(Level.FILE_HEADER);
		dos.write((byte) 0);

		dos.writeShort((short) width);
		dos.writeShort((short) height);

		for (int i = 0; i < width; i++) {
			dos.write(map[i]);
			dos.write(data[i]);
		}
	}

	public void tick() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[x][y] > 0)
					data[x][y]--;
			}
		}
	}

	public byte getBlockCapped(int x, int y) {
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x >= width)
			x = width - 1;
		if (y >= height)
			y = height - 1;
		return map[x][y];
	}

	public byte getBlock(int x, int y) {
		if (x < 0)
			x = 0;
		if (y < 0)
			return 0;
		if (x >= width)
			x = width - 1;
		if (y >= height)
			y = height - 1;
		return map[x][y];
	}

	public void setBlock(int x, int y, byte b) {
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		map[x][y] = b;
	}

	public void setBlockData(int x, int y, byte b) {
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		data[x][y] = b;
	}

	public static boolean isBlocking(byte block, float ya) {
		boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) > 0;
		blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_UPPER) > 0; //C1R1 SAB
		blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_LOWER) > 0;

		return blocking;
	}

	public boolean isBlocking(int x, int y, float xa, float ya) {
		return Level.isBlocking(getBlock(x, y), ya);
		// byte block = getBlock(x, y);
		// boolean blocking = ((TILE_BEHAVIORS[block & 0xff]) & BIT_BLOCK_ALL) >
		// 0;
		// blocking |= (ya > 0) && ((TILE_BEHAVIORS[block & 0xff]) &
		// BIT_BLOCK_UPPER) > 0;
		// blocking |= (ya < 0) && ((TILE_BEHAVIORS[block & 0xff]) &
		// BIT_BLOCK_LOWER) > 0;
		//
		// return blocking; //moved to static method
	}

	public SpriteTemplate getSpriteTemplate(GlobalCoarse mapPosition) {
		int x = mapPosition.x;
		int y = mapPosition.y;
		if (x < 0)
			return null;
		if (y < 0)
			return null;
		if (x >= width)
			return null;
		if (y >= height)
			return null;
		return spriteTemplates[x][y];
	}

	public void setSpriteTemplate(GlobalCoarse mapPosition, SpriteTemplate spriteTemplate) {
		int x = mapPosition.x;
		int y = mapPosition.y;
		if (x < 0)
			return;
		if (y < 0)
			return;
		if (x >= width)
			return;
		if (y >= height)
			return;
		spriteTemplates[x][y] = spriteTemplate;
	}

	public int getWidthCells() {
		return width;
	}

	public double getWidthPhys() {
		return width * 16;
	}
}
