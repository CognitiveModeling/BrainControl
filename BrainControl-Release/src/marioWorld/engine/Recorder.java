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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Recorder {
	private ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private DataOutputStream dos = new DataOutputStream(baos);

	private byte lastTick = 0;
	private int tickCount = 0;

	public void addLong(long val) {
		try {
			dos.writeLong(val);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addTick(byte tick) {
		try {
			if (tick == lastTick) {
				tickCount++;
			} else {
				dos.writeInt(tickCount);
				dos.write(tick);
				lastTick = tick;
				tickCount = 1;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getBytes() {
		try {
			dos.writeInt(tickCount);
			dos.write(-1);
			dos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
}
