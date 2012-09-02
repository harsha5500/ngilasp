/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package System;

import Messaging.QueueUser;
import Agents.Agent;
import GlobalData.CTANetwork;
import GlobalData.Constants;
import Messaging.Message;
import Messaging.QueueManager;
import Messaging.QueueParameters;
import Utilities.ConfigLoader;
import Utilities.KmlUtility;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Default CareTakerAgent. All types of CareTakes extends this class. The
 * properties and methods of this class is shared among all care taker agents.
 */
public abstract class CareTakerAgent implements QueueUser, Serializable {

    /**
     * The current TICK number. TICK number indicates the steps of
     * simulation and is used for Syncronization of various CTAs.
     */
    public static int currentTickNumber;
    /**
     * This flag when set will direct the CTA to start saving its state
     */
    public static boolean initiateStateSave = false;
    /**
     * This is the tick number at which other CTAs will save thier states
     */
    public static int tickNumberForStateSave = -1;
    /*
     *
     */
    /**
     * 
     */
    protected KmlUtility kmlUtility = null;
    /**
     * A list of all agents handled by the CTA
     */
    public List<Agent> agents;
    /**
     *
     */
    public QueueManager queueManager;
    /**
     *
     */
    public static Map<String, Integer> CTAStatus;

    /**
     * 
     * @return
     */
    public int getCurrentTickNumber() {
        return currentTickNumber;
    }

    /**
     * Abstract function to be overridden by each CTA. This function should contain the logic for checking if every agent has completed its task for the current tick.
     * @return
     */
    public abstract boolean checkIfAllAgentsReadyForNextTick();

    /**
     *
     */
    public abstract void activateAgentBehaviour();

    /**
     *
     * @param flag 
     * @return
     */
    public boolean checkIfAllCTAsReadyForNextTick(boolean flag) {
        if (CTAStatus.size() == 0) {
            Utilities.Log.logger.info("I am the only host");
            return true;
        }
        Utilities.Log.logger.info("Holdmessages status:" + flag);
        if (CTAStatus.containsValue(CTANetwork.CTA_COMPUTING)) {
            Utilities.Log.logger.info("Some hosts are busy");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function checks if every CTA in the system is ready to start saving their state.
     * @return true if each CTA is ready, false otherwise
     */
    public boolean checkIfAllCTAsReadyForStateSave() {
        if (CTAStatus.size() == 0) {
            Utilities.Log.logger.info("I am the only host");
            return true;
        }

        if (CTAStatus.containsValue(CTANetwork.CTA_COMPUTING)) {
            Utilities.Log.logger.info("Some hosts are busy");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function checks if every CTA in the system has finished saving their states.
     * @return true if each CTA is ready, false otherwise
     */
    public boolean checkIfAllCTAsDoneWithStateSave() {
        if (CTAStatus.size() == 0) {
            Utilities.Log.logger.info("I am the only host");
            return true;
        }

        if (CTAStatus.containsValue(CTANetwork.CTA_COMPUTING) || CTAStatus.containsValue(CTANetwork.CTA_SAVING_STATE)) {
            Utilities.Log.logger.info("Some hosts are busy");
            return false;
        } else {
            return true;
        }
    }

    /**
     * This function checks if every CTA in the system has completed restoring their states from an input file.
     * @return true if each CTA has completed, false otherwise
     */
    public boolean checkIfAllCTAsDoneWithRestoreState() {
        if (CTAStatus.size() == 0) {
            Utilities.Log.logger.info("I am the only host");
            return true;
        }

        if (CTAStatus.containsValue(CTANetwork.CTA_COMPUTING) || CTAStatus.containsValue(CTANetwork.CTA_SAVING_STATE)) {
            Utilities.Log.logger.info("Some hosts are busy");
            return false;
        } else {
            return true;
        }
    }

    /**
     *
     */
    public void addQueueListener() {
        QueueParameters queueParameters = CTANetwork.hostQueueMap.get(Constants.localHost);
        queueManager = QueueManager.getInstance(queueParameters, this);
        queueManager.start();

        //processMessage = new ProcessReceivedMessage(agents);
        //processMessage.start();
        processMessage.agents = agents;
    }

    /**
     *
     */
    public void sendReadyForTick() {
        for (String host : CTAStatus.keySet()) {
            Integer status = CTAStatus.get(host);
            if (status == CTANetwork.CTA_COMPUTING || status == CTANetwork.CTA_READY_FOR_NEXT_TICK) {
                //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_READY_FOR_NEXT_TICK);
                Message sendTickMessage = new Message();
                sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
                sendTickMessage.hostName = Constants.localHost;

                Message statusMessage = new Message();
                statusMessage.type = CTANetwork.CTA_READY_FOR_NEXT_TICK;
                statusMessage.messageObject = Constants.localHost;
                statusMessage.hostName = Constants.localHost;

                sendTickMessage.messageObject = statusMessage;
                queueManager.send(host, sendTickMessage);
            }
        }
    }

    /**
     *
     */
    public void sendDoneWithWork() {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING || CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK) {
                //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_DONE_WITH_WORK);
                Message sendTickMessage = new Message();
                sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
                sendTickMessage.hostName = Constants.localHost;

                Message statusMessage = new Message();
                statusMessage.type = CTANetwork.CTA_DONE_WITH_WORK;
                statusMessage.hostName = Constants.localHost;
                statusMessage.messageObject = Constants.localHost;

                sendTickMessage.messageObject = statusMessage;
                queueManager.send(host, sendTickMessage);
            }
        }
    }

    /**
     *
     * @param tickNumber 
     */
    public void sendSavingState(int tickNumber) {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING || CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK || CTAStatus.get(host) == CTANetwork.CTA_SAVING_STATE) {
                //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_DONE_WITH_WORK);
                Message sendTickMessage = new Message();
                sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
                sendTickMessage.hostName = Constants.localHost;

                Message statusMessage = new Message();
                statusMessage.type = CTANetwork.CTA_SAVING_STATE;
                statusMessage.hostName = Constants.localHost;
                statusMessage.messageObject = tickNumber;

                sendTickMessage.messageObject = statusMessage;
                queueManager.send(host, sendTickMessage);
            }
        }
    }

    /**
     *
     */
    public void sendSavedState() {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING || CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK || CTAStatus.get(host) == CTANetwork.CTA_SAVING_STATE) {
                //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_DONE_WITH_WORK);
                Message sendTickMessage = new Message();
                sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
                sendTickMessage.hostName = Constants.localHost;

                Message statusMessage = new Message();
                statusMessage.type = CTANetwork.CTA_SAVED_STATE;
                statusMessage.hostName = Constants.localHost;
                statusMessage.messageObject = Constants.localHost;

                sendTickMessage.messageObject = statusMessage;
                queueManager.send(host, sendTickMessage);
            }
        }
    }

    /**
     *
     */
    public void sendRestoreState() {
        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info("In Send Restore Method");
            //if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING || CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK) {
            //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_READY_FOR_NEXT_TICK);
            Message sendTickMessage = new Message();
            sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
            sendTickMessage.hostName = Constants.localHost;

            Message statusMessage = new Message();
            statusMessage.type = CTANetwork.CTA_RESTORED_STATE;
            statusMessage.messageObject = Constants.localHost;
            statusMessage.hostName = Constants.localHost;

            sendTickMessage.messageObject = statusMessage;
            queueManager.send(host, sendTickMessage);
            //}
        }
    }

//    public void sendFinishedExecutingCTA() {
//        for (String host : CTAStatus.keySet()) {
//            //if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING || CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK) {
//            //queueManager.send(host, CTANetwork.RMQ_TYPE_STATUS_UPDATE + ":" + Constants.localHost + ":" + CTANetwork.CTA_DONE_WITH_WORK);
//            Message sendTickMessage = new Message();
//            sendTickMessage.type = CTANetwork.RMQ_TYPE_STATUS_UPDATE;
//            sendTickMessage.hostName = Constants.localHost;
//
//            Message statusMessage = new Message();
//            statusMessage.type = CTANetwork.CTA_COMPLETE_EXIT;
//            statusMessage.hostName = Constants.localHost;
//            statusMessage.messageObject = Constants.localHost;
//
//            sendTickMessage.messageObject = statusMessage;
//            queueManager.send(host, sendTickMessage);
//            // }
//        }
//    }
    /**
     *
     * @param host
     * @param status
     */
    public static void changeCTAStatus(String host, Integer status) {
//        if (CTAStatus.containsKey(host)) {
//            CTAStatus.remove(host);
//        }
        CTAStatus.put(host, status);
    }

    /**
     *
     */
    public void buildCTAStatus() {
        Utilities.Log.logger.info("Building list");
        Iterator<String> hosts = CTANetwork.hosts.iterator();
        while (hosts.hasNext()) {
            String host = hosts.next();
            if (!host.equalsIgnoreCase(Constants.localHost)) {
                CTAStatus.put(host, CTANetwork.CTA_COMPUTING);
            }
        }
    }

    /**
     *
     */
    public void updateTimeOutList() {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_COMPUTING) {
                //CTAStatus.remove(host);
                Utilities.Log.logger.info(host + " : CTA has timed out");
                CTAStatus.put(host, CTANetwork.CTA_TIMED_OUT);
            }
        }

    }

    /**
     *
     */
    public void changeReadyCTAsToComputing() {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_READY_FOR_NEXT_TICK || CTAStatus.get(host) == CTANetwork.CTA_SAVING_STATE || CTAStatus.get(host) == CTANetwork.CTA_SAVED_STATE) {
                //CTAStatus.remove(host);
                //Utilities.Log.logger.info(host + " : CTA has timed out");
                CTAStatus.put(host, CTANetwork.CTA_COMPUTING);
            }
        }
    }

    /**
     * Changes the status of all CTAs to ready, once they finish saving their states.
     * This is done because the status of the CTA would be SAVED_STATE after they finish, and the system would not move forward.
     */
    public void changeSavedCTAsToReady() {
        for (String host : CTAStatus.keySet()) {
            if (CTAStatus.get(host) == CTANetwork.CTA_SAVED_STATE) {
                //CTAStatus.remove(host);
                //Utilities.Log.logger.info(host + " : CTA has timed out");
                CTAStatus.put(host, CTANetwork.CTA_READY_FOR_NEXT_TICK);
            }
        }
    }

    /**
     * 
     * @param ctaName
     */
    protected void writeKMLFile(String ctaName) {
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        //kmlUtility.writeFile();
        kmlUtility.writeFile("kml/" + ctaName + "_" + stamp + "_" + currentTickNumber + ".kml");

    }

    /**
     * 
     */
    protected void readConfigurations() {
        // Configuration functions
        try {
            ConfigLoader.loadMachineConfigurations("config/machineConfig");
            ConfigLoader.loadAgentConfigurations("config/agentConfig");
            ConfigLoader.loadHospitalLocations("config/hospitalConfig");
            ConfigLoader.loadDisasterLocations("config/disasterConfig");
            ConfigLoader.loadCivilVehicleConfigurations("config/vehicleConfig");
            ConfigLoader.loadEmergencyVehicleConfigurations("config/emergencyVehicleConfig");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Function to save state. Overridden by every CTA
     */
    protected void saveCTAState() {
    }

    /**
     * Function to restore state, from a file indicated by the parameter
     * @param filename 
     */
    protected void restoreState(String filename) {
    }
}
