package Bar;
import robocode.*;
import java.awt.Color;
import java.util.Random;

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
		}
	}

	public class Target {
		String name;
		int counter;
		double adjust;
		double distance;
	};
	void adjustTargetDistance(double factor) {
		double newtarget_distance = target_distance * factor;
		newtarget_distance = Math.max(newtarget_distance, 100);
		newtarget_distance = Math.min(newtarget_distance, 400);
		target_distance = newtarget_distance;
	}
	
	Target target;
	public void run() {

		Random rand_gen = new Random();
		double prand;

		setColors(Color.red,Color.blue,Color.green);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		moveToCenter();
		while(true) {
			if (target == null) {
				double distance_until_border;

				turnRadarLeftRadians(Math.PI / 2);
				
				distance_until_border = getDistanceUntilBorder(getHeadingRadians(), direction);
				if (distance_until_border < border_triger_direction) {
					if (atTheBoarder())
					{
						moveToCenter();
					}
					out.println("Avoid hit the border.");
				}
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
			prand = rand_gen.nextDouble();
			if (prand < 0.05) {
				direction *= -1;
			} else if (prand < 0.075) {
				adjustTargetDistance(1.1);
			} else if (prand < 0.100) {
				adjustTargetDistance(0.9);
			}
		}
	}
	
	double ortogonalAdjust = Math.PI / 2;
	double direction = 1;
	double border_distance = 100;
	double target_distance = 300;
	double border_triger_direction = 120;

	double pi2npi(double angle) {
		return (angle + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
	}

	double getDistanceUntilBorder(double angle, double direction) {
		double top, botton, left, right;

		if (direction == -1)
			angle += Math.PI;

		top = (getBattleFieldHeight() - getY()) / Math.cos(angle);
		botton = (0-getY()) / Math.cos(angle);

		right = (getBattleFieldWidth() - getX()) / Math.sin(angle);
		left = (0-getX()) / Math.sin(angle);

		if (top < 0)
			top = getBattleFieldHeight();

		if (botton < 0)
			botton = getBattleFieldHeight();

		if (left < 0)
			left = getBattleFieldWidth();

		if (right < 0)
			right = getBattleFieldWidth();

		return Math.min(Math.min(left, right), Math.min(top, botton));
	}

	double getDistanceUntilBorder() {
		double top, botton, left, right;

		top = getBattleFieldHeight() - getY();
		botton = getY();
		right = getBattleFieldWidth() - getX();
		left = getX();

		return Math.min(Math.min(left, right), Math.min(top, botton));
	}
	
	boolean atTheBoarder() {
		return getDistanceUntilBorder() < border_distance;
	}
	
	void moveToCenter() {
		double xc, yc;
		xc = getBattleFieldWidth() / 2;
		yc = getBattleFieldHeight() / 2;
		
		double angle = Math.atan2(xc - getX(), yc - getY());
		angle = pi2npi(angle - getHeadingRadians());

		if (Math.abs(angle) < Math.PI/2)
		{
			setTurnRightRadians(angle);
			setAhead(border_distance);
		}else{
			angle = pi2npi(angle + Math.PI);
			setTurnRightRadians(angle);
			setAhead(-border_distance);
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

		double fire_power = Math.max(Math.min(3 * (100 / e.getDistance()), 3), 1);
		PredictRobotEvent pe = new PredictRobotEvent(e, this, fire_power);

		target.counter++;
		target.distance = e.getDistance();
				
		//Tracking functions
		double dist_adjust, angle;
		//This adjust is between 0 and 90 degrees to get close the target
		dist_adjust = direction * (Math.PI / 2) * (target.distance - target_distance) / getBattleFieldWidth();
		//This adjust make the direction be always orthogonal to the target
		angle = pi2npi(Math.PI / 2 - e.getBearingRadians() - dist_adjust);
		double distance_until_border = getDistanceUntilBorder(angle + getHeadingRadians(), direction);
		if (distance_until_border < border_triger_direction) {
			if (atTheBoarder())	{
				moveToCenter();
			}else{
				setTurnLeftRadians(angle);
				setAhead(border_triger_direction * direction);
			}
			direction *= -1;
		}else{
			setTurnLeftRadians(angle);
			setAhead(border_triger_direction * direction);
		}

		//Radar compensation
		double diff_radar = getRadarHeadingRadians() - getHeadingRadians();
		double adjust_radar = pi2npi(e.getBearingRadians() - diff_radar);
		target.adjust = adjust_radar;
		
		//Gun adjust with prediction or not
		double diff = getGunHeadingRadians() - getHeadingRadians();
		double adjust_gun;
		if (pe.getHitPrediction()) {
			adjust_gun = pi2npi(diff - pe.getBearingRadians());
		} else {
			adjust_gun = pi2npi(diff - e.getBearingRadians());
		}
		setTurnGunLeftRadians(adjust_gun);

		//Fire if we are sure
		if (getGunHeat() == 0 && pe.getHitPrediction() == true) {
			setFire(fire_power);
		}
	}

	public void onHitByBullet(HitByBulletEvent e) {
		adjustTargetDistance(1.2);
	}
	
	public void onBulletHit(BulletHitEvent event) {
		adjustTargetDistance(0.8);
	}

	public void onHitWall(HitWallEvent e) {	
		out.println("Hit wall.");
	}
}
