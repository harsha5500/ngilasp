package System;

import Agents.Agent;
import Agents.Person;
import Database.Sql;
import Entity.CustomStyle;
import Entity.Disaster;
import Entity.Hospital;
import Entity.TrafficLight;
import GeographicInformation.BoundingBox;
import GeographicInformation.Location;
import GlobalData.CTANetwork;
import GlobalData.Constants;
import GlobalData.SharedData;
import Messaging.Message;
import Messaging.QueueParameters;
import Utilities.KmlUtility;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Care taker agent for all traffic lights. This CTA is responsible to generate
 * the individual traffic lights and changes the light based on the number of ticks.
 * The CTA also exchanges information with the Vehicle CTA.
 */
public class TrafficLightCTA extends CareTakerAgent {

     HashMap<String, CustomStyle> trafficLightStyles = new HashMap<String, CustomStyle>();

    /**
     * Stop receiving messages when the flag is true.
     */
    private boolean holdMessages = false;
    /**
     * The locations where the traffic lights will be drawn on the map.
     * The locations are constant for the traffic light and only the style
     * changes for every tick.
     */
    private ArrayList<Location> agentLocations;
    /**
     * The style of the individual traffic lights change.
     */
    private ArrayList<String> agentStyles;
    /**
     * The status of all the traffic lights to be broadcasted to all the VehicleCTA
     */
    //private HashMap<Long, Integer> agentSignalStatus;
    private ArrayList<TrafficLight> agentSignalStatus;
    private boolean restoreState = false;

    /**
     *  Constructor to create the TrafficLight CTA
     */
    public TrafficLightCTA() {
        Utilities.Log.ConfigureLogger();
        readConfigurations();
        createObjects();
        //CTAStatus = Collections.synchronizedMap(new HashMap<String, Integer>());
        buildCTAStatus();
        //agents = new ArrayList<Agent>();
        //agentLocations = new ArrayList<Location>();
        //agentStyles = new ArrayList<String>();
        //agentSignalStatus = new HashMap<Long, Integer>();
        //agentSignalStatus = new ArrayList<TrafficLight>();
        addQueueListener();

        Utilities.Log.logger.info("TrafficLightHub CTA Started");
        Utilities.Log.logger.info("Number of CTAs : " + CTANetwork.hosts.size());
    }

    private TrafficLightCTA(String filename) {
        Utilities.Log.ConfigureLogger();
        createObjects();
        restoreState(filename);
        addQueueListener();
        restoreState = true;
        Utilities.Log.logger.info("Restored state after " + currentTickNumber + " tick");
        sendRestoreState();
        Utilities.Log.logger.info("Sent Restored State Message");
        long timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsDoneWithRestoreState()) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saving state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        buildCTAStatus();
        Utilities.Log.logger.info("Number of CTAs : " + CTANetwork.hosts.size());
    }

    /**
     * Creates all the objects necessary for the CTA operation.
     * @see EmergencyServiceCTA
     * @see CareTakerAgent
     */
    private void createObjects() {
        CTAStatus = Collections.synchronizedMap(new HashMap<String, Integer>());
        agents = new ArrayList<Agent>();
        agentStyles = new ArrayList<String>();
        agentSignalStatus = new ArrayList<TrafficLight>();
        agentLocations = new ArrayList<Location>();
    }

    /**
     * Create the traffic light Hub agents that are responsible for changing the
     * status of the traffic lights they control for their respective locations.
     */
    private void createTrafficLightHubs() {

        //Read the database for a list of hubs and create the agents.
        ArrayList<Integer> hubIds = Sql.getTrafficHubs();
        Utilities.Log.logger.info("TrafficLightHubCTA:: Number of Hubs:" + hubIds.size());

        for (int i = 0; i < hubIds.size(); i++) {
            int hubId = hubIds.get(i);
            TrafficLightHub trafficLightHub = new TrafficLightHub(hubId);
            agents.add(trafficLightHub);
        }

        /* For each hub add the locations of the TrafficLights to the agentlocations to
        place the placemarks in the KML*/
        for (Agent t : agents) {
            ArrayList<TrafficLight> agentLights = ((TrafficLightHub) t).getAttributes().trafficLights;
            for (TrafficLight l : agentLights) {
                agentLocations.add(l.getLocation());
                agentStyles.add("" + l.getLight());
            }
        }
        Utilities.Log.logger.info("Finished Creating TrafficHubs. Added " + agents.size() + " agents with" + agentLocations.size() + "traffic lights.");
    }

    /**
     * Check if all the agents are ready for next tick.
     * @return true if all agents are ready.
     */
    @Override
    public boolean checkIfAllAgentsReadyForNextTick() {
        int count = 0;
        for (Agent p : agents) {
            if (p.statusFlag) {
                count++;
            }
        }
        if ((count == agents.size()) && (count > 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Activate the agents, by calling their action methods
     */
    @Override
    public void activateAgentBehaviour() {
        Collections.shuffle(agents);
        for (Agent p : agents) {
            p.run();
        }
    }

    /**
     * Cleanup operations performed by the care taker after each run of all agents.
     * The cleanup operation adds the placemarks to the kml files and writes out
     * the output. In addition it changes the status of the CTA to ready for next
     * tick.
     */
    private void cleanUp() {
        sendReadyForTick();
         kmlUtility.addPlacemarks(agentLocations, agents, "traffic", agentStyles,this.getCurrentTickNumber());
        //kmlUtility.addPlacemarks(agentLocations, agentStyles, currentTickNumber);
        if (currentTickNumber % 50 == 0) {
            writeKMLFile("trafffic");
            kmlUtility = new KmlUtility(trafficLightStyles);
        }
        for (String host : CTAStatus.keySet()) {
            Utilities.Log.logger.info(host + " : " + CTAStatus.get(host));
        }

        if ((currentTickNumber % SharedData.saveStateAtTick == 0 && SharedData.isStateSaveEnabled) || initiateStateSave) {
            initiateStateSave = false;
            Utilities.Log.logger.info("Sending message for CTAs to save their respective states at tick: " + currentTickNumber);
            sendSavingState(currentTickNumber);
            beginStateSave();
            holdMessages = true;
            changeSavedCTAsToReady();
            holdMessages = false;
        }
    }

    /**
     * This begins the process of saving states, for the CTA.
     * After sending a message to other CTAs saying it is saving its state, it waits until every CTA is also doing the same.
     * It then writes the state as binary data into a file, and sends a message saying it has completed saving state.
     * It waits until all CTAs have completed saving their respective states before exiting the funtion.
     */
    private void beginStateSave() {
        // Check if all CTAs are saving state
        long timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsReadyForStateSave()) {
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saving state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        saveCTAState();
        initiateStateSave = false;
        sendSavedState();

        // Check if all CTAs have finished saving state
        timeBeforewaiting = System.currentTimeMillis();
        while (!checkIfAllCTAsDoneWithStateSave()) {
            Utilities.Log.logger.info("Waiting for CTA to finish saving state");
            try {
                long timeNow = System.currentTimeMillis();
                if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_SAVING_STATE) {
                    updateTimeOutList();
                    Utilities.Log.logger.info("Continuing with run after timeout for saved state");
                    break;
                } else {
                    //wait until all CTAs are ready
                    Thread.sleep(500);
                }

            } catch (InterruptedException ex) {
                Logger.getLogger(PeopleCTA.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Create the all the style objects that will be used by the any of the
     * traffic lights. The style color will depend on the state of the
     * traffic light and will change for every tick.
     */
    private void createStyles() {

       

        CustomStyle red = new CustomStyle();
        red.color = Constants.SIGNAL_COLOR_RED;
        red.scale = Constants.SIGNAL_SCALE;
        red.url = Constants.SIGNAL_IMAGE;

        CustomStyle green = new CustomStyle();
        green.color = Constants.SIGNAL_COLOR_GREEN;
        green.scale = Constants.SIGNAL_SCALE;
        green.url = Constants.SIGNAL_IMAGE;

        trafficLightStyles.put("" + Constants.SIGNAL_RED, red);
        trafficLightStyles.put("" + Constants.SIGNAL_GREEN, green);

        kmlUtility = new KmlUtility(trafficLightStyles);
    }

    /**
     * This method is called to set up the CTA for operation
     */
    private void setUp() {
        // Boot functions
        if (Utilities.AIDGenerator.initializeAIDGen()) {
            createStyles();
        } else {
            Utilities.Log.logger.info("AIDs cannot be generated");
            //System.exit(0);
            exitSystem();
        }

        // Generate Agents
        if (!restoreState) {
            createTrafficLightHubs();
        }

        Utilities.Log.logger.info("Finished TrafficLight SetUp");
        sendReadyForTick();
    }

    /**
     * this method is called by the CTA when it needs to terminate.
     */
    private void exitSystem() {
        sendDoneWithWork();
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        kmlUtility.writeFile("kml/" + stamp + ".kml");
        System.exit(0);
    }

    /**
     * The run agents calls the individual agents run methods and advances the
     * agent by one tick. The CTA does not move forward untill all agents have
     * completed their run behaviour once within a given timeout.
     * The EmergencyService run agent calls the run method of the agents but does not
     * check if the agents are home as these agents are active for the entire simulation,
     * similar to a real emergency services.
     * @see CareTakerAgent
     */
    private void runAgents() {
        activateAgentBehaviour();
        while (checkIfAllAgentsReadyForNextTick()) {
            //Change the styles for signal placemarks to indicate a signal change.
            /* For every trafficLight in handeled by trafficLightHub add a new style
            and record its status. */
            for (int i = agents.size() - 1; i > -1; i--) {
                agents.get(i).statusFlag = false;
            }
            //String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
            //kmlUtility.writeFile("kml/" + stamp + ".kml");
            //Purge the previous values
            agentStyles = new ArrayList<String>();
            //agentSignalStatus = new HashMap<Long, Integer>();
            agentSignalStatus = new ArrayList<TrafficLight>();
            for (int i = agents.size() - 1; i > -1; i--) {
                ArrayList<TrafficLight> agentLights = ((TrafficLightHub) agents.get(i)).getAttributes().trafficLights;
                for (TrafficLight l : agentLights) {
                    agentStyles.add("" + l.getLight());
                    //agentSignalStatus.put(l.getRoadid(), l.getColor());
                    //agentSignalStatus.put(l.getCellid(), l.getColor());
                    agentSignalStatus.add(l);
                    //Serilize object and broadcast it to all VehicleCTAs
                }
            }
        }
        Utilities.Log.logger.info("TrafficLightCTA::Completed runAgents");
    }

    /**
     *  This method serilizes the state of all the traffic lights (not hubs) and
     * sends it to the vehicle cta.
     * @return true if the message is sent successfully
     */
    private boolean sendTrafficlightStatus() {
        Message statusUpdate = new Message();
        statusUpdate.type = CTANetwork.RMQ_TYPE_AGENT_DATA;
        statusUpdate.messageObject = agentSignalStatus;

        ArrayList<String> broadCastList = CTANetwork.hostTypeMap.get(Constants.CTA_TYPE_VEHICLE);

        /*Chcek if there is a VehicleCTA at all?
         * If there are no vehicle CTA it means there are no vehicles. Hence the TrafficLightCTA can exit
         */
        if (broadCastList == null || broadCastList.size() == 0) {
            Utilities.Log.logger.info("TrafficLightCTA::There are no vehicle CTAs to send the message to.");
            //return;
            Utilities.Log.logger.info("Exiting TrafficLightCTA");
            //exitSystem();
            return false;
        } else {

            ArrayList<String> activeHosts = new ArrayList<String>();
            for (String host : broadCastList) {
                //Check the status of the VehicleCTAs
                //Utilities.Log.logger.info("Current host status: " + host + ":" + CTAStatus.get(host));
                if (CTAStatus.get(host) != CTANetwork.CTA_DONE_WITH_WORK) {
                    activeHosts.add(host);
                } else {
                    Utilities.Log.logger.info("TrafficLightCTA:: " + host + "Host has Done with its work.");
                }
                Utilities.Log.logger.info("TrafficLightCTA:: ActiveHost Size: " + activeHosts.size());
            }

            Utilities.Log.logger.info("TrafficLightCTA:: " + activeHosts.size() + " VehicleCTAs are currently active.");
            if (activeHosts.size() == 0) {
                Utilities.Log.logger.info("There are no vehicle CTAs to send the message to.");
                Utilities.Log.logger.info("Exiting TrafficLightCTA");
                //exitSystem();
                return false;
            } else {
                for (String host : activeHosts) {
                    //queueManager.send(host, outputBuffer.toByteArray());
                    queueManager.send(host, statusUpdate);
                }
                return true;
            }
        }
    }

    /**
     * Any specific CTA initilizations are done here.
     */
    private void initialize() {
    }

    /**
     *  This function checks if all the agents on this CTA have satisfied their
     * objective, in which case the CTA has completed its task.
     * @return true if all agents in the CTA have completed their objectives
     */
    private boolean objectiveSatisfiedForAllAgents() {
        int count = 0;
        for (Agent p : agents) {
            if (p.objectiveFlag) {
                count++;
            }
        }
        if (count == agents.size()) {
            return true;
        } else {
            return false;
        }

    }

    /**
     *  This method complets one iteration of the CTA's operation. The method
     * terminates when all the agents in the CTA have completed their tasks.
     */
    private void runCTA() {
        try {
            setUp();

            //Runs until objectives for all agents is fulfilled
            while (!objectiveSatisfiedForAllAgents()) {
                initialize();

                //Time out for waiting for other agents
                long timeBeforewaiting = System.currentTimeMillis();

                //now check if all agents are ready
                while (!checkIfAllCTAsReadyForNextTick(holdMessages)) {
                    try {
                        long timeNow = System.currentTimeMillis();
                        if (timeNow - timeBeforewaiting >= CTANetwork.MAXIMUM_TIME_OUT_FOR_CTA) {
                            updateTimeOutList();
                            Utilities.Log.logger.info("Continuing with run after timeout");
                            break;

                        } else {
                            //wait until all CTAs are ready
                            Thread.sleep(500);
                        }


                    } catch (InterruptedException ex) {
                        Logger.getLogger(TrafficLightCTA.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (initiateStateSave && currentTickNumber >= tickNumberForStateSave) {
                } else {
                    holdMessages = true;
                    changeReadyCTAsToComputing();


                    //If sening the traffic light status fails then stop the CTA
                    if (sendTrafficlightStatus() == false) {
                        break;
                    }
                    Utilities.Log.logger.info("TrafficLightCTA: Size of the signalStatus:" + agentSignalStatus.size());
                    holdMessages = false;
                    runAgents();

                    Utilities.Log.logger.info("Current Tick Number: " + currentTickNumber);
                    currentTickNumber++;
                }
                cleanUp();
            }
            exitSystem();
        } catch (Exception e) {
            Utilities.Log.logger.info("Unable to complete work due to exection. Terminating" + e.getMessage());
            e.printStackTrace();
            sendDoneWithWork();
            exitSystem();
        }
    }

    /**
     *
     */
    @Override
    protected void saveCTAState() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        DataOutputStream dos = null;
        String stamp = new SimpleDateFormat("hh-mm-ss-aaa_dd-MMMMM-yyyy").format(new Date()).toString();
        String fileName = "savedstates/" + stamp + "_" + currentTickNumber + "_peopleCTA.data";
        try {

            fos = new FileOutputStream(fileName);
            out = new ObjectOutputStream(fos);
            dos = new DataOutputStream(fos);

            // Objects of PeopleCTA
            //out.writeObject(kmlUtility);
            //out.writeObject(queueManager);
            out.writeObject(CTAStatus);
            out.writeObject(agents);
            out.writeObject(agentLocations);
            out.writeObject(agentStyles);

            // Objects of CTANetwork
            out.writeObject(CTANetwork.hostQueueMap);
            out.writeObject(CTANetwork.hostTypeMap);
            out.writeObject(CTANetwork.hosts);

            // Objects of SharedData
            out.writeObject(SharedData.boundingBox);
            out.writeObject(SharedData.ambulancePerHospital);
            out.writeObject(SharedData.deadPeopleAgents);
            out.writeObject(SharedData.disasters);
            out.writeObject(SharedData.hospitals);
            out.writeObject(SharedData.trafficLights);
            out.writeObject(SharedData.injuredPeopleBeingServed);
            out.writeObject(SharedData.injuredPeopleRescued);
            out.writeObject(SharedData.newInjuredPeople);
            out.writeObject(SharedData.pickedUpinjuredPeople);

            dos.writeInt(SharedData.numberOfAgents);
            dos.writeInt(SharedData.numberOfAmbulances);
            dos.writeInt(SharedData.numberOfCivilVehicles);
            dos.writeInt(SharedData.numberOfFireVehicles);
            dos.writeInt(SharedData.numberOfPeopleCTA);
            dos.writeInt(SharedData.numberOfPoliceVehicles);
            dos.writeBoolean(SharedData.isDisasterTriggered);

            dos.writeInt(currentTickNumber);

            dos.writeBoolean(holdMessages);

            dos.close();
            fos.close();
            out.close();

            Utilities.Log.logger.info("Saved State");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Restores the state of this CTA from a file. Thsi will restore all objects of the CTA saved previously.
     * The CTA will continue running from the point where state was restored, at the point in the file where it was previously saved
     * @param filename
     */
    @Override
    protected void restoreState(String filename) {
        FileInputStream fis = null;
        ObjectInputStream in = null;
        DataInputStream din = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            din = new DataInputStream(fis);

            //queueManager = (QueueManager) in.readObject();
            CTAStatus = (Map<String, Integer>) in.readObject();
            agents = (ArrayList<Agent>) in.readObject();
            agentLocations = (ArrayList<Location>) in.readObject();
            agentStyles = (ArrayList<String>) in.readObject();

            CTANetwork.hostQueueMap = (HashMap<String, QueueParameters>) in.readObject();
            CTANetwork.hostTypeMap = (HashMap<Integer, ArrayList<String>>) in.readObject();
            CTANetwork.hosts = (ArrayList<String>) in.readObject();

            SharedData.boundingBox = (BoundingBox) in.readObject();
            SharedData.ambulancePerHospital = (List<Integer>) in.readObject();
            SharedData.deadPeopleAgents = (List<Person>) in.readObject();
            SharedData.disasters = (List<Disaster>) in.readObject();
            SharedData.hospitals = (List<Hospital>) in.readObject();
            SharedData.trafficLights = (List<TrafficLight>) in.readObject();
            SharedData.injuredPeopleBeingServed = (List<Person>) in.readObject();
            SharedData.injuredPeopleRescued = (List<Person>) in.readObject();
            SharedData.newInjuredPeople = (List<Person>) in.readObject();
            SharedData.pickedUpinjuredPeople = (List<Person>) in.readObject();

            SharedData.numberOfAgents = (int) din.readInt();
            SharedData.numberOfAmbulances = (int) din.readInt();
            SharedData.numberOfCivilVehicles = (int) din.readInt();
            SharedData.numberOfFireVehicles = (int) din.readInt();
            SharedData.numberOfPeopleCTA = (int) din.readInt();
            SharedData.numberOfPoliceVehicles = (int) din.readInt();
            SharedData.isDisasterTriggered = (boolean) din.readBoolean();

            currentTickNumber = (int) din.readInt();
            holdMessages = (boolean) din.readBoolean();

            din.close();
            in.close();
            fis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
//        }
        }
    }

    /**
     * CTA Main function
     * @param args
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            TrafficLightCTA obj = new TrafficLightCTA();
            obj.runCTA();
        } else {
            TrafficLightCTA obj = new TrafficLightCTA(args[0]);
            obj.restoreState = true;
            obj.runCTA();

        }
    }

    /**
     * This is the method called to handle any message received by this CTA
     * @param message the message object form any other CTA
     * @see CTANetwork
     * @see Messaging.QueueParameters
     * @see Messaging.QueueManager
     */
    public void receivedMessage(Message message) {
        //update status of the host from which message was received
        while (holdMessages) {
        }
        switch (message.type) {
            case CTANetwork.RMQ_TYPE_STATUS_UPDATE:
                //Find out what type of status update it is
                Message statusType = (Message) message.messageObject;
                switch (statusType.type) {
                    case CTANetwork.CTA_READY_FOR_NEXT_TICK:
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;

                    case CTANetwork.CTA_DONE_WITH_WORK:
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;

                    case CTANetwork.CTA_SAVING_STATE:
                        Utilities.Log.logger.info("Received state save message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        tickNumberForStateSave = (Integer) statusType.messageObject;
                        initiateStateSave = true;
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_SAVED_STATE:
                        Utilities.Log.logger.info("Received state saved message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;
                    case CTANetwork.CTA_RESTORED_STATE:
                        Utilities.Log.logger.info("Received restored state message from " + statusType.hostName + "at tick: " + currentTickNumber);
                        changeCTAStatus(statusType.hostName, statusType.type);
                        break;

//                    case CTANetwork.CTA_COMPLETE_EXIT:
//                        Utilities.Log.logger.info("VehicleCTA: Done with work and Exitting.....");
//                        changeCTAStatus(statusType.hostName, statusType.type);
//                        break;
                }
                break;
        }
        Utilities.Log.logger.info("TrafficCTA: ReceivedMessage of type: " + message.type);
    }
}
