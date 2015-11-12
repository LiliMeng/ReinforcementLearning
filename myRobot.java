package RL;

import java.awt.geom.*;
import java.io.*;

import robocode.*;

import robocode.AdvancedRobot;

public class myRobot extends AdvancedRobot
{
	private static int winningRound;
	private static int losingRound;
	public static final double PI = Math.PI;
	private Target target;
	private Qtable table;
	private Qlearning learner;
	private double reinforcement = 0.0;
	private double firePower;
	private int direction = 1;
	private int isHitWall = 0;
	private int isHitByBullet = 0;
	
	public void run()
	{
		table = new Qtable();
		learner = new Qlearning(table);
		target = new Target();
		target.distance = 100000;
		
		//setColors(Color.green, Color.white, Color.red);
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		turnRadarRightRadians(2 * PI);
		while(true)
		{
			robotMovement();
			firePower = 400/target.distance; 
			if(firePower > 3)
			{
				firePower = 3;
			}
			radarMovement();
			gunMovement();
			if (getGunHeat() ==0)
			{
				setFire(firePower);
			}
			execute();
		}
	}
	
	void doMovement()
	{
		if(getTime()%20 ==0)
		{
			direction *= -1; //reverse direction
			setAhead(direction*300); //move in that direction
		}
		setTurnRightRadians(target.bearing + (PI/2));
	}
	
	private void robotMovement()
	{
		int state = getState();
		int action = learner.selectAction(state, getTime());
		out.println("Action selected: "+ action);
		learner.learn(state, action, reinforcement);
		reinforcement = 0.0;
		isHitWall = 0;
		isHitByBullet = 0;
		
		switch(action)
		{
			case Action.Ahead:
				setAhead(Action.RobotMoveDistance);
				break;
			case Action.Back:
				setBack(Action.RobotMoveDistance);
				break;
			case Action.TurnLeft:
				setTurnLeft(Action.RobotTurnDegree);
				break;
			case Action.TurnRight:
				setTurnRight(Action.RobotTurnDegree);
				break;
	    }
	}
 

	private int getState()
	{
		int heading = State.getHeading(getHeading());
		int targetDistance = State.getTargetDistance(target.distance);
		int targetBearing = State.getTargetBearing(target.bearing);
		out.println("State(" + heading + ", " + targetDistance + ", " + targetBearing +", " + isHitWall + ", "+ isHitByBullet +")");
		int state = State.Mapping[heading][targetDistance][targetBearing][isHitWall][isHitByBullet];
		return state;		
	}
	
	private void radarMovement()
	{
		double radarOffset;
		if(getTime() - target.ctime > 4)
		{
			radarOffset = 4*PI;
		}
		else
		{
			radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(target.y - getY(), target.x - getX()));
			radarOffset = NormaliseBearing(radarOffset);
			if(radarOffset < 0)
			{
				radarOffset -= PI/10;
			}
			else
			{
				radarOffset += PI/10;
			}
		}
		setTurnRadarLeftRadians(radarOffset);
	}
	
	private void gunMovement()
	{
		long time;
		long nextTime;
		Point2D.Double p;
		p = new Point2D.Double(target.x, target.y);
		for(int i=0; i<20; i++)
		{
			nextTime = (int)Math.round(getrange(getX(), getY(), p.x, p.y)/(20-(3*firePower)));
			time = getTime() + nextTime - 10;
			p =target.guessPosition(time);
		}
		
		double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(), p.x - getX()));
		setTurnGunLeftRadians(NormaliseBearing(gunOffset));
	}
 
	double NormaliseBearing(double ang)
	{
		if(ang > PI)
		{
			ang -= 2*PI;
		}
		if(ang <-PI)
		{
			ang += 2*PI;
		}
		return ang;
	}

	double NormliseHeading(double ang)
	{
		if(ang > 2*PI)
		{
			ang -= 2*PI;
		}
		if(ang < 0)
		{
			ang += 2*PI;
		}
		return ang;
	}
 
	public double getrange(double x1, double y1, double x2, double y2)
	{
		double xo = x2-x1;
		double yo = y2-y1;
		double h = Math.sqrt(xo*xo + yo*yo);
		return h;
	}
	
	public double absbearing(double x1, double y1, double x2, double y2)
	{
		double xo = x2 - x1;
		double yo = y2 - y1;
		double h = getrange(x1, y1, x2, y2);
		if(xo>0 && yo>0)
		{
			return Math.asin( xo/h);
		}
		if(xo >0 && yo < 0)
		{
			return Math.PI + Math.asin( -xo / h);
		}
		if(xo < 0 && yo > 0)
		{
			return 2.0*Math.PI - Math.asin( -xo / h);
		}
		return 0;
	}
 
	public void onBulletHit(BulletHitEvent e)
	{
		if(target.name == e.getName())
		{
			double change = e.getBullet().getPower() * 9;
			out.println("Bullet Hit: " + change);
			reinforcement += change;
		}
	}

	public void onBulletMissed(BulletMissedEvent e)
	{
		double change = -e.getBullet().getPower();
		out.println("Bullet Missed: " + change);
		reinforcement += change;
	}
 
	public void onHitByBullet(HitByBulletEvent e)
	{
		if(target.name == e.getName())
		{
			double power = e.getBullet().getPower();
			double change = -(4*power +2 *(power-1));
			out.println("Hit by Bullet: " + change);
			reinforcement +=change;
		}
		isHitByBullet = 1;
	}
 
	public void onHitRobot(HitRobotEvent e)
	{
		if(target.name == e.getName())
		{
			double change = -6.0;
			out.println("Hit Robot: " + change);
			reinforcement +=change;
		}
	}
 
	public void onHitWall(HitWallEvent e)
	{
		double change = -(Math.abs(getVelocity()) * 0.5 -1);
		out.println("Hit Wall: " + change);
		reinforcement +=change;
		isHitWall = 1;
	}
 
	public void onScannedRobot(ScannedRobotEvent e)
	{
		if((e.getDistance() < target.distance) || (target.name == e.getName()))
		{
			//the next line gets the absolute bearing to the point where the bot is
			double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2*PI);
			
			//this section sets all the information about our target
			target.name = e.getName();
			
			double h = NormaliseBearing(e.getHeadingRadians() - target.head);
			h = h / (getTime() - target.ctime);
			target.changehead = h;
			target.x = getX() + Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the target is 
			target.y = getY() + Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the target is
			target.bearing = e.getBearingRadians();
			target.head = e.getHeadingRadians();
			target.ctime = getTime();
			target.speed = e.getVelocity();
			target.distance = e.getDistance();
			target.energy = e.getEnergy();
		}
	}
   
	public void onRobotDeath(RobotDeathEvent e)
	{
		if(e.getName() == target.name)
			target.distance = 10000;
	}
	
	public void onWin(WinEvent event)
	{
		winningRound ++;
		
		PrintStream w = null;
			try
			{
				w = new PrintStream(new RobocodeFileOutputStream("survival.txt", true));
				w.println(winningRound + "  " +losingRound + "  "+table.getTotalValue());
				if(w.checkError())
					System.out.println("Could not save the data!");
				w.close();
			}
			catch (IOException e)
			{
				System.out.println("IOException trying to write: " + e);
			}
			finally
			{
				try
				{
					if(w!=null)
						w.close();
				}
				catch (Exception e)
				{
					System.out.println("IOException trying to write: " + e);
				}
	    } 
  } 

}
