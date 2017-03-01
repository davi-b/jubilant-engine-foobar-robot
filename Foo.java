package Bar;
import robocode.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class Foo extends Robot
{
	public class Target {
		double x;
		double y;
		String name;
		int counter;
		double adjust;
		double distance;
	};
	Target target;
	public void run() {

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setAdjustRadarForGunTurn(true);

		while(true) {
			if (target == null) {
				turnRadarRight(90);
			}else{
				double aperture;
				aperture = 5 * (getBattleFieldWidth() / target.distance);
				target.counter = 0;
				turnRadarRight(aperture + target.adjust);
				turnRadarLeft(aperture - target.adjust);
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
					
		if (target == null) {
			target = new Target();
			target.name = e.getName();
		}
		
		if (e.getName() != target.name)
			return;
			
		target.counter++;
		target.distance = e.getDistance();
		
		double diff = 	getGunHeading() - getHeading();
		double adjust = (e.getBearing() - diff + 540) % 360 - 180;
		double diff_radar = getRadarHeading() - getHeading();
		double adjust_radar = (e.getBearing() - diff_radar + 540) % 360 - 180;
		target.adjust = adjust_radar;
				
		double angle = Math.toRadians(e.getHeading() + getHeading());
		target.x = e.getDistance() * Math.cos(angle);
		target.y = e.getDistance() * Math.sin(angle);

		//out.println("x:" + target.x + " y:" + target.y);
		//out.println("my x:" + getX() + " my y:" + getY());

		if (Math.abs(adjust) > 0.01) {
			turnGunRight(adjust);
		}

		if (getGunHeat() == 0) {
			fire(1);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		//back(10);
	}
	
	public void onHitWall(HitWallEvent e) {
		//back(20);
	}	
}
