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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Observable;

import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioWorld.engine.GlobalOptions;
import marioWorld.engine.LevelScene;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.SpriteRenderProperties;
import marioWorld.engine.SpriteRenderer;
import marioWorld.engine.SpriteSheet;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.level.SpriteTemplate;
import marioWorld.utils.ResetStaticInterface;

public class Sprite extends Observable implements ISprite, ResetStaticInterface {
	//TODO: update player kinds (clark etc) and individual class names
	public static final int KIND_NONE = 0;
	public static final int KIND_PLAYER = -31;
	public static final int KIND_BRUCE = -1;
	public static final int KIND_CLARK = -2;
	public static final int KIND_PETER = -3;
	public static final int KIND_JAY = -4;
	public static final int KIND_LUKO = 2;
	public static final int KIND_LUKO_WINGED = 3;
	public static final int KIND_RED_VIRUS = 4;
	public static final int KIND_RED_VIRUS_WINGED = 5;
	public static final int KIND_GREEN_VIRUS = 6;
	public static final int KIND_GREEN_VIRUS_WINGED = 7;
	public static final int KIND_BULLET_WILLY = 8;
	public static final int KIND_SHALLY = 9;
	public static final int KIND_SHALLY_WINGED = 10;
	// public static final int KIND_ENEMY_FLOWER = 11;
	public static final int KIND_ENEMY_FLOWER = 12;
	public static final int KIND_GRUMPY = 13;
	public static final int KIND_WRENCH = 14;
	public static final int KIND_BULB_FLOWER = 15;
	public static final int KIND_PARTICLE = 21;
	public static final int KIND_SPARCLE = 22;
	public static final int KIND_ENERGY_ANIM = 20;
	public static final int KIND_FIREBALL = 25;
	public static final int KIND_END_ITEM = 1;

	public static final int KIND_UNDEF = -42;

	public static SpriteContext spriteContext;
	public byte kind = KIND_UNDEF;

	public float x, xOld, xOldOld;
	public float y, yOld, yOldOld;	
	public float xa, xaOld;
	public float ya, yaOld;
	
	public int mapX, mapY;
	
	// flag, if Sprite is carried by anyone
	private Sprite carriedBy = null;
	// flag, if Sprite carries anyone
	private Sprite carries = null;
	
	
	public Sprite getCarriedBy() {
		return carriedBy;
	}

	public void setCarriedBy(Sprite carriedBy) {
		this.carriedBy = carriedBy;
	}

	public Sprite getCarries() {
		return carries;
	}

	public void setCarries(Sprite carries) {
		this.carries = carries;		
//		if(carries==null)
//		{
//			try
//			{
//				throw new Exception();
//			} catch (Exception e)
//			{
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	/*
	 * Graphics and rendering
	 * */
	public SpriteRenderer renderer = null;

	
	
	private boolean isAlive = true;

	public int layer = 1;

	public SpriteTemplate spriteTemplate;

	public Sprite()
	{
		classesWithStaticStuff.add(this.getClass());
	}

	public void move() 
	{
		x += xa;
		y += ya;
	}

	/**
	 * Default rendering function for sprites  
	 */
	public void render(Graphics2D og, float alpha) 
	{
//		if (!renderer.isVisible)
//			return;
//
//		// int xPixel = (int)(xOld+(x-xOld)*alpha)-xPicO;
//		// int yPixel = (int)(yOld+(y-yOld)*alpha)-yPicO; 
//		
//		int xPixel = (int) x * GlobalOptions.resolution_factor - renderer.renderFORx;
//		int yPixel = (int) y * GlobalOptions.resolution_factor - renderer.renderFORy;
//		
//		//WORKAROUND!!!
//		renderer.selectedFrameX = Math.min(renderer.selectedFrameX,sheet.xImages());
//		renderer.selectedFrameY = Math.min(renderer.selectedFrameY,sheet.yImages());
//		
//		og.drawImage(sheet.images[renderer.selectedFrameX][renderer.selectedFrameY], xPixel + (renderer.flipFrameX ? renderer.renderWidth : 0), yPixel + (renderer.flipFrameY ? renderer.renderHeight : 0), renderer.flipFrameX ? -renderer.renderWidth : renderer.renderWidth, renderer.flipFrameY ? -renderer.renderHeight : renderer.renderHeight, null);
//		
//		if (GlobalOptions.Labels)
//			og.drawString("" + xPixel + "," + yPixel, xPixel, yPixel);
		
		renderer.render(og, alpha);
	}

	public final void tick(/* boolean isReal */) {
		xOldOld = xOld;
		yOldOld = yOld;
		xOld = x;
		yOld = y;
		xaOld = xa;
		yaOld = ya;
		mapX = (int) (xOld / 16);
		mapY = (int) (yOld / 16);
		move();
	}

	public final void tickNoMove() {
		xOld = x;
		yOld = y;
		xaOld = xa;
		yaOld = ya;
	}

	// public float getX(float alpha)
	// {
	// return (xOld+(x-xOld)*alpha)-xPicO;
	// }
	//
	// public float getY(float alpha)
	// {
	// return (yOld+(y-yOld)*alpha)-yPicO;
	// }

	public void collideCheck(Sprite targetSprite) {
	}

	public void bumpCheck(int xTile, int yTile) {
	}

	public boolean grumpyCollideCheck(Grumpy grumpy) {
		return false;
	}

	public void release(Player player) {
	}

	public boolean fireballCollideCheck(Fireball fireball) {
		return false;
	}

	public GlobalContinuous getPosition() {
		return new GlobalContinuous(x, y);
	}

	/**
	 * Height in continuous coordinates (i.e. pixels) Override where needed
	 * 
	 * @return
	 */
	public int getHeight() {
		return 0;
	}

	@Override
	protected Sprite clone() {
		Sprite ret = cloneNonRecursive();
		if (spriteTemplate == null) {
			ret.spriteTemplate = null;
		} else {
			ret.spriteTemplate = spriteTemplate.cloneNonRecursive();
			ret.spriteTemplate.sprite = ret;
		}
		return ret;
	}

	//TODO: nonsense function!?
	public Sprite cloneWithNewLevelScene(LevelScene ls) {
		return null;
	}

	public Sprite cloneNonRecursive() {
		Sprite ret;
		try {
			ret = (Sprite) super.clone();
		} catch (CloneNotSupportedException e) {
			ret = new Sprite();
		}
		ret.kind = kind;
		ret.xOld = xOld;
		ret.xOldOld = xOldOld;
		ret.yOld = yOld;
		ret.yOldOld = yOldOld;
		ret.x = x;
		ret.y = y;
		ret.xa = xa;
		ret.ya = ya;
		ret.xaOld = xaOld;
		ret.yaOld = yaOld;
		ret.mapX = mapX;
		ret.mapY = mapY;
		ret.isAlive = isAlive;		
		ret.renderer=renderer.clone();
		ret.layer = layer;
		ret.spriteTemplate = null;
		return ret;
	}
	
	
	 /**
	  * set the boolean isAlive to false
	  * get called when sprite dies
	  */
	protected void die(){
		this.isAlive = false;
	}

	@Override
	public HashMap<Field, Object> getAttributeMap() {
		Field[] fields = getClass().getFields();
		HashMap<Field, Object> map = new HashMap<Field, Object>();
		try { // loop over all fields
			for (Field field : fields) {
				map.put(field, field.get(this)); // read the value inside the
													// field for the instance
													// given by "this" and put
													// the value in the map
			}
		} catch (IllegalAccessException e) {
			new Throwable(e).printStackTrace();
		}
		return map;
	}

	/**
	 * @param timeDelay
	 *            For timeDelay=0 (1, 2), this method uses x, (xOld, xOldOld), analogous for y.
	 * @return
	 */
	@Override
	public GlobalContinuous getPosition(int timeDelay) {
		switch (timeDelay) {
		case 0:
			return new GlobalContinuous(x, y);
		case 1:
			return new GlobalContinuous(xOld, yOld);
		case 2:
			return new GlobalContinuous(xOldOld, yOldOld);
		default:
			new Throwable("timeDelay " + timeDelay + " not supported.").printStackTrace();
			return null;
		}
	}

	@Override
	public GlobalContinuous getVelocity(int timeDelay) {
		switch (timeDelay) {
		case 0:
			return new GlobalContinuous(xa, ya);
		case 1:
			return new GlobalContinuous(xaOld, yaOld);
		default:
			new Throwable("timeDelay " + timeDelay + " not supported.").printStackTrace();
			return null;
		}
	}

	@Override
	public GlobalCoarse getMapPosition(int timeDelay) {
		if (timeDelay == 0)
			return new GlobalCoarse(mapX, mapY); // faster than:
		return getPosition(timeDelay).toGlobalCoarse();
	}

	@Override
	public PlayerWorldObject getType() {
		return PlayerWorldObject.getElement(this);
	}

	@Override
	public String toString() {
		String s = getType().name();
		s += " | Pos: (" + x + ", " + y + ")";
		s += " | Vel: (" + xa + ", " + ya + ")";
		s += " | MapPos: (" + mapX + ", " + mapY + ")";
		return s;
	}

	@Override
	public boolean isFacingDefined() {
		// Override where needed
		return false;
	}

	@Override
	public int getFacing() {
		// Override where needed
		return 0;
	}

	@Override
	public boolean isOnGroundDefined() {
		// Override where needed
		return false;
	}

	@Override
	public boolean isOnGround() {
		// Override where needed
		return true;
	}

	@Override
	public boolean isDeadTimeDefined() {
		// Override where needed
		return false;
	}

	@Override
	public int getDeadTime() {
		// Override where needed
		return 0;
	}

	public static void deleteStaticAttributes() {
		spriteContext = null;
	}

	public static void resetStaticAttributes() {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isAlive(){
		return isAlive;
	}
	
	protected void setIsAlive(boolean living){
		this.isAlive = living;
	}

}
