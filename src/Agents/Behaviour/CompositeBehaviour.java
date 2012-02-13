package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import java.util.ArrayList;

/**
 * This behaviour allows the agents to execute one behaviour after another in a list. Each agent has only one composite
 * behaviour. However, multiple behaviours can be grouped withing the composite behaviour.
 * @author flummoxed
 * @author bubby
 */

//TODO a scheduling mechanism may be defined for the entire list.

public class CompositeBehaviour implements Behaviour {

    private ArrayList<Behaviour> behaviours = new ArrayList<Behaviour>();

    /**
     * To add a new behaviour to the end of the list.
     * @param newBehaviour the new behaviour to be added
     */
    public void add(Behaviour newBehaviour) {
        behaviours.add(newBehaviour);
    }

    /**
     * To add a new behaviour at a specified index in the list.
     * @param newBehaviour  the new behaviour to be added.
     * @param index  the index at which the behaviour needs to be added.
     */
    public void add(Behaviour newBehaviour, int index) {
        behaviours.add(index, newBehaviour);
    }

    /**
     * To remove the last behaviour from the end of the list.
     * @param newBehaviour  the behaviour to be removed,
     */

    public void remove(Behaviour newBehaviour) {
        behaviours.remove(newBehaviour);
    }

    /**
     * To clear all the behaviours from the list.
     */
    public void purge() {
        behaviours.clear();
    }

    public void run(AgentAttributes agentAttributes) {
        for (int i = 0; i < behaviours.size(); i++) {
            behaviours.get(i).run(agentAttributes);
        }
    }
}