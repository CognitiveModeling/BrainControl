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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.GrayFilter;

import marioWorld.utils.ResourceStream;

public class Art {
	public static SpriteSheet clark;
	public static SpriteSheet smallClark;
	public static SpriteSheet bulbClark;
	public static SpriteSheet jay;
	public static SpriteSheet smallJay;
	public static SpriteSheet bulbJay;	
	public static SpriteSheet bruce;
	public static SpriteSheet smallBruce;
	public static SpriteSheet bulbBruce;
	public static SpriteSheet peter;
	public static SpriteSheet smallPeter;
	public static SpriteSheet bulbPeter;
	public static SpriteSheet newClark;
	public static SpriteSheet enemies;
	public static SpriteSheet items;
	public static SpriteSheet level;
	public static SpriteSheet particles;
	public static SpriteSheet font;
	public static SpriteSheet bg;
	//public static SpriteSheet map;
	//public static SpriteSheet endScene;
	//public static SpriteSheet gameOver;
	public static SpriteSheet peach;
	public static SpriteSheet camera;
	public static SpriteSheet computer;
	public static SpriteSheet microphone;
	public static SpriteSheet dreamCatcher;
	public static SpriteSheet Maria;
	public static SpriteSheet cursor;
	public static SpriteSheet waiting;
	
	public static Image logo;
	public static Image titleScreen;
	public static Image play;
	public static Image pause;
	public static Image speaker;
	public static Image mute;
	
	public static Image health;
	public static Image progress;
	public static Image curiosity;
	public static Image energy;
	
	public static Image grayHealth;
	public static Image grayProgress;
	public static Image grayCuriosity;
	public static Image grayEnergy;
	
	final static String curDir = System.getProperty("user.dir");
	final static String img = curDir + "/../img/";
	
	public static void init(GraphicsConfiguration gc) {
		try 
		{			
			//System.out.println("Image Directory: " + img);
			// System.out.println(curDir);
			
			level = cutImage(gc, "mapsheet.png", 16, 16);			//level graphics
			bg = cutImage(gc, "bgsheet.png", 520, 256);				//background graphics
			//map = cutImage(gc, "worldmap.png", 16, 16);				//TODO: obsolete graphics?
			enemies = cutImage(gc, "enemysheet.png", 16, 32);
			items = cutImage(gc, "itemsheet.png", 16, 16);
			particles = cutImage(gc, "particlesheet.png", 8, 8);
			waiting = cutImage(gc, "gearsSmall.png", 16,16);
			
//			play = cutImage(gc, "play128.png",16,16);
//			pause = cutImage(gc, "pause128.png",16,16);
			//logo = getImage(gc, "logo.gif");
			//titleScreen = getImage(gc, "title.gif");
			//font = cutImage(gc, "font.gif", 8, 8);
			//endScene = cutImage(gc, "endscene.gif", 96, 96);
			//gameOver = cutImage(gc, "gameovergost.gif", 96, 64);
			
			camera = cutImage(gc, "camera.png", 50, 50);
			computer = cutImage(gc, "computer.png", 50, 50);
			microphone = cutImage(gc, "microphone.png", 50, 50);
			dreamCatcher = cutImage(gc, "dreamCatcherSmall.png", 50, 50);
			Maria = cutImage(gc, "Maria_M_Enditem.png", 50, 50);
			
			cursor = cutImage(gc, "cursor.png", 20, 20);

			clark = cutImage(gc, "clark_sheet.png", 32, 32);
			smallClark = cutImage(gc, "clark_small_sheet.png", 32, 32);
			bulbClark = cutImage(gc, "clark_bulb_sheet.png", 32, 32);
			
			jay = cutImage(gc, "jay_sheet.png", 32, 32);
			smallJay = cutImage(gc, "jay_small_sheet.png", 32, 32);
			bulbJay = cutImage(gc, "jay_bulb_sheet.png", 32, 32);			
			
			peter = cutImage(gc, "peter_sheet.png", 32, 32);
			smallPeter = cutImage(gc, "peter_small_sheet.png", 32, 32);
			bulbPeter = cutImage(gc, "peter_bulb_sheet.png", 32, 32);
			
			bruce = cutImage(gc, "bruce_sheet.png", 32, 32);
			smallBruce = cutImage(gc, "bruce_small_sheet.png", 32, 32);
			bulbBruce = cutImage(gc, "bruce_bulb_sheet.png", 32, 32);
			
			
			String path = ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/play32.png";
			play = ImageIO.read(ResourceStream.getResourceStream(path));
//			play = createResizedCopy(img,24, 24, true);
			path =  ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/pause32.png";
			pause = ImageIO.read(ResourceStream.getResourceStream(path));
			
			path =  ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/speaker32.png";
			speaker = ImageIO.read(ResourceStream.getResourceStream(path));
			
			path =  ResourceStream.DIR_WORLD_RESOURCES+"/engine/resources/mute32.png";
			mute = ImageIO.read(ResourceStream.getResourceStream(path));
			
			health = getImage(gc, "health.png");
			grayHealth = GrayFilter.createDisabledImage(health);
			curiosity = getImage(gc, "curiosity.png");
			grayCuriosity = GrayFilter.createDisabledImage(curiosity);
			progress = getImage(gc, "progress.png");
			grayProgress = GrayFilter.createDisabledImage(progress);
			energy = getImage(gc, "energy.png");
			grayEnergy = GrayFilter.createDisabledImage(energy);
			

			System.out.println("Done loading arts");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

	}

	private static Image getImage(GraphicsConfiguration gc, String imageName) throws IOException 
	{
		//System.out.println("trying to get image: " + "resources" + Integer.toString(GlobalOptions.xScreen) + "/" + imageName);
		
		BufferedImage source = null;
		try {
			source = ImageIO.read(ResourceStream.getResourceStream(Art.class, "resources" + Integer.toString(GlobalOptions.xScreen) + "/" + imageName));
			// System.out.println("source: " + source);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (source == null) {
			// System.out.println("Could not read image through getResourceAsStream");
			imageName = img + imageName;
			File file = new File(imageName);
			// System.out.println("File: " + file + ", exists " + file.exists()
			// + ", length " + file.length ());
			source = ImageIO.read(file);
			// System.out.println("source: " + source);
		}
		if (source == null) { // still!!
			// System.out.println("Could not read image through ImageIO either!");
			File file = new File(imageName);
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			String suffix = imageName.substring(imageName.length() - 3, imageName.length());
			// System.out.println("suffix: " + suffix);
			ImageReader reader = ImageIO.getImageReadersBySuffix(suffix).next();
			// System.out.println("Let's try this reader: " + reader);
			reader.setInput(iis, true);
			source = reader.read(0);
			// System.out.println("source: " + source);
		}
		// System.out.println("gc: " + gc);
		Image image = gc.createCompatibleImage(source.getWidth(), source.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.setComposite(AlphaComposite.Src);
		g.drawImage(source, 0, 0, null);
		g.dispose();
		return image;
	}

	private static SpriteSheet cutImage(GraphicsConfiguration gc, String imageName, int xSize, int ySize) throws IOException 
	{
		xSize*=GlobalOptions.resolution_factor;
		ySize*=GlobalOptions.resolution_factor;
				
		Image source = getImage(gc, imageName);		
				
		Image[][] cutsource = new Image[source.getWidth(null) / xSize][source.getHeight(null) / ySize];		
		
		for (int x = 0; x < source.getWidth(null) / xSize; x++) 
		{
			for (int y = 0; y < source.getHeight(null) / ySize; y++) 
			{
				Image image = gc.createCompatibleImage(xSize, ySize, Transparency.TRANSLUCENT);
				Graphics2D g = (Graphics2D) image.getGraphics();
								
				//g.setComposite(AlphaComposite.Src);
				
				//ENABLE PARTIAL TRANSPARENCY
				AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC);
		        g.setComposite(ac);						
				
				g.drawImage(source, -x * xSize, -y * ySize, null);
				g.dispose();
				
				cutsource[x][y] = image;
			}
		}		
		
		System.out.println(imageName);
		SpriteSheet ret = new SpriteSheet(xSize, ySize, source.getWidth(null) / xSize, source.getHeight(null) / ySize, cutsource);
		
		return ret;
	}
	public static BufferedImage createResizedCopy(Image originalImage, 
			int scaledWidth, int scaledHeight, 
			boolean preserveAlpha) {
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
		g.dispose();
		return scaledBI;
	}

}
