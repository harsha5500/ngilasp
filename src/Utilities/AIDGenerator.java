/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Assigns Unique Agent identifiers 
 * @author flummoxed
 */
public class AIDGenerator {

    private static boolean notInitialized = true;
    private static ConcurrentHashMap<String, Integer> currentAID = new ConcurrentHashMap<String, Integer>();

    /**
     * This function must be called in order to generate unique ids for all agents
     * @return true if initialization possible, false if already initialized
     */
    public static boolean initializeAIDGen() {
        if (notInitialized) {
            currentAID.put("Agents.Person", Integer.MAX_VALUE);
            currentAID.put("Agents.TrafficLight", Integer.MAX_VALUE);
            currentAID.put("Agents.Vehicle", Integer.MAX_VALUE);
            currentAID.put("Agents.Group", Integer.MAX_VALUE);
            currentAID.put("Agents.EmergencyService", Integer.MAX_VALUE);
            notInitialized = false;
            return true;
        } else {
            return true;
        }
    }

    /**
     * 
     * @param type
     * @return
     */
    public static String newID(String type) {
        Object test = new Object();
        if (!notInitialized) {
            synchronized (test) {
                String AID;
                int num = currentAID.get(type);
                AID = type + "." + num--;
                //currentAID.remove(type);
                currentAID.put(type, num);
                Utilities.Log.logger.info("AID Generated : " + AID);
                return AID;
            }


        } else {
            throw new IllegalAccessError("AIDGenerator has not been initialized");
        }

    }
}
