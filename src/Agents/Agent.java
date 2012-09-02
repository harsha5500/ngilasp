package Agents;

import Agents.Behaviour.CompositeBehaviour;
import java.io.Serializable;

/**
 * Abstract class which must be extended by all agents
 * The class defines that an agent is unique and has a behaviour
 * associated with it.
 * @author bubby
 */
abstract public class Agent extends Thread implements Serializable {

    /** A unique identifier for each individual agent.*/
    String AID;
    
    /**
     * The flag to indicate if the final objective for the agent is complete.
     */
    public boolean objectiveFlag;
    
    /** The status of the agent for this particular run. */
    public boolean statusFlag;
    
    /** The behaviour for this agent which is a composition of different 
     * behaviours.
     */
    public CompositeBehaviour behaviour = null;

    /**
     * returns an agent's unique identifier.
     * @return returns the agent id.
     */
    public String getAID() {
        return AID;
    }

    // TODO Shift Agent attributes variable here
}
