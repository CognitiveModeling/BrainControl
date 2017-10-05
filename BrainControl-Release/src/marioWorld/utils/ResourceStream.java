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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ResourceStream {


	public static final String DIR_RESOURCE = "resources";
	public static final String DIR_AI_RESOURCES = "marioAI";
	public static final String DIR_UI_RESOURCES = "marioUI";
	public static final String DIR_WORLD_RESOURCES = "marioWorld";
	
	public static final String[] RESOURCES_NEEDED_TO_COPY = new String[] {
			//"marioUI/speechSynthesis/responses.list",
			"marioWorld/engine/resources/activatedLevel.txt",
			"marioUI/speechSynthesis/responses.map",
			"marioUI/voiceControl/input/input.config.xml",
			"marioUI/voiceControl/acoustic_model/means",
			"marioUI/voiceControl/acoustic_model/variances",
			"marioUI/voiceControl/acoustic_model/mixture_weights",
			"marioUI/voiceControl/acoustic_model/transition_matrices",
			"marioUI/voiceControl/acoustic_model/mdef",
			"marioUI/voiceControl/acoustic_model/noisedict",
			"marioUI/voiceControl/acoustic_model/dict/cmudict_with_worldobjects.dict",
			"marioUI/voiceControl/grammars/menu.gram",
			"marioUI/voiceControl/grammars/goal.gram",
			"marioUI/voiceControl/grammars/knowledge.gram",
			"marioUI/voiceControl/grammars/players.gram",
			"marioUI/voiceControl/grammars/directions.gram",
			"marioUI/voiceControl/grammars/objects.gram",
			"marioUI/voiceControl/grammars/smalltalk.gram",
			"marioUI/voiceControl/grammars/reservoir.gram",
			"marioUI/voiceControl/grammars/numbers.gram",
	};


	private static final boolean RESOURCES_WERE_COPIED = copyResources();

	public static boolean resourcesWereCopied() {
		return RESOURCES_WERE_COPIED;
	}

	/**
	 * Copy resources that are expected in local directory (and might be modified)
	 */
	public static boolean copyResources() {
		boolean copied = false;
		for (String relPath : RESOURCES_NEEDED_TO_COPY) {
			ClassLoader cl = ResourceStream.class.getClassLoader();
			String filename = ResourceStream.DIR_RESOURCE+"/"+relPath;
			File file = new File(filename);
			if (!file.exists()) {
				InputStream resStream = cl.getResourceAsStream(filename);
				if (resStream != null) {
					System.out.println("Copying resource file "+relPath);	
					try {
						File dir = file.getParentFile();
						if (!dir.exists())
							dir.mkdirs();
						FileOutputStream out = new FileOutputStream(filename);
						byte[] buffer = new byte[1024];
						int read;
						while ((read = resStream.read(buffer)) > 0) {
							out.write(buffer,0,read);
						}
						out.close();
						resStream.close();
						copied = true;
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					} 
				} else {
					System.err.println("Could not copy resource: "+filename);
				}
			}
		}
		return copied;
	}


	public static InputStream getResourceStream(String pathInResourceDir) {
		ClassLoader cl = ResourceStream.class.getClassLoader();
		String filename = ResourceStream.DIR_RESOURCE+"/"+pathInResourceDir;
		InputStream resStream = cl.getResourceAsStream(filename);
		if (resStream == null) {
			// could not get as resource stream, try find the file in local path
			File file = new File(filename);
			if (file.exists()) {
				try {
					resStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					System.err.println("Could not open resource file: "+filename);
				}
			} else {
				System.err.println("Could not find resource file: "+filename);
			}
		}
		return resStream;
	}
	
	public static InputStream getResourceStream(Class<?> class1, String relPath) {
		String pathInResourceDir = class1.getPackage().getName().replaceAll("\\.", "/")+"/"+relPath;
		return getResourceStream(pathInResourceDir);
	}

}
