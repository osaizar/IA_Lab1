package tddc17;


	import aima.core.environment.liuvacuum.*;
	import aima.core.agent.Action;
	import aima.core.agent.AgentProgram;
	import aima.core.agent.Percept;
	import aima.core.agent.impl.*;

	import java.util.Random;

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
	}

	class MyAgentProgram implements AgentProgram {

		private int initnialRandomActions = 10;  // Original value: 10
		private Random random_generator = new Random();

		// Here you can define your variables!
		public int iterationCounter = 1000;  // Original value: 10
		public MyAgentState state = new MyAgentState();
		// Added variables
		private boolean bottomRight = false;
		private boolean rightWall = false;
		private boolean additionalRow = false;
		private boolean evenRow = false;
		private boolean homeCheckpoint = false;

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

		    //iterationCounter--;

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


	    	// v1.0:
			// Once we are on the bottom right corner, the cleaner runs row by row
			// Conditions: No obstacles

			// First of all, let's direct the vacuum cleaner to the bottom right corner
			if (!bottomRight) {

				if (state.agent_direction != MyAgentState.EAST && !rightWall) {
					state.agent_last_action = state.ACTION_TURN_RIGHT;
					state.agent_direction = ((state.agent_direction+1) % 4);
					return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}

				if (!bump) {
					state.agent_last_action = state.ACTION_MOVE_FORWARD;
					return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
				else {
					if (!rightWall) {
						// The cleaner has found the right wall, now should go to the bottom
						rightWall = true;
						state.agent_last_action = state.ACTION_TURN_RIGHT;
						state.agent_direction = ((state.agent_direction+1) % 4);
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
					}
					else {
						bottomRight = true;
					}
				}
			}

			/// Then we face west
			if (state.agent_direction != MyAgentState.WEST && rightWall) {
			    // Don't take the rightWall variable name too seriously, it's just a reutilized one. We only want to check
                // that the program only enters here once
				rightWall = false;
				System.out.println("Checking for additionalRow... ");
				if (state.agent_y_position % 2 == 0 ) {
					System.out.println("true");
					additionalRow = true;
				}
				else {
					System.out.println("false");
					additionalRow = false;
					homeCheckpoint = true;
				}

				state.agent_last_action = state.ACTION_TURN_RIGHT;
				state.agent_direction = ((state.agent_direction + 1) % 4);
				return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
			}

			if (state.agent_direction != MyAgentState.WEST && state.agent_direction != MyAgentState.EAST) {

				if (additionalRow && homeCheckpoint) {  // This is optional, the code would work without it but we avoid a "bump"
					state.agent_last_action = state.ACTION_TURN_LEFT;
					state.agent_direction = state.agent_direction - 1;
			    	if (state.agent_direction < 0)
			    		state.agent_direction += 4;
					return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				else if (state.agent_last_action != state.ACTION_MOVE_FORWARD) {
					evenRow = !evenRow;
					System.out.println("Starting turning movement");
					state.agent_last_action = state.ACTION_MOVE_FORWARD;
		    		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
				}
				else {
					if (!evenRow) {
						System.out.println("Ending turning movement -> Left");
						state.agent_last_action = state.ACTION_TURN_LEFT;
						state.agent_direction = state.agent_direction - 1;
			    		if (state.agent_direction < 0)
			    			state.agent_direction += 4;
						return LIUVacuumEnvironment.ACTION_TURN_LEFT;
					}
					else {
						System.out.println("Ending turning movement -> Right");
						state.agent_last_action = state.ACTION_TURN_RIGHT;
						state.agent_direction = ((state.agent_direction + 1) % 4);
						return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
					}
				}
			}

		    // Next action selection based on the percept value
		    if (dirt) {
		    	System.out.println("DIRT -> choosing SUCK action!");
		    	state.agent_last_action = state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
		    }
		    else if (home && additionalRow && !homeCheckpoint) {
                System.out.println("Got home, additional row pending");
				homeCheckpoint = true;
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
		    	return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
		    }
		    else if (home && homeCheckpoint) {
		    	System.out.println("HOME -> Shuting down");
		    	state.agent_last_action = state.ACTION_NONE;
		    	return NoOpAction.NO_OP;
		    }
		    else if (bump) {
				System.out.println("BUMP! -> entering turning mode");
				if (homeCheckpoint && additionalRow) {
					System.out.println("Returning home -> 180º turn");
					state.agent_last_action = state.ACTION_TURN_LEFT;
					state.agent_direction = state.agent_direction - 1;
			    	if (state.agent_direction < 0)
			    		state.agent_direction += 4;
				    return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				else if (evenRow) {
					System.out.println("Turning mode -> evenRow (left)");
					state.agent_last_action = state.ACTION_TURN_LEFT;
					state.agent_direction = state.agent_direction - 1;
			    	if (state.agent_direction < 0)
			    		state.agent_direction += 4;
				    return LIUVacuumEnvironment.ACTION_TURN_LEFT;
				}
				else {
					System.out.println("Turning mode -> unevenRow (right)");
		    		state.agent_last_action = state.ACTION_TURN_RIGHT;
		    		state.agent_direction = ((state.agent_direction + 1) % 4);
			    	return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
				}
		    }
		    else {
				state.agent_last_action = state.ACTION_MOVE_FORWARD;
		    	return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
            }
        }
    }

	public class MyVacuumAgent extends AbstractAgent {
	    public MyVacuumAgent() {
	    	super(new MyAgentProgram());
  }
}
