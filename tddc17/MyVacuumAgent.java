	package tddc17;


	import aima.core.environment.liuvacuum.*;
	import aima.core.agent.Action;
	import aima.core.agent.AgentProgram;
	import aima.core.agent.Percept;
	import aima.core.agent.impl.*;

	import java.util.Random;
	import java.util.Stack;
  import java.util.EmptyStackException;

	class MyAgentState
	{
		public int[][] world = new int[30][30];
		public int initialized = 0;
		final int UNKNOWN 	= 0;
		final int WALL 		= 1;
		final int CLEAR 	= 2;
		final int DIRT		= 3;
		final int HOME		= 4;
		final int ACTION_NONE 			= 0;
		final int ACTION_MOVE_FORWARD 	= 1;
		final int ACTION_TURN_RIGHT 	= 2;
		final int ACTION_TURN_LEFT 		= 3;
		final int ACTION_SUCK	 		= 4;

		public int agent_x_position = 1;
		public int agent_y_position = 1;
		public int agent_last_action = ACTION_NONE;

		public static final int NORTH = 0;
		public static final int EAST = 1;
		public static final int SOUTH = 2;
		public static final int WEST = 3;
		public int agent_direction = EAST;

		MyAgentState()
		{
			for (int i=0; i < world.length; i++)
				for (int j=0; j < world[i].length ; j++)
					world[i][j] = UNKNOWN;
			world[1][1] = HOME;
			agent_last_action = ACTION_NONE;
		}
		// Based on the last action and the received percept updates the x & y agent position
		public void updatePosition(DynamicPercept p)
		{
			Boolean bump = (Boolean)p.getAttribute("bump");

			if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
		    {
				switch (agent_direction) {
				case MyAgentState.NORTH:
					agent_y_position--;
					break;
				case MyAgentState.EAST:
					agent_x_position++;
					break;
				case MyAgentState.SOUTH:
					agent_y_position++;
					break;
				case MyAgentState.WEST:
					agent_x_position--;
					break;
				}
		    }

		}

		public void updateWorld(int x_position, int y_position, int info)
		{
			world[x_position][y_position] = info;
		}

		public void printWorldDebug()
		{
			for (int i=0; i < world.length; i++)
			{
				for (int j=0; j < world[i].length ; j++)
				{
					if (world[j][i]==UNKNOWN)
						System.out.print(" ? ");
					if (world[j][i]==WALL)
						System.out.print(" # ");
					if (world[j][i]==CLEAR)
						System.out.print(" . ");
					if (world[j][i]==DIRT)
						System.out.print(" D ");
					if (world[j][i]==HOME)
						System.out.print(" H ");
				}
				System.out.println("");
			}
		}

		//Our Functions
		//Returns a direction near the agent where there's a unknown block
		public int getUnknownWorld(){
			if(world[agent_x_position][agent_y_position-1] == UNKNOWN)
				return MyAgentState.NORTH;
			if(world[agent_x_position+1][agent_y_position] == UNKNOWN)
				return MyAgentState.EAST;
			if(world[agent_x_position][agent_y_position+1] == UNKNOWN)
				return MyAgentState.SOUTH;
			if(world[agent_x_position-1][agent_y_position] == UNKNOWN)
				return MyAgentState.WEST;

			return -1; //Error
		}

		//Checks weather the next step of the agent is viable or not (known or not)
		public boolean checkNextStep(){
			int next = -1;

			if(agent_direction == MyAgentState.NORTH)
				next = world[agent_x_position][agent_y_position-1];
			if(agent_direction == MyAgentState.EAST)
				next = world[agent_x_position+1][agent_y_position];
			if(agent_direction == MyAgentState.WEST)
				next = world[agent_x_position-1][agent_y_position];
			if(agent_direction == MyAgentState.SOUTH)
				next = world[agent_x_position][agent_y_position+1];

			if(next == UNKNOWN)
				return true;
			else
				return false;
		}

		//Just a function to make turning right or left easier
		public void doAction(int action){
			agent_last_action = action;

			if(action == ACTION_TURN_RIGHT){
				agent_direction = ((agent_direction+1) % 4);
			}
			else if(action == ACTION_TURN_LEFT){
				agent_direction = ((agent_direction-1) % 4);
			    if (agent_direction<0)
			    	agent_direction +=4;
			}
		}
	}

	class MyAgentProgram implements AgentProgram {

		private int initnialRandomActions = 10;  // Original value: 10
		private Random random_generator = new Random();

		// Here you can define your variables!
		public int iterationCounter = 10;  // Original value: 10
		public MyAgentState state = new MyAgentState();

		// Added variables
		private Stack movementHistory = new Stack();
		private boolean backtracking = false; //if true, the agent enters backtracking mode
		private int turnsLeft = -1; //if positive, the agent will make the turns that are indicated.

		// moves the Agent to a random start position
		// uses percepts to update the Agent position - only the position, other percepts are ignored
		// returns a random action
		private Action moveToRandomStartPosition(DynamicPercept percept) {
			int action = random_generator.nextInt(6);
			initnialRandomActions--;
			state.updatePosition(percept);
			if(action==0) {
			    state.agent_direction = ((state.agent_direction-1) % 4);
			    if (state.agent_direction<0)
			    	state.agent_direction +=4;
			    state.agent_last_action = state.ACTION_TURN_LEFT;
				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
			} else if (action==1) {
				state.agent_direction = ((state.agent_direction+1) % 4);
			    state.agent_last_action = state.ACTION_TURN_RIGHT;
			    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}
			state.agent_last_action=state.ACTION_MOVE_FORWARD;
			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		}


		@Override
		public Action execute(Percept percept) {

			// DO NOT REMOVE this if condition!!!
	    	if (initnialRandomActions>0) {
	    		return moveToRandomStartPosition((DynamicPercept) percept);
	    	} else if (initnialRandomActions==0) {
					// process percept for the last step of the initial random actions
	    		initnialRandomActions--;
	    		state.updatePosition((DynamicPercept) percept);
				System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
				state.agent_last_action=state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
				}

	    	// This example agent program will update the internal agent state while only moving forward.
	    	// START HERE - code below should be modified!

	    	System.out.println("x=" + state.agent_x_position);
	    	System.out.println("y=" + state.agent_y_position);
	    	System.out.println("dir=" + state.agent_direction);

		    //iterationCounter--; // infinite iterations

		    if (iterationCounter==0)
		    	return NoOpAction.NO_OP;

		    DynamicPercept p = (DynamicPercept) percept;
		    Boolean bump = (Boolean)p.getAttribute("bump");
		    Boolean dirt = (Boolean)p.getAttribute("dirt");
		    Boolean home = (Boolean)p.getAttribute("home");
		    System.out.println("percept: " + p);

		    // State update based on the percept value and the last action
		    state.updatePosition((DynamicPercept)percept);
		    if (bump) {
				switch (state.agent_direction) {
				case MyAgentState.NORTH:
					state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
					break;
				case MyAgentState.EAST:
					state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
					break;
				case MyAgentState.SOUTH:
					state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
					break;
				case MyAgentState.WEST:
					state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
					break;
				}
		    }
		    if (dirt)
		    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
		    else
		    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR);

		    state.printWorldDebug();


		   // v2.0:
		   // Conditions: Obstacles
			 /*
			 Idea:
			 The agent will walk on a straight line untill it bumps or untill it is
			 going to step in a known square. Then the agent will change direction to
			 a near unknown block. If there is not any unknown position nearby, The
			 agent will undo its movements untill it finds one.

			 With this algorithm we know that the agent will go to every possible
			 square on the map and it will end on his start possition. (Home or not)

			 Currently, the backtracking is not working very well.
				*/

		    if (dirt) {
		    	System.out.println("DIRT -> choosing SUCK action!");
		    	state.agent_last_action = state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
		  	}
		  	else if(turnsLeft > 0){
		  		turnsLeft--;
		  		state.doAction(state.ACTION_TURN_RIGHT); //all the turns are to the right. (Not a very efficient way)
		  		if(!backtracking)movementHistory.push(state.ACTION_TURN_RIGHT);
		  		return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		  	}
		  	else if(backtracking){
		    	if(state.getUnknownWorld() != -1){ //if we have a unknown direction to go, backtracking has ended
		    		System.out.println("backtrack ended");
		    		backtracking = false;
		    	}
		    	else{
		    			System.out.println("backtracking...");
							int action;
							try{
								action = (Integer) movementHistory.pop();
							}catch(EmptyStackException e){
                System.out.println("Stack ended, all the positions checked.");
                return NoOpAction.NO_OP;
              }

		    			if(action == state.ACTION_TURN_LEFT){
		    				state.doAction(state.ACTION_TURN_RIGHT);
		    				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		    			}
		    			else if(action == state.ACTION_TURN_RIGHT){
		    				state.doAction(state.ACTION_TURN_LEFT);
		    				return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		    			}
		    			state.doAction(state.ACTION_MOVE_FORWARD);
		    			return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		    		}
		    	}
		  	else if(bump || !state.checkNextStep()){ //if this is true, we need a path change
		  		System.out.println("Path change needed bump["+bump+"] next step ["+state.checkNextStep()+"]");
		    	if(state.getUnknownWorld() != -1){ //if there is a unknown direction to go, the agent will go there.
		    		System.out.println("Finding new Path...");
		    		int path = state.getUnknownWorld();
		    		turnsLeft = path - state.agent_direction;
		    		if(turnsLeft < 0)
		    			turnsLeft = 4 + turnsLeft;
		    		System.out.println("Path: "+path+" turnsLeft: "+turnsLeft+" (rigth)");
		    	}else{ //if there's not, we need to backtrack
		    		System.out.println("Starting backtrack...");
		    		backtracking = true;
		    		turnsLeft = 2; //180 turn to go back
		    	}
		    }
		  	else{
		    	movementHistory.push(state.ACTION_MOVE_FORWARD);
		    	state.doAction(state.ACTION_MOVE_FORWARD);
		    	return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		    }

		    state.doAction(state.ACTION_SUCK); //do nothing. "NoOpAction.NO_OP" stops the agent
		    return LIUVacuumEnvironment.ACTION_SUCK;
		  }
		}

	public class MyVacuumAgent extends AbstractAgent {
	    public MyVacuumAgent() {
	    	super(new MyAgentProgram());
		}
	}
