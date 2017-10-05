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
package marioWorld.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import mario.main.Settings;

public class Sounds {

	public static enum Type {
		// TODO: add some more 
		START_LEVEL("tada",".wav", 1),
		ENERGY("smw_energy",".wav", 1),
		JUMP("smw_jump",".wav", 1),
		BLOCK_DESTRUCTION("smw_swooper",".wav", 1),
		SOUNDTRACK("Main",".wav", 5)
		//SOUNDTRACK("BrainControl.wav")
		;

		private String filenameBase;
		private String extension;
		private int current, num;

		Type(String filenameBase, String extension, int num) {
			this.filenameBase = filenameBase;
			this.extension = extension;
			this.num = num;
			this.current = 0;
		}

		String getFileName(int version) {
			return filenameBase+(num > 1 ? "_"+version : "")+extension;
		}
		
		public void setCurrentVersion(int version) {
			//System.err.println("Set Version  for "+filenameBase+": "+version);
			this.current = version % num;
		}
	}

	private static Map<Type,Clip[]> clips = loadClips();

	private static Map<Type, Clip[]> loadClips()  {
		Map<Type,Clip[]> res = new HashMap<>(); 
		try {
			for (Type type : Type.values()) {
				Clip[] clips = new Clip[type.num];
				for (int i = 0; i < type.num; i++) {
					InputStream input = ResourceStream.getResourceStream("marioWorld/sounds/"+type.getFileName(i));
					//System.out.println(url);
					//System.out.println(ResourceStream.DIR_RESOURCE+"/"+ResourceStream.DIR_WORLD_RESOURCES+"/sounds/"+type.getFileName());
					BufferedInputStream stream = new BufferedInputStream(input);
					AudioInputStream sound = AudioSystem.getAudioInputStream(stream);
					// load the sound into memory (a Clip)
					DataLine.Info info = new DataLine.Info(Clip.class, sound.getFormat());
					Line line = AudioSystem.getLine(info);
					Clip clip = (Clip) line;
					clip.open(sound);
					clip.setFramePosition(0);
					clips[i] = clip;
				}
				res.put(type, clips);

				//master gain "not supported" for me (linux)...
				//FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
				//volume.setValue(volume.getMaximum());

			}
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}		

	public static void play(Type type) 
	{
		Clip[] cliparr = clips.get(type);
		Clip clip = cliparr[type.current];

		if(clip.isActive())
		{
			clip.flush();
			clip.stop();
		}
		clip.setFramePosition(0);
		clip.start();
	}

	public static void loop(final Type type) {
		Clip[] cliparr = clips.get(type);
		Clip clip = cliparr[type.current];
		clip.setFramePosition(0);		
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	public static void stop(final Type type) {
		Clip[] cliparr = clips.get(type);
		Clip clip = cliparr[type.current];
		clip.stop();
	}

	public static void setVolume(final Type type, float level) {
		Clip[] cliparr = clips.get(type);
		Clip clip = cliparr[type.current];
		FloatControl volume = null;
		if (clip.isControlSupported(FloatControl.Type.VOLUME))
			volume = (FloatControl) clip.getControl(FloatControl.Type.VOLUME);
		else
			if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
				volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);					
		if (volume != null) {
			float min = volume.getMinimum();
			float max = volume.getMaximum();
			if (volume.getUnits().toLowerCase().equals("db")) {
				min = Math.max(min, Settings.MIN_LEVEL_IN_DB);
			} 
			float value = min + (max-min)*level;
			volume.setValue(value);

			//System.out.println("Volume set to "+level);
			//System.out.println(volume.getMinimum() + "..:" +volume.getMaximum());
		} else {			
			System.err.println("Could not set volume");
		}
	}

	/*public static void main(String[] args) 
	{
		System.out.println("Playing sound");		

		try 
		{
			loop(Type.SOUNDTRACK);

			Thread.sleep(2000);
			
			setVolume(Type.SOUNDTRACK, 0.7f);

			Thread.sleep(2000);

			play(Type.ENERGY);			
			Thread.sleep(80);
			play(Type.JUMP);
			Thread.sleep(80);
			play(Type.ENERGY);

			Thread.sleep(2000);

			play(Type.JUMP);
			Thread.sleep(80);
			play(Type.ENERGY);
			Thread.sleep(80);
			play(Type.ENERGY);

			Thread.sleep(5000);

			System.out.println("ENDE");
			
			stop(Type.SOUNDTRACK);
			
			Thread.sleep(5000);
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
*/
}
