package Bar;
import robocode.*;
import java.util.Random;
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
		
		boolean hitPrediction;
		public boolean getHitPrediction()
		{
			return hitPrediction;
		}
		
		private double getBulletVelocity(double firepower)
		{
			return 20 - 3 * firepower;
		}
		
		private double x, y;
		public PredictRobotEvent(ScannedRobotEvent e, Robot robot, double firepower) {
			double dxdt, dydt, angle;
			double nx, ny, dt;
			
			newHeading = e.getHeading();
			
			angle = Math.toRadians(e.getBearing());
						
			y = e.getDistance() * Math.cos(angle);
			x = e.getDistance() * Math.sin(angle);
			
			double head2 = e.getHeading() - robot.getHeading();
			dxdt = Math.sin(Math.toRadians(head2)) * e.getVelocity();
			dydt = Math.cos(Math.toRadians(head2)) * e.getVelocity();

			double a = Math.pow(dxdt, 2) + Math.pow(dydt, 2) - Math.pow(getBulletVelocity(firepower), 2);
			double b = 2 * (x * dxdt + y * dydt);
			double c = Math.pow(x, 2) + Math.pow(y, 2);

			if ( a > 0)
				dt = (-b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
			else
				dt = (-b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

			ny = y + dydt * dt;
			nx = x + dxdt * dt;
						
			double newAngle = Math.atan2(nx, ny);
			newBearing = Math.toDegrees(newAngle);
			newDistance = Math.hypot(ny, nx);
			
			//Computes if the target will be outside of the BattleField
			angle = Math.toRadians(newBearing + robot.getHeading());
			y = newDistance * Math.cos(angle) + robot.getY();
			x = newDistance * Math.sin(angle) + robot.getX();
			
			hitPrediction = x > 0 && y > 0 && x < robot.getBattleFieldWidth() && y < getBattleFieldHeight();
			hitPrediction &= e.getVelocity() == 8;
			
			//If the target can't run from the bullet we fire.
			double error = dt * 8;
			if (error < robot.getWidth() || error < robot.getHeight()) {
				hitPrediction = true;
			}
			out.println("error:" + error);
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

		double fire_power = 1;
		PredictRobotEvent pe = new PredictRobotEvent(e, this, fire_power);
					
		target.counter++;
		target.distance = e.getDistance();
	
		double diff_radar = getRadarHeading() - getHeading();
		double adjust_radar = (e.getBearing() - diff_radar + 540) % 360 - 180;
		target.adjust = adjust_radar;

		double diff = getGunHeading() - getHeading();
		double adjust_gun;
		if (pe.getHitPrediction()) {
			adjust_gun = (diff - pe.getBearing() + 540) % 360 - 180;
		} else {
			adjust_gun = (diff - e.getBearing() + 540) % 360 - 180;
		}
		
		
		if (Math.abs(adjust_gun) > 0.01) {
			turnGunLeft(adjust_gun);
		}

		if (getGunHeat() == 0 && pe.getHitPrediction() == true) {
			fire(fire_power);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
	    // Go somewhere pseudo random
	    Random rand_gen = new Random();
	    double prand = rand_gen.nextDouble();
	    prand *= 2;
	    prand -= 1;
	    prand *= 90;
	    back(prand);
	    
	}
	
	public void onHitWall(HitWallEvent e) {
		//back(20);
	}	
}
