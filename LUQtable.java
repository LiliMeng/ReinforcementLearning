package ReinforcementLearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ReinforcementLearning.RobotAction;
import ReinforcementLearning.RobotState;


public class LUQTable {
	
	private double[][] qTable;
	
	public LUQTable()
	{
		qTable = new double[RobotState.numStates][RobotAction.numRobotActions];
		initializeQtable();
	}
	
	public void initializeQtable()
	{
		for(int i=0; i<RobotState.numStates; i++)
		{
			for(int j=0; j<RobotAction.numRobotActions; j++)
			{
				qTable[i][j] = 0.0;
			}
		}
	}
	
	public void saveQTable()
	{
		try 
		{
			FileWriter fw = new FileWriter(new File("/home/lili/workspace/EECE592/ReinforcementLearning/src/ReinforcementLearning/myQtable.txt"));
	
			for(int i=0; i<RobotState.numStates; i++)
			{
				for(int j=0; j<RobotAction.numRobotActions; j++)
				{
					fw.write("state:  "+i+"  action:   "+j+"  Qvalue   "+Double.toString(getQValue(i,j)));
					fw.write("\r\n");
				}
			}
			fw.close();
		 }
		catch (IOException ex) 
		{
			
            ex.printStackTrace();

        }
    }
	
	
	public double maxQValue(int state)
	{
		double maxQvalue = qTable[state][0];
		for(int i=0; i<qTable[state].length;i++)
		{	
			if(qTable[state][i] > maxQvalue)
			{
				maxQvalue = qTable[state][i];
			}
		}
		return maxQvalue;
	}
	
	public int bestAction(int state)
	{
		double maxQvalue = qTable[state][0];
		
		int bestAct = 0;
		for(int i=0; i<qTable[state].length; i++)
		{
			double qValue = qTable[state][i];
			if(qValue > maxQvalue)
			{
				maxQvalue = qValue;
				bestAct = i;
			}
		}
		return bestAct;
	}
 
	public double getQValue(int state, int action)
	{
		return qTable[state][action];
	}
	
	public void setQValue(int state, int action, double value)
	{
		qTable[state][action] = value;
	}
	
	public double totalValue()
	{
		double sum =0.0;
		for(int i=0; i<RobotState.numStates; i++)
		{
			for(int j=0; j<RobotAction.numRobotActions; j++)
			{
				sum = sum + qTable[i][j];
				
			}
		}
		return sum;
	}
	
	
}
