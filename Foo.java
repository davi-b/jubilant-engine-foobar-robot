package Bar;
import robocode.*;
import java.util.Random;
//import java.awt.Color;

// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html

public class Foo extends AdvancedRobot
{
	public class PredictRobotEvent {

		double newDistance;
		public double getDistance()
		{
			return newDistance;
		}
		
		double newBearingRadians;
		public double getBearingRadians()
		{
			return newBearingRadians;
		}

		double newHeadingRadians;
		public double getHeadingRadians()
		{
			return newHeadingRadians;
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
		public PredictRobotEvent(ScannedRobotEvent e, AdvancedRobot robot, double firepower) {
			double dxdt, dydt, angle;
			double nx, ny, dt;
			
			newHeadingRadians = e.getHeadingRadians();
			
			angle = e.getBearingRadians();
						
			y = e.getDistance() * Math.cos(angle);
			x = e.getDistance() * Math.sin(angle);
			
			angle = e.getHeadingRadians() - robot.getHeadingRadians();
			dxdt = Math.sin(angle) * e.getVelocity();
			dydt = Math.cos(angle) * e.getVelocity();

			double a = Math.pow(dxdt, 2) + Math.pow(dydt, 2) - Math.pow(getBulletVelocity(firepower), 2);
			double b = 2 * (x * dxdt + y * dydt);
			double c = Math.pow(x, 2) + Math.pow(y, 2);

			if ( a > 0)
				dt = (-b + Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);
			else
				dt = (-b - Math.sqrt(Math.pow(b, 2) - 4 * a * c)) / (2 * a);

			ny = y + dydt * dt;
			nx = x + dxdt * dt;
						
			newBearingRadians = Math.atan2(nx, ny);
			newDistance = Math.hypot(ny, nx);
			
			//Computes if the target will be outside of the BattleField
			angle = newBearingRadians + robot.getHeadingRadians();
			y = newDistance * Math.cos(angle) + robot.getY();
			x = newDistance * Math.sin(angle) + robot.getX();
			
			hitPrediction = x > 0 && y > 0 && x < robot.getBattleFieldWidth() && y < getBattleFieldHeight();
			hitPrediction &= e.getVelocity() == 8;
			
			double error;
			//If the target can't run from the bullet we fire.
			error = dt * 8;
			if (error < robot.getWidth() || error < robot.getHeight()) {
				hitPrediction = true;
			}

			angle = Math.abs(getBearingRadians() - e.getBearingRadians()) / 2;
			error = 2 * e.getDistance() * Math.sin(angle);
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
				turnRadarLeftRadians(Math.PI / 2);
			}else{
				double aperture;
				aperture = (5 * Math.PI / 180) * (getBattleFieldWidth() / target.distance);
				target.counter = 0;
				turnRadarLeftRadians(aperture - target.adjust);
				turnRadarRightRadians(aperture + target.adjust);
				if (target.counter == 0) {
					out.println("Lost Target.");
					target = null;
				}
			}
		}
	}

	double pi2npi(double angle) {
		return (angle + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
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
	
		double diff_radar = getRadarHeadingRadians() - getHeadingRadians();
		double adjust_radar = pi2npi(e.getBearingRadians() - diff_radar);
		target.adjust = adjust_radar;
		
		out.println(Math.toDegrees(adjust_radar));

		double diff = getGunHeadingRadians() - getHeadingRadians();
		double adjust_gun;
		if (pe.getHitPrediction()) {
			adjust_gun = pi2npi(diff - pe.getBearingRadians());
		} else {
			adjust_gun = pi2npi(diff - e.getBearingRadians());
		}
		
		
		if (Math.abs(adjust_gun) > 0.01) {
			turnGunLeftRadians(adjust_gun);
		}

		if (getGunHeat() == 0 && pe.getHitPrediction() == true) {
			fire(fire_power);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
	    // Go somewhere pseudo random
	    Random rand_gen = new Random();
	    double prand = rand_gen.nextDouble();
	    // turn right 25% of time
	    // just because
	    if (prand <= 0.25) {
		turnRight(90);
	    } 
	    prand *= 2;
	    prand -= 1;
	    prand *= 90;
	    back(prand);
	    
	}
	
	public void onHitWall(HitWallEvent e) {
		//back(20);
	}	
}
