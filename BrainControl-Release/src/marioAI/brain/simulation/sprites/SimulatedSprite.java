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
package marioAI.brain.simulation.sprites;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import marioAI.brain.Generalizer;
import marioAI.brain.simulation.LevelSceneAdapter;
import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.collision.CollisionAtPosition;
import marioAI.util.GeneralUtil;
import marioWorld.engine.PlayerWorldObject;
import marioWorld.engine.coordinates.CoordinatesUtil;
import marioWorld.engine.coordinates.GlobalCoarse;
import marioWorld.engine.coordinates.GlobalContinuous;
import marioWorld.engine.sprites.Enemy;
import marioWorld.engine.sprites.ISprite;
import marioWorld.engine.sprites.Player;
import marioWorld.engine.sprites.Sprite;

public class SimulatedSprite implements ISprite {
	public static final float GROUND_INERTIA = 0.89f;
	public static final float AIR_INERTIA = 0.89f;
	public static final int STANDARD_HEIGHT = 24;
	public static final int SMALL_HEIGHT = 12;

	/** position. Old refers to the last time step. */
	public float xOld, xOldOld, yOld, yOldOld, x, y;
	/** velocity component (in x or y direction separately) */
	public float xa, ya, xaOld, yaOld;
	protected final boolean avoidCliffs;
	protected final boolean winged;
	/**
	 * whether mario is standing on ground and not sliding. onGround and sliding are mutually exclusive
	 */
	public boolean onGround = true;
	/**
	 * Mario may basically jump if he is either onGround or sliding and the jump key is not pressed. This flag is somewhat redundant to onGround and sliding,
	 * but the dependency works time-delayed :(
	 */
	public boolean mayJump;
	/** whether mario is sliding. sliding and onGround are mutually exclusive */
	public boolean sliding;
	/**
	 * the absolute value of this is how long mario has already been jumping. positive values encode jumps straight upwards, negative values encode diagonal
	 * jumps. (cf. Sprite.Mario)
	 */
	public int jumpTime;

	public final PlayerWorldObject type;
	public final boolean needsTracking;

	/** -1: left, +1: right */
	public int facing;
	
	private SimulatedSprite carries = null;
	private SimulatedSprite carriedBy = null;	
	
	/**
	 * The original sprite from the engine.
	 */
	protected final Sprite originalSprite;

	public SimulatedSprite(Sprite sprite) 
	{
		this.type = inferType(sprite);
		//@c2-pavel: ex-pwo.PLAYER (NOT)
		this.needsTracking = (type != PlayerWorldObject.BULB_FLOWER && !type.isPlayer());
		this.x = sprite.x;
		this.y = sprite.y;
		this.xOld = sprite.xOld;
		this.yOld = sprite.yOld;
		this.xOldOld = sprite.xOldOld;
		this.yOldOld = sprite.yOldOld;
		this.xa = sprite.xa;
		this.ya = sprite.ya;
		this.xaOld = sprite.xaOld;
		this.yaOld = sprite.yaOld;
		this.winged = type.isWinged();
		//this.avoidCliffs = type == PlayerWorldObject.RED_KOOPA;
		this.avoidCliffs = type == PlayerWorldObject.RED_VIRUS;
		this.originalSprite = sprite;
		this.carriedBy=null;
		this.carries=null;
	}

	// public static SimulatedSprite createSimulatedSprite(Sprite sprite) {
	// MarioWorldObject mWO = sprite.getType();
	// SimulatedSprite ret = new SimulatedSprite(sprite,
	// mWO!=MarioWorldObject.FIRE_FLOWER);
	// switch (mWO) {
	// /**instances of Enemy*/
	// case LUKO:
	// case LUKO_WINGED:
	// case RED_KOOPA:
	// case RED_KOOPA_WINGED:
	// case GREEN_KOOPA:
	// case GREEN_KOOPA_WINGED:
	// case SHALLY:
	// case SHALLY_WINGED:
	// case PIRHANA_PLANT:
	// case FLOWER_ENEMY:
	// ret = new SimulatedEnemy(sprite);
	// Enemy enemy = (Enemy)sprite;
	// ret.facing = enemy.facing;
	// ret.onGround = enemy.isOnGround();
	// break;
	// case MARIO:
	// ret = new SimulatedMario((Mario)sprite);
	// break;
	// case BULLET_WILLY:
	// BulletWilly bulletWilly = (BulletWilly)sprite;
	// ret.facing = bulletWilly.facing;
	// break;
	// case BULB_FLOWER:
	// BulbFlower bulbFlower = (BulbFlower)sprite;
	// ret.facing = bulbFlower.facing;
	// case GRUMPY_STILL:
	// case GRUMPY_MOVING:
	// Grumpy grumpy = (Grumpy)sprite;
	// ret.facing = grumpy.facing;
	// ret.onGround = grumpy.
	// case WRENCH:
	// case FIREBALL:
	// ret = new SimulatedSprite(sprite, mWO!=MarioWorldObject.FIRE_FLOWER);
	// ret.facing
	// case ENERGY_ANIM:
	// case PARTICLE:
	// case SPARCLE:
	// case UNDEF:
	// return null;
	// default:
	// break;
	// }
	// }


	public SimulatedSprite(SimulatedPlayer sprite) {
		this.type = sprite.type;
		//@c2-pavel: ex-pwo.PLAYER (NOT)
		this.needsTracking = (type != PlayerWorldObject.BULB_FLOWER && !type.isPlayer());
		this.x = sprite.x;
		this.y = sprite.y;
		this.xOld = sprite.xOld;
		this.yOld = sprite.yOld;
		this.xOldOld = sprite.xOldOld;
		this.yOldOld = sprite.yOldOld;
		this.xa = sprite.xa;
		this.ya = sprite.ya;
		this.xaOld = sprite.xaOld;
		this.yaOld = sprite.yaOld;
		this.winged = type.isWinged();
		//this.avoidCliffs = type == PlayerWorldObject.RED_KOOPA;
		this.avoidCliffs = type == PlayerWorldObject.RED_VIRUS;
		this.originalSprite = sprite.originalSprite;
		this.carriedBy=null;
		this.carries=null;
	}

	public SimulatedSprite getCarries() {
		return carries;
	}

	public void setCarries(SimulatedSprite carries) 
	{
		this.carries = carries;
	}

	public SimulatedSprite getCarriedBy() {
		return carriedBy;
	}

	public void setCarriedBy(SimulatedSprite carriedBy) 
	{
		this.carriedBy = carriedBy;
	}

	public static SimulatedSprite createSimulatedSprite(Sprite sprite) {
		if (!sprite.getType().isRelevantToLearning()) {
			return null;
		}
		if (!sprite.isAlive()) {
			/**
			 * this sprite is already dead. It only exists in the engine for GUI purposes a few more frames. Sadly, it's the same instance as while living. We
			 * do not want to include this in our simulation.
			 */
			return null;
		}
		SimulatedSprite ret;
		if (sprite.kind == Sprite.KIND_GRUMPY || sprite instanceof Enemy) {
			ret = new SimulatedEnemy(sprite);
		} else {
			ret = new SimulatedSprite(sprite);
		}
		if (sprite.isFacingDefined()) {
			ret.facing = sprite.getFacing();
		}
		if (sprite.isOnGroundDefined()) {
			ret.onGround = sprite.isOnGround();
		}
		return ret;
	}

	/**
	 * Overridden in Enemy to distinguish between winged/non-winged lukos/koopas/... and still/moving grumpys
	 * 
	 * @param spriteKind
	 * @return
	 */
	protected PlayerWorldObject inferType(Sprite sprite) {
		return PlayerWorldObject.getElement(sprite);
	}
	
	/**
	 * returns whether the original sprite is dead
	 * @return
	 */
	public boolean isAlive(){
		return this.originalSprite.isAlive();
	}

	/**
	 * Returns whether this and another instance stem from the same underlying engine sprite.
	 * 
	 * @param other
	 * @return
	 */
	public boolean stemsFromSameEngineSprite(SimulatedSprite other) {
		if(other!=null)	//TODO: why is this needed?
			return originalSprite == other.originalSprite;
		else
			return false;
	}

	public int customHashCode() {
		return originalSprite.hashCode();
	}

	@Override
	public SimulatedSprite clone() 
	{
		try 
		{
			SimulatedSprite spriteclone = (SimulatedSprite) super.clone();
			
			//ATTENTION: a player cannot directly be cloned because the carry flags refer to the real sprites!
			//set to null for safety... have to be set to the new, cloned players afterwards!
			spriteclone.setCarriedBy(null);
			spriteclone.setCarries(null);
			
			return spriteclone;
		} 
		catch (Exception e) 
		{
			// Should never happen.
			throw new RuntimeException(e);
		}
	}

	@Override
	public HashMap<Field, Object> getAttributeMap() {
		Field[] fields = SimulatedSprite.class.getDeclaredFields();
		HashMap<Field, Object> map = new HashMap<Field, Object>();
		try { // loop over all fields
			for (Field field : fields) {
				/** ignore code generated to support enums */
				if (!field.getName().contains("SWITCH_TABLE")) {
					/** put field & value */
					map.put(field, field.get(this)); // read the value inside
					// the
					// field for the
					// instance
					// given by "this" and
					// put
					// the value in the map
				}
			}
		} catch (IllegalAccessException e) {
			new Throwable(e).printStackTrace();
		}
		return map;
	}

	// /**
	// * Override this method to implement sprite-specific behavior
	// *
	// * @param levelSceneAdapter required in overridden methods
	// * @param keys
	// * only relevant if this is Mario
	// * @return if a collision between mario and a sprite occured. WARNING:
	// this is currently only supported by SimulatedMario, not by
	// SimulatedEnemy.
	// */
	// protected boolean move(MarioWorldObject[][] map, boolean[] keys) {
	// x += xa;
	// y += ya;
	// }

	public void tickNoMove() {
		xOld = x;
		yOld = y;
		xaOld = xa;
		yaOld = ya;
	}
	
	
	
	/**
	 * 
	 * @param map
	 * @param keys
	 *            only relevant if this is Mario
	 * @param simulatedPlayersToCarry
	 * @return whether the static object with which mario will collide
	 */
	protected List<CollisionAtPosition> move(PlayerWorldObject[][] map, boolean[] keys, ArrayList<SimulatedPlayer> simulatedPlayersToCarry) {
		// Override where needed
		return null;
	}

	
	/**
	 * 
	 * @param map
	 * @param keys
	 *            only relevant if this is Mario
	 * @return whether the static object with which mario will collide
	 */
	protected List<CollisionAtPosition> move(PlayerWorldObject[][] map, boolean[] keys, SimulatedLevelScene scene)
	{
		return null;
	}


	/**
	 * only used for mario.
	 * 
	 * @return
	 */
	public int getHealth() {
		return Integer.MIN_VALUE;
	}

	/**
	 * move with the specified velocity, EITHER in x-direction, OR in y-direction. If a diagonal move is desired, call this method first with a move in
	 * x-direction, then in y-direction. This method also implements collision-dependent movement changes. The method modifies the attributes x, y, xa, ya and
	 * sliding.
	 * 
	 * @param xa
	 *            x-component of the velocity
	 * @param ya
	 *            y-component of the velocity
	 * @return a collision which is relevant to learning. (Null if none occured).
	 */
	protected Entry<Integer, CollisionAtPosition> move(float xa, float ya, PlayerWorldObject[][] map) {
		if(!this.isAlive()){
			return null;
		}
		//@c2-pavel: ex-pwo.PLAYER
		final float addXForIsBlockingToRight = Player.USE_SYMMETRIC_COLLISIONS && this.type.isPlayer() ? (float) (-1E-3) : 0;
		if (xa != 0 && ya != 0) {
			throw new RuntimeException("Please call with one velocity component at a time.");
		}
		final float HALF_CELL_SIZE_X = CoordinatesUtil.UNITS_PER_TILE / 2;
		final float HALF_CELL_SIZE_Y = CoordinatesUtil.UNITS_PER_TILE / 2;
		final int bumpingWidth = type.getWidthForBumping();
		final int collisionWidth = type.getWidthForCollisionDetection();
		int tempHeight = this.carries != null? getHeight()*2 : getHeight();

		/**
		 * move component-wise (first x-movement, than y-movement), maximally as far as half a cell (16/2=8). Check for collision. If no collision, repeat.
		 * Else, break.
		 */
		Entry<Integer, CollisionAtPosition> collision = null;
		while (xa > HALF_CELL_SIZE_X) {
			if ((collision = move(HALF_CELL_SIZE_X, 0f, map)).getKey() > 0) {
				return collision;
			}
			xa -= HALF_CELL_SIZE_X;
		}
		while (xa < -HALF_CELL_SIZE_X) {
			if ((collision = move(-HALF_CELL_SIZE_X, 0f, map)).getKey() > 0) {
				return collision;
			}
			xa += HALF_CELL_SIZE_X;
		}
		while (ya > HALF_CELL_SIZE_Y) {
			if ((collision = move(0f, HALF_CELL_SIZE_Y, map)).getKey() > 0) {
				return collision;
			}
			ya -= HALF_CELL_SIZE_Y;
		}
		while (ya < -HALF_CELL_SIZE_Y) {
			if ((collision = move(0f, -HALF_CELL_SIZE_Y, map)).getKey() > 0) {
				return collision;
			}
			ya += HALF_CELL_SIZE_Y;
		}

		boolean turnAroundAtEndOfCliff = false;
		/** actual collision check is done here */
		if (xa != 0 || ya != 0) {
			collision = GeneralUtil.detectCollisionWithStaticObjects(map, getHealth(), x, y, xa, ya, collisionWidth, tempHeight, type, wasInvulnerable(),
					addXForIsBlockingToRight);
			//@c2-pavel: ex-pwo.PLAYER (NOT)
			if (!this.type.isPlayer() && xa != 0) {
				turnAroundAtEndOfCliff = turnAroundAtEndOfCliff(map, avoidCliffs, onGround, x, y, xa, collisionWidth);
			}
		} else {
			collision = new AbstractMap.SimpleEntry<Integer, CollisionAtPosition>(0, null);
		}

		// /** handle sliding flag */
		//@c2-pavel: ex-pwo.PLAYER
		if (xa != 0 && type.isPlayer()) {
			boolean oldSliding = sliding;
			sliding = (collision.getKey() >= 3); // mario only slides if he

			// fully collides. 3 is the
			// number of points which
			// are checked for
			// collision.
			// only if all 3 collide, he slides. The points can be for example
			// mario's left top, left middle, and left bottom if he is moving
			// left.
		}

		/** handle collision by replacing mario at a not occupied position. */
		if (collision.getKey() > 0 || turnAroundAtEndOfCliff) {
			if (xa < 0) {
				x = (int) ((x - bumpingWidth) / 16) * 16 + bumpingWidth;
				this.xa = 0;
			}
			if (xa > 0) {
				//@c2-pavel: ex-pwo.PLAYER (NOT)
				x = (int) ((x + bumpingWidth + addXForIsBlockingToRight) / 16 + 1) * 16 - bumpingWidth - (!type.isPlayer() ? 1 : 0);
				this.xa = 0;
			}
			if (ya < 0) {
				y = (int) ((y - tempHeight) / 16) * 16 + tempHeight;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0) {
				//@c2-pavel: ex-pwo.PLAYER
				float playerSpecificChange = type.isPlayer() ? -1 : 0;
				y = (int) ((y + playerSpecificChange) / 16 + 1) * 16 - 1;
				onGround = true;

				
				
			}
			//@c2-pavel: ex-pwo.PLAYER
			if (turnAroundAtEndOfCliff || (!this.type.isPlayer() && xa != 0 && collision.getKey() > 0)) {
				facing = -facing;
			}
			/** if no collision happened, simply execute the movement */
		} else {
			x += xa;
			y += ya;

			return collision;
		}
		return collision;
	}

	// /**
	// * Checks, if a collision with a static object would occur, if the sprite
	// executed the desired movement.
	// * This method does not check for collisions with moving objects.
	// *
	// * @param map this contains the static objects, with
	// * @param avoidCliffs whether the moving sprite turns around at the end of
	// a platform
	// * @param onGround whether the moving sprite (e.g. mario) stands on the
	// ground
	// * @param x position of the moving sprite before movement
	// * @param y position of the moving sprite before movement
	// * @param xa desired movement. This is executed fully if no collision
	// occurs. Else, it is executed partly (as far as possible).
	// * @param ya desired movement. This is executed fully if no collision
	// occurs. Else, it is executed partly (as far as possible).
	// * @param width width of the moving sprite.
	// * @param height height of the moving sprite.
	// * @return the static object with which the sprite collided (Null if no
	// collision occured).
	// */
	// protected static ConditionDirectionPair
	// testForCollision(MarioWorldObject[][] map, final int marioHealth, final
	// float x, final float y, final float xa, final float ya, final float
	// width, final float height, final boolean isMario) {
	// /** determine whether the point (x+xa, y+ya) collides with anything. Six
	// * testing points around this are relevant: NE, NW, SE, SW, E, W, where
	// * N=North, ... Asymmetric, because mario is taller than he is wide.
	// * depending on the movement (left, right, up, down), only two or three
	// * of those four testing points need to be tested. if moving up: test NW
	// * and NE. down: SW and SE. left: NW and SW. right: NE and SE.
	// * For some sprites, turning around at the end of a cliff is handled,
	// too.*/
	// float[] testingPointsX = new float[xa != 0 ? 3 : 2];
	// float[] testingPointsY = new float[xa != 0 ? 3 : 2];
	// float bottomAdd = ya > 0 ? 1 : 0;
	// testingPointsX[0] = x + xa + (xa > 0 ? width : -width);
	// // test first mario's left side, unless he's moving to the right. In
	// // that case, test twice right
	// testingPointsX[1] = x + xa + (xa < 0 ? -width : width);
	// // then test mario's right side, unless he's moving to the left...
	// testingPointsY[0] = y + ya + (ya > 0 ? bottomAdd : -height);
	// // test mario's head unless he's moving down.
	// // In that case, test twice bottom.
	// testingPointsY[1] = y + ya + (ya < 0 ? -height : bottomAdd);
	// if (xa != 0) {
	// testingPointsX[2] = testingPointsX[1];
	// // or x2=x0, doesn't matter. x1 and x0 are the same if
	// // x2 is required
	// testingPointsY[2] = y + ya - height / 2;
	// }
	//
	// /** now iterate over all 2 or 3 points */
	// for (int i = 0; i < testingPointsY.length; i++) {
	// GlobalContinuous testingPoint = new GlobalContinuous(testingPointsX[i],
	// testingPointsY[i]);
	// if (LevelSceneAdapter.isInMap(map[0].length, map.length,
	// testingPoint).getKey()
	// && LevelSceneAdapter.isBlocking(map, testingPoint, ya)) {
	// // GlobalCoarse oldOldGlobalDiscreteMarioPos =
	// simulatedMario.getPosition(2).toGlobalCoarse();
	// // CollisionDirection direction = GeneralUtil.getDirection(x, y,
	// testingPoint.x, testingPoint.y);
	// CollisionDirection direction = GeneralUtil.getDirection(xa, ya, isMario);
	// MarioWorldObject collisionObject = LevelSceneAdapter.getObject(map,
	// testingPoint);
	// return new ConditionDirectionPair(new Condition(collisionObject,
	// marioHealth), direction);
	// // collide = true;
	// // break;
	// }
	// }
	// return null;
	// }

	protected static boolean turnAroundAtEndOfCliff(PlayerWorldObject[][] map, final boolean avoidCliffs, final boolean onGround, final float x, final float y,
			final float xa, final float width) {
		/** and fourth point */
		if (avoidCliffs && onGround) {
			GlobalContinuous fourthPoint = new GlobalContinuous(x + xa + (xa > 0 ? width : -width), y + CoordinatesUtil.UNITS_PER_TILE);
			if (LevelSceneAdapter.isInMap(map[0].length, map.length, fourthPoint).getKey()
					// different to above, now we test if the tile is FREE
					&& !LevelSceneAdapter.isBlocking(map, fourthPoint, 1)) {
				return true;
				// collide = true;
			}
		}
		return false;
	}

	/**
	 * simulates all movement-related changes given the keys. Part of the brain's forward model. This includes movement-related changes done by object
	 * interactions. Changes which are irrelevant for the movement (at least in the current time step) are excluded, such as destruction of other objects or
	 * mario getting hurt.
	 * 
	 * @param keys
	 *            only relevant if this is mario
	 * @param levelSceneAdapter
	 * @param simulatedPlayersToCarry
	 */
	public final List<CollisionAtPosition> tick(PlayerWorldObject[][] map, boolean[] keys, SimulatedLevelScene scene) 
	{
		xOldOld = xOld;
		yOldOld = yOld;
		xOld = x;
		yOld = y;
		xaOld = xa;
		yaOld = ya;
		return move(map, keys, scene);
	}

	// public final void tickNoMove() {
	// xOldOld = xOld;
	// yOldOld = yOld;
	// xOld = x;
	// yOld = y;
	// }
	
	public GlobalContinuous getOriginalSpritePosition() {
		return originalSprite.getPosition();
	}

	public Sprite getOriginalSprite() {
		return originalSprite;
	}	
	
	public GlobalContinuous getOriginalSpriteVelocity() {
		return new GlobalContinuous(originalSprite.xa, originalSprite.ya);
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

	public void setPosition(GlobalContinuous set) 
	{
		x=set.x;
		y=set.y;
	}
	
	public void setPositionOfCarriedSprite(GlobalContinuous set){
		if(this.carries != null){
			this.carries.setPosition(set);
		}
	}
	
	/**
	 * @param timeDelay
	 *            For timeDelay=0 (1, 2), this method uses x, (xOld, xOldOld), analogous for y.
	 * @return
	 */
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

	/**
	 * Height in continuous coordinates (i.e. pixels) Override where needed
	 * 
	 * @return
	 */
	public int getHeight() {
		return originalSprite.getHeight();
	}

	@Override
	public PlayerWorldObject getType() {
		return type;
	}

	@Override
	public GlobalCoarse getMapPosition(int timeDelay) 
	{
		return getPosition(timeDelay).toGlobalCoarse();
	}

	// public int getOldInvulnerableTime() {
	// return 0;
	// }

	public boolean wasInvulnerable() {
		// Override where needed
		return false;
	}

	@Override
	public boolean isFacingDefined() {
		if (this.type.isStatic) {
			return false;
		}
		switch (this.type) {
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
			/*
		case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
		//case GREEN_VIRUS:
		case GRUMPY:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY_WINGED:
		case SHALLY:
		case SHALLY_WINGED:
		case GRUMPY_STILL:
		case GRUMPY_MOVING:
		case WRENCH:
		case FLOWER_ENEMY:
		case BULB_FLOWER:
		case FIREBALL:
		case BULLET_WILLY:
			return true;
		case ENERGY_ANIM:
		case PARTICLE:
		case SPARCLE:
			return false;
		default:
			new Throwable("Encountered unsupported enum field " + this.type.name()).printStackTrace();
			return false;
		}
	}

	@Override
	public int getFacing() {
		return facing;
	}

	@Override
	public boolean isOnGroundDefined() {
		if (this.type.isStatic) {
			return false;
		}
		switch (this.type) {
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
			/*
		case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
		//case GREEN_VIRUS:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY:
		case GRUMPY_WINGED:
		case SHALLY:
		case SHALLY_WINGED:
		case GRUMPY_STILL:
		case GRUMPY_MOVING:
		case WRENCH:
		case FIREBALL:
			return true;
		case BULLET_WILLY:
		case ENERGY_ANIM:
		case PARTICLE:
		case SPARCLE:
		case BULB_FLOWER:
		case FLOWER_ENEMY:
			return false;
		default:
			new Throwable("Encountered unsupported enum field " + this.type.name()).printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isOnGround() {
		return onGround;
	}

	public boolean isMayJump() {
		return mayJump;
	}

	public boolean isSliding() {
		return sliding;
	}

	@Override
	public String toString() {
		String s = type.name();
		s += " | Pos: (" + x + ", " + y + ")";
		s += " | Vel: (" + xa + ", " + ya + ")";
		GlobalCoarse globalCoarse = new GlobalContinuous(x, y).toGlobalCoarse();
		s += " | MapPos: (" + globalCoarse.x + ", " + globalCoarse.y + ")";
		return s;
	}

	/**
	 * WARNING: Simulated Sprites never define deadTime, so this method always returns false. However, engine sprites sometimes do define it, and accordingly
	 * THEIR isDeadTimeDefined() method sometimes returns true.
	 */
	@Override
	public boolean isDeadTimeDefined() {
		return false;
	}

	@Override
	public int getDeadTime() {
		return 0;
	}

	@Override
	public int hashCode() {
		return originalSprite.hashCode();
	}

	@Override
	public boolean equals(Object obj) 
	{
		return (this.originalSprite == ((SimulatedSprite) obj).originalSprite);
		
		//ORIGINAL:
//		if (this == obj)
//			return true;
//		try 
//		{
//			return this.originalSprite == ((SimulatedSprite) obj).originalSprite;
//		} 
//		catch (ClassCastException e) 
//		{
//			return false;
//		}
	}
	
	public boolean isHostile(){
		boolean isHostile = false;

		switch(this.type){
		//case LUKO:
		//case LUKO_WINGED:
		case GREEN_VIRUS:
		case GREEN_VIRUS_WINGED:
			/*
		case RED_KOOPA:
		case RED_KOOPA_WINGED:
		case GREEN_KOOPA:
		case GREEN_KOOPA_WINGED:
		*/
		case RED_VIRUS:
		case RED_VIRUS_WINGED:
		//case GREEN_VIRUS:
		//case GREEN_VIRUS_WINGED:
		case GRUMPY:
		case GRUMPY_WINGED:
		case BULLET_WILLY:
		case SHALLY:
		case SHALLY_WINGED:
		case PIRHANA_PLANT:
		case GRUMPY_STILL:
		case GRUMPY_MOVING:
		case FLOWER_ENEMY:
//		case FIRE_FLOWER:
			isHostile = true;
		default:           
		}//end switch

		return isHostile;
	}

	/**
	 * calculates the distance to otherSpirte
	 * returns Float.MAX_VALUE if otherSprite is null
	 * @param otherSprite
	 * @return
	 */
	public float distance(SimulatedSprite otherSprite){
		if(otherSprite == null){
			return Float.MAX_VALUE;
		}

		float xDist = this.getPosition(0).x - otherSprite.getPosition(0).x;
		float yDist = this.getPosition(0).y - otherSprite.getPosition(0).y;

		return (float)Math.sqrt(xDist * xDist + yDist * yDist);
	}
	/**
	 * 
	 * 
	 * !!USED FOR REACHABILITY; SLIGHTLY MODIFIED
	 * 
	 * 
	 * move with the specified velocity, EITHER in x-direction, OR in y-direction. If a diagonal move is desired, call this method first with a move in
	 * x-direction, then in y-direction. This method also implements collision-dependent movement changes. The method modifies the attributes x, y, xa, ya and
	 * sliding.
	 * 
	 * @param xa
	 *            x-component of the velocity
	 * @param ya
	 *            y-component of the velocity
	 * @return a collision which is relevant to learning. (Null if none occured).
	 */
	protected Entry<Integer, CollisionAtPosition> move(float xa, float ya, PlayerWorldObject[][] map,boolean modified) {
		if(!this.isAlive()){
			return null;
		}
		final float addXForIsBlockingToRight = Player.USE_SYMMETRIC_COLLISIONS && PlayerWorldObject.PLAYER == Generalizer.generalizePlayer(this.type)/*(this.type == PlayerWorldObject.MARIO || this.type == PlayerWorldObject.TOAD)*/ ? (float) (-1E-3) : 0;
		if (xa != 0 && ya != 0) {
			throw new RuntimeException("Please call with one velocity component at a time.");
		}
		final float HALF_CELL_SIZE_X = CoordinatesUtil.UNITS_PER_TILE / 2;
		final float HALF_CELL_SIZE_Y = CoordinatesUtil.UNITS_PER_TILE / 2;
		final int bumpingWidth = type.getWidthForBumping();
		final int collisionWidth = type.getWidthForCollisionDetection();
		
		int tempHeight = this.carries != null? getHeight()*2 : getHeight();

		/**
		 * move component-wise (first x-movement, than y-movement), maximally as far as half a cell (16/2=8). Check for collision. If no collision, repeat.
		 * Else, break.
		 */
		Entry<Integer, CollisionAtPosition> collision = null;
		while (xa > HALF_CELL_SIZE_X) {
			if ((collision = move(HALF_CELL_SIZE_X, 0f, map,true)).getKey() > 0) {
				return collision;
			}
			xa -= HALF_CELL_SIZE_X;
		}
		while (xa < -HALF_CELL_SIZE_X) {
			if ((collision = move(-HALF_CELL_SIZE_X, 0f, map,true)).getKey() > 0) {
				return collision;
			}
			xa += HALF_CELL_SIZE_X;
		}
		while (ya > HALF_CELL_SIZE_Y) {
			if ((collision = move(0f, HALF_CELL_SIZE_Y, map,true)).getKey() > 0) {
				return collision;
			}
			ya -= HALF_CELL_SIZE_Y;
		}
		while (ya < -HALF_CELL_SIZE_Y) {
			if ((collision = move(0f, -HALF_CELL_SIZE_Y, map,true)).getKey() > 0) {
				return collision;
			}
			ya += HALF_CELL_SIZE_Y;
		}

		boolean turnAroundAtEndOfCliff = false;
		/** actual collision check is done here */
		if (xa != 0 || ya != 0) {
			if(collision != null &&collision.getValue() != null) {
				Entry<Integer, CollisionAtPosition> collision2 = GeneralUtil.detectCollisionWithStaticObjects(map, getHealth(), x, y, xa, ya, collisionWidth, tempHeight, type, wasInvulnerable(),
						addXForIsBlockingToRight,true);
				if(collision2.getValue() != null) {
					collision = collision2;
				}
			} else {
				collision = GeneralUtil.detectCollisionWithStaticObjects(map, getHealth(), x, y, xa, ya, collisionWidth,tempHeight , type, wasInvulnerable(),
						addXForIsBlockingToRight,true);
			}

			
			if ((PlayerWorldObject.PLAYER != Generalizer.generalizePlayer(this.type)/*this.type != PlayerWorldObject.MARIO && this.type != PlayerWorldObject.TOAD*/) && xa != 0) {
				turnAroundAtEndOfCliff = turnAroundAtEndOfCliff(map, avoidCliffs, onGround, x, y, xa, collisionWidth);
			}
		} else {
			collision = new AbstractMap.SimpleEntry<Integer, CollisionAtPosition>(0, null);
		}

		// /** handle sliding flag */
		if (xa != 0 && PlayerWorldObject.PLAYER == Generalizer.generalizePlayer(type)/*(type == PlayerWorldObject.MARIO ||type == PlayerWorldObject.TOAD)*/) {
			sliding = (collision.getKey() >= 4); // mario only slides if he
			// fully collides. 3 is the
			// number of points which
			// are checked for
			// collision.
			// only if all 3 collide, he slides. The points can be for example
			// mario's left top, left middle, and left bottom if he is moving
			// left.
		}

		/** handle collision by replacing mario at a not occupied position. */
		if (collision.getKey() > 0 || turnAroundAtEndOfCliff) {
			if (xa < 0) {
				x = (int) ((x - bumpingWidth) / 16) * 16 + bumpingWidth;
				this.xa = 0;
				//von mir geï¿½ndert
				//TODO mayjump
				mayJump = true;
				
			}
			if (xa > 0) {
				x = (int) ((x + bumpingWidth + addXForIsBlockingToRight) / 16 + 1) * 16 - bumpingWidth - (PlayerWorldObject.PLAYER != Generalizer.generalizePlayer(type) ? 1 : 0);
				this.xa = 0;
				//von mir geï¿½ndert
				//TODO mayjump
				mayJump = true;
			}
			if (ya < 0) {
				y = (int) ((y - tempHeight) / 16) * 16 + tempHeight;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0) {
				float playerSpecificChange = PlayerWorldObject.PLAYER == Generalizer.generalizePlayer(type) ? -1 : 0;
				y = (int) ((y + playerSpecificChange) / 16 + 1) * 16 - 1;
				//puts marios x position in the middle of the colliding tile below
				x = collision.getValue().getPosition().toGlobalContinuous().x;
				onGround = true;
				//von mir geï¿½ndert
				//TODO mayjump
				mayJump = true;
				this.xa = 0;
			}
			if (turnAroundAtEndOfCliff || (PlayerWorldObject.PLAYER != Generalizer.generalizePlayer(type) && xa != 0 && collision.getKey() > 0)) {
				facing = -facing;
			}
			/** if no collision happened, simply execute the movement */
		} else {
			x += xa;
			y += ya;
		}
		return collision;
	}
}
