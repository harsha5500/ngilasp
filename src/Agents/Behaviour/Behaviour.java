/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Agents.Behaviour;

import Agents.Attributes.AgentAttributes;
import java.io.Serializable;

/**
 * The behavior mechanism is implemented through 'Composite' design pattern.
 * @author sagar
 */
public interface Behaviour extends Serializable{

    /**
     * The logic of the behaiour
     * 
     * @param agentAttributes
     */
    public void run(AgentAttributes agentAttributes);

}
