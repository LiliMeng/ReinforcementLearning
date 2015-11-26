package ReinforcementLearning;

import ReinforcementLearning.RobotAction;
import ReinforcementLearning.LUQTable;
import java.util.Random;

public class QLearning
{
	public static final double LearningRate = 0.1;
	public static final double DiscountRate = 0.9;
	public double ExploitationRate = 0.01;
	private int lastState;
	private int lastAction;
	private LUQTable Qtable;

	public double setExploitationRate(double value)
	{
		ExploitationRate=value;
		return ExploitationRate;
	}
	public QLearning(LUQTable table)
	{
		this.Qtable = table;
	}

	public void learn(int state, int action, double reward)
	{
		double oldQValue = Qtable.getQValue(lastState, lastAction);
		double newQValue = oldQValue + LearningRate * (reward+ DiscountRate * Qtable.maxQValue(state)-oldQValue);
			
		Qtable.setQValue(lastState, lastAction, newQValue);
		
		
		lastState = state;
		lastAction = action;
	}
	
	
	public int selectAction(int state){

		double thres = Math.random();
		
		int actionIndex = 0;
		
		if (thres<ExploitationRate)
		{//randomly select one action from action(0,1,2,3)
			Random ran = new Random();
			actionIndex = ran.nextInt(((RobotAction.numRobotActions-1 - 0) + 1));
		}
		else
		{//greedy
			actionIndex=Qtable.bestAction(state);
		}
		return actionIndex;
	}
   
}
