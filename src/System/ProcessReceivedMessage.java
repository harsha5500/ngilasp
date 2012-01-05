/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package System;

import Agents.Agent;
import Agents.Person;
import GlobalData.CTANetwork;
import Messaging.Message;
import Messaging.QueueUser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implemented as a thread will interpret the message received
 * 
 */
public class ProcessReceivedMessage extends Thread implements QueueUser, Serializable {

    public List<Agent> agents;
    public Message message;

    public ProcessReceivedMessage() {
        Utilities.Log.logger.info("Created Message Receiver");
        
    }

    public void run() {
        receivedMessage();
    }


    public void receivedMessageHelper(Message message) {
        this.message = message;
    }

    public void receivedMessage() {
        //update status of the host from which message was received
        Utilities.Log.logger.info("PeopleCTA: ReceivedMessage");
        switch (message.type) {
            case CTANetwork.RMQ_TYPE_STATUS_UPDATE:
                //Find out what type of status update it is
                Message statusType = (Message) message.messageObject;
                switch (statusType.type) {
                    case CTANetwork.CTA_READY_FOR_NEXT_TICK:
                        CareTakerAgent.changeCTAStatus(statusType.hostName, statusType.type);
                        Utilities.Log.logger.info("Received Next Tick from : " + statusType.hostName + ":" + statusType.type);
                        break;
                    case CTANetwork.CTA_DONE_WITH_WORK:
                        CareTakerAgent.changeCTAStatus(statusType.hostName, statusType.type);
                        Utilities.Log.logger.info("Received Done with work from : " + statusType.hostName + ":" + statusType.type);
                        break;
                    case CTANetwork.CTA_SAVING_STATE:
                        Utilities.Log.logger.info("Received state save message from " + statusType.hostName + "at tick: " + CareTakerAgent.currentTickNumber);
                        CareTakerAgent.tickNumberForStateSave = (Integer) statusType.messageObject;
                        CareTakerAgent.initiateStateSave = true;
                        CareTakerAgent.changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_SAVED_STATE:
                        Utilities.Log.logger.info("Received state saved message from " + statusType.hostName + "at tick: " + CareTakerAgent.currentTickNumber);
                        CareTakerAgent.changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_RESTORED_STATE:
                        Utilities.Log.logger.info("Received restored state message from " + statusType.hostName + "at tick: " + CareTakerAgent.currentTickNumber);
                        CareTakerAgent.changeCTAStatus(statusType.hostName, statusType.type);
                        break;
//                    case CTANetwork.CTA_COMPLETE_EXIT:
//                        Utilities.Log.logger.info("VehicleCTA: Done with work and Exitting.....");
//                        changeCTAStatus(statusType.hostName, statusType.type);
//                        break;
                }
                break;

            case CTANetwork.RMQ_TYPE_AGENT_DATA:
                List<Person> pickedUpPersonList = (List<Person>) message.messageObject;
                //chcek for duplicates and update the critically injured list
                Utilities.Log.logger.info("Received Picked up People List : " + pickedUpPersonList.size());
                for (Person person : pickedUpPersonList) {
                    if (agents.contains(person)) {
                        agents.remove(person);
                    }
                }
                //Utilities.Log.logger.info("Received Critically Injured People " + criticalPersonList.size() + "With values (id,value):" + criticalPersonList);
                break;
        }
    }
}
