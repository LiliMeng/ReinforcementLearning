package ReinforcementLearning;

import java.awt.*; 
import java.awt.geom.*; 
import java.io.*;

import ReinforcementLearning.RobotAction;
import ReinforcementLearning.QLearning;
import ReinforcementLearning.LUQTable;
import ReinforcementLearning.RobotState;
import ReinforcementLearning.Enemy;

import robocode.*; 
 
import robocode.AdvancedRobot; 

public class mmRobot extends AdvancedRobot 
{
	private double winningRound;
	private double losingRound;
	private double winningRate;
	private double losingRate; 
	public static final double PI = Math.PI;
	private Enemy enemy;
	private LUQTable qtable;
	private QLearning learner; 
	private double firePower; 
	private int direction = 1; 
	private int isHitWall = 0; 
	private int isHitByBullet = 0; 
	 
	double accumuReward=0.0;
	double currentReward=0.0;
	
	private static final double rewardForWin = 10;
	private static final double rewardForDeath = -10; 
	
	private static final double rewardForHitRobot = -3; 
	
	private static final double rewardForBulletHit = 5;
	private static final double rewardForBulletMissed = -3;
	private static final double rewardForHitByBullet = -5; 
 
	private static final double rewardForHitWall = -3; 
	private static final double rewardForCloseWall = -3;
	
	
	  public void run() 
	  { 
	    qtable = new LUQTable(); 
	    learner = new QLearning(qtable); 
	    enemy = new Enemy(); 
	    enemy.distance = 100000; 
	 
	    setColors(Color.green, Color.white, Color.red); 
	    setAdjustGunForRobotTurn(true); 
	    setAdjustRadarForGunTurn(true); 
	    turnRadarRightRadians(2 * PI); 
	    int countRound=0;
	    
	    while (true) 
	    { 
	      countRound++;
	      
	      
	      if(countRound>1000)
	      {
	    	  out.println("Before exploitationRate"+learner.ExploitationRate);
	    	  learner.setExploitationRate(0);
	    	  out.println("After exploitationRate"+learner.ExploitationRate);
	      }
	      
	      robotMovement(); 
	      firePower = 400/enemy.distance; 
	      if (firePower > 3) 
	        firePower = 3; 
	      radarMovement(); 
	      gunMovement(); 
	      if (getGunHeat() == 0) 
	      {
	        setFire(firePower); 
	      } 
	      execute(); 
	    } 
	  } 
	 
	  void doMovement() 
	  { 
	    if (getTime()%20 == 0) 
	    { 
	      direction *= -1;		//reverse direction 
	      setAhead(direction*300);	//move in that direction 
	    } 
	    setTurnRightRadians(enemy.bearing + (PI/2));
	  } 
	 
	  private void robotMovement() 
	  { 
	    int state = getState(); 
	    int action = learner.selectAction(state); 
	    out.println("RobotAction selected: " + action); 
	    learner.learn(state, action, currentReward); 
	    currentReward = 0.0; 
	    isHitWall = 0; 
	    isHitByBullet = 0; 
	 
	    switch (action) 
	    { 
	      case RobotAction.Ahead:
	        setAhead(RobotAction.RobotMoveDistance); 
	        break; 
	      case RobotAction.Back: 
	        setBack(RobotAction.RobotMoveDistance); 
	        break; 
	      case RobotAction.TurnLeft: 
	        setTurnLeft(RobotAction.RobotTurnDegree); 
	        break; 
	      case RobotAction.TurnRight: 
	        setTurnRight(RobotAction.RobotTurnDegree); 
	        break; 
	    } 
	  } 
	 
	  private int getState() 
	  { 
	    int heading = RobotState.getHeading(getHeading()); 
	    int enemyDistance = RobotState.getEnemyDistance(enemy.distance); 
	    int enemyBearing = RobotState.getEnemyBearing(enemy.bearing); 
	    out.println("Stste(" + heading + ", " + enemyDistance + ", " + enemyBearing + ", " + isHitWall + ", " + isHitByBullet + ")"); 
	    int state = RobotState.mapping[heading][enemyDistance][enemyBearing][isHitWall][isHitByBullet]; 
	    return state; 
	  } 
	 
	  private void radarMovement() 
	  { 
	    double radarOffset; 
	    if (getTime() - enemy.ctime > 4) { //if we haven't seen anybody for a bit.... 
	      radarOffset = 4*PI;				//rotate the radar to find a enemy 
	    } else { 
	 
	      radarOffset = getRadarHeadingRadians() - (Math.PI/2 - Math.atan2(enemy.y - getY(),enemy.x - getX())); 
	      radarOffset = NormaliseBearing(radarOffset); 
	      if (radarOffset < 0) 
	        radarOffset -= PI/10; 
	      else 
	        radarOffset += PI/10; 
	    } 
	    setTurnRadarLeftRadians(radarOffset); 
	  } 
	 
	  private void gunMovement() 
	  { 
	    long time; 
	    long nextTime; 
	    Point2D.Double p; 
	    p = new Point2D.Double(enemy.x, enemy.y); 
	    for (int i = 0; i < 20; i++) 
	    { 
	      nextTime = (int)Math.round((getRange(getX(),getY(),p.x,p.y)/(20-(3*firePower)))); 
	      time = getTime() + nextTime - 10; 
	      p = enemy.guessPosition(time); 
	    } 
	    double gunOffset = getGunHeadingRadians() - (Math.PI/2 - Math.atan2(p.y - getY(),p.x -  getX())); 
	    setTurnGunLeftRadians(NormaliseBearing(gunOffset)); 
	  } 
	 
	  double NormaliseBearing(double ang) { 
	    if (ang > PI) 
	      ang -= 2*PI; 
	    if (ang< -PI) 
	      ang += 2*PI; 
	    return ang; 
	  } 
	 
	  double NormaliseHeading(double ang) { 
	    if (ang > 2*PI) 
	      ang -= 2*PI; 
	    if (ang < 0) 
	      ang += 2*PI; 
	    return ang; 
	  } 
	 
	  public double getRange( double x1,double y1, double x2,double y2 ) 
	  { 
	    return Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
	  } 
	 
	  public double absbearing( double x1,double y1, double x2,double y2 ) 
	  { 
	    double xo = x2-x1; 
	    double yo = y2-y1; 
	    double h = getRange( x1,y1, x2,y2 ); 
	    if( xo > 0 && yo > 0 ) 
	    { 
	      return Math.asin( xo / h ); 
	    } 
	    if( xo > 0 && yo < 0 ) 
	    { 
	      return Math.PI - Math.asin( xo / h ); 
	    } 
	    if( xo < 0 && yo< 0 ) 
	    { 
	      return Math.PI + Math.asin( -xo / h ); 
	    } 
	    if( xo < 0 && yo > 0 ) 
	    { 
	      return 2.0*Math.PI - Math.asin( -xo / h ); 
	    } 
	    return 0; 
	  } 
	 
	  public void onBulletHit(BulletHitEvent e) 
	  { 
	    if (enemy.name == e.getName()) 
	    { 
	      currentReward=rewardForBulletHit;
	    } 
	  } 
	 
	  public void onBulletMissed(BulletMissedEvent e) 
	  { 
	     currentReward=rewardForBulletMissed;
	  } 
	 
	  public void onHitByBullet(HitByBulletEvent e) 
	  { 
	    if (enemy.name == e.getName()) 
	    { 
	      currentReward=rewardForHitByBullet;
	    } 
	    isHitByBullet = 1; 
	  } 
	 
	  public void onHitRobot(HitRobotEvent e) 
	  { 
	    if (enemy.name == e.getName()) 
	    { 
	      currentReward= rewardForHitRobot; 
	    } 
	  } 
	 
	  public void onHitWall(HitWallEvent e) 
	  { 
		currentReward=rewardForHitWall;
	    isHitWall = 1; 
	  } 
	 
	  public void onScannedRobot(ScannedRobotEvent e) 
	  { 
	    if ((e.getDistance() < enemy.distance)||(enemy.name == e.getName())) 
	    { 
	      //the next line gets the absolute bearing to the point where the bot is 
	      double absbearing_rad = (getHeadingRadians()+e.getBearingRadians())%(2*PI); 
	      //this section sets all the information about our enemy 
	      enemy.name = e.getName(); 
	      double h = NormaliseBearing(e.getHeadingRadians() - enemy.head); 
	      h = h/(getTime() - enemy.ctime); 
	      enemy.changehead = h; 
	      enemy.x = getX()+Math.sin(absbearing_rad)*e.getDistance(); //works out the x coordinate of where the enemy is 
	      enemy.y = getY()+Math.cos(absbearing_rad)*e.getDistance(); //works out the y coordinate of where the enemy is 
	      enemy.bearing = e.getBearingRadians(); 
	      enemy.head = e.getHeadingRadians(); 
	      enemy.ctime = getTime();				//game time at which this scan was produced 
	      enemy.speed = e.getVelocity(); 
	      enemy.distance = e.getDistance(); 
	      enemy.energy = e.getEnergy(); 
	    } 
	  } 
	 
	  public void onRobotDeath(RobotDeathEvent e) 
	  {
	 
	    if (e.getName() == enemy.name) 
	      enemy.distance = 10000; 
	  }   

	  public void onWin(WinEvent event) 
	  { 
		 winningRound++; 
		 currentReward=rewardForWin;
		 
		 PrintStream w = null; 
		    try 
		    { 
		      w = new PrintStream(new RobocodeFileOutputStream("/home/lili/workspace/EECE592/ReinforcementLearning/src/ReinforcementLearning/survival.xlsx", true)); 
		      w.println(accumuReward); 
		      if (w.checkError()) 
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
		        if (w != null) 
		          w.close(); 
		      } 
		      catch (Exception e) 
		      { 
		        System.out.println("Exception trying to close witer: " + e); 
		      } 
		    } 
		  }  

	  public void onDeath(DeathEvent event) 
	  { 
	     losingRound++; 
	     currentReward=rewardForDeath;
	     PrintStream w = null; 
		    try 
		    { 
		      w = new PrintStream(new RobocodeFileOutputStream("/home/lili/workspace/EECE592/ReinforcementLearning/src/ReinforcementLearning/survival.xlsx", true)); 
		      w.println(accumuReward); 
		      if (w.checkError()) 
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
		        if (w != null) 
		          w.close(); 
		      } 
		      catch (Exception e) 
		      { 
		        System.out.println("Exception trying to close witer: " + e); 
		      } 
		    } 
	  } 
}
