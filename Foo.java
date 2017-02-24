package Bar;
import robocode.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

/**
 * Foo - a robot by (your name here)
 */
public class Foo extends Robot
{
	/**
	 * run: Foo's default behavior
	 */	
	public class Target {
		double x;
		double y;
		String name;
		int counter;
	};
	Target target;
	public void run() {
		// Initialization of the robot should be put here

		// After trying out your robot, try uncommenting the import at the top,
		// and the next line:

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setAdjustRadarForGunTurn(true);
		// Robot main loop
		while(true) {
			if (target == null) {
				turnRadarRight(100);
			}else{
				target.counter = 0;
				turnRadarRight(100);
				turnRadarRight(-100);
				if (target.counter == 0) {
					out.println("Lost Target.");
					target = null;
				}
			}
		}
	}

	/**
	 * onScannedRobot: What to do when you see another robot
	 */
	public void onScannedRobot(ScannedRobotEvent e) {
			
		out.println("See:" + e.getName());
		
		if (target == null) {
			target = new Target();
			target.name = e.getName();
			
			double diff_radar = getRadarHeading() - getHeading();
			double adjust_radar = (e.getBearing() - diff_radar + 540) % 360 - 180;
			out.println("Radar Ajudst: " + adjust_radar);		
			if (Math.abs(adjust_radar) > 0.01) {
				turnRadarRight(adjust_radar);
			}
		}
		
		if (e.getName() != target.name)
			return;
			
		target.counter++;
		
		// Replace the next line with any behavior you would like
		double diff = 	getGunHeading() - getHeading();
		double adjust = (e.getBearing() - diff + 540) % 360 - 180;
		
		double angle = Math.toRadians(e.getHeading() + getHeading());
		target.x = e.getDistance() * Math.cos(angle);
		target.y = e.getDistance() * Math.sin(angle);

		out.println("x:" + target.x + " y:" + target.y);
		//out.println("my x:" + getX() + " my y:" + getY());


		out.println("Ajudst: " + adjust);
		if (Math.abs(adjust) > 0.01) {
			out.println("Turn adjust:" + adjust);
			turnGunRight(adjust);
		}else{
			out.println("Skip:" + adjust);
		}

		out.println("Gun:" + getGunHeat());		
		if (getGunHeat() != 0) {
			out.println("Gun:" + getGunHeat());
		}else{
			out.println("Fire Attempt!");
			fire(1);
			out.println("Fire!");
		}
	}

	/**
	 * onHitByBullet: What to do when you're hit by a bullet
	 */
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
		back(10);
	}
	
	/**
	 * onHitWall: What to do when you hit a wall
	 */
	public void onHitWall(HitWallEvent e) {
		// Replace the next line with any behavior you would like
		back(20);
	}	
}
