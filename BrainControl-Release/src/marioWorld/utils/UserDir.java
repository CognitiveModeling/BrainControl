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

import mario.main.Settings;

public class UserDir {

	public static final String MARIO_DIR = ".mario";
	public static final String SUB_DIR = "resources";
	
	public static File getMarioDir() {
		String path = Settings.USE_LOCAL_DIR_FOR_FILES ? "." : System.getProperty("user.dir")+"/"+MARIO_DIR;
		path += "/"+SUB_DIR;
		File file = new File(path);
		if (!file.exists()) {
			System.out.println("Creating dir "+file.getPath());
			file.mkdirs();
		}
		return file;
	}

	public static File getUserFile(String relativePath) {
		File res = new File(getMarioDir(),relativePath);
		File parentDir = res.getParentFile();
		if (!parentDir.exists()) {
			System.out.println("Creating dir "+parentDir.getPath());
			parentDir.mkdirs();
		}
		System.out.println("User file: "+res.getPath());
		return res;
	}

}
