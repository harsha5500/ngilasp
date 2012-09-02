/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import java.util.ArrayList;

/**
 *
 * @author flummoxed
 */
public class CompositeBehaviour implements Behaviour {

    private ArrayList<Behaviour> behaviours = new ArrayList<Behaviour>();

    /**
     *
     * @param newBehaviour
     */
    public void add(Behaviour newBehaviour) {
        behaviours.add(newBehaviour);
    }

    /**
     * 
     * @param newBehaviour
     */
    public void remove(Behaviour newBehaviour) {
        behaviours.remove(newBehaviour);
    }

    /**
     * 
     */
    public void removeall() {
        behaviours.clear();
    }

    /**
     *
     * @param newBehaviour
     * @param index
     */
    public void add(Behaviour newBehaviour, int index) {
        behaviours.add(index, newBehaviour);
    }

    public void run(AgentAttributes agentAttributes) {
        for (int i = 0; i < behaviours.size(); i++) {
            behaviours.get(i).run(agentAttributes);
        }
    }
}
