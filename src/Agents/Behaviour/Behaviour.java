package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import java.io.Serializable;

/**
 * The behavior is a single task that can be run. Each task is thus stated as Serializable to allow for running multiple
 * behaviours as a group of java threads.
 * @author bubby
 */
public interface Behaviour extends Serializable{

    /**
     * Each behaviour is run as a single thread.
     * 
     * @param agentAttributes the attribute values required to execute the behaviour.
     */
    public void run(AgentAttributes agentAttributes);

}
