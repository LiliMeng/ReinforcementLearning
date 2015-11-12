package RL;

public class Qtable
{
	private double[][] table;
	
	public Qtable()
	{
		table = new double[State.NumStates][Action.NumRobotActions];
		initialize();
	}
	
	private void initialize()
	{
		for(int i=0; i < State.NumStates; i++)
		{
			for(int j=0; j < Action.NumRobotActions; j++)
			{
				table[i][j] = 0.0;
			}
		}
	}
	
	public double getMaxQValue(int state)
	{
		double maximum = 0.0;
		for (int i=0; i< table[state].length; i++)
		{
			if(table[state][i] > maximum)
			{
				maximum = table[state][i];
			}
		}
		
		return maximum;
	}
 
	public int getBestAction(int state)
	{
		double maximum = 0.0;
		int bestAction = 0;
		for(int i=0; i<table[state].length; i++)
		{
			double qValue = table[state][i];
			if(qValue > maximum)
			{
				maximum = qValue;
				bestAction = i;
			}
		}
		return bestAction;
	}
	
	public double getQValue(int state, int action)
	{
		return table[state][action];
	}
	
	public void setQValue(int state, int action, double value)
	{
		table[state][action] = value;
	}
 
	public double getTotalValue()
	{
		double sum = 0.0;
		for(int i=0; i<State.NumStates; i++)
		{
			for(int j=0; j<Action.NumRobotActions; j++)
			{
				sum = sum + table[i][j];
			}
		}
		return sum;
	}
}
