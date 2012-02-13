package Agents;

import Agents.Attributes.AgentAttributes;
import Agents.Behaviour.CompositeBehaviour;
import java.io.Serializable;

/**
 * Abstract class which must be extended by all agents. The class defines that an agent is unique and has a behaviour
 * associated with it. The agents use the behaviours to achieve a set of objectives. When all the objectives are set are
 * achieved the objective flag is set by the agent. Agents also have a single or a set of utility functions to measure
 * if the agent has actually achieved its objectives.
 *
 * Every agent as a set of attributes and a set of behaviou
 * The agent may live in a universe or independent of the universe i.e. they their interaction is direct and does not
 * depend on each other's position.
 * @author bubby
 */

//TODO implement both types of agents.

abstract public class Agent extends Thread implements Serializable {

    /*
     * Unique Identifier for each agent
     */
    private String AID;
    /*
     * The flag to indicate if the final objective for the agent is complete
     */
    private boolean objectiveFlag;
    /*
     * Status of the agent for this particular run
     */
    private boolean statusFlag;
    /*
     * The behaviour for this agent
     */
    public CompositeBehaviour behaviour = null;

    public AgentAttributes agentAttributes;

    /**
     * returns the agent id
     * @return returns the agent id.
     */
    public String getAID() {
        return AID;
    }

    public boolean getObjectiveFlag() {
        return objectiveFlag;
    }

    public boolean getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(boolean status){
        statusFlag = status;
    }
}
