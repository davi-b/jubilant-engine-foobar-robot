package Bar;
import robocode.*;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class Foo extends Robot
{
	public class PredictRobotEvent {

		double newDistance;
		public double getDistance()
		{
			return newDistance;
		}
		
		double newBearing;
		public double getBearing()
		{
			return newBearing;
		}

		double newHeading;
		public double getHeading()
		{
			return newHeading;
		}
		
		private double getBulletVelocity(double firepower)
		{
			return 20 - 3 * firepower;
		}
		
		private double x, y;
		public PredictRobotEvent(ScannedRobotEvent e, double firepower, double Heading) {
			double dxdt, dydt, angle;
			double nx, ny, dt;
			
			dt = e.getDistance() / getBulletVelocity(1);
			newHeading = e.getHeading();
			
			angle = Math.toRadians(e.getBearing());
						
			y = e.getDistance() * Math.cos(angle);
			x = e.getDistance() * Math.sin(angle);
			
			double head2 = e.getHeading() - Heading;
			dxdt = Math.sin(Math.toRadians(head2)) * e.getVelocity();
			dydt = Math.cos(Math.toRadians(head2)) * e.getVelocity();

			ny = y + dydt * dt;
			nx = x + dxdt * dt;
			
			double newAngle = Math.atan2(nx, ny);
			newBearing = Math.toDegrees(newAngle);
			newDistance = Math.hypot(ny, nx);

			out.println("angle:" + getBearing() + " new angle:" + e.getBearing());
			//out.println("x:" + x + " y:" + y);
			//out.println("new x:" + nx + " new y:" + ny);
			out.println("dx/dt:" + dxdt + " dy/dt:" + dydt);
		}
	}

	public class Target {
		String name;
		int counter;
		double adjust;
		double distance;
	};
	Target target;
	public void run() {

		// setColors(Color.red,Color.blue,Color.green); // body,gun,radar
		setAdjustRadarForGunTurn(true);
		ahead(10);
		while(true) {
			if (target == null) {
				turnRadarLeft(90);
			}else{
				double aperture;
				aperture = 5 * (getBattleFieldWidth() / target.distance);
				target.counter = 0;
				turnRadarLeft(aperture - target.adjust);
				turnRadarRight(aperture + target.adjust);
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

		PredictRobotEvent pe = new PredictRobotEvent(e, 1, getHeading());
					
		target.counter++;
		target.distance = e.getDistance();
		
		double diff = getGunHeading() - getHeading();
		double adjust = (diff - e.getBearing() + 540) % 360 - 180;
		double padjust = (diff - pe.getBearing() + 540) % 360 - 180;		
		
		double diff_radar = getRadarHeading() - getHeading();
		double adjust_radar = (e.getBearing() - diff_radar + 540) % 360 - 180;
		target.adjust = adjust_radar;

		//target.x = pe.x;
		//target.y = pe.y;
		//out.println("my x:" + getX() + " my y:" + getY());

		out.println("Adjust:" + adjust + " pAdjust:" + padjust);
		if (Math.abs(adjust) > 0.01) {
			turnGunLeft(padjust);
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
