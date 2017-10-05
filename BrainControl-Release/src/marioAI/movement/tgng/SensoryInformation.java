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
package marioAI.movement.tgng;

import java.io.Serializable;

import marioAI.brain.simulation.SimulatedLevelScene;
import marioAI.brain.simulation.sprites.SimulatedPlayer;
import marioAI.brain.simulation.sprites.SimulatedSprite;
import marioAI.movement.tgng.Tgng.TgngType;
import marioWorld.engine.Logging;
import marioWorld.engine.PlayerWorldObject;

/**
 * Class which collects and manages all the relevant information for the TGNG
 * ALgorithm
 * 
 * @author benjamin
 *
 */
public class SensoryInformation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the simplified blockwise environment representation
	 */
	private byte[][] environment;

	/**
	 * agent's movement values
	 */
	private double xa, ya;

	/**
	 * agent's health calculated as lives * 3 + k where k is 0 if small, 1 if
	 * large and 2 if fire or can_destroy_objects this ensures that all possible
	 * states can be ordered where a bigger value means more lives/health
	 */
	private int health;

	/**
	 * if enemy is player same as health, if it is fireball constant 1
	 * (fireballs can't be killed), else 1 if living and 0 if dead
	 */
	private int enemyHealth;

	/**
	 * the closest enemy in the given environment
	 */
	private transient SimulatedSprite enemy;

	/**
	 * enemies relative coordinates to the player
	 */
	private double enemyX, enemyY;
	/**
	 * enemies movement values
	 */
	private double enemyXa, enemyYa;

	/**
	 * the generalized type of the enemy
	 */
	private TgngType type;

	/**
	 * default Constructor is useless for this
	 */
	@SuppressWarnings("unused")
	private SensoryInformation() {
	}

	/**
	 * an object containing a simple environment representation, the agent's
	 * velocity and health, the relative position and velocity of the closest
	 * enemy and its health
	 * 
	 * @param environment
	 * @param xa
	 * @param ya
	 * @param health
	 * @param enemy
	 * @param enemyHealth
	 * @param enemyX
	 * @param enemyY
	 * @param enemyXa
	 * @param enemyYa
	 */
	public SensoryInformation(byte[][] environment, double xa, double ya,
			int health, SimulatedSprite enemy, TgngType type,
			int enemyHealth, double enemyX, double enemyY, double enemyXa,
			double enemyYa) {
		this.environment = environment;
		this.xa = xa;
		this.ya = ya;
		this.health = health;
		this.enemy = enemy;
		this.type = type;
		this.enemyHealth = enemyHealth;
		this.enemyX = enemyX;
		this.enemyY = enemyY;
		this.enemyXa = enemyXa;
		this.enemyYa = enemyYa;
	}

	/**
	 * an object containing a simple environment representation, the agent's
	 * velocity and health, the relative position and velocity of the closest
	 * enemy and its health all parameters are extracted from the
	 * SimulatedLevelScene
	 * 
	 * @param scene
	 */
	public SensoryInformation(SimulatedLevelScene scene) {
		Logging.addLogger("SensoryInformation");

		this.environment = createSimplifiedObservation(scene);

		SimulatedPlayer player = scene.getPlanningPlayer();

		this.xa = player.getVelocity(0).x;
		this.ya = player.getVelocity(0).y;
		
		// TODO: validate this
		this.health = 1*3;
		//this.health = player.lives * 3;
		
		switch (player.getHealth()) {
		case 2:
			this.health += 2;
			break;
		case 1:
			this.health += 1;
			break;
		case 0:
			// this.health += 0; not necessary
			break;
		default:
			Logging.logWarning("SensoryInformation", "HealthType unknown." + player.getHealth());
		}

		SimulatedSprite closestEnemy = scene.getClosestEnemy();

		this.enemy = closestEnemy;

		if (this.enemy != null) {
			setEnemyAttributes(this.enemy, player);
		}
	}


	/**
	 * sets the enemy-dependent parameters to the given enemy used if the enemy
	 * has just been killed and is therefore ignored by the getClosestEnemy()
	 * method
	 * 
	 * @param environment
	 * @param enemy
	 */
	public SensoryInformation(SimulatedLevelScene environment,
			SimulatedSprite enemy) {
		this(environment);
		this.enemy = enemy;
		setEnemyAttributes(this.enemy, environment.getPlanningPlayer());
	}

	/**
	 * sets all enemy-dependent parameters
	 * 
	 * @param enemy
	 * @param player
	 */
	private void setEnemyAttributes(SimulatedSprite enemy,
			SimulatedPlayer player) 
	{
		this.type = getSimplifiedType(this.enemy);
		
		/*if (this.type == TgngType.INTELLIGENT) 
		{
			//TODO: Validate this
			this.enemyHealth = 1*3;
			//this.enemyHealth = ((SimulatedPlayer) this.enemy).lives * 3;
			switch (((SimulatedPlayer) this.enemy).getHealth()) 
			{
			case 2:
				this.enemyHealth += 2;
				break;
			case 1:
				this.enemyHealth += 1;
				break;
			case 0:
				// this.enemyHealth +=0; not necessary
				break;
			default:
				Logging.logWarning("SensoryInformation", "HealthType unknown.");
			}
		} 
		else */if (this.enemy.type.isHostile()) 
		{
			this.enemyHealth = (this.enemy.isAlive() ? 1 : 0);
		}/* 
		else if (this.enemy.type == PlayerWorldObject.FIREBALL) 
		{
			this.enemyHealth = 1;
		} 
		*/else 
		{
			Logging.logWarning("SensoryInformation", "EnemyType " + this.enemy.type + " unknown.");
		}

		this.enemyX = this.enemy.getPosition(0).x - player.getPosition(0).x;
		this.enemyY = this.enemy.getPosition(0).y - player.getPosition(0).y;
		this.enemyXa = this.enemy.getVelocity(0).x;
		this.enemyYa = this.enemy.getVelocity(0).y;
	}

	/**
	 * returns the type of the enemy, differentiates between
	 * intelligent(player), non-intelligent(luko, grumpy etc) and fireballs
	 * 
	 * @param enemy
	 * @return
	 */
	private static TgngType getSimplifiedType(SimulatedSprite enemy) 
	{
		//TODO: currently all enemies are handled by the same TGNG:
		return TgngType.NON_INTELLIGENT;
//		
//		if (enemy.type == PlayerWorldObject.PLAYER) {
//			return TgngType.INTELLIGENT;
//		} else if (enemy.type == PlayerWorldObject.FIREBALL) {
//			return TgngType.FIREBALL;
//		} else if (enemy.isHostile()) {
//			return TgngType.NON_INTELLIGENT;
//		}
//		Logging.logWarning("SensoryInformation", "Type unknown");
//		return null;
	}

	/**
	 * checks whether the enemy is in the range of the tgng
	 * 
	 * @param closestEnemy
	 * @param player
	 * @return
	 */
	public static boolean isInObservationRange(SimulatedSprite closestEnemy,
			SimulatedPlayer player) {
		if (!closestEnemy.isAlive()) {
			return false;
		}
		float playerX = player.getPosition(0).x;
		float playerY = player.getPosition(0).y;
		float enemyX = closestEnemy.getPosition(0).x;
		float enemyY = closestEnemy.getPosition(0).y;

		if (enemyX - playerX < (Tgng.TGNG_SIZE[0] + 1) * 16
				&& playerX - enemyX < (Tgng.TGNG_SIZE[2] + 1) * 16) {
			// is in range of horizontal thresholds
			if (playerY - enemyY < (Tgng.TGNG_SIZE[0] + 2) * 16
					&& enemyY - playerY < (Tgng.TGNG_SIZE[2] + 1) * 16) {
				// is in range of vertical thresholds
				return true;
			}
		}
		return false;
	}

	public byte[][] getEnvironment() {
		return environment;
	}

	public double getXa() {
		return xa;
	}

	public double getYa() {
		return ya;
	}

	public int getHealth() {
		return health;
	}

	public SimulatedSprite getEnemy() {
		return enemy;
	}

	public double getEnemyX() {
		return enemyX;
	}

	public double getEnemyY() {
		return enemyY;
	}

	public double getEnemyXa() {
		return enemyXa;
	}

	public double getEnemyYa() {
		return enemyYa;
	}

	public int getEnemyHealth() {
		return enemyHealth;
	}

	public TgngType getEnemyType() {
		return type;
	}

	/**
	 * codes the health, movement and position coordinates aswell as the
	 * environment as double values field 0 is agents health, 1-2 is agents
	 * movement, 3 is enemy health, 4-5 is enemies position, 6-7 is enemies
	 * movement, 8-end is blockwise environment
	 * 
	 * @return
	 */
	public double[] toVector() {
		int index = 0;
		double theta = Tgng.THETA;
		double[] vector = new double[8 + this.environment.length
				* this.environment[0].length];

		// make sure that different health types lead to different nodes by
		// making the distance between health types bigger than theta
		vector[index] = this.health * 2 * theta;
		index++;

		// copy player movement into the vector
		vector[index] = this.xa;
		index++;
		vector[index] = this.ya;
		index++;

		// make sure that different enemy health leads to different nodes by
		// making the distance between enemy health bigger than theta
		vector[index] = this.enemyHealth * 2 * theta;
		index++;

		// copy enemy movement and position into the vector
		vector[index] = this.enemyX;
		index++;
		vector[index] = this.enemyY;
		index++;
		vector[index] = this.enemyXa;
		index++;
		vector[index] = this.enemyYa;
		index++;

		// make sure that differences in the environment lead to different nodes
		// by
		// making the distance between environment types bigger than theta
		for (int yIndex = 0; yIndex < this.environment.length; yIndex++) {
			for (int xIndex = 0; xIndex < this.environment[0].length; xIndex++, index++) {
				vector[index] = 2 * theta * this.environment[yIndex][xIndex];
			}
		}

		return vector;
	}

	/**
	 * calculates and returns mean values for all continuous parameters (xa, ya,
	 * enemyX, enemyY, enemyXa, enemyYa) environment, health, enemyHealth,
	 * enemyType and enemy will be taken from the first argument, these should
	 * be identical anyways
	 * 
	 * @param SI
	 * @param otherSI
	 * @return
	 */
	public static SensoryInformation getMean(SensoryInformation SI,
			SensoryInformation otherSI) {
		if (SI == null || otherSI == null) {
			Logging.logWarning("SensoryInformation",
					"GetMean was called with null argument.");
			return null;
		}
		double meanXa = (SI.xa + otherSI.xa) / 2;
		double meanYa = (SI.ya + otherSI.ya) / 2;
		double meanEnemyX = (SI.enemyX + otherSI.enemyX) / 2;
		double meanEnemyY = (SI.enemyY + otherSI.enemyY) / 2;
		double meanEnemyXa = (SI.enemyXa + otherSI.enemyXa) / 2;
		double meanEnemyYa = (SI.enemyYa + otherSI.enemyYa) / 2;

		return new SensoryInformation(SI.environment, meanXa, meanYa,
				SI.health, SI.enemy, SI.type, SI.enemyHealth, meanEnemyX,
				meanEnemyY, meanEnemyXa, meanEnemyYa);
	}

	/**
	 * returns a byte-array centered around the player containing the type
	 * information of each of the blocks
	 * 
	 * @param scene
	 * @return
	 */
	private static byte[][] createSimplifiedObservation(
			SimulatedLevelScene scene) {

		// calculate size of the matrix, 2 is playerHeight, 1 is playerWidth
		int tgngHeight = 2 + Tgng.TGNG_SIZE[0]
				+ Tgng.TGNG_SIZE[2];
		int tgngWidth = 1 + Tgng.TGNG_SIZE[1] + Tgng.TGNG_SIZE[3];

		byte[][] simplifiedEnvironment = new byte[tgngHeight][tgngWidth];

		/*
		 * 22x22 matrix centered around the agent(coordinates 11,11 in the
		 * matrix)
		 */
		PlayerWorldObject[][] environment = scene.getStaticLocalMap();

		// calculate the positions in the environment to take the information
		// from
		int xOffset = 11 - Tgng.TGNG_SIZE[3];
		int xThreshold = 11 + Tgng.TGNG_SIZE[1];
		int yOffset = 11 - Tgng.TGNG_SIZE[0] - 1;
		int yThreshold = 11 + Tgng.TGNG_SIZE[2];

		for (int y = yOffset, yIndex = 0; y <= yThreshold; y++, yIndex++) {
			for (int x = xOffset, xIndex = 0; x <= xThreshold; x++, xIndex++) {
				simplifiedEnvironment[yIndex][xIndex] = getSimplifiedType(
						environment[y][x], yIndex);
			}
		}

		return simplifiedEnvironment;
	}

	/**
	 * returns 1 for blocking objects and 0 for non-blocking objects top/bottom
	 * only blocking objects count as blocking, if they would block the player
	 * moving from his current position in the direction of the object can be
	 * extended if more information is needed
	 * 
	 * @param object
	 * @param xIndex
	 * @param yIndex
	 * @return
	 */
	private static byte getSimplifiedType(PlayerWorldObject object, int yIndex) {
		if (object == null) {
			// out of map, happens if player dies or is close to the edge
			return 0;
		}
		switch (object.blocksPath) {
		case FULL:
			return 1;
		case BOTTOM:
			if (yIndex < 3) {
				// object is on a higher level than the player
				return 1;
			} else {
				return 0;
			}
		case TOP:
			if (yIndex == 4) {
				// blocking object is on a lower level than the player
				return 1;
			} else {
				return 0;
			}
		case NONE:
			return 0;
		default:
			Logging.logWarning("SensoryInformation",
					"Unknown BlockingType detected.");
		}
		return 0;
	}

	/*
	 * ### DEBUG ###
	 */

	@Override
	public String toString() {
		String ret = "";
		ret += "PlayerXa: " + this.xa + ", PlayerYa: " + this.ya + "\n";
		ret += "PlayerHealthCondition: " + this.health + "\n";
		// ret += "Enemy: " + this.enemy + "\n";
		ret += "EnemyXa: " + this.enemyXa + ", EnemyYa: " + this.enemyYa + "\n";
		ret += "EnemyX: " + this.enemyX + ", enemyY: " + this.enemyY + "\n";
		ret += "Environment:\n";
		ret += "ooooooo\n";
		for (int i = 0; i < this.environment.length; i++) {
			ret += "o";
			for (int j = 0; j < this.environment[0].length; j++) {
				if (environment[i][j] == 0) {
					ret += " ";
				} else {
					ret += "X";
				}
			}
			ret += "o\n";
		}
		ret += "ooooooo";
		return ret;
	}
}
